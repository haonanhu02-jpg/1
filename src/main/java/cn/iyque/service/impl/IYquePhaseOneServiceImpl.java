package cn.iyque.service.impl;

import cn.iyque.config.IYqueParamConfig;
import cn.iyque.dao.IYQueCustomerInfoDao;
import cn.iyque.dao.IYquePhaseAlertLogDao;
import cn.iyque.dao.IYquePhaseApiCallLogDao;
import cn.iyque.dao.IYquePhaseBusinessLogDao;
import cn.iyque.dao.IYquePhaseCallbackIdempotentDao;
import cn.iyque.dao.IYquePhaseCustomerChatRelationDao;
import cn.iyque.dao.IYquePhaseCustomerRouteLogDao;
import cn.iyque.dao.IYquePhaseEmployeePoolDao;
import cn.iyque.dao.IYquePhaseFailedTaskDao;
import cn.iyque.dao.IYquePhaseGroupPoolChatDao;
import cn.iyque.dao.IYquePhaseGroupPoolDao;
import cn.iyque.dao.IYquePhaseGroupRouteRuleDao;
import cn.iyque.dao.IYqueUserCodeDao;
import cn.iyque.domain.IYqueCallBackBaseMsg;
import cn.iyque.domain.IYquePhaseMockRequest;
import cn.iyque.domain.IYquePhaseStatisticsQuery;
import cn.iyque.domain.fileType.Text;
import cn.iyque.domain.IYQueCustomerInfo;
import cn.iyque.entity.IYqueMsgAnnex;
import cn.iyque.entity.IYquePhaseAlertLog;
import cn.iyque.entity.IYquePhaseApiCallLog;
import cn.iyque.entity.IYquePhaseBusinessLog;
import cn.iyque.entity.IYquePhaseCallbackIdempotent;
import cn.iyque.entity.IYquePhaseCustomerChatRelation;
import cn.iyque.entity.IYquePhaseCustomerRouteLog;
import cn.iyque.entity.IYquePhaseEmployeePool;
import cn.iyque.entity.IYquePhaseFailedTask;
import cn.iyque.entity.IYquePhaseGroupPool;
import cn.iyque.entity.IYquePhaseGroupPoolChat;
import cn.iyque.entity.IYquePhaseGroupRouteRule;
import cn.iyque.entity.IYqueRobot;
import cn.iyque.entity.IYqueRobotSub;
import cn.iyque.entity.IYqueUserCode;
import cn.iyque.enums.CustomerAddWay;
import cn.iyque.enums.CustomerStatusType;
import cn.iyque.service.IYQueRobotService;
import cn.iyque.service.IYquePhaseOneService;
import cn.iyque.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IYquePhaseOneServiceImpl implements IYquePhaseOneService {

    private static final int DEL_NORMAL = 0;
    private static final int DEL_DELETED = 1;
    private static final String ALERT_HIGH = "HIGH";
    private static final String ALERT_MEDIUM = "MEDIUM";
    private static final String ROUTE_MATCHED = "MATCHED";
    private static final String ROUTE_NO_RULE = "NO_ROUTE_RULE";
    private static final String ROUTE_POOL_DISABLED = "TARGET_POOL_DISABLED";
    private static final String ROUTE_NO_CHAT = "NO_AVAILABLE_CHAT";
    private static final List<String> RULE_MATCH_ORDER = List.of(
            IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG,
            IYquePhaseGroupRouteRule.TYPE_CHANNEL_TAG,
            IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT,
            IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT
    );

    private final IYquePhaseEmployeePoolDao employeePoolDao;
    private final IYquePhaseGroupPoolDao groupPoolDao;
    private final IYquePhaseGroupPoolChatDao groupPoolChatDao;
    private final IYquePhaseGroupRouteRuleDao groupRouteRuleDao;
    private final IYquePhaseCustomerChatRelationDao customerChatRelationDao;
    private final IYquePhaseCustomerRouteLogDao customerRouteLogDao;
    private final IYquePhaseBusinessLogDao businessLogDao;
    private final IYquePhaseCallbackIdempotentDao callbackIdempotentDao;
    private final IYquePhaseApiCallLogDao apiCallLogDao;
    private final IYquePhaseAlertLogDao alertLogDao;
    private final IYquePhaseFailedTaskDao failedTaskDao;
    private final IYqueUserCodeDao userCodeDao;
    private final IYQueCustomerInfoDao customerInfoDao;
    private final IYQueRobotService robotService;
    private final IYqueParamConfig paramConfig;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public IYquePhaseOneServiceImpl(IYquePhaseEmployeePoolDao employeePoolDao,
                                    IYquePhaseGroupPoolDao groupPoolDao,
                                    IYquePhaseGroupPoolChatDao groupPoolChatDao,
                                    IYquePhaseGroupRouteRuleDao groupRouteRuleDao,
                                    IYquePhaseCustomerChatRelationDao customerChatRelationDao,
                                    IYquePhaseCustomerRouteLogDao customerRouteLogDao,
                                    IYquePhaseBusinessLogDao businessLogDao,
                                    IYquePhaseCallbackIdempotentDao callbackIdempotentDao,
                                    IYquePhaseApiCallLogDao apiCallLogDao,
                                    IYquePhaseAlertLogDao alertLogDao,
                                    IYquePhaseFailedTaskDao failedTaskDao,
                                    IYqueUserCodeDao userCodeDao,
                                    IYQueCustomerInfoDao customerInfoDao,
                                    IYQueRobotService robotService,
                                    IYqueParamConfig paramConfig,
                                    ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.employeePoolDao = employeePoolDao;
        this.groupPoolDao = groupPoolDao;
        this.groupPoolChatDao = groupPoolChatDao;
        this.groupRouteRuleDao = groupRouteRuleDao;
        this.customerChatRelationDao = customerChatRelationDao;
        this.customerRouteLogDao = customerRouteLogDao;
        this.businessLogDao = businessLogDao;
        this.callbackIdempotentDao = callbackIdempotentDao;
        this.apiCallLogDao = apiCallLogDao;
        this.alertLogDao = alertLogDao;
        this.failedTaskDao = failedTaskDao;
        this.userCodeDao = userCodeDao;
        this.customerInfoDao = customerInfoDao;
        this.robotService = robotService;
        this.paramConfig = paramConfig;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public Page<IYquePhaseEmployeePool> findEmployeePoolPage(IYquePhaseEmployeePool query, Pageable pageable) {
        Specification<IYquePhaseEmployeePool> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getEmployeeName())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("employeeName"), like(query.getEmployeeName())));
        }
        if (query != null && query.getEnabled() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), query.getEnabled()));
        }
        return employeePoolDao.findAll(spec, pageable);
    }

    @Override
    public IYquePhaseEmployeePool saveEmployeePool(IYquePhaseEmployeePool employeePool) {
        IYquePhaseEmployeePool saved = employeePoolDao.saveAndFlush(employeePool);
        recordBusinessLog("EMPLOYEE_POOL_CONFIG", String.valueOf(saved.getId()), null, saved.getEmployeeId(), null, null,
                "save employee pool", saved.getEmployeeName(), true, null, null);
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployeePools(String ids) {
        for (Long id : parseIds(ids)) {
            employeePoolDao.findById(id).ifPresent(item -> {
                item.setDelFlag(DEL_DELETED);
                employeePoolDao.save(item);
            });
        }
    }

    @Override
    public Page<IYquePhaseGroupPool> findGroupPoolPage(IYquePhaseGroupPool query, Pageable pageable) {
        Specification<IYquePhaseGroupPool> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getPoolName())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("poolName"), like(query.getPoolName())));
        }
        if (query != null && query.getEnabled() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), query.getEnabled()));
        }
        Page<IYquePhaseGroupPool> page = groupPoolDao.findAll(spec, pageable);
        page.getContent().forEach(this::fillGroupPoolChats);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IYquePhaseGroupPool saveGroupPool(IYquePhaseGroupPool groupPool) {
        IYquePhaseGroupPool saved = groupPoolDao.saveAndFlush(groupPool);

        // 群池绑定关系以页面提交为准：先软删除旧关系，再保存新关系，避免残留旧群。
        List<IYquePhaseGroupPoolChat> oldChats = groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(saved.getId(), DEL_NORMAL);
        oldChats.forEach(item -> item.setDelFlag(DEL_DELETED));
        groupPoolChatDao.saveAll(oldChats);

        if (groupPool.getChats() != null) {
            List<IYquePhaseGroupPoolChat> newChats = groupPool.getChats().stream()
                    .filter(item -> StringUtils.isNotBlank(item.getChatId()))
                    .peek(item -> {
                        item.setId(null);
                        item.setGroupPoolId(saved.getId());
                        item.setDelFlag(DEL_NORMAL);
                    })
                    .collect(Collectors.toList());
            groupPoolChatDao.saveAll(newChats);
        }
        fillGroupPoolChats(saved);
        recordBusinessLog("GROUP_POOL_CONFIG", String.valueOf(saved.getId()), null, null, null, saved.getChannelCodeId(),
                "save group pool", saved.getPoolName(), true, null, null);
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupPools(String ids) {
        for (Long id : parseIds(ids)) {
            groupPoolDao.findById(id).ifPresent(pool -> {
                pool.setDelFlag(DEL_DELETED);
                groupPoolDao.save(pool);
                List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(id, DEL_NORMAL);
                chats.forEach(item -> item.setDelFlag(DEL_DELETED));
                groupPoolChatDao.saveAll(chats);
            });
        }
    }

    @Override
    public Page<IYquePhaseGroupRouteRule> findGroupRouteRulePage(IYquePhaseGroupRouteRule query, Pageable pageable) {
        Specification<IYquePhaseGroupRouteRule> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getRuleName())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("ruleName"), like(query.getRuleName())));
        }
        if (query != null && query.getEnabled() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), query.getEnabled()));
        }
        if (query != null && StringUtils.isNotBlank(query.getRuleType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("ruleType"), query.getRuleType()));
        }
        Page<IYquePhaseGroupRouteRule> page = groupRouteRuleDao.findAll(spec, pageable);
        page.getContent().forEach(rule -> {
            normalizeRouteRule(rule);
            fillRouteRulePoolName(rule);
        });
        return page;
    }

    @Override
    public IYquePhaseGroupRouteRule saveGroupRouteRule(IYquePhaseGroupRouteRule groupRouteRule) {
        normalizeRouteRule(groupRouteRule);
        IYquePhaseGroupRouteRule saved = groupRouteRuleDao.saveAndFlush(groupRouteRule);
        fillRouteRulePoolName(saved);
        recordBusinessLog("GROUP_ROUTE_RULE_CONFIG", String.valueOf(saved.getId()), null, null, null, saved.getChannelCodeId(),
                "save route rule", saved.getRuleName(), true, null, null);
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupRouteRules(String ids) {
        for (Long id : parseIds(ids)) {
            groupRouteRuleDao.findById(id).ifPresent(rule -> {
                rule.setDelFlag(DEL_DELETED);
                groupRouteRuleDao.save(rule);
            });
        }
    }

    @Override
    public Map<String, Object> matchGroupRoute(IYquePhaseGroupRouteRule query) {
        return matchGroupRoute(query, false, "preview");
    }

    private Map<String, Object> matchGroupRoute(IYquePhaseGroupRouteRule query, boolean recordException, String businessKey) {
        List<IYquePhaseGroupRouteRule> rules = groupRouteRuleDao.findByEnabledAndDelFlagOrderByPriorityDesc(true, DEL_NORMAL);
        rules.forEach(this::normalizeRouteRule);
        Set<String> inputTags = splitToSet(query == null ? null : query.getRequiredTagIds());
        String channelCodeId = query == null ? null : query.getChannelCodeId();

        IYquePhaseGroupRouteRule finalRule = null;
        for (String ruleType : RULE_MATCH_ORDER) {
            Optional<IYquePhaseGroupRouteRule> matchedRule = rules.stream()
                    .filter(rule -> Objects.equals(ruleType, rule.getRuleType()))
                    .filter(rule -> routeRuleMatched(rule, channelCodeId, inputTags))
                    .max(Comparator.comparing(rule -> rule.getPriority() == null ? 0 : rule.getPriority()));
            if (matchedRule.isPresent()) {
                finalRule = matchedRule.get();
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matched", finalRule != null);
        result.put("rule", finalRule);

        if (finalRule == null) {
            if (recordException) {
                createAlertAndFailedTask("GROUP_ROUTE_MISS", ALERT_HIGH, "未匹配到分群规则",
                        "渠道和标签没有命中任何启用规则", safe(businessKey));
            }
            result.put("reason", "未匹配到启用规则");
            result.put("routeStatus", ROUTE_NO_RULE);
            result.put("matchPath", "未命中");
            return result;
        }

        fillRouteRulePoolName(finalRule);
        Optional<IYquePhaseGroupPool> poolOptional = groupPoolDao.findById(finalRule.getTargetPoolId());
        if (poolOptional.isEmpty() || !Boolean.TRUE.equals(poolOptional.get().getEnabled())) {
            if (recordException) {
                createAlertAndFailedTask("GROUP_POOL_DISABLED", ALERT_HIGH, "分群规则目标群池不可用",
                        "规则命中的群池不存在或未启用", "rule=" + finalRule.getId() + ":" + safe(businessKey));
            }
            result.put("reason", "目标群池不可用");
            result.put("ruleType", finalRule.getRuleType());
            result.put("matchPath", ruleTypeLabel(finalRule.getRuleType()));
            result.put("routeStatus", ROUTE_POOL_DISABLED);
            return result;
        }

        IYquePhaseGroupPool pool = poolOptional.get();
        fillGroupPoolChats(pool);
        if (recordException) {
            pool.getChats().stream()
                    .filter(this::chatThresholdReached)
                    .forEach(chat -> createAlert("GROUP_CHAT_THRESHOLD_REACHED", ALERT_MEDIUM, "chat=" + safe(chat.getChatId()),
                            "客户群达到容量阈值", "群池[" + safe(pool.getPoolName()) + "]客户群[" + safe(chat.getChatName()) + "]已达到容量阈值"));
        }
        IYquePhaseGroupPoolChat selectedChat = pool.getChats().stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .filter(item -> !chatThresholdReached(item))
                .min(Comparator.comparingInt((IYquePhaseGroupPoolChat item) -> Boolean.TRUE.equals(item.getBackupFlag()) ? 1 : 0)
                        .thenComparing(item -> item.getSort() == null ? 0 : item.getSort()))
                .orElse(null);

        if (selectedChat == null) {
            if (recordException) {
                createAlertAndFailedTask("GROUP_POOL_FULL", ALERT_HIGH, "群池无可用客户群",
                        "群池内客户群均未启用或已达到容量阈值", "pool=" + pool.getId() + ":" + safe(businessKey));
            }
            result.put("reason", "群池无可用客户群");
            result.put("pool", pool);
            result.put("ruleType", finalRule.getRuleType());
            result.put("matchPath", ruleTypeLabel(finalRule.getRuleType()));
            result.put("routeStatus", ROUTE_NO_CHAT);
            return result;
        }

        result.put("pool", pool);
        result.put("chat", selectedChat);
        result.put("ruleType", finalRule.getRuleType());
        result.put("matchPath", ruleTypeLabel(finalRule.getRuleType()));
        result.put("routeStatus", ROUTE_MATCHED);
        result.put("reason", "匹配成功");
        return result;
    }

    @Override
    public Page<IYquePhaseCustomerChatRelation> findCustomerChatRelationPage(IYquePhaseCustomerChatRelation query, Pageable pageable) {
        Specification<IYquePhaseCustomerChatRelation> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getExternalUserid())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("externalUserid"), like(query.getExternalUserid())));
        }
        if (query != null && StringUtils.isNotBlank(query.getCustomerName())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("customerName"), like(query.getCustomerName())));
        }
        if (query != null && StringUtils.isNotBlank(query.getChatId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("chatId"), query.getChatId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getEventType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("eventType"), query.getEventType()));
        }
        if (query != null && query.getStartTime() != null && query.getEndTime() != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("eventTime"),
                    DateUtils.setTimeToStartOfDay(query.getStartTime()), DateUtils.setTimeToEndOfDay(query.getEndTime())));
        }
        return customerChatRelationDao.findAll(spec, pageable);
    }

    @Override
    public Page<IYquePhaseCustomerRouteLog> findCustomerRouteLogPage(IYquePhaseCustomerRouteLog query, Pageable pageable) {
        Specification<IYquePhaseCustomerRouteLog> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getExternalUserid())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("externalUserid"), like(query.getExternalUserid())));
        }
        if (query != null && StringUtils.isNotBlank(query.getUserId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), query.getUserId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getChannelCodeId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("channelCodeId"), query.getChannelCodeId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getRuleType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("ruleType"), query.getRuleType()));
        }
        if (query != null && StringUtils.isNotBlank(query.getRouteStatus())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("routeStatus"), query.getRouteStatus()));
        }
        if (query != null && query.getStartTime() != null && query.getEndTime() != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("eventTime"),
                    DateUtils.setTimeToStartOfDay(query.getStartTime()), DateUtils.setTimeToEndOfDay(query.getEndTime())));
        }
        return customerRouteLogDao.findAll(spec, pageable);
    }

    @Override
    public Page<IYquePhaseAlertLog> findAlertLogPage(IYquePhaseAlertLog query, Pageable pageable) {
        Specification<IYquePhaseAlertLog> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getAlertType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("alertType"), query.getAlertType()));
        }
        if (query != null && StringUtils.isNotBlank(query.getAlertLevel())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("alertLevel"), query.getAlertLevel()));
        }
        if (query != null && StringUtils.isNotBlank(query.getStatus())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), query.getStatus()));
        }
        return alertLogDao.findAll(spec, pageable);
    }

    @Override
    public IYquePhaseAlertLog handleAlert(Long id, IYquePhaseAlertLog alertLog) {
        IYquePhaseAlertLog old = alertLogDao.findById(id).orElseThrow(() -> new IllegalArgumentException("告警不存在"));
        String targetStatus = StringUtils.defaultIfBlank(alertLog.getStatus(), "HANDLED");
        old.setStatus(targetStatus);
        old.setHandledBy(alertLog.getHandledBy());
        old.setHandledRemark(alertLog.getHandledRemark());
        old.setHandledTime(new Date());
        IYquePhaseAlertLog saved = alertLogDao.saveAndFlush(old);
        recordBusinessLog("ALERT_HANDLE", String.valueOf(saved.getId()), null, null, null, null,
                alertLog.getHandledRemark(), targetStatus, true, null, saved.getId());
        return saved;
    }

    @Override
    public Page<IYquePhaseFailedTask> findFailedTaskPage(IYquePhaseFailedTask query, Pageable pageable) {
        Specification<IYquePhaseFailedTask> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getTaskType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("taskType"), query.getTaskType()));
        }
        if (query != null && StringUtils.isNotBlank(query.getStatus())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), query.getStatus()));
        }
        return failedTaskDao.findAll(spec, pageable);
    }

    @Override
    public IYquePhaseFailedTask retryFailedTask(Long id) {
        IYquePhaseFailedTask task = failedTaskDao.findById(id).orElseThrow(() -> new IllegalArgumentException("失败任务不存在"));
        task.setStatus("PENDING");
        task.setNextRetryTime(new Date());
        return failedTaskDao.saveAndFlush(task);
    }

    @Override
    public Page<IYquePhaseApiCallLog> findApiCallLogPage(IYquePhaseApiCallLog query, Pageable pageable) {
        Specification<IYquePhaseApiCallLog> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getApiName())) {
            spec = spec.and((root, q, cb) -> cb.like(root.get("apiName"), like(query.getApiName())));
        }
        if (query != null && query.getSuccess() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("success"), query.getSuccess()));
        }
        return apiCallLogDao.findAll(spec, pageable);
    }

    @Override
    public Map<String, Object> summary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("employeePoolCount", employeePoolDao.count());
        summary.put("groupPoolCount", groupPoolDao.count());
        summary.put("routeRuleCount", groupRouteRuleDao.count());
        summary.put("joinCount", customerChatRelationDao.countByEventTypeAndDelFlag("JOIN", DEL_NORMAL));
        summary.put("leaveCount", customerChatRelationDao.countByEventTypeAndDelFlag("LEAVE", DEL_NORMAL));
        summary.put("routeMatchedCount", customerRouteLogDao.countByRouteStatusAndDelFlag(ROUTE_MATCHED, DEL_NORMAL));
        summary.put("routeNoAvailableChatCount", customerRouteLogDao.countByRouteStatusAndDelFlag(ROUTE_NO_CHAT, DEL_NORMAL));
        summary.put("pendingAlertCount", alertLogDao.countByStatusAndDelFlag("PENDING", DEL_NORMAL));
        summary.put("pendingHighAlertCount", alertLogDao.countByAlertLevelAndStatusAndDelFlag(ALERT_HIGH, "PENDING", DEL_NORMAL));
        summary.put("pendingFailedTaskCount", failedTaskDao.countByStatusAndDelFlag("PENDING", DEL_NORMAL));
        return summary;
    }

    @Override
    public Map<String, Object> statisticsReport(IYquePhaseStatisticsQuery query) {
        List<IYquePhaseCustomerRouteLog> routeLogs = customerRouteLogDao.findAll(routeLogStatisticsSpec(query));
        List<IYquePhaseCustomerChatRelation> relations = customerChatRelationDao.findAll(relationStatisticsSpec(query));
        Map<String, Object> report = new HashMap<>();
        report.put("summary", buildStatisticsSummary(routeLogs, relations));
        report.put("channelStats", buildChannelStats(routeLogs, relations));
        report.put("employeeStats", buildEmployeeStats(routeLogs, relations));
        report.put("groupPoolStats", buildGroupPoolStats(query, routeLogs));
        report.put("exceptionStats", buildExceptionStats());
        return report;
    }

    @Override
    public Page<IYquePhaseBusinessLog> findBusinessLogPage(IYquePhaseBusinessLog query, Pageable pageable) {
        return businessLogDao.findAll(businessLogSpec(query), pageable);
    }

    @Override
    public List<Map<String, Object>> acceptanceSummary() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(acceptanceItem("渠道活码", "后台可创建和展示渠道活码", userCodeDao.count() > 0));
        items.add(acceptanceItem("员工池", "至少存在启用员工池配置", employeePoolDao.count() > 0));
        items.add(acceptanceItem("群池", "至少存在群池并可绑定客户群", groupPoolDao.count() > 0));
        items.add(acceptanceItem("分群规则", "存在客户标签、渠道或兜底分群规则", groupRouteRuleDao.count() > 0));
        items.add(acceptanceItem("分群引导", "新增客户后可沉淀分群引导记录", customerRouteLogDao.count() > 0));
        items.add(acceptanceItem("进退群记录", "客户群回调后可沉淀进退群关系", customerChatRelationDao.count() > 0));
        items.add(acceptanceItem("统计报表", "统计摘要、渠道、员工、群池、异常统计接口已提供", true));
        items.add(acceptanceItem("日志中心", "统一业务日志和企业微信 API 日志可查询", businessLogDao.count() > 0 || apiCallLogDao.count() > 0));
        items.add(acceptanceItem("告警中心", "告警可查询并记录处理人、处理时间、处理备注", true));
        items.add(acceptanceItem("端到端链路", "真实通过企业微信回调产生客户、分群、进退群、统计、日志、告警数据", customerRouteLogDao.count() > 0 && customerChatRelationDao.count() > 0));
        return items;
    }

    @Override
    public List<Map<String, Object>> exportStatisticsRows(IYquePhaseStatisticsQuery query) {
        Map<String, Object> report = statisticsReport(query);
        List<Map<String, Object>> rows = new ArrayList<>();
        appendExportRows(rows, "核心摘要", (Map<String, Object>) report.get("summary"));
        appendListExportRows(rows, "渠道统计", (List<Map<String, Object>>) report.get("channelStats"));
        appendListExportRows(rows, "员工统计", (List<Map<String, Object>>) report.get("employeeStats"));
        appendListExportRows(rows, "群池统计", (List<Map<String, Object>>) report.get("groupPoolStats"));
        appendExportRows(rows, "异常统计", (Map<String, Object>) report.get("exceptionStats"));
        return rows;
    }

    @Override
    public String buildCallbackEventKey(IYqueCallBackBaseMsg callBackBaseMsg, String rawXml) {
        if (callBackBaseMsg == null) {
            return "callback:" + digest("empty:" + safe(rawXml));
        }
        String rawKey = String.join(":",
                "callback",
                safe(callBackBaseMsg.getEvent()),
                safe(callBackBaseMsg.getChangeType()),
                safe(callBackBaseMsg.getExternalUserID()),
                safe(callBackBaseMsg.getUserID()),
                safe(callBackBaseMsg.getChatId()),
                String.valueOf(callBackBaseMsg.getCreateTime()),
                safe(rawXml));
        return "callback:" + digest(rawKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean prepareCallback(String eventKey, IYqueCallBackBaseMsg callBackBaseMsg, String rawXml) {
        Optional<IYquePhaseCallbackIdempotent> old = callbackIdempotentDao.findByEventKeyAndDelFlag(eventKey, DEL_NORMAL);
        if (old.isPresent()) {
            IYquePhaseCallbackIdempotent idempotent = old.get();
            if ("SUCCESS".equals(idempotent.getProcessStatus()) || "PROCESSING".equals(idempotent.getProcessStatus())) {
                return false;
            }
            idempotent.setProcessStatus("PROCESSING");
            idempotent.setErrorMsg(null);
            callbackIdempotentDao.save(idempotent);
            return true;
        }

        IYquePhaseCallbackIdempotent idempotent = IYquePhaseCallbackIdempotent.builder()
                .eventKey(eventKey)
                .eventType(callBackBaseMsg == null ? null : callBackBaseMsg.getEvent())
                .changeType(callBackBaseMsg == null ? null : callBackBaseMsg.getChangeType())
                .processStatus("PROCESSING")
                .rawDigest(digest(rawXml))
                .build();
        callbackIdempotentDao.save(idempotent);
        return true;
    }

    @Override
    public void finishCallbackSuccess(String eventKey) {
        callbackIdempotentDao.findByEventKeyAndDelFlag(eventKey, DEL_NORMAL).ifPresent(item -> {
            item.setProcessStatus("SUCCESS");
            item.setErrorMsg(null);
            callbackIdempotentDao.save(item);
        });
    }

    @Override
    public void finishCallbackFailure(String eventKey, Exception exception, String rawXml) {
        callbackIdempotentDao.findByEventKeyAndDelFlag(eventKey, DEL_NORMAL).ifPresent(item -> {
            item.setProcessStatus("FAILED");
            item.setErrorMsg(exception == null ? "未知回调异常" : exception.getMessage());
            callbackIdempotentDao.save(item);
        });
        createAlertAndFailedTask("CALLBACK_FAILED", ALERT_HIGH, "企业微信回调处理失败",
                exception == null ? "未知回调异常" : exception.getMessage(), safe(rawXml));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordNewCustomerRoute(IYqueCallBackBaseMsg callBackBaseMsg) {
        if (callBackBaseMsg == null) {
            return;
        }

        Optional<IYqueUserCode> userCodeOptional = findUserCodeByState(callBackBaseMsg.getState());
        IYqueUserCode userCode = userCodeOptional.orElse(null);
        String channelCodeId = userCode == null || userCode.getId() == null ? null : userCode.getId().toString();
        String tagIds = userCode == null ? null : userCode.getTagId();
        String businessKey = "external=" + safe(callBackBaseMsg.getExternalUserID())
                + ":state=" + safe(callBackBaseMsg.getState());

        IYquePhaseGroupRouteRule routeQuery = IYquePhaseGroupRouteRule.builder()
                .channelCodeId(channelCodeId)
                .requiredTagIds(tagIds)
                .build();
        Map<String, Object> routeResult = matchGroupRoute(routeQuery, true, businessKey);
        IYquePhaseGroupRouteRule matchedRule = (IYquePhaseGroupRouteRule) routeResult.get("rule");
        IYquePhaseGroupPool matchedPool = (IYquePhaseGroupPool) routeResult.get("pool");
        IYquePhaseGroupPoolChat matchedChat = (IYquePhaseGroupPoolChat) routeResult.get("chat");
        String routeStatus = StringUtils.defaultIfBlank((String) routeResult.get("routeStatus"), ROUTE_NO_RULE);
        String reason = (String) routeResult.get("reason");

        if (userCode == null) {
            reason = StringUtils.defaultIfBlank(reason, "无法识别渠道，已按全局兜底规则尝试分群");
        }

        IYquePhaseCustomerRouteLog routeLog = IYquePhaseCustomerRouteLog.builder()
                .externalUserid(callBackBaseMsg.getExternalUserID())
                .userId(callBackBaseMsg.getUserID())
                .channelCodeId(channelCodeId)
                .channelCodeName(userCode == null ? null : userCode.getCodeName())
                .channelState(callBackBaseMsg.getState())
                .tagIds(tagIds)
                .ruleType(matchedRule == null ? (String) routeResult.get("ruleType") : matchedRule.getRuleType())
                .matchedRuleId(matchedRule == null ? null : matchedRule.getId())
                .matchedRuleName(matchedRule == null ? null : matchedRule.getRuleName())
                .matchPath((String) routeResult.get("matchPath"))
                .targetPoolId(matchedPool == null ? null : matchedPool.getId())
                .targetPoolName(matchedPool == null ? null : matchedPool.getPoolName())
                .targetChatId(matchedChat == null ? null : matchedChat.getChatId())
                .targetChatName(matchedChat == null ? null : matchedChat.getChatName())
                .routeStatus(routeStatus)
                .failureReason(ROUTE_MATCHED.equals(routeStatus) ? null : reason)
                .eventTime(callBackBaseMsg.getCreateTime() == null ? new Date() : new Date(callBackBaseMsg.getCreateTime() * 1000L))
                .build();
        customerRouteLogDao.save(routeLog);
        recordBusinessLog("CUSTOMER_ADD", businessKey, callBackBaseMsg.getExternalUserID(), callBackBaseMsg.getUserID(), null, channelCodeId,
                "new customer callback", routeStatus, ROUTE_MATCHED.equals(routeStatus), reason, null);
        recordBusinessLog("GROUP_ROUTE_MATCH", businessKey, callBackBaseMsg.getExternalUserID(), callBackBaseMsg.getUserID(),
                matchedChat == null ? null : matchedChat.getChatId(), channelCodeId,
                matchedRule == null ? null : matchedRule.getRuleName(), routeStatus, ROUTE_MATCHED.equals(routeStatus), reason, null);
    }

    @Override
    public void recordCustomerChatCallback(IYqueCallBackBaseMsg callBackBaseMsg, String rawXml) {
        if (callBackBaseMsg == null || !isCustomerChatCallback(callBackBaseMsg)) {
            return;
        }
        if (StringUtils.isBlank(callBackBaseMsg.getExternalUserID()) && StringUtils.isBlank(callBackBaseMsg.getChatId())) {
            return;
        }

        IYquePhaseCustomerChatRelation relation = IYquePhaseCustomerChatRelation.builder()
                .externalUserid(callBackBaseMsg.getExternalUserID())
                .userId(callBackBaseMsg.getUserID())
                .chatId(callBackBaseMsg.getChatId())
                .chatName(callBackBaseMsg.getChatName())
                .channelCodeId(findUserCodeByState(callBackBaseMsg.getState())
                        .map(item -> item.getId() == null ? null : item.getId().toString())
                        .orElse(null))
                .channelState(callBackBaseMsg.getState())
                .eventType(resolveCustomerChatEventType(callBackBaseMsg))
                .relationStatus(resolveCustomerChatStatus(callBackBaseMsg))
                .eventTime(callBackBaseMsg.getCreateTime() == null ? new Date() : new Date(callBackBaseMsg.getCreateTime() * 1000L))
                .build();

        fillRelationPoolInfo(relation);
        customerChatRelationDao.save(relation);
        updateGroupPoolChatMemberCount(relation);
        recordBusinessLog("CUSTOMER_CHAT_" + relation.getEventType(), safe(relation.getChatId()),
                relation.getExternalUserid(), relation.getUserId(), relation.getChatId(), relation.getChannelCodeId(),
                safe(rawXml), relation.getRelationStatus(), true, null, null);
    }

    @Override
    public void retryDueFailedTasks() {
        List<IYquePhaseFailedTask> tasks = failedTaskDao.findTop20ByStatusInAndNextRetryTimeLessThanEqualAndDelFlagOrderByNextRetryTimeAsc(
                Arrays.asList("PENDING", "FAILED"), new Date(), DEL_NORMAL);
        for (IYquePhaseFailedTask task : tasks) {
            // 当前一期失败任务先具备可观测和可重试状态流转能力，具体业务补偿由 taskType 后续接执行器。
            int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
            int maxRetryCount = task.getMaxRetryCount() == null ? 3 : task.getMaxRetryCount();
            task.setLastRetryTime(new Date());
            task.setRetryCount(retryCount + 1);
            if (retryCount + 1 >= maxRetryCount) {
                task.setStatus("EXHAUSTED");
                task.setNextRetryTime(null);
                createAlert("FAILED_TASK_EXHAUSTED", ALERT_HIGH, task.getBusinessKey(),
                        "失败任务重试耗尽", safe(task.getErrorMsg()));
            } else {
                task.setStatus("FAILED");
                task.setNextRetryTime(nextRetryTime(retryCount + 1));
            }
            failedTaskDao.save(task);
        }
    }

    @Override
    public void logApiCall(String apiName, String requestSummary, String responseSummary, boolean success, long costMs, String errorMsg) {
        apiCallLogDao.save(IYquePhaseApiCallLog.builder()
                .apiName(apiName)
                .requestSummary(maskSecret(requestSummary))
                .responseSummary(maskSecret(responseSummary))
                .success(success)
                .costMs(costMs)
                .errorMsg(maskSecret(errorMsg))
                .build());
        recordBusinessLog("API_CALL", apiName, null, null, null, null, requestSummary, responseSummary, success, errorMsg, null);
    }

    @Override
    public Map<String, Object> mockScan(IYquePhaseMockRequest request) {
        long start = System.currentTimeMillis();
        IYquePhaseMockRequest input = request == null ? new IYquePhaseMockRequest() : request;
        Optional<IYqueUserCode> userCodeOptional = findUserCodeByMockRequest(input);
        String state = resolveMockState(input, userCodeOptional.orElse(null));
        Optional<IYqueUserCode> recognizedUserCode = findUserCodeByState(state);
        IYqueUserCode userCode = recognizedUserCode.orElse(userCodeOptional.orElse(null));
        String channelCodeId = userCode == null || userCode.getId() == null ? input.getChannelId() : userCode.getId().toString();

        Map<String, Object> result = new HashMap<>();
        result.put("mockMode", true);
        result.put("event", "SCAN");
        result.put("state", state);
        result.put("channelCodeId", channelCodeId);
        result.put("channelCodeName", userCode == null ? null : userCode.getCodeName());
        result.put("recognized", userCode != null);
        result.put("message", userCode == null ? "未识别到渠道，已记录模拟扫码日志" : "已识别渠道并记录模拟扫码日志");

        recordBusinessLog("MOCK_SCAN", safe(state), null, null, null, channelCodeId,
                mockRequestSummary(input), String.valueOf(result.get("message")), true, null, null);
        logApiCall("MOCK_SCAN", mockRequestSummary(input), result.toString(), true, System.currentTimeMillis() - start, null);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockAddContact(IYquePhaseMockRequest request) {
        long start = System.currentTimeMillis();
        IYquePhaseMockRequest input = request == null ? new IYquePhaseMockRequest() : request;
        Optional<IYqueUserCode> userCodeOptional = findUserCodeByMockRequest(input);
        IYqueUserCode userCode = userCodeOptional.orElse(null);
        String state = resolveMockState(input, userCode);
        if (userCode == null) {
            userCode = findUserCodeByState(state).orElse(null);
        }
        String channelCodeId = userCode == null || userCode.getId() == null ? null : userCode.getId().toString();
        String externalUserId = mockValue(input.getExternalUserId(), "mock_external_user_", digest(state).substring(0, 8));
        String userId = mockValue(input.getUserId(), "mock_user_", channelCodeId == null ? "default" : channelCodeId);
        String customerName = mockValue(input.getCustomerName(), "mock_customer_", digest(externalUserId).substring(0, 8));
        String tagIds = resolveMockTagIds(userCode);

        IYQueCustomerInfo customerInfo = upsertMockCustomer(externalUserId, userId, customerName, state, tagIds);

        IYqueCallBackBaseMsg callback = mockBaseMsg("change_external_contact", "add_external_contact", externalUserId, userId, state);
        recordNewCustomerRoute(callback);

        IYquePhaseGroupRouteRule routeQuery = IYquePhaseGroupRouteRule.builder()
                .channelCodeId(channelCodeId)
                .requiredTagIds(tagIds)
                .build();
        Map<String, Object> routeResult = matchGroupRoute(routeQuery);

        String businessKey = "mock:add-contact:" + externalUserId + ":" + userId;
        recordBusinessLog("MOCK_CUSTOMER_UPSERT", businessKey, externalUserId, userId, null, channelCodeId,
                mockRequestSummary(input), customerInfo.getEId(), true, null, null);
        recordBusinessLog("MOCK_TAG_APPLY", businessKey, externalUserId, userId, null, channelCodeId,
                tagIds, "模拟标签已写入客户记录", true, null, null);
        recordBusinessLog("WELCOME_SEND", businessKey, externalUserId, userId, null, channelCodeId,
                userCode == null ? "mock welcome" : safe(userCode.getWeclomeMsg()), "模拟欢迎语记录，不调用企业微信 API", true, null, null);

        Map<String, Object> result = new HashMap<>();
        result.put("mockMode", true);
        result.put("event", "ADD_CONTACT");
        result.put("externalUserId", externalUserId);
        result.put("userId", userId);
        result.put("customerName", customerName);
        result.put("state", state);
        result.put("channelCodeId", channelCodeId);
        result.put("channelCodeName", userCode == null ? null : userCode.getCodeName());
        result.put("tagIds", tagIds);
        result.put("customerEId", customerInfo.getEId());
        result.put("welcomeLogged", true);
        result.put("routeResult", routeResult);
        result.put("message", "模拟加好友已完成：客户入库、标签、欢迎语日志、分群引导记录已写入");
        logApiCall("MOCK_ADD_CONTACT", mockRequestSummary(input), result.toString(), true, System.currentTimeMillis() - start, null);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockJoinGroup(IYquePhaseMockRequest request) {
        return mockCustomerChatEvent(request, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockLeaveGroup(IYquePhaseMockRequest request) {
        return mockCustomerChatEvent(request, false);
    }

    private Map<String, Object> mockCustomerChatEvent(IYquePhaseMockRequest request, boolean join) {
        long start = System.currentTimeMillis();
        IYquePhaseMockRequest input = request == null ? new IYquePhaseMockRequest() : request;
        Optional<IYqueUserCode> userCodeOptional = findUserCodeByMockRequest(input);
        IYqueUserCode userCode = userCodeOptional.orElse(null);
        String state = resolveMockState(input, userCode);
        if (userCode == null) {
            userCode = findUserCodeByState(state).orElse(null);
        }
        String channelCodeId = userCode == null || userCode.getId() == null ? null : userCode.getId().toString();
        String externalUserId = mockValue(input.getExternalUserId(), "mock_external_user_", digest(state).substring(0, 8));
        String userId = mockValue(input.getUserId(), "mock_user_", channelCodeId == null ? "default" : channelCodeId);
        IYquePhaseGroupPoolChat poolChat = resolveMockPoolChat(input);
        String chatId = StringUtils.defaultIfBlank(input.getChatId(), poolChat == null ? null : poolChat.getChatId());
        chatId = mockValue(chatId, "mock_chat_", digest(externalUserId).substring(0, 8));
        String chatName = StringUtils.defaultIfBlank(input.getChatName(), poolChat == null ? null : poolChat.getChatName());
        chatName = mockValue(chatName, "mock_chat_name_", digest(chatId).substring(0, 8));

        IYqueCallBackBaseMsg callback = mockBaseMsg("change_external_chat", join ? "add_member" : "del_member", externalUserId, userId, state);
        callback.setChatId(chatId);
        callback.setChatName(chatName);
        callback.setUpdateDetail(join ? "add_member" : "del_member");
        recordCustomerChatCallback(callback, "mock:" + (join ? "join-group" : "leave-group"));

        List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByChatIdAndDelFlag(chatId, DEL_NORMAL);
        IYquePhaseGroupPoolChat latestChat = chats.isEmpty() ? null : chats.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("mockMode", true);
        result.put("event", join ? "JOIN_GROUP" : "LEAVE_GROUP");
        result.put("externalUserId", externalUserId);
        result.put("userId", userId);
        result.put("state", state);
        result.put("channelCodeId", channelCodeId);
        result.put("chatId", chatId);
        result.put("chatName", chatName);
        result.put("groupId", latestChat == null ? input.getGroupId() : latestChat.getGroupPoolId());
        result.put("currentMemberCount", latestChat == null ? null : latestChat.getCurrentMemberCount());
        result.put("message", join ? "模拟进群已完成：进群记录和群池人数已更新" : "模拟退群已完成：退群记录和群池人数已更新");
        recordBusinessLog(join ? "MOCK_JOIN_GROUP" : "MOCK_LEAVE_GROUP", safe(chatId), externalUserId, userId, chatId, channelCodeId,
                mockRequestSummary(input), String.valueOf(result.get("message")), true, null, null);
        logApiCall(join ? "MOCK_JOIN_GROUP" : "MOCK_LEAVE_GROUP", mockRequestSummary(input), result.toString(), true, System.currentTimeMillis() - start, null);
        return result;
    }

    private IYqueCallBackBaseMsg mockBaseMsg(String event, String changeType, String externalUserId, String userId, String state) {
        IYqueCallBackBaseMsg callback = new IYqueCallBackBaseMsg();
        callback.setEvent(event);
        callback.setChangeType(changeType);
        callback.setExternalUserID(externalUserId);
        callback.setUserID(userId);
        callback.setState(state);
        callback.setCreateTime(System.currentTimeMillis() / 1000L);
        callback.setMsgType("event");
        return callback;
    }

    private Optional<IYqueUserCode> findUserCodeByMockRequest(IYquePhaseMockRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        if (StringUtils.isNotBlank(request.getState())) {
            Optional<IYqueUserCode> byState = findUserCodeByState(request.getState());
            if (byState.isPresent()) {
                return byState;
            }
        }
        Long channelId = parseLong(request.getChannelId());
        if (channelId == null && StringUtils.isNotBlank(request.getState())) {
            channelId = parseLong(request.getState());
        }
        if (channelId != null) {
            return userCodeDao.findById(channelId);
        }
        return Optional.empty();
    }

    private String resolveMockState(IYquePhaseMockRequest request, IYqueUserCode userCode) {
        if (StringUtils.isNotBlank(request == null ? null : request.getState())) {
            return request.getState();
        }
        if (userCode != null && StringUtils.isNotBlank(userCode.getCodeState())) {
            return userCode.getCodeState();
        }
        String seed = StringUtils.defaultIfBlank(request == null ? null : request.getChannelId(), String.valueOf(System.currentTimeMillis()));
        return "mock_state_" + digest(seed).substring(0, 12);
    }

    private IYquePhaseGroupPoolChat resolveMockPoolChat(IYquePhaseMockRequest request) {
        if (request == null || request.getGroupId() == null || StringUtils.isNotBlank(request.getChatId())) {
            return null;
        }
        List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(request.getGroupId(), DEL_NORMAL);
        return chats.stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .findFirst()
                .orElse(chats.isEmpty() ? null : chats.get(0));
    }

    private IYQueCustomerInfo upsertMockCustomer(String externalUserId, String userId, String customerName, String state, String tagIds) {
        IYQueCustomerInfo customerInfo = customerInfoDao.findByExternalUseridAndUserId(externalUserId, userId);
        if (customerInfo == null) {
            customerInfo = IYQueCustomerInfo.builder()
                    .eId(externalUserId + "&" + userId)
                    .build();
        }
        customerInfo.setCustomerName(customerName);
        customerInfo.setExternalUserid(externalUserId);
        customerInfo.setUserId(userId);
        customerInfo.setState(state);
        customerInfo.setTagIds(tagIds);
        customerInfo.setType(1);
        customerInfo.setAddWay(String.valueOf(CustomerAddWay.ADD_WAY_SMEWM.getKey()));
        customerInfo.setAddTime(new Date());
        customerInfo.setStatus(CustomerStatusType.CUSTOMER_STATUS_TYPE_COMMON.getCode());
        return customerInfoDao.saveAndFlush(customerInfo);
    }

    private String resolveMockTagIds(IYqueUserCode userCode) {
        if (userCode != null && StringUtils.isNotBlank(userCode.getTagId())) {
            return userCode.getTagId();
        }
        return "mock_tag_auto";
    }

    private String mockValue(String value, String prefix, String fallbackSeed) {
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return prefix + StringUtils.defaultIfBlank(fallbackSeed, digest(String.valueOf(System.currentTimeMillis())).substring(0, 8));
    }

    private String mockRequestSummary(IYquePhaseMockRequest request) {
        return request == null ? "{}" : request.toString();
    }

    private Long parseLong(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> buildStatisticsSummary(List<IYquePhaseCustomerRouteLog> routeLogs, List<IYquePhaseCustomerChatRelation> relations) {
        long addFriendCount = routeLogs.size();
        long joinCount = relations.stream().filter(item -> "JOIN".equals(item.getEventType())).count();
        long leaveCount = relations.stream().filter(item -> "LEAVE".equals(item.getEventType())).count();
        Map<String, Object> summary = new HashMap<>();
        summary.put("addFriendCount", addFriendCount);
        summary.put("joinCount", joinCount);
        summary.put("leaveCount", leaveCount);
        summary.put("joinConversionRate", percent(joinCount, addFriendCount));
        summary.put("welcomeSuccessCount", businessLogDao.countByLogTypeAndSuccessAndDelFlag("WELCOME_SEND", true, DEL_NORMAL));
        summary.put("welcomeFailureCount", businessLogDao.countByLogTypeAndSuccessAndDelFlag("WELCOME_SEND", false, DEL_NORMAL));
        summary.put("apiFailureCount", apiCallLogDao.countBySuccessAndDelFlag(false, DEL_NORMAL));
        return summary;
    }

    private List<Map<String, Object>> buildChannelStats(List<IYquePhaseCustomerRouteLog> routeLogs, List<IYquePhaseCustomerChatRelation> relations) {
        Map<String, List<IYquePhaseCustomerRouteLog>> routeByChannel = routeLogs.stream()
                .collect(Collectors.groupingBy(item -> safe(item.getChannelCodeId())));
        return routeByChannel.entrySet().stream()
                .map(entry -> {
                    String channelCodeId = entry.getKey();
                    List<IYquePhaseCustomerRouteLog> channelRoutes = entry.getValue();
                    long addFriendCount = channelRoutes.size();
                    long joinCount = relations.stream()
                            .filter(item -> Objects.equals(safe(item.getChannelCodeId()), channelCodeId))
                            .filter(item -> "JOIN".equals(item.getEventType()))
                            .count();
                    long leaveCount = relations.stream()
                            .filter(item -> Objects.equals(safe(item.getChannelCodeId()), channelCodeId))
                            .filter(item -> "LEAVE".equals(item.getEventType()))
                            .count();
                    Map<String, Object> row = new HashMap<>();
                    row.put("channelCodeId", channelCodeId);
                    row.put("channelCodeName", firstNonBlank(channelRoutes.stream().map(IYquePhaseCustomerRouteLog::getChannelCodeName).collect(Collectors.toList())));
                    row.put("addFriendCount", addFriendCount);
                    row.put("joinCount", joinCount);
                    row.put("leaveCount", leaveCount);
                    row.put("joinConversionRate", percent(joinCount, addFriendCount));
                    row.put("welcomeSuccessCount", 0);
                    row.put("welcomeFailureCount", 0);
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildEmployeeStats(List<IYquePhaseCustomerRouteLog> routeLogs, List<IYquePhaseCustomerChatRelation> relations) {
        Map<String, List<IYquePhaseCustomerRouteLog>> routeByEmployee = routeLogs.stream()
                .collect(Collectors.groupingBy(item -> safe(item.getUserId())));
        return routeByEmployee.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    List<IYquePhaseCustomerRouteLog> employeeRoutes = entry.getValue();
                    long customerCount = employeeRoutes.size();
                    long joinCount = relations.stream()
                            .filter(item -> Objects.equals(safe(item.getUserId()), userId))
                            .filter(item -> "JOIN".equals(item.getEventType()))
                            .count();
                    Map<String, Object> row = new HashMap<>();
                    row.put("userId", userId);
                    row.put("userName", firstNonBlank(employeeRoutes.stream().map(IYquePhaseCustomerRouteLog::getUserName).collect(Collectors.toList())));
                    row.put("customerCount", customerCount);
                    row.put("channelDistribution", employeeRoutes.stream()
                            .collect(Collectors.groupingBy(item -> StringUtils.defaultIfBlank(item.getChannelCodeName(), "未知渠道"), Collectors.counting())));
                    row.put("joinCount", joinCount);
                    row.put("joinConversionRate", percent(joinCount, customerCount));
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildGroupPoolStats(IYquePhaseStatisticsQuery query, List<IYquePhaseCustomerRouteLog> routeLogs) {
        return groupPoolDao.findAll().stream()
                .filter(pool -> query == null || query.getGroupPoolId() == null || Objects.equals(pool.getId(), query.getGroupPoolId()))
                .map(pool -> {
                    fillGroupPoolChats(pool);
                    int currentMemberCount = pool.getChats().stream()
                            .mapToInt(item -> item.getCurrentMemberCount() == null ? 0 : item.getCurrentMemberCount())
                            .sum();
                    int remainingCapacity = pool.getChats().stream()
                            .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                            .mapToInt(item -> {
                                int threshold = item.getCapacityThreshold() == null ? 0 : item.getCapacityThreshold();
                                int current = item.getCurrentMemberCount() == null ? 0 : item.getCurrentMemberCount();
                                return Math.max(0, threshold - current);
                            })
                            .sum();
                    long noAvailableCount = routeLogs.stream()
                            .filter(item -> Objects.equals(item.getTargetPoolId(), pool.getId()))
                            .filter(item -> ROUTE_NO_CHAT.equals(item.getRouteStatus()))
                            .count();
                    Map<String, Object> row = new HashMap<>();
                    row.put("groupPoolId", pool.getId());
                    row.put("groupPoolName", pool.getPoolName());
                    row.put("availableChatCount", pool.getAvailableChatCount());
                    row.put("currentMemberCount", currentMemberCount);
                    row.put("remainingCapacity", remainingCapacity);
                    row.put("thresholdReachedCount", pool.getChats().stream().filter(this::chatThresholdReached).count());
                    row.put("noAvailableAlertCount", noAvailableCount);
                    return row;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildExceptionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("apiFailureCount", apiCallLogDao.countBySuccessAndDelFlag(false, DEL_NORMAL));
        stats.put("welcomeFailureCount", businessLogDao.countByLogTypeAndSuccessAndDelFlag("WELCOME_SEND", false, DEL_NORMAL));
        stats.put("tagFailureCount", businessLogDao.countByLogTypeAndSuccessAndDelFlag("TAG_APPLY", false, DEL_NORMAL));
        stats.put("groupQrInvalidCount", alertLogDao.countByAlertTypeAndDelFlag("GROUP_QR_INVALID", DEL_NORMAL));
        stats.put("callbackFailureCount", alertLogDao.countByAlertTypeAndDelFlag("CALLBACK_FAILED", DEL_NORMAL));
        stats.put("syncFailureCount", businessLogDao.countByLogTypeAndSuccessAndDelFlag("DATA_SYNC", false, DEL_NORMAL));
        return stats;
    }

    private Specification<IYquePhaseCustomerRouteLog> routeLogStatisticsSpec(IYquePhaseStatisticsQuery query) {
        Specification<IYquePhaseCustomerRouteLog> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getChannelCodeId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("channelCodeId"), query.getChannelCodeId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getUserId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), query.getUserId()));
        }
        if (query != null && query.getGroupPoolId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("targetPoolId"), query.getGroupPoolId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getChatId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("targetChatId"), query.getChatId()));
        }
        if (query != null && query.getStartTime() != null && query.getEndTime() != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("eventTime"),
                    DateUtils.setTimeToStartOfDay(query.getStartTime()), DateUtils.setTimeToEndOfDay(query.getEndTime())));
        }
        return spec;
    }

    private Specification<IYquePhaseCustomerChatRelation> relationStatisticsSpec(IYquePhaseStatisticsQuery query) {
        Specification<IYquePhaseCustomerChatRelation> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getChannelCodeId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("channelCodeId"), query.getChannelCodeId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getUserId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), query.getUserId()));
        }
        if (query != null && query.getGroupPoolId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("groupPoolId"), query.getGroupPoolId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getChatId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("chatId"), query.getChatId()));
        }
        if (query != null && query.getStartTime() != null && query.getEndTime() != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("eventTime"),
                    DateUtils.setTimeToStartOfDay(query.getStartTime()), DateUtils.setTimeToEndOfDay(query.getEndTime())));
        }
        return spec;
    }

    private Specification<IYquePhaseBusinessLog> businessLogSpec(IYquePhaseBusinessLog query) {
        Specification<IYquePhaseBusinessLog> spec = Specification.where(null);
        if (query != null && StringUtils.isNotBlank(query.getLogType())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("logType"), query.getLogType()));
        }
        if (query != null && StringUtils.isNotBlank(query.getExternalUserid())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("externalUserid"), query.getExternalUserid()));
        }
        if (query != null && StringUtils.isNotBlank(query.getUserId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("userId"), query.getUserId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getChatId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("chatId"), query.getChatId()));
        }
        if (query != null && StringUtils.isNotBlank(query.getChannelCodeId())) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("channelCodeId"), query.getChannelCodeId()));
        }
        if (query != null && query.getSuccess() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("success"), query.getSuccess()));
        }
        if (query != null && query.getStartTime() != null && query.getEndTime() != null) {
            spec = spec.and((root, q, cb) -> cb.between(root.get("createTime"),
                    DateUtils.setTimeToStartOfDay(query.getStartTime()), DateUtils.setTimeToEndOfDay(query.getEndTime())));
        }
        return spec;
    }

    private void appendExportRows(List<Map<String, Object>> rows, String section, Map<String, Object> values) {
        values.forEach((key, value) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("section", section);
            row.put("metric", key);
            row.put("value", value);
            rows.add(row);
        });
    }

    private void appendListExportRows(List<Map<String, Object>> rows, String section, List<Map<String, Object>> values) {
        for (Map<String, Object> value : values) {
            Map<String, Object> row = new HashMap<>(value);
            row.put("section", section);
            rows.add(row);
        }
    }

    private Map<String, Object> acceptanceItem(String item, String standard, boolean passed) {
        Map<String, Object> row = new HashMap<>();
        row.put("item", item);
        row.put("standard", standard);
        row.put("status", passed ? "PASS" : "PENDING");
        return row;
    }

    private void normalizeRouteRule(IYquePhaseGroupRouteRule rule) {
        if (rule == null) {
            return;
        }
        if (StringUtils.isBlank(rule.getRuleType())) {
            if (Boolean.TRUE.equals(rule.getFallbackFlag())) {
                rule.setRuleType(IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT);
            } else if (StringUtils.isNotBlank(rule.getRequiredTagIds())) {
                rule.setRuleType(IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG);
            } else if (StringUtils.isNotBlank(rule.getChannelCodeId())) {
                rule.setRuleType(IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT);
            } else {
                rule.setRuleType(IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT);
            }
        }
        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }
        rule.setFallbackFlag(IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT.equals(rule.getRuleType()));
    }

    private boolean routeRuleMatched(IYquePhaseGroupRouteRule rule, String inputChannelCodeId, Set<String> inputTags) {
        String ruleType = rule.getRuleType();
        if (IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG.equals(ruleType)) {
            return optionalChannelMatched(rule.getChannelCodeId(), inputChannelCodeId)
                    && StringUtils.isNotBlank(rule.getRequiredTagIds())
                    && tagsMatched(rule.getRequiredTagIds(), inputTags);
        }
        if (IYquePhaseGroupRouteRule.TYPE_CHANNEL_TAG.equals(ruleType)) {
            return channelMatched(rule.getChannelCodeId(), inputChannelCodeId)
                    && StringUtils.isNotBlank(rule.getRequiredTagIds())
                    && tagsMatched(rule.getRequiredTagIds(), inputTags);
        }
        if (IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT.equals(ruleType)) {
            return StringUtils.isNotBlank(rule.getChannelCodeId())
                    && Objects.equals(rule.getChannelCodeId(), inputChannelCodeId);
        }
        if (IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT.equals(ruleType)) {
            return true;
        }
        return false;
    }

    private String ruleTypeLabel(String ruleType) {
        if (IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG.equals(ruleType)) {
            return "客户标签规则";
        }
        if (IYquePhaseGroupRouteRule.TYPE_CHANNEL_TAG.equals(ruleType)) {
            return "渠道标签规则";
        }
        if (IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT.equals(ruleType)) {
            return "渠道默认群池";
        }
        if (IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT.equals(ruleType)) {
            return "全局默认群池";
        }
        return "未知规则";
    }

    private boolean chatThresholdReached(IYquePhaseGroupPoolChat chat) {
        if (chat == null || !Boolean.TRUE.equals(chat.getEnabled())) {
            return false;
        }
        Integer current = chat.getCurrentMemberCount();
        Integer threshold = chat.getCapacityThreshold();
        return current != null && threshold != null && current >= threshold;
    }

    private Optional<IYqueUserCode> findUserCodeByState(String state) {
        if (StringUtils.isBlank(state)) {
            return Optional.empty();
        }
        return Optional.ofNullable(userCodeDao.findByCodeState(state));
    }

    private void updateGroupPoolChatMemberCount(IYquePhaseCustomerChatRelation relation) {
        if (relation == null || StringUtils.isBlank(relation.getChatId())) {
            return;
        }
        if (!"JOIN".equals(relation.getEventType()) && !"LEAVE".equals(relation.getEventType())) {
            return;
        }
        List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByChatIdAndDelFlag(relation.getChatId(), DEL_NORMAL);
        if (chats.isEmpty()) {
            createAlert("CUSTOMER_CHAT_NOT_BOUND", ALERT_MEDIUM, "chat=" + safe(relation.getChatId()),
                    "客户群未绑定群池", "客户群回调无法匹配到一期群池绑定关系");
            return;
        }
        for (IYquePhaseGroupPoolChat chat : chats) {
            int current = chat.getCurrentMemberCount() == null ? 0 : chat.getCurrentMemberCount();
            if ("JOIN".equals(relation.getEventType())) {
                chat.setCurrentMemberCount(current + 1);
            } else {
                chat.setCurrentMemberCount(Math.max(0, current - 1));
            }
            if (chatThresholdReached(chat)) {
                createAlert("GROUP_CHAT_THRESHOLD_REACHED", ALERT_MEDIUM, "chat=" + safe(chat.getChatId()),
                        "客户群达到容量阈值", "客户群[" + safe(chat.getChatName()) + "]当前人数已达到容量阈值");
            }
        }
        groupPoolChatDao.saveAll(chats);
    }

    private void fillGroupPoolChats(IYquePhaseGroupPool pool) {
        List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(pool.getId(), DEL_NORMAL);
        pool.setChats(chats);
        pool.setChatCount(chats.size());
        pool.setAvailableChatCount((int) chats.stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .filter(item -> item.getCurrentMemberCount() == null || item.getCapacityThreshold() == null
                        || item.getCurrentMemberCount() < item.getCapacityThreshold())
                .count());
    }

    private void fillRouteRulePoolName(IYquePhaseGroupRouteRule rule) {
        if (rule.getTargetPoolId() != null) {
            groupPoolDao.findById(rule.getTargetPoolId()).ifPresent(pool -> rule.setTargetPoolName(pool.getPoolName()));
        }
    }

    private void fillRelationPoolInfo(IYquePhaseCustomerChatRelation relation) {
        if (StringUtils.isBlank(relation.getChatId())) {
            return;
        }
        List<IYquePhaseGroupPoolChat> chats = groupPoolChatDao.findByChatIdAndDelFlag(relation.getChatId(), DEL_NORMAL);
        if (chats.isEmpty()) {
            return;
        }
        IYquePhaseGroupPoolChat poolChat = chats.get(0);
        relation.setGroupPoolId(poolChat.getGroupPoolId());
        relation.setChatName(StringUtils.defaultIfBlank(relation.getChatName(), poolChat.getChatName()));
        groupPoolDao.findById(poolChat.getGroupPoolId()).ifPresent(pool -> relation.setGroupPoolName(pool.getPoolName()));
    }

    private boolean isCustomerChatCallback(IYqueCallBackBaseMsg msg) {
        String event = StringUtils.lowerCase(safe(msg.getEvent()));
        String changeType = StringUtils.lowerCase(safe(msg.getChangeType()));
        String updateDetail = StringUtils.lowerCase(safe(msg.getUpdateDetail()));
        return event.contains("external_chat")
                || changeType.contains("member")
                || updateDetail.contains("member");
    }

    private String resolveCustomerChatEventType(IYqueCallBackBaseMsg msg) {
        String value = StringUtils.lowerCase(safe(msg.getChangeType()) + " " + safe(msg.getUpdateDetail()));
        if (value.contains("del") || value.contains("quit") || value.contains("leave")) {
            return "LEAVE";
        }
        if (value.contains("add") || value.contains("join") || value.contains("create")) {
            return "JOIN";
        }
        return "UPDATE";
    }

    private String resolveCustomerChatStatus(IYqueCallBackBaseMsg msg) {
        String eventType = resolveCustomerChatEventType(msg);
        if ("JOIN".equals(eventType)) {
            return "IN";
        }
        if ("LEAVE".equals(eventType)) {
            return "OUT";
        }
        return "UNKNOWN";
    }

    private void createAlertAndFailedTask(String type, String level, String title, String content, String businessKey) {
        createAlert(type, level, businessKey, title, content);
        failedTaskDao.save(IYquePhaseFailedTask.builder()
                .taskType(type)
                .businessKey(businessKey)
                .requestBody(content)
                .errorMsg(content)
                .status("PENDING")
                .retryCount(0)
                .maxRetryCount(3)
                .nextRetryTime(nextRetryTime(0))
                .build());
    }

    private void createAlert(String type, String level, String businessKey, String title, String content) {
        IYquePhaseAlertLog alert = alertLogDao.save(IYquePhaseAlertLog.builder()
                .alertType(type)
                .alertLevel(level)
                .businessKey(businessKey)
                .alertTitle(title)
                .alertContent(content)
                .status("PENDING")
                .notifyChannels(ALERT_HIGH.equals(level) ? "BACKEND,WECHAT_ROBOT,EMAIL" : "BACKEND")
                .notifyStatus(ALERT_HIGH.equals(level) ? "PENDING" : "BACKEND_ONLY")
                .build());
        recordBusinessLog("ALERT", businessKey, null, null, null, null, title, content, false, content, alert.getId());
        notifyHighAlert(alert);
    }

    private void notifyHighAlert(IYquePhaseAlertLog alert) {
        if (alert == null || !ALERT_HIGH.equals(alert.getAlertLevel())) {
            return;
        }
        String notifyText = buildAlertNotifyText(alert);
        Long robotId = paramConfig == null ? null : paramConfig.getPhaseOneAlertRobotId();
        String[] emailReceivers = resolveEmailReceivers();
        List<String> notifyErrors = new ArrayList<>();

        boolean robotConfigured = robotId != null;
        boolean emailConfigured = emailReceivers.length > 0;
        boolean robotSent = false;
        boolean emailSent = false;

        if (robotConfigured) {
            try {
                sendAlertRobot(alert, robotId, notifyText);
                robotSent = true;
            } catch (Exception e) {
                notifyErrors.add("企业微信机器人通知失败: " + e.getMessage());
            }
        } else {
            notifyErrors.add("未配置 iyque.phaseOneAlertRobotId");
        }

        if (emailConfigured) {
            JavaMailSender mailSender = mailSenderProvider == null ? null : mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                notifyErrors.add("未配置 spring.mail.*，无法发送邮件");
            } else {
                try {
                    sendAlertMail(alert, mailSender, emailReceivers, notifyText);
                    emailSent = true;
                } catch (Exception e) {
                    notifyErrors.add("邮件通知失败: " + e.getMessage());
                }
            }
        } else {
            notifyErrors.add("未配置 iyque.phaseOneAlertEmailReceivers");
        }

        String notifyStatus = resolveNotifyStatus(robotConfigured, emailConfigured, robotSent, emailSent);
        alert.setNotifyStatus(notifyStatus);
        alert.setNotifyError(notifyErrors.isEmpty() ? null : String.join("；", notifyErrors));
        alertLogDao.save(alert);
        recordBusinessLog("ALERT_NOTIFY", String.valueOf(alert.getId()), null, null, null, null,
                alert.getNotifyChannels(), alert.getNotifyStatus(), "SENT".equals(notifyStatus), alert.getNotifyError(), alert.getId());
    }

    private String buildAlertNotifyText(IYquePhaseAlertLog alert) {
        return "【一期高等级告警】" + safe(alert.getAlertTitle()) + "\n"
                + "类型：" + safe(alert.getAlertType()) + "\n"
                + "对象：" + safe(alert.getBusinessKey()) + "\n"
                + "内容：" + safe(alert.getAlertContent());
    }

    private void sendAlertRobot(IYquePhaseAlertLog alert, Long robotId, String notifyText) throws Exception {
        Text text = new Text();
        text.setContent(notifyText);
        IYqueRobotSub sub = IYqueRobotSub.builder()
                .msgType(IYqueMsgAnnex.MsgType.MSG_TEXT)
                .text(text)
                .build();
        IYqueRobot robot = IYqueRobot.builder()
                .robotId(robotId)
                .msgTitle(alert.getAlertTitle())
                .robotSubList(List.of(sub))
                .build();
        robotService.sendRobotMsg(robot);
    }

    private void sendAlertMail(IYquePhaseAlertLog alert, JavaMailSender mailSender, String[] receivers, String notifyText) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        if (paramConfig != null && StringUtils.isNotBlank(paramConfig.getPhaseOneAlertEmailFrom())) {
            mailMessage.setFrom(paramConfig.getPhaseOneAlertEmailFrom());
        }
        mailMessage.setTo(receivers);
        mailMessage.setSubject("【一期高等级告警】" + safe(alert.getAlertTitle()));
        mailMessage.setText(notifyText);
        mailSender.send(mailMessage);
    }

    private String[] resolveEmailReceivers() {
        if (paramConfig == null || StringUtils.isBlank(paramConfig.getPhaseOneAlertEmailReceivers())) {
            return new String[0];
        }
        return Arrays.stream(paramConfig.getPhaseOneAlertEmailReceivers().split("[,;，；\\s]+"))
                .filter(StringUtils::isNotBlank)
                .toArray(String[]::new);
    }

    private String resolveNotifyStatus(boolean robotConfigured, boolean emailConfigured, boolean robotSent, boolean emailSent) {
        boolean anyConfigured = robotConfigured || emailConfigured;
        boolean anySent = robotSent || emailSent;
        if (!anyConfigured) {
            return "PENDING_CONFIG";
        }
        if (robotConfigured && emailConfigured && robotSent && emailSent) {
            return "SENT";
        }
        if (robotSent && !emailConfigured) {
            return "ROBOT_SENT";
        }
        if (emailSent && !robotConfigured) {
            return "EMAIL_SENT";
        }
        if (anySent) {
            return "PARTIAL_SENT";
        }
        if (!robotConfigured || !emailConfigured) {
            return "PENDING_CONFIG";
        }
        return "FAILED";
    }

    private void recordBusinessLog(String logType,
                                   String businessId,
                                   String externalUserid,
                                   String userId,
                                   String chatId,
                                   String channelCodeId,
                                   String requestSummary,
                                   String responseSummary,
                                   boolean success,
                                   String errorMsg,
                                   Long alertId) {
        try {
            businessLogDao.save(IYquePhaseBusinessLog.builder()
                    .logType(logType)
                    .businessId(businessId)
                    .externalUserid(externalUserid)
                    .userId(userId)
                    .chatId(chatId)
                    .channelCodeId(channelCodeId)
                    .requestSummary(maskSecret(requestSummary))
                    .responseSummary(maskSecret(responseSummary))
                    .success(success)
                    .errorMsg(maskSecret(errorMsg))
                    .alertId(alertId)
                    .build());
        } catch (Exception e) {
            log.warn("一期业务日志写入失败: {}", e.getMessage());
        }
    }

    private Date nextRetryTime(int retryCount) {
        long[] delays = new long[]{5L, 30L, 120L};
        int index = Math.min(Math.max(retryCount, 0), delays.length - 1);
        return new Date(System.currentTimeMillis() + delays[index] * 60L * 1000L);
    }

    private boolean channelMatched(String ruleChannelCodeId, String inputChannelCodeId) {
        return StringUtils.isNotBlank(ruleChannelCodeId) && Objects.equals(ruleChannelCodeId, inputChannelCodeId);
    }

    private boolean optionalChannelMatched(String ruleChannelCodeId, String inputChannelCodeId) {
        return StringUtils.isBlank(ruleChannelCodeId) || Objects.equals(ruleChannelCodeId, inputChannelCodeId);
    }

    private boolean tagsMatched(String ruleTagIds, Set<String> inputTags) {
        Set<String> requiredTags = splitToSet(ruleTagIds);
        return requiredTags.isEmpty() || inputTags.containsAll(requiredTags);
    }

    private Set<String> splitToSet(String value) {
        if (StringUtils.isBlank(value)) {
            return new HashSet<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    private List<Long> parseIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return new ArrayList<>();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private String like(String value) {
        return "%" + StringUtils.trimToEmpty(value) + "%";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(List<String> values) {
        return values.stream()
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
    }

    private String percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return "0.00%";
        }
        return String.format("%.2f%%", numerator * 100.0D / denominator);
    }

    private String maskSecret(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replaceAll("(?i)(access_token=)[^&\\s]+", "$1******")
                .replaceAll("(?i)(secret=)[^&\\s]+", "$1******")
                .replaceAll("(?i)(encodingAESKey=)[^&\\s]+", "$1******");
    }

    private String digest(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(safe(value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : digest) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(safe(value).hashCode());
        }
    }
}
