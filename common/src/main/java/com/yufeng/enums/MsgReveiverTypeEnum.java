
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 消息接受者类型
 */
public enum MsgReveiverTypeEnum {

    HR(1, "HR"),
    CANDIDATE(2, "求职者");

    public final Integer type;
    public final String value;

    MsgReveiverTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
