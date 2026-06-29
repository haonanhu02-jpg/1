package cn.iyque.domain;

import cn.iyque.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IYquePhaseStatisticsQuery extends BaseEntity {

    private String channelCodeId;

    private String userId;

    private Long groupPoolId;

    private String chatId;
}
