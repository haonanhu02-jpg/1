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
 * 新增客户分群引导记录。
 *
 * <p>该表记录一期真实回调链路的分群匹配结果，不负责真正发送欢迎语；
 * 欢迎语仍沿用 Iyque 原有客户新增回调链路。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_customer_route_log")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseCustomerRouteLog extends IYquePhaseBaseEntity {

    @Column(name = "external_userid")
    private String externalUserid;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "channel_code_id")
    private String channelCodeId;

    @Column(name = "channel_code_name")
    private String channelCodeName;

    @Column(name = "channel_state")
    private String channelState;

    @Column(name = "tag_ids")
    private String tagIds;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "matched_rule_id")
    private Long matchedRuleId;

    @Column(name = "matched_rule_name")
    private String matchedRuleName;

    @Column(name = "match_path")
    private String matchPath;

    @Column(name = "target_pool_id")
    private Long targetPoolId;

    @Column(name = "target_pool_name")
    private String targetPoolName;

    @Column(name = "target_chat_id")
    private String targetChatId;

    @Column(name = "target_chat_name")
    private String targetChatName;

    /**
     * MATCHED、NO_ROUTE_RULE、TARGET_POOL_DISABLED、NO_AVAILABLE_CHAT。
     */
    @Column(name = "route_status")
    private String routeStatus;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "event_time")
    private Date eventTime;
}
