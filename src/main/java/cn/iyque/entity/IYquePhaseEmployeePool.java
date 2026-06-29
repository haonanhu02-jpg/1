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
 * 员工池配置。
 *
 * <p>一期先用于承载员工承接排序、启停和备注，后续可以继续扩展均衡分配策略。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "iyque_phase_employee_pool")
@Where(clause = "del_flag = 0")
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseEmployeePool extends IYquePhaseBaseEntity {

    /**
     * 企业微信成员 UserID。
     */
    @Column(name = "employee_id")
    private String employeeId;

    /**
     * 企业微信成员名称。
     */
    @Column(name = "employee_name")
    private String employeeName;

    /**
     * 承接排序，数字越小越靠前。
     */
    private Integer sort;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 运营备注。
     */
    private String remark;

    @PrePersist
    @PreUpdate
    private void fillDefaultValue() {
        if (this.enabled == null) {
            this.enabled = true;
        }
        if (this.sort == null) {
            this.sort = 0;
        }
    }
}
