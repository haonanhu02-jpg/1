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

/**
 * 群池与客户群的绑定关系。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_group_pool_chat")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseGroupPoolChat extends IYquePhaseBaseEntity {

    @Column(name = "group_pool_id")
    private Long groupPoolId;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "chat_name")
    private String chatName;

    /**
     * 群容量阈值。达到阈值后，分群预览会选择下一个可用群。
     */
    @Column(name = "capacity_threshold")
    private Integer capacityThreshold;

    /**
     * 当前成员数。企业微信同步尚未接入实时人数时，运营可手工维护。
     */
    @Column(name = "current_member_count")
    private Integer currentMemberCount;

    /**
     * 是否备用群。主群不可用时才优先考虑备用群。
     */
    @Column(name = "backup_flag")
    private Boolean backupFlag;

    private Integer sort;

    private Boolean enabled;

    @PrePersist
    @PreUpdate
    private void fillDefaultValue() {
        if (this.capacityThreshold == null) {
            this.capacityThreshold = 180;
        }
        if (this.currentMemberCount == null) {
            this.currentMemberCount = 0;
        }
        if (this.backupFlag == null) {
            this.backupFlag = false;
        }
        if (this.sort == null) {
            this.sort = 0;
        }
        if (this.enabled == null) {
            this.enabled = true;
        }
    }
}
