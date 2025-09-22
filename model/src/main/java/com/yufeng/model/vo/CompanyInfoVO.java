package com.yufeng.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * <p>
 * 企业信息视图对象(主要用于给admin端展示公司信息)
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompanyInfoVO {

    private String companyId;                                     // 公司ID
    private String companyName;                                   // 公司全称
    private String shortName;                                     // 公司简称
    private String logo;                                          // 公司logo

    private String province;                                      // 公司所在省份
    private String city;                                          // 公司所在城市
    private String district;                                      // 公司所在区县
    private String address;                                       // 公司详细地址


    private String peopleSize;                                    // 公司规模
    private String nature;                                        // 公司性质
    private String industry;                                      // 公司行业
    private String financStage;                                   // 公司融资阶段

    private String workTime;                                      // 工作时间
    private String introduction;                                  // 公司介绍

    private String advantage;                                     // 公司优势
    private String benefits;                                      // 公司福利
    private String bonus;                                         // 公司奖金
    private String subsidy;                                       // 公司补贴

    private Integer reviewStatus;                                 // 审核状态
    private String reviewReplay;                                  // 审核回复

    private LocalDate commitDate;                                 // 提交审核时间
    private String commitUserId;                                  // 提交审核的用户ID
    private String commitUser;                                    // 提交审核的用户名称
    private String commitMobile;                                  // 提交审核的用户手机号
    private String authLetter;                                    // 企业认证函

    private String bizLicense;                                    // 营业执照

    private LocalDate buildDate;                                  // 公司成立时间
    private String registCapital;                                 // 注册资本
    private String registPlace;                                   // 注册地点
    private String legalRepresentative;                           // 法人代表
}
