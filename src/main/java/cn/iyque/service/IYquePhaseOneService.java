package cn.iyque.service;

import cn.iyque.domain.IYqueCallBackBaseMsg;
import cn.iyque.domain.IYquePhaseMockRequest;
import cn.iyque.domain.IYquePhaseStatisticsQuery;
import cn.iyque.entity.IYquePhaseAlertLog;
import cn.iyque.entity.IYquePhaseApiCallLog;
import cn.iyque.entity.IYquePhaseBusinessLog;
import cn.iyque.entity.IYquePhaseCustomerChatRelation;
import cn.iyque.entity.IYquePhaseCustomerRouteLog;
import cn.iyque.entity.IYquePhaseEmployeePool;
import cn.iyque.entity.IYquePhaseFailedTask;
import cn.iyque.entity.IYquePhaseGroupPool;
import cn.iyque.entity.IYquePhaseGroupRouteRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IYquePhaseOneService {

    Page<IYquePhaseEmployeePool> findEmployeePoolPage(IYquePhaseEmployeePool query, Pageable pageable);

    IYquePhaseEmployeePool saveEmployeePool(IYquePhaseEmployeePool employeePool);

    void deleteEmployeePools(String ids);

    Page<IYquePhaseGroupPool> findGroupPoolPage(IYquePhaseGroupPool query, Pageable pageable);

    IYquePhaseGroupPool saveGroupPool(IYquePhaseGroupPool groupPool);

    void deleteGroupPools(String ids);

    Page<IYquePhaseGroupRouteRule> findGroupRouteRulePage(IYquePhaseGroupRouteRule query, Pageable pageable);

    IYquePhaseGroupRouteRule saveGroupRouteRule(IYquePhaseGroupRouteRule groupRouteRule);

    void deleteGroupRouteRules(String ids);

    Map<String, Object> matchGroupRoute(IYquePhaseGroupRouteRule query);

    Page<IYquePhaseCustomerChatRelation> findCustomerChatRelationPage(IYquePhaseCustomerChatRelation query, Pageable pageable);

    Page<IYquePhaseCustomerRouteLog> findCustomerRouteLogPage(IYquePhaseCustomerRouteLog query, Pageable pageable);

    Page<IYquePhaseAlertLog> findAlertLogPage(IYquePhaseAlertLog query, Pageable pageable);

    IYquePhaseAlertLog handleAlert(Long id, IYquePhaseAlertLog alertLog);

    Page<IYquePhaseFailedTask> findFailedTaskPage(IYquePhaseFailedTask query, Pageable pageable);

    IYquePhaseFailedTask retryFailedTask(Long id);

    Page<IYquePhaseApiCallLog> findApiCallLogPage(IYquePhaseApiCallLog query, Pageable pageable);

    Map<String, Object> summary();

    Map<String, Object> statisticsReport(IYquePhaseStatisticsQuery query);

    Page<IYquePhaseBusinessLog> findBusinessLogPage(IYquePhaseBusinessLog query, Pageable pageable);

    java.util.List<Map<String, Object>> acceptanceSummary();

    java.util.List<Map<String, Object>> exportStatisticsRows(IYquePhaseStatisticsQuery query);

    String buildCallbackEventKey(IYqueCallBackBaseMsg callBackBaseMsg, String rawXml);

    boolean prepareCallback(String eventKey, IYqueCallBackBaseMsg callBackBaseMsg, String rawXml);

    void finishCallbackSuccess(String eventKey);

    void finishCallbackFailure(String eventKey, Exception exception, String rawXml);

    void recordNewCustomerRoute(IYqueCallBackBaseMsg callBackBaseMsg);

    void recordCustomerChatCallback(IYqueCallBackBaseMsg callBackBaseMsg, String rawXml);

    void retryDueFailedTasks();

    void logApiCall(String apiName, String requestSummary, String responseSummary, boolean success, long costMs, String errorMsg);

    Map<String, Object> mockScan(IYquePhaseMockRequest request);

    Map<String, Object> mockAddContact(IYquePhaseMockRequest request);

    Map<String, Object> mockJoinGroup(IYquePhaseMockRequest request);

    Map<String, Object> mockLeaveGroup(IYquePhaseMockRequest request);
}
