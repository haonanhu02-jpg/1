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
 * 客户进群 / 退群关系事件。
 *
 * <p>一期按事件流水存储，查询当前关系时取同一客户和群的最新事件。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_customer_chat_relation")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseCustomerChatRelation extends IYquePhaseBaseEntity {

    @Column(name = "external_userid")
    private String externalUserid;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "group_pool_id")
    private Long groupPoolId;

    @Column(name = "group_pool_name")
    private String groupPoolName;

    @Column(name = "channel_code_id")
    private String channelCodeId;

    @Column(name = "channel_state")
    private String channelState;

    /**
     * JOIN、LEAVE、UPDATE。
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * IN、OUT、UNKNOWN。
     */
    @Column(name = "relation_status")
    private String relationStatus;

    @Column(name = "event_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;
}
