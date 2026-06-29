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
import java.util.List;

/**
 * 独立群池。
 *
 * <p>群池与 Iyque 原有客户群列表解耦，一个群池可以绑定多个企业微信群，
 * 分群规则只选择群池，不直接写死某一个客户群。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_group_pool")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseGroupPool extends IYquePhaseBaseEntity {

    @Column(name = "pool_name")
    private String poolName;

    /**
     * 可选绑定渠道活码，用于运营上识别这个群池主要服务哪个渠道。
     */
    @Column(name = "channel_code_id")
    private String channelCodeId;

    @Column(name = "channel_code_name")
    private String channelCodeName;

    private Boolean enabled;

    private String remark;

    /**
     * 前端保存群池时一起提交的客户群绑定关系，不直接落在群池主表。
     */
    @Transient
    private List<IYquePhaseGroupPoolChat> chats;

    @Transient
    private Integer chatCount;

    @Transient
    private Integer availableChatCount;

    @PrePersist
    @PreUpdate
    private void fillDefaultValue() {
        if (this.enabled == null) {
            this.enabled = true;
        }
    }
}
