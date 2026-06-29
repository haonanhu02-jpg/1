package cn.iyque.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.iyque.constant.IYqueWxCpConsts;
import cn.iyque.domain.IYQueCallback;
import cn.iyque.domain.IYqueCallBackBaseMsg;
import cn.iyque.entity.IYqueConfig;
import cn.iyque.enums.CustomerStatusType;
import cn.iyque.service.IYqueConfigService;
import cn.iyque.service.IYqueCustomerInfoService;
import cn.iyque.service.IYqueKfService;
import cn.iyque.service.IYqueMsgAuditService;
import cn.iyque.service.IYquePhaseOneService;
import cn.iyque.utils.IYqueCryptUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.constant.WxCpConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业微信系统回调入口。
 *
 * <p>保留 Iyque 原有客服、会话存档、客户变更处理逻辑；
 * 一期新增回调幂等、客户进群 / 退群关系记录和失败告警。</p>
 */
@RestController
@RequestMapping("/iycallback")
@Slf4j
public class IYcallbackController {

    @Autowired
    private IYqueConfigService iYqueConfigService;

    @Autowired
    private IYqueCustomerInfoService iYqueCustomerInfoService;

    @Autowired
    private IYqueKfService iYqueKfService;

    @Autowired
    private IYqueMsgAuditService iWeMsgAuditService;

    @Autowired
    private IYquePhaseOneService iYquePhaseOneService;

    /**
     * 企业微信 URL 验证。
     */
    @GetMapping(value = "/handle")
    public String handle(IYQueCallback queCallback) {
        String tip = "error";
        IYqueConfig iYqueConfig = iYqueConfigService.findIYqueConfig();

        if (StrUtil.isNotEmpty(iYqueConfig.getToken())
                && StrUtil.isNotEmpty(iYqueConfig.getEncodingAESKey())
                && StrUtil.isNotEmpty(iYqueConfig.getCorpId())) {
            IYqueCryptUtil iYqueCryptUtil = new IYqueCryptUtil(iYqueConfig.getToken(), iYqueConfig.getEncodingAESKey(), iYqueConfig.getCorpId());

            try {
                if (StrUtil.isEmpty(queCallback.getEchostr())) {
                    return "success";
                }
                tip = iYqueCryptUtil.verifyURL(queCallback.getMsg_signature(), queCallback.getTimestamp(), queCallback.getNonce(), queCallback.getEchostr());
            } catch (Exception e) {
                log.error("企业微信回调 URL 验证失败", e);
                return "error";
            }
        }

        return tip;
    }

    /**
     * 企业微信 POST 回调处理。
     */
    @PostMapping(value = "/handle")
    public String handle(@RequestBody String msg,
                         @RequestParam(name = "msg_signature") String signature,
                         String timestamp,
                         String nonce) {
        IYqueConfig iYqueConfig = iYqueConfigService.findIYqueConfig();
        IYqueCryptUtil iYqueCryptUtil = new IYqueCryptUtil(iYqueConfig.getToken(), iYqueConfig.getEncodingAESKey(), iYqueConfig.getCorpId());
        String callbackEventKey = null;
        String decrypt = null;
        try {
            decrypt = iYqueCryptUtil.decrypt(signature, timestamp, nonce, msg);
            IYqueCallBackBaseMsg callBackBaseMsg = XmlUtil.xmlToBean(XmlUtil.parseXml(decrypt).getFirstChild(), IYqueCallBackBaseMsg.class);
            log.info("企业微信回调 {}", callBackBaseMsg);

            if (callBackBaseMsg != null) {
                // 一期新增：回调先写幂等表，重复事件不重复执行业务写库。
                callbackEventKey = iYquePhaseOneService.buildCallbackEventKey(callBackBaseMsg, decrypt);
                if (!iYquePhaseOneService.prepareCallback(callbackEventKey, callBackBaseMsg, decrypt)) {
                    return decrypt;
                }

                handleOriginalIyqueCallback(callBackBaseMsg);

                // 一期新增：客户群变更事件写入进群 / 退群关系记录。
                iYquePhaseOneService.recordCustomerChatCallback(callBackBaseMsg, decrypt);
                iYquePhaseOneService.finishCallbackSuccess(callbackEventKey);
            }

            return decrypt;
        } catch (Exception e) {
            if (callbackEventKey != null) {
                iYquePhaseOneService.finishCallbackFailure(callbackEventKey, e, decrypt == null ? msg : decrypt);
            }
            String sRespData = iYqueCryptUtil.getTextRespData("success");
            return iYqueCryptUtil.encrypt(sRespData);
        }
    }

    /**
     * Iyque 原有回调业务处理逻辑集中保留在这里，避免一期幂等逻辑打散原分支。
     */
    private void handleOriginalIyqueCallback(IYqueCallBackBaseMsg callBackBaseMsg) throws Exception {
        // 客服事件
        if (IYqueWxCpConsts.KfChangeType.KF_MSG_OR_EVENT.equals(callBackBaseMsg.getEvent())) {
            iYqueKfService.handleKfMsg(callBackBaseMsg);
        }

        // 会话存档通知
        if (WxCpConsts.EventType.MSGAUDIT_NOTIFY.equals(callBackBaseMsg.getEvent())) {
            iWeMsgAuditService.synchMsg();
        }

        if (WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT.equals(callBackBaseMsg.getEvent())) {
            // 客户新增
            if (WxCpConsts.ExternalContactChangeType.ADD_EXTERNAL_CONTACT.equals(callBackBaseMsg.getChangeType())) {
                iYqueCustomerInfoService.addCustomerCallBackAction(callBackBaseMsg);
                iYquePhaseOneService.recordNewCustomerRoute(callBackBaseMsg);
            }

            // 客户流失：客户删除员工
            if (WxCpConsts.ExternalContactChangeType.DEL_FOLLOW_USER.equals(callBackBaseMsg.getChangeType())) {
                iYqueCustomerInfoService.updateCustomerInfoStatus(
                        callBackBaseMsg.getExternalUserID(),
                        callBackBaseMsg.getUserID(),
                        CustomerStatusType.CUSTOMER_STATUS_TYPE_LS.getCode()
                );
            }

            // 删除客户：员工删除客户
            if (WxCpConsts.ExternalContactChangeType.DEL_EXTERNAL_CONTACT.equals(callBackBaseMsg.getChangeType())) {
                iYqueCustomerInfoService.updateCustomerInfoStatus(
                        callBackBaseMsg.getExternalUserID(),
                        callBackBaseMsg.getUserID(),
                        CustomerStatusType.CUSTOMER_STATUS_TYPE_DEL.getCode()
                );
            }
        }
    }
}
