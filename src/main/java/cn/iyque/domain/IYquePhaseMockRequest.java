package cn.iyque.domain;

import lombok.Data;

@Data
public class IYquePhaseMockRequest {

    private String channelId;

    private String state;

    private String externalUserId;

    private String userId;

    private String customerName;

    private String chatId;

    private String chatName;

    private Long groupId;
}
