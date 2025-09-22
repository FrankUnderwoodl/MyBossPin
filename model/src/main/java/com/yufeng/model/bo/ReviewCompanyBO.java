package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCompanyBO {

    @NotBlank
    private String hrUserId;   // HR用户ID

    private String realname;   // HR真实姓名

    private String hrMobile;   // HR手机号

    @NotBlank
    private String companyId;  // 公司ID
    private String authLetter; // 授权函图片地址

    // 以下在审核的时候使用到
    private Integer reviewStatus; // 审核状态
    private String reviewReplay;  // 审核回复

}
