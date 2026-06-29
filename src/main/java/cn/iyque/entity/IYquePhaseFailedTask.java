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
 * 失败任务重试记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_failed_task")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseFailedTask extends IYquePhaseBaseEntity {

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "business_key")
    private String businessKey;

    @Column(name = "request_body", columnDefinition = "text")
    private String requestBody;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;

    /**
     * PENDING、FAILED、EXHAUSTED、SUCCESS。
     */
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retry_count")
    private Integer maxRetryCount;

    @Column(name = "next_retry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextRetryTime;

    @Column(name = "last_retry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastRetryTime;
}
