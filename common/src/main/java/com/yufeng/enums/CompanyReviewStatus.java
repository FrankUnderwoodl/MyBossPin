
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.enums;

/**
 * @Desc: 企业 审核状态 枚举
 */
public enum CompanyReviewStatus {
    /**
     * 审核状态
     0：未发起审核认证(未进入审核流程)
     1：审核认证通过
     2：审核认证失败
     3：审核中（等待审核）
     */

    NOTHING(0, "未发起审核认证"),
    SUCCESSFUL(1, "审核认证通过"),
    FAILED(2, "审核认证失败"),
    REVIEW_ING(3, "审核中");

    public final Integer type;
    public final String value;

    CompanyReviewStatus(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
