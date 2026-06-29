package cn.iyque.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.iyque.config.WeComConfigProperties;
import cn.iyque.dao.IYqueConfigDao;
import cn.iyque.entity.IYqueConfig;
import cn.iyque.service.IYqueConfigService;
import me.chanjar.weixin.cp.api.WxCpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class IYqueConfigServiceImpl implements IYqueConfigService {

    @Autowired
    IYqueConfigDao iYqueConfigDao;

    @Autowired
    private WxCpServiceFactory wxCpServiceFactory;

    @Autowired
    private WeComConfigProperties weComConfigProperties;

    @Override
    public IYqueConfig findIYqueConfig() {
        List<IYqueConfig> iYqueConfigs = iYqueConfigDao.findAll();
        if(CollectionUtil.isEmpty(iYqueConfigs)){
            return applyEnvOverrides(new IYqueConfig());
        }
        IYqueConfig iYqueConfig = iYqueConfigs.stream().findFirst().get();

        return applyEnvOverrides(iYqueConfig);
    }

    @Override
    public void saveOrUpdate(IYqueConfig iYqueConfig) {
        iYqueConfigDao.saveAndFlush(iYqueConfig);
    }

    @Override
    public WxCpService findWxcpservice() throws Exception {

        WxCpService config = wxCpServiceFactory.createWxCpService(findIYqueConfig());
        if(null == config){
            throw new Exception("请配置系统应用参数");
        }

        return config;
    }

    private IYqueConfig applyEnvOverrides(IYqueConfig config) {
        if (StrUtil.isNotBlank(weComConfigProperties.getCorpId())) {
            config.setCorpId(weComConfigProperties.getCorpId());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getAgentId())) {
            config.setAgentId(weComConfigProperties.getAgentId());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getAgentSecret())) {
            config.setAgentSecert(weComConfigProperties.getAgentSecret());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getToken())) {
            config.setToken(weComConfigProperties.getToken());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getEncodingAesKey())) {
            config.setEncodingAESKey(weComConfigProperties.getEncodingAesKey());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getMsgAuditLibPath())) {
            config.setMsgAuditLibPath(weComConfigProperties.getMsgAuditLibPath());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getMsgAuditPrivateKey())) {
            config.setMsgAuditPriKey(weComConfigProperties.getMsgAuditPrivateKey());
        }
        if (StrUtil.isNotBlank(weComConfigProperties.getMsgAuditSecret())) {
            config.setMsgAuditSecret(weComConfigProperties.getMsgAuditSecret());
        }
        return config;
    }
}
