package com.yufeng.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <p>
 * 企业信息简要视图对象(主要供给App端用于简短展示信息)
 * </p>
 * {
 *   "id": "COMP001",
 *   "companyName": "北京科技有限公司",
 *   "shortName": "北京科技",
 *   "logo": "https://example.com/logo/company-logo.png",
 *
 *   "peopleSize": "100-500人",
 *   "industry": "互联网/软件开发",
 *   "nature": "民营企业",
 *   "address": "北京市朝阳区中关村科技园",
 *
 *   "reviewStatus": 1,
 *   "reviewReplay": "审核通过，企业信息完整",
 *   "hrCounts": 3
 * }
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CompanySimpleVO {

    // 企业ID
    private String id;
    // 企业全称
    private String companyName;
    // 企业简称
    private String shortName;
    // 企业logo
    private String logo;

    // 人员规模/企业规模
    private String peopleSize;
    // 企业所在行业
    private String industry;
    // 公司性质
    private String nature;
    // 企业所在地
    private String address;

    /**
     * 审核状态
     0：未发起审核认证(未进入审核流程)
     1：审核认证通过
     2：审核认证失败
     3：审核中（等待审核）
     */
    private Integer reviewStatus;  // 审核状态
    private String reviewReplay;   // 审核回复

    // 企业下所绑定的HR数量
    private Long hrCounts = Long.valueOf("0");
}
