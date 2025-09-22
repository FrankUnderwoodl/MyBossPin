package com.yufeng.model.bo;

import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.pojo.ar.AdminAR;
import com.yufeng.utils.MD5Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 14:43
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResetPwdBo {

    @NotBlank(message = "管理员ID不能为空")
    private String adminId;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String rePassword;


    /**
     * 校验管理员ID是否存在
     * 如果不存在，则抛出异常
     */
    private void checkAdminID() {
        AdminAR adminAR = new AdminAR();
        adminAR.setId(this.adminId);
        AdminAR existingAdmin = adminAR.selectById();
        if (existingAdmin == null) {
            GraceException.doException(ResponseStatusEnum.ADMIN_NOT_EXIST);
        }
    }


    /**
     * 重置密码
     * 这里的逻辑是：如果管理员存在，则重置密码
     * 如果不存在，则抛出异常
     */
    private void resetPwd() {
        // 这里可以添加具体的密码重置逻辑，比如更新数据库中的密码
        AdminAR adminAR = new AdminAR();
        adminAR.setId(this.adminId);

        // 生成随机数字的盐值
        String salt = MD5Utils.getRandomSalt();
        // 对密码进行
        String encryptedPassword = MD5Utils.encrypt(this.rePassword, salt);
        // 设置加密后的密码和盐值
        adminAR.setPassword(encryptedPassword);
        adminAR.setSlat(salt);

        // 设置更新时间
        adminAR.setUpdatedTime(LocalDateTime.now());

        // 这里的updateById方法是MyBatis-Plus的ActiveRecord风格的更新方法，相当于就是相当于执行SQL：UPDATE admin SET password=?, slat=?, updated_time=? WHERE id=?
        adminAR.updateById();
    }


    /**
     * 校验密码是否一致
     * 如果不一致，则抛出异常
     */
    private void  checkIsSame() {
        if (!this.password.equalsIgnoreCase(this.rePassword)) {
            GraceException.doException(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
        }
    }

    /**
     * 提供在Controller中调用的校验方法(不用经过service层)
     */
    public void modifyPwd() {
        // ①校验密码是否一致
        checkIsSame();

        // ②使用AdminARMapper查询管理员是否存在
        checkAdminID();

        // ③如果存在，则修改密码
        resetPwd();
    }

}
