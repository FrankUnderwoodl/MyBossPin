
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 职位状态
 */
public enum JobStatus {
    OPEN(1, "招聘中"),
    CLOSE(2, "已关闭"),
    DELETE(3, "违规删除");

    public final Integer type;
    public final String value;

    JobStatus(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
