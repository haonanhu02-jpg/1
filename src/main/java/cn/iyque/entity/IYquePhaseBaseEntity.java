package cn.iyque.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

/**
 * 一期新增表的公共字段。
 *
 * <p>Iyque 原项目多数业务表都有雪花主键、创建时间、更新时间和 delFlag，
 * 一期二开表统一继承这里，避免每张表重复写同一套默认值逻辑。</p>
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class IYquePhaseBaseEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "snowflakeIdGenerator")
    @GenericGenerator(
            name = "snowflakeIdGenerator",
            strategy = "cn.iyque.utils.SnowFlakeUtils"
    )
    private Long id;

    @Column(name = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 软删除标记：0 正常，1 删除。
     */
    @Column(name = "del_flag")
    private Integer delFlag;

    @PrePersist
    protected void prePersist() {
        Date now = new Date();
        if (this.createTime == null) {
            this.createTime = now;
        }
        this.updateTime = now;
        if (this.delFlag == null) {
            this.delFlag = 0;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updateTime = new Date();
        if (this.delFlag == null) {
            this.delFlag = 0;
        }
    }
}
