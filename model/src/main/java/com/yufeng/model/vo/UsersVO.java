package com.yufeng.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 * 用于封装用户相关信息，包括基本信息、工作信息、HR信息等
 *
 * @author yufeng
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersVO {

    /** 用户ID */
    private String id;

    /** 手机号码 */
    private String mobile;

    /** 用户昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 显示哪个名字标识 */
    private Integer showWhichName;

    /** 性别 */
    private Integer sex;

    /** 头像地址 */
    private String face;

    /** 邮箱地址 */
    private String email;

    /** 生日 */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8") // 使用LocalDate序列化和反序列化
    private LocalDate birthday;

    /** 国家 */
    private String country;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区域 */
    private String district;

    /** 个人描述 */
    private String description;

    /** 开始工作日期 */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startWorkDate;

    /** 职位 */
    private String position;

    /** 角色 */
    private Integer role;

    /** HR所在公司ID */
    private String hrInWhichCompanyId;

    /** HR签名 */
    private String hrSignature;

    /** HR标签 */
    private String hrTags;

    /** 创建时间 */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;

    /** 用户token，传递给前端(跟Users的唯一区别) */
    private String userToken;       // 用户token，传递给前端(跟Users的唯一区别)
}
