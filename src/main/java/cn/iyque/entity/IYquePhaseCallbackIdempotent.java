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
 * 企业微信回调幂等记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_callback_idempotent")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseCallbackIdempotent extends IYquePhaseBaseEntity {

    /**
     * 回调唯一键，优先由企业微信事件字段组合生成。
     */
    @Column(name = "event_key", unique = true, length = 128)
    private String eventKey;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "change_type")
    private String changeType;

    /**
     * PROCESSING、SUCCESS、FAILED。
     */
    @Column(name = "process_status")
    private String processStatus;

    @Column(name = "raw_digest")
    private String rawDigest;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;
}
