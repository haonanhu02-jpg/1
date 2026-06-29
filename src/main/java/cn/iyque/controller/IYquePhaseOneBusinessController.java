package cn.iyque.controller;

import cn.iyque.domain.ResponseResult;
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
import cn.iyque.service.IYquePhaseOneService;
import cn.iyque.utils.TableSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一期 01、02 缺失能力的业务接口。
 *
 * <p>该 Controller 只承接新增的一期模块，Iyque 原有活码、素材、标签、客户、
 * 客群等接口保持原路径不变，避免硬改可复用模块。</p>
 */
@RestController
@RequestMapping("/phaseOne")
public class IYquePhaseOneBusinessController {

    private final IYquePhaseOneService phaseOneService;

    public IYquePhaseOneBusinessController(IYquePhaseOneService phaseOneService) {
        this.phaseOneService = phaseOneService;
    }

    @GetMapping("/employeePool/findPage")
    public ResponseResult findEmployeePoolPage(IYquePhaseEmployeePool query) {
        Page<IYquePhaseEmployeePool> page = phaseOneService.findEmployeePoolPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @PostMapping("/employeePool/save")
    public ResponseResult saveEmployeePool(@RequestBody IYquePhaseEmployeePool employeePool) {
        return new ResponseResult<>(phaseOneService.saveEmployeePool(employeePool));
    }

    @PutMapping("/employeePool/update")
    public ResponseResult updateEmployeePool(@RequestBody IYquePhaseEmployeePool employeePool) {
        return new ResponseResult<>(phaseOneService.saveEmployeePool(employeePool));
    }

    @DeleteMapping("/employeePool/{ids}")
    public ResponseResult deleteEmployeePool(@PathVariable String ids) {
        phaseOneService.deleteEmployeePools(ids);
        return new ResponseResult<>();
    }

    @GetMapping("/groupPool/findPage")
    public ResponseResult findGroupPoolPage(IYquePhaseGroupPool query) {
        Page<IYquePhaseGroupPool> page = phaseOneService.findGroupPoolPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @PostMapping("/groupPool/save")
    public ResponseResult saveGroupPool(@RequestBody IYquePhaseGroupPool groupPool) {
        return new ResponseResult<>(phaseOneService.saveGroupPool(groupPool));
    }

    @PutMapping("/groupPool/update")
    public ResponseResult updateGroupPool(@RequestBody IYquePhaseGroupPool groupPool) {
        return new ResponseResult<>(phaseOneService.saveGroupPool(groupPool));
    }

    @DeleteMapping("/groupPool/{ids}")
    public ResponseResult deleteGroupPool(@PathVariable String ids) {
        phaseOneService.deleteGroupPools(ids);
        return new ResponseResult<>();
    }

    @PostMapping("/groupPool/bindChat")
    public ResponseResult bindGroupPoolChat(@RequestBody IYquePhaseGroupPool groupPool) {
        return new ResponseResult<>(phaseOneService.saveGroupPool(groupPool));
    }

    @GetMapping("/groupRouteRule/findPage")
    public ResponseResult findGroupRouteRulePage(IYquePhaseGroupRouteRule query) {
        Page<IYquePhaseGroupRouteRule> page = phaseOneService.findGroupRouteRulePage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @PostMapping("/groupRouteRule/save")
    public ResponseResult saveGroupRouteRule(@RequestBody IYquePhaseGroupRouteRule groupRouteRule) {
        return new ResponseResult<>(phaseOneService.saveGroupRouteRule(groupRouteRule));
    }

    @PutMapping("/groupRouteRule/update")
    public ResponseResult updateGroupRouteRule(@RequestBody IYquePhaseGroupRouteRule groupRouteRule) {
        return new ResponseResult<>(phaseOneService.saveGroupRouteRule(groupRouteRule));
    }

    @DeleteMapping("/groupRouteRule/{ids}")
    public ResponseResult deleteGroupRouteRule(@PathVariable String ids) {
        phaseOneService.deleteGroupRouteRules(ids);
        return new ResponseResult<>();
    }

    @PostMapping("/groupRouteRule/matchPreview")
    public ResponseResult matchGroupRoute(@RequestBody IYquePhaseGroupRouteRule query) {
        return new ResponseResult<>(phaseOneService.matchGroupRoute(query));
    }

    @GetMapping("/customerChatRelation/findPage")
    public ResponseResult findCustomerChatRelationPage(IYquePhaseCustomerChatRelation query) {
        Page<IYquePhaseCustomerChatRelation> page = phaseOneService.findCustomerChatRelationPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @GetMapping("/customerRouteLog/findPage")
    public ResponseResult findCustomerRouteLogPage(IYquePhaseCustomerRouteLog query) {
        Page<IYquePhaseCustomerRouteLog> page = phaseOneService.findCustomerRouteLogPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @GetMapping("/alertLog/findPage")
    public ResponseResult findAlertLogPage(IYquePhaseAlertLog query) {
        Page<IYquePhaseAlertLog> page = phaseOneService.findAlertLogPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @PutMapping("/alertLog/handle")
    public ResponseResult handleAlert(@RequestBody IYquePhaseAlertLog alertLog) {
        return new ResponseResult<>(phaseOneService.handleAlert(alertLog.getId(), alertLog));
    }

    @GetMapping("/failedTask/findPage")
    public ResponseResult findFailedTaskPage(IYquePhaseFailedTask query) {
        Page<IYquePhaseFailedTask> page = phaseOneService.findFailedTaskPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @PostMapping("/failedTask/retry/{id}")
    public ResponseResult retryFailedTask(@PathVariable Long id) {
        return new ResponseResult<>(phaseOneService.retryFailedTask(id));
    }

    @GetMapping("/apiCallLog/findPage")
    public ResponseResult findApiCallLogPage(IYquePhaseApiCallLog query) {
        Page<IYquePhaseApiCallLog> page = phaseOneService.findApiCallLogPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @GetMapping("/businessLog/findPage")
    public ResponseResult findBusinessLogPage(IYquePhaseBusinessLog query) {
        Page<IYquePhaseBusinessLog> page = phaseOneService.findBusinessLogPage(query, defaultPageRequest());
        return new ResponseResult(page.getContent(), page.getTotalElements());
    }

    @GetMapping("/statistics/summary")
    public ResponseResult statisticsSummary() {
        return new ResponseResult<>(phaseOneService.summary());
    }

    @GetMapping("/statistics/report")
    public ResponseResult statisticsReport(IYquePhaseStatisticsQuery query) {
        return new ResponseResult<>(phaseOneService.statisticsReport(query));
    }

    @GetMapping("/acceptance/summary")
    public ResponseResult acceptanceSummary() {
        return new ResponseResult<>(phaseOneService.acceptanceSummary());
    }

    @GetMapping("/statistics/exportCsv")
    public void exportStatisticsCsv(IYquePhaseStatisticsQuery query, HttpServletResponse response) throws IOException {
        writeCsv(response, "phase_one_statistics", phaseOneService.exportStatisticsRows(query));
    }

    @GetMapping("/customerChatRelation/exportCsv")
    public void exportCustomerChatRelationCsv(IYquePhaseCustomerChatRelation query, HttpServletResponse response) throws IOException {
        Page<IYquePhaseCustomerChatRelation> page = phaseOneService.findCustomerChatRelationPage(query, PageRequest.of(0, 10000));
        writeCsv(response, "phase_one_customer_chat_relation", page.getContent().stream().map(item -> csvRow(
                "externalUserid", safe(item.getExternalUserid()),
                "userId", safe(item.getUserId()),
                "chatId", safe(item.getChatId()),
                "chatName", safe(item.getChatName()),
                "groupPoolName", safe(item.getGroupPoolName()),
                "channelCodeId", safe(item.getChannelCodeId()),
                "eventType", safe(item.getEventType()),
                "relationStatus", safe(item.getRelationStatus()),
                "eventTime", item.getEventTime() == null ? "" : item.getEventTime()
        )).toList());
    }

    @GetMapping("/alertLog/exportCsv")
    public void exportAlertLogCsv(IYquePhaseAlertLog query, HttpServletResponse response) throws IOException {
        Page<IYquePhaseAlertLog> page = phaseOneService.findAlertLogPage(query, PageRequest.of(0, 10000));
        writeCsv(response, "phase_one_alert_log", page.getContent().stream().map(item -> csvRow(
                "alertType", safe(item.getAlertType()),
                "alertLevel", safe(item.getAlertLevel()),
                "businessKey", safe(item.getBusinessKey()),
                "alertTitle", safe(item.getAlertTitle()),
                "status", safe(item.getStatus()),
                "notifyStatus", safe(item.getNotifyStatus()),
                "handledBy", safe(item.getHandledBy()),
                "handledRemark", safe(item.getHandledRemark()),
                "createTime", item.getCreateTime() == null ? "" : item.getCreateTime()
        )).toList());
    }

    @GetMapping("/businessLog/exportCsv")
    public void exportBusinessLogCsv(IYquePhaseBusinessLog query, HttpServletResponse response) throws IOException {
        Page<IYquePhaseBusinessLog> page = phaseOneService.findBusinessLogPage(query, PageRequest.of(0, 10000));
        writeCsv(response, "phase_one_business_log", page.getContent().stream().map(item -> csvRow(
                "logType", safe(item.getLogType()),
                "businessId", safe(item.getBusinessId()),
                "externalUserid", safe(item.getExternalUserid()),
                "userId", safe(item.getUserId()),
                "chatId", safe(item.getChatId()),
                "channelCodeId", safe(item.getChannelCodeId()),
                "success", item.getSuccess() == null ? "" : item.getSuccess(),
                "errorMsg", safe(item.getErrorMsg()),
                "createTime", item.getCreateTime() == null ? "" : item.getCreateTime()
        )).toList());
    }

    private PageRequest defaultPageRequest() {
        return PageRequest.of(TableSupport.buildPageRequest().getPageNum(), TableSupport.buildPageRequest().getPageSize());
    }

    private Map<String, Object> csvRow(Object... keyValues) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            row.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return row;
    }

    private void writeCsv(HttpServletResponse response, String fileName, List<Map<String, Object>> rows) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedName + ".csv");
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        Set<String> headers = new LinkedHashSet<>();
        rows.forEach(row -> headers.addAll(row.keySet()));
        builder.append(String.join(",", headers)).append("\n");
        for (Map<String, Object> row : rows) {
            boolean first = true;
            for (String header : headers) {
                if (!first) {
                    builder.append(",");
                }
                builder.append(csvValue(row.get(header)));
                first = false;
            }
            builder.append("\n");
        }
        response.getWriter().write(builder.toString());
    }

    private String csvValue(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
