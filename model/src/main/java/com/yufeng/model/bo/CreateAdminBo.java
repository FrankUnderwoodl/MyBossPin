package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 14:43
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminBo {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String remark; // 备注信息，可选字段
}
