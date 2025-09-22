
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 用户身份角色，1: 求职者，2: 求职者可以切换为HR进行招聘，同时拥有两个身份
 */
public enum UserRole {
    CANDIDATE(1, "求职者"),
    RECRUITER(2, "求职者、招聘者");

    public final Integer type;
    public final String value;

    UserRole(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
