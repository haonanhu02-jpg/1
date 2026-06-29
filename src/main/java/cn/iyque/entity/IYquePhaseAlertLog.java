package cn.iyque.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

/**
 * 一期告警日志。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_alert_log")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseAlertLog extends IYquePhaseBaseEntity {

    @Column(name = "alert_type")
    private String alertType;

    @Column(name = "alert_level")
    private String alertLevel;

    @Column(name = "business_key")
    private String businessKey;

    @Column(name = "alert_title")
    private String alertTitle;

    @Column(name = "alert_content", columnDefinition = "text")
    private String alertContent;

    /**
     * PENDING、HANDLED。
     */
    private String status;

    @Column(name = "handled_by")
    private String handledBy;

    @Column(name = "handled_remark")
    private String handledRemark;

    @Column(name = "handled_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handledTime;

    @Column(name = "notify_channels")
    private String notifyChannels;

    @Column(name = "notify_status")
    private String notifyStatus;

    @Column(name = "notify_error", columnDefinition = "text")
    private String notifyError;
}
