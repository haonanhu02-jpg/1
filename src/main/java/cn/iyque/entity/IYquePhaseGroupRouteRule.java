package cn.iyque.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/**
 * 分群规则。
 *
 * <p>规则按优先级从高到低匹配，命中后只返回目标群池，由群池内部排序选择实际客户群。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_group_route_rule")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseGroupRouteRule extends IYquePhaseBaseEntity {

    public static final String TYPE_CUSTOMER_TAG = "CUSTOMER_TAG";
    public static final String TYPE_CHANNEL_TAG = "CHANNEL_TAG";
    public static final String TYPE_CHANNEL_DEFAULT = "CHANNEL_DEFAULT";
    public static final String TYPE_GLOBAL_DEFAULT = "GLOBAL_DEFAULT";

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "channel_code_id")
    private String channelCodeId;

    @Column(name = "channel_code_name")
    private String channelCodeName;

    /**
     * 需要同时满足的客户标签 ID，多个 ID 用英文逗号分隔。
     */
    @Column(name = "required_tag_ids")
    private String requiredTagIds;

    @Column(name = "target_pool_id")
    private Long targetPoolId;

    @Transient
    private String targetPoolName;

    /**
     * 优先级，数字越大越先匹配。
     */
    private Integer priority;

    /**
     * 是否兜底规则。普通规则全部不命中时，才使用兜底规则。
     */
    @Column(name = "fallback_flag")
    private Boolean fallbackFlag;

    private Boolean enabled;

    private String remark;

    @PrePersist
    @PreUpdate
    private void fillDefaultValue() {
        if (this.priority == null) {
            this.priority = 0;
        }
        if (this.ruleType == null) {
            this.ruleType = Boolean.TRUE.equals(this.fallbackFlag) ? TYPE_GLOBAL_DEFAULT : TYPE_CHANNEL_DEFAULT;
        }
        if (this.fallbackFlag == null) {
            this.fallbackFlag = TYPE_GLOBAL_DEFAULT.equals(this.ruleType);
        }
        if (this.enabled == null) {
            this.enabled = true;
        }
    }
}
