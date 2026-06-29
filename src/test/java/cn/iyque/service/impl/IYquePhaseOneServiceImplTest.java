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
import cn.iyque.domain.IYQueCustomerInfo;
import cn.iyque.entity.IYquePhaseCustomerRouteLog;
import cn.iyque.entity.IYquePhaseAlertLog;
import cn.iyque.entity.IYquePhaseGroupPool;
import cn.iyque.entity.IYquePhaseGroupPoolChat;
import cn.iyque.entity.IYquePhaseGroupRouteRule;
import cn.iyque.entity.IYqueUserCode;
import cn.iyque.service.IYQueRobotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IYquePhaseOneServiceImplTest {

    @Mock
    private IYquePhaseEmployeePoolDao employeePoolDao;
    @Mock
    private IYquePhaseGroupPoolDao groupPoolDao;
    @Mock
    private IYquePhaseGroupPoolChatDao groupPoolChatDao;
    @Mock
    private IYquePhaseGroupRouteRuleDao groupRouteRuleDao;
    @Mock
    private IYquePhaseCustomerChatRelationDao customerChatRelationDao;
    @Mock
    private IYquePhaseCustomerRouteLogDao customerRouteLogDao;
    @Mock
    private IYquePhaseBusinessLogDao businessLogDao;
    @Mock
    private IYquePhaseCallbackIdempotentDao callbackIdempotentDao;
    @Mock
    private IYquePhaseApiCallLogDao apiCallLogDao;
    @Mock
    private IYquePhaseAlertLogDao alertLogDao;
    @Mock
    private IYquePhaseFailedTaskDao failedTaskDao;
    @Mock
    private IYqueUserCodeDao userCodeDao;
    @Mock
    private IYQueCustomerInfoDao customerInfoDao;
    @Mock
    private IYQueRobotService robotService;
    @Mock
    private IYqueParamConfig paramConfig;
    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock
    private JavaMailSender mailSender;

    private IYquePhaseOneServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new IYquePhaseOneServiceImpl(
                employeePoolDao,
                groupPoolDao,
                groupPoolChatDao,
                groupRouteRuleDao,
                customerChatRelationDao,
                customerRouteLogDao,
                businessLogDao,
                callbackIdempotentDao,
                apiCallLogDao,
                alertLogDao,
                failedTaskDao,
                userCodeDao,
                customerInfoDao,
                robotService,
                paramConfig,
                mailSenderProvider
        );
    }

    @Test
    void previewUsesFixedRuleTypeOrderAndDoesNotCreateAlerts() {
        IYquePhaseGroupRouteRule customerTagRule = rule("customer", IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG, "vip", "1", 10, 101L);
        IYquePhaseGroupRouteRule globalRule = rule("global", IYquePhaseGroupRouteRule.TYPE_GLOBAL_DEFAULT, null, null, 100, 102L);
        IYquePhaseGroupPool pool = pool(101L);
        IYquePhaseGroupPoolChat chat = chat("chat-a", 10, 100);

        when(groupRouteRuleDao.findByEnabledAndDelFlagOrderByPriorityDesc(true, 0)).thenReturn(List.of(globalRule, customerTagRule));
        when(groupPoolDao.findById(101L)).thenReturn(Optional.of(pool));
        when(groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(101L, 0)).thenReturn(List.of(chat));

        Map<String, Object> result = service.matchGroupRoute(IYquePhaseGroupRouteRule.builder()
                .channelCodeId("1")
                .requiredTagIds("vip")
                .build());

        assertThat(result.get("ruleType")).isEqualTo(IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG);
        assertThat(((IYquePhaseGroupRouteRule) result.get("rule")).getRuleName()).isEqualTo("customer");
        assertThat(((IYquePhaseGroupPoolChat) result.get("chat")).getChatId()).isEqualTo("chat-a");
        verify(alertLogDao, never()).save(any());
        verify(failedTaskDao, never()).save(any());
    }

    @Test
    void realNewCustomerCallbackLogsNoAvailableChatAndCreatesAlert() throws Exception {
        IYqueUserCode userCode = IYqueUserCode.builder()
                .id(1L)
                .codeName("渠道A")
                .codeState("state-a")
                .tagId("vip")
                .build();
        IYquePhaseGroupRouteRule rule = rule("channel", IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT, null, "1", 10, 101L);
        IYquePhaseGroupPool pool = pool(101L);
        IYquePhaseGroupPoolChat fullChat = chat("chat-full", 100, 100);
        IYqueCallBackBaseMsg msg = new IYqueCallBackBaseMsg();
        msg.setState("state-a");
        msg.setExternalUserID("external-a");
        msg.setUserID("employee-a");
        msg.setCreateTime(1000L);

        when(userCodeDao.findByCodeState("state-a")).thenReturn(userCode);
        when(groupRouteRuleDao.findByEnabledAndDelFlagOrderByPriorityDesc(true, 0)).thenReturn(List.of(rule));
        when(groupPoolDao.findById(101L)).thenReturn(Optional.of(pool));
        when(groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(101L, 0)).thenReturn(List.of(fullChat));
        stubAlertSave();

        service.recordNewCustomerRoute(msg);

        ArgumentCaptor<IYquePhaseCustomerRouteLog> routeLogCaptor = ArgumentCaptor.forClass(IYquePhaseCustomerRouteLog.class);
        verify(customerRouteLogDao).save(routeLogCaptor.capture());
        assertThat(routeLogCaptor.getValue().getRouteStatus()).isEqualTo("NO_AVAILABLE_CHAT");
        assertThat(routeLogCaptor.getValue().getChannelCodeId()).isEqualTo("1");
        verify(alertLogDao, atLeastOnce()).save(any());
        verify(failedTaskDao).save(any());
    }

    @Test
    void highAlertSendsWechatRobotAndEmailWhenConfigured() throws Exception {
        IYqueUserCode userCode = IYqueUserCode.builder()
                .id(1L)
                .codeName("channel-a")
                .codeState("state-a")
                .tagId("vip")
                .build();
        IYquePhaseGroupRouteRule rule = rule("channel", IYquePhaseGroupRouteRule.TYPE_CHANNEL_DEFAULT, null, "1", 10, 101L);
        IYquePhaseGroupPool pool = pool(101L);
        IYquePhaseGroupPoolChat fullChat = chat("chat-full", 100, 100);
        IYqueCallBackBaseMsg msg = new IYqueCallBackBaseMsg();
        msg.setState("state-a");
        msg.setExternalUserID("external-a");
        msg.setUserID("employee-a");
        msg.setCreateTime(1000L);

        when(userCodeDao.findByCodeState("state-a")).thenReturn(userCode);
        when(groupRouteRuleDao.findByEnabledAndDelFlagOrderByPriorityDesc(true, 0)).thenReturn(List.of(rule));
        when(groupPoolDao.findById(101L)).thenReturn(Optional.of(pool));
        when(groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(101L, 0)).thenReturn(List.of(fullChat));
        when(paramConfig.getPhaseOneAlertRobotId()).thenReturn(99L);
        when(paramConfig.getPhaseOneAlertEmailReceivers()).thenReturn("ops@example.com;dev@example.com");
        when(paramConfig.getPhaseOneAlertEmailFrom()).thenReturn("noreply@example.com");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        stubAlertSave();

        service.recordNewCustomerRoute(msg);

        verify(robotService).sendRobotMsg(any());
        verify(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<IYquePhaseAlertLog> alertCaptor = ArgumentCaptor.forClass(IYquePhaseAlertLog.class);
        verify(alertLogDao, atLeastOnce()).save(alertCaptor.capture());
        assertThat(alertCaptor.getAllValues()).anyMatch(alert -> "SENT".equals(alert.getNotifyStatus()));
    }

    @Test
    void mockAddContactWritesCustomerAndRoutesWithoutWechatApi() throws Exception {
        IYqueUserCode userCode = IYqueUserCode.builder()
                .id(1L)
                .codeName("channel-a")
                .codeState("state-a")
                .tagId("vip")
                .build();
        IYquePhaseGroupRouteRule rule = rule("customer", IYquePhaseGroupRouteRule.TYPE_CUSTOMER_TAG, "vip", "1", 10, 101L);
        IYquePhaseGroupPool pool = pool(101L);
        IYquePhaseGroupPoolChat chat = chat("mock_chat_a", 10, 100);
        IYquePhaseMockRequest request = new IYquePhaseMockRequest();
        request.setState("state-a");
        request.setExternalUserId("mock_external_a");
        request.setUserId("mock_user_a");
        request.setCustomerName("mock_customer_a");

        when(userCodeDao.findByCodeState("state-a")).thenReturn(userCode);
        when(customerInfoDao.findByExternalUseridAndUserId("mock_external_a", "mock_user_a")).thenReturn(null);
        when(customerInfoDao.saveAndFlush(any(IYQueCustomerInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRouteRuleDao.findByEnabledAndDelFlagOrderByPriorityDesc(true, 0)).thenReturn(List.of(rule));
        when(groupPoolDao.findById(101L)).thenReturn(Optional.of(pool));
        when(groupPoolChatDao.findByGroupPoolIdAndDelFlagOrderByBackupFlagAscSortAsc(101L, 0)).thenReturn(List.of(chat));

        Map<String, Object> result = service.mockAddContact(request);

        assertThat(result.get("mockMode")).isEqualTo(true);
        assertThat(result.get("externalUserId")).isEqualTo("mock_external_a");
        assertThat(result.get("channelCodeId")).isEqualTo("1");
        verify(customerInfoDao).saveAndFlush(any(IYQueCustomerInfo.class));
        verify(customerRouteLogDao).save(any());
        verify(apiCallLogDao).save(any());
        verify(robotService, never()).sendRobotMsg(any());
    }

    @Test
    void customerChatJoinUpdatesPoolChatMemberCount() {
        IYquePhaseGroupPoolChat chat = chat("chat-a", 3, 100);
        IYqueCallBackBaseMsg msg = new IYqueCallBackBaseMsg();
        msg.setEvent("change_external_chat");
        msg.setChangeType("add_member");
        msg.setExternalUserID("external-a");
        msg.setChatId("chat-a");
        msg.setCreateTime(1000L);

        when(groupPoolChatDao.findByChatIdAndDelFlag("chat-a", 0)).thenReturn(List.of(chat));

        service.recordCustomerChatCallback(msg, "<xml/>");

        assertThat(chat.getCurrentMemberCount()).isEqualTo(4);
        verify(customerChatRelationDao).save(any());
        verify(groupPoolChatDao).saveAll(List.of(chat));
    }

    private IYquePhaseGroupRouteRule rule(String name, String type, String tags, String channel, Integer priority, Long poolId) {
        IYquePhaseGroupRouteRule rule = IYquePhaseGroupRouteRule.builder()
                .ruleName(name)
                .ruleType(type)
                .requiredTagIds(tags)
                .channelCodeId(channel)
                .targetPoolId(poolId)
                .priority(priority)
                .enabled(true)
                .build();
        rule.setId(priority == null ? 1L : priority.longValue());
        return rule;
    }

    private IYquePhaseGroupPool pool(Long id) {
        IYquePhaseGroupPool pool = IYquePhaseGroupPool.builder()
                .poolName("pool-" + id)
                .enabled(true)
                .build();
        pool.setId(id);
        return pool;
    }

    private IYquePhaseGroupPoolChat chat(String chatId, Integer current, Integer threshold) {
        return IYquePhaseGroupPoolChat.builder()
                .chatId(chatId)
                .chatName(chatId)
                .currentMemberCount(current)
                .capacityThreshold(threshold)
                .backupFlag(false)
                .sort(0)
                .enabled(true)
                .build();
    }

    private void stubAlertSave() {
        when(alertLogDao.save(any(IYquePhaseAlertLog.class))).thenAnswer(invocation -> {
            IYquePhaseAlertLog alert = invocation.getArgument(0);
            if (alert.getId() == null) {
                alert.setId(1L);
            }
            return alert;
        });
    }
}
