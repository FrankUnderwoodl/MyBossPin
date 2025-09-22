package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyBO {

    // 企业ID
    private String companyId;

    // 企业全称
    private String companyName;
    // 企业简称
    private String shortName;
    // 公司logo的URL
    private String logo;
    // 公司性质
    private String bizLicense;

    // 人员规模/企业规模
    private String peopleSize;
    // 企业所在行业
    private String industry;

    /**
     * 审核状态
     0：未发起审核认证(未进入审核流程)
     1：审核认证通过
     2：审核认证失败
     3：审核中（等待审核）
     */
    private Integer reviewStatus;

}
