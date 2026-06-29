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
 * 一期统一业务日志。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_business_log")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseBusinessLog extends IYquePhaseBaseEntity {

    @Column(name = "log_type")
    private String logType;

    @Column(name = "business_id")
    private String businessId;

    @Column(name = "external_userid")
    private String externalUserid;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "channel_code_id")
    private String channelCodeId;

    @Column(name = "request_summary", columnDefinition = "text")
    private String requestSummary;

    @Column(name = "response_summary", columnDefinition = "text")
    private String responseSummary;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;

    private Boolean success;

    @Column(name = "alert_id")
    private Long alertId;
}
