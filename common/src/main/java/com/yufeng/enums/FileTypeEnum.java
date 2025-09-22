
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 文件类型 枚举
 */
public enum FileTypeEnum {
    BGIMG(1, "用户背景图"),
    FACE(2, "用户头像");

    public final Integer type;
    public final String value;

    FileTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
