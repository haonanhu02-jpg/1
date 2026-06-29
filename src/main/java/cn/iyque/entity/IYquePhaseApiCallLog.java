package cn.iyque.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 企业微信 API 调用日志。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_api_call_log")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseApiCallLog extends IYquePhaseBaseEntity {

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "request_summary", columnDefinition = "text")
    private String requestSummary;

    @Column(name = "response_summary", columnDefinition = "text")
    private String responseSummary;

    private Boolean success;

    @Column(name = "cost_ms")
    private Long costMs;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;
}
