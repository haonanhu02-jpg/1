package cn.iyque.controller;

import cn.iyque.domain.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 一期业务范围与运营后台菜单接口。
 */
@RestController
@RequestMapping("/phaseOne")
public class IYquePhaseOneController {

    /**
     * 返回一期必须完成的业务模块，供运营工作台展示验收范围。
     */
    @GetMapping("/scope")
    public ResponseResult<List<Map<String, Object>>> scope() {
        return new ResponseResult<>(List.of(
                scopeItem("渠道活码管理", "复用 Iyque 员工活码能力，保留 state 渠道识别和欢迎语配置。", "复用改造"),
                scopeItem("员工池管理", "新增员工承接配置，支持启停、排序和备注。", "新增"),
                scopeItem("客户扫码加好友处理", "复用企业微信联系我回调，补充幂等记录和失败告警。", "复用改造"),
                scopeItem("本地模拟模式", "新增不依赖客户联系 Secret、真实回调 URL、Token 和 EncodingAESKey 的一期闭环调试入口。", "新增"),
                scopeItem("欢迎语与自动标签", "复用 Iyque 活码欢迎语和客户标签能力，后续按业务继续拆分模板。", "复用改造"),
                scopeItem("素材管理", "复用 Iyque 普通素材、附件和上传能力。", "直接复用"),
                scopeItem("标签管理", "复用 Iyque 客户标签同步与打标能力。", "直接复用"),
                scopeItem("客户与客群同步", "复用 Iyque 客户、客群同步，客群同步已接入 API 调用日志。", "复用改造"),
                scopeItem("独立群池模型", "新增群池主表和群池-客户群绑定关系，支持容量阈值、备用群和排序。", "新增"),
                scopeItem("分群规则优先级", "新增显式规则类型，按客户标签、渠道标签、渠道默认、全局默认固定顺序匹配。", "新增"),
                scopeItem("客户分群引导记录", "新增客户添加后的分群匹配结果表，记录命中规则、目标群池、目标客户群和失败原因。", "新增"),
                scopeItem("客户进群 / 退群关系记录", "新增客户群关系事件表，并接入官方回调链路和群人数更新。", "新增"),
                scopeItem("回调幂等表", "新增回调唯一键表，重复回调不重复执行业务写库。", "新增"),
                scopeItem("企业微信 API 调用日志", "新增 API 日志表，客群同步调用已落日志并做敏感信息脱敏。", "新增"),
                scopeItem("统一业务日志", "新增一期业务日志中心，记录客户添加、分群匹配、进退群、告警处理等关键节点。", "新增"),
                scopeItem("告警日志", "新增告警日志和处理入口。", "新增"),
                scopeItem("统计报表与导出", "新增渠道、员工、群池、异常统计和 CSV 导出。", "新增"),
                scopeItem("一期验收看板", "新增验收项状态页，用于核对 01-07 完成情况。", "新增"),
                scopeItem("失败任务重试", "新增失败任务表、手动重试入口和定时扫描任务。", "新增"),
                scopeItem("access_token Redis 缓存", "改造 WxJava 配置为 RedisTemplate 官方缓存存储。", "改造")
        ));
    }

    /**
     * 返回一期运营后台菜单规划。
     */
    @GetMapping("/menus")
    public ResponseResult<List<Map<String, Object>>> menus() {
        return new ResponseResult<>(List.of(
                menuItem("运营工作台", "/phase-one/workbench", "展示一期范围、统计摘要和模块入口。"),
                menuItem("本地模拟", "/phase-one/mock", "无企业微信真实回调配置时验证一期内部闭环。"),
                menuItem("渠道活码", "/phase-one/channel-code", "复用 Iyque 员工活码。"),
                menuItem("员工池管理", "/phase-one/employee-pool", "一期新增员工承接配置。"),
                menuItem("欢迎语模板", "/phase-one/welcome-template", "当前复用活码欢迎语配置。"),
                menuItem("素材库", "/phase-one/material", "复用 Iyque 普通素材。"),
                menuItem("客户标签", "/phase-one/customer-tag", "复用 Iyque 客户标签。"),
                menuItem("群池管理", "/phase-one/group-pool", "一期新增独立群池模型。"),
                menuItem("分群规则", "/phase-one/group-route-rule", "一期新增规则优先级和匹配预览。"),
                menuItem("客户管理", "/phase-one/customer", "复用 Iyque 客户列表。"),
                menuItem("进群 / 退群记录", "/phase-one/customer-chat-relation", "一期新增客户群关系事件和分群引导记录。"),
                menuItem("统计报表", "/phase-one/statistics", "一期新增渠道、员工、群池、异常统计和导出。"),
                menuItem("日志中心", "/phase-one/logs", "一期新增统一业务日志和企业微信 API 调用日志。"),
                menuItem("告警中心", "/phase-one/alerts", "一期新增告警处理入口。"),
                menuItem("失败任务", "/phase-one/failed-task", "一期新增失败任务重试入口。"),
                menuItem("验收看板", "/phase-one/acceptance", "一期新增 01-07 验收标准状态核对。"),
                menuItem("系统基础配置", "/phase-one/system-config", "复用 Iyque 企业微信配置。")
        ));
    }

    private Map<String, Object> scopeItem(String moduleName, String description, String buildType) {
        return Map.of(
                "moduleName", moduleName,
                "description", description,
                "buildType", buildType
        );
    }

    private Map<String, Object> menuItem(String title, String path, String description) {
        return Map.of(
                "title", title,
                "path", path,
                "description", description
        );
    }
}
