package cn.iyque.domain;

import lombok.Data;

@Data
public class IYqueCallBackBaseMsg {

    //企业微信CorpID
    private String ToUserName;

    //此事件该值固定为sys
    private String FromUserName;

    //消息创建时间 （整型）
    private Long CreateTime;

    //消息的类型，此时固定为event
    private String MsgType;

    //事件的类型
    private String Event;

    //变更类型
    private String ChangeType;

    //token
    private String Token;

    //应用ID
    private String AgentID;

    //企业服务人员的UserID
    private String UserID;

    //外部联系人的userid，注意不是企业成员的帐号
    private String ExternalUserID;

    //添加此用户的「联系我」方式配置的state参数，可用于识别添加此用户的渠道
    private String State;

    // 客户群 ID。企业微信客户群变更回调会带该字段。
    private String ChatId;

    // 客户群名称。部分回调不返回该字段，落库时会再尝试从群池绑定关系补齐。
    private String ChatName;

    // 客户群变更明细，如 add_member、del_member 等。
    private String UpdateDetail;

    // 客户进群场景。
    private String JoinScene;

    // 客户退群场景。
    private String QuitScene;


    //删除客户的操作来源，DELETE_BY_TRANSFER表示此客户是因在职继承自动被转接成员删除
    private String Source;

    //接替失败的原因, customer_refused-客户拒绝， customer_limit_exceed-接替成员的客户数达到上限
    private String FailReason;



    //有新消息的客服账号。可通过sync_msg接口指定open_kfid获取此客服账号的消息
    private String OpenKfId;
}
