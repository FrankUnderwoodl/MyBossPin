
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 消息类型
 */
public enum MessageEnum {
    FOLLOW_YOU(1, "关注", "follow"),
    LIKE_VLOG(2, "点赞视频", "likeVideo"),
    COMMENT_VLOG(3, "评论视频", "comment"),
    REPLY_YOU(4, "回复评论", "replay"),
    LIKE_COMMENT(5, "点赞评论", "likeComment");

    public final Integer type;
    public final String value;
    public final String enValue;

    MessageEnum(Integer type, String value, String enValue) {
        this.type = type;
        this.value = value;
        this.enValue = enValue;
    }
}
