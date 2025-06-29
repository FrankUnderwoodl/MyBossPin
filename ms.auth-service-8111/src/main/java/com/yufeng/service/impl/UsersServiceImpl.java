package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yufeng.enums.Sex;
import com.yufeng.enums.ShowWhichName;
import com.yufeng.enums.UserRole;
import com.yufeng.pojo.Users;
import com.yufeng.mapper.UsersMapper;
import com.yufeng.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufeng.utils.DesensitizationUtil;
import com.yufeng.utils.LocalDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    // 默认头像链接
    private static final String USER_FACE = "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg";

    // 注入mapper
    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Users queryUserIsExist(String mobile) {
        // 通过mobile字段查询用户是否存在
        Users user = usersMapper.selectOne(new QueryWrapper<Users>()
                .eq("mobile", mobile));
        return user;
    }

    @Override
    public Users createUser(String mobile) {
        // 创建一个全新的用户pojo对象
        Users users = new Users();
        // 设置手机号
        users.setMobile(mobile);

        // 给用户设置默认的nickname(结合脱敏工具)
        users.setNickname("用户" + DesensitizationUtil.commonDisplay(mobile));
        // 设置用户的realName
        users.setRealName("用户" + DesensitizationUtil.commonDisplay(mobile));
        // 设置用户的showWhichName(不要写死，因为可能会有其他的展示方式，所以进行枚举化处理)
        users.setShowWhichName(ShowWhichName.nickname.type); // 默认显示昵称

        // 设置用户的性别(默认保密)
        users.setSex(Sex.secret.type); // 2:保密
        // 设置用户的头像(默认头像)
        users.setFace(USER_FACE); // 默认头像链接
        // 设置用户的邮箱(默认空)
        users.setEmail("");

        // 设置用户的生日(默认1980)
        /*LocalDate 是 Java 8 引入的日期/时间 API 的一部分，比旧的 Date 类更加清晰和易用*/
        LocalDate localDate = LocalDateUtils.parseLocalDate("1980-01-01", LocalDateUtils.DATE_PATTERN);
        users.setBirthday(localDate);

        // 设置省市区(默认为空)
        users.setCountry("中国"); // 默认国家为中国
        users.setProvince("");
        users.setCity("");
        users.setDistrict("");

        // 设置用户的默认签名
        users.setDescription("这家伙很懒，什么都没留下~");
        // 设置用户的工作开始日期(默认使用注册当天的日期)
        users.setStartWorkDate(LocalDate.now()); // 使用当前日期作为工作开始日期
        // 设置用户的职位(默认底层码农)
        users.setPosition("底层码农");
        // 设置用户的角色(默认候选人)
        users.setRole(UserRole.CANDIDATE.type); // 1:候选人
        // 设置HR所在的公司ID(默认空)
        users.setHrInWhichCompanyId("");

        // 设置创建时间和更新时间(默认使用当前时间)
        users.setCreatedTime(LocalDateTime.now()); // 使用当前日期作为创建时间
        users.setUpdatedTime(LocalDateTime.now()); // 使用当前日期作为更新时间

        // 插入用户到MySQL中
        usersMapper.insert(users);
        return users;
    }


    public static void main(String[] args) {
        // 测试LocalDate.now()
        System.out.println("现在的时间为(只含日期):" + LocalDate.now());
    }
}
