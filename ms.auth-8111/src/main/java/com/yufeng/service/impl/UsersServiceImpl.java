package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufeng.api.feign.WorkMSFeign;
import com.yufeng.enums.Sex;
import com.yufeng.enums.ShowWhichName;
import com.yufeng.enums.UserRole;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.UsersMapper;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.UsersService;
import com.yufeng.utils.DesensitizationUtil;
import com.yufeng.utils.LocalDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.apache.seata.tm.api.GlobalTransactionContext;
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
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    // 默认头像链接
    private static final String USER_FACE = "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg";

    // 注入mapper
    @Autowired
    private UsersMapper usersMapper;


    /* 注入远程调用的工作模块Feign接口
    · 这个Feign接口用于调用工作模块的服务，比如初始化用户的简历信息
    · 注意：Feign是Spring Cloud提供的一种声明式的HTTP客户端，可以简化服务间的调用
    · 通过Feign接口，我们可以像调用本地方法一样调用
    · 远程服务，而不需要手动编写HTTP请求代码
    · 这里的MSWorkFeign是一个Feign客户端接口，它会自动被Spring Boot扫描并注册到容器中，
    · 这样我们就可以通过@Autowired注解将它注入到当前类中，
    · 然后就可以直接调用MSWorkFeign中定义的方法来进行远程调用了 */
    @Autowired
    private WorkMSFeign workMSFeign;



    @Override
    public Users queryUserIsExist(String mobile) {
        // 通过mobile字段查询用户是否存在
        Users user = usersMapper.selectOne(new QueryWrapper<Users>()
                .eq("mobile", mobile));
        return user;
    }



    @Override
    // @Transactional
    @GlobalTransactional
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


        // 发起远程调用，调用工作微服务里的initResume API
        GraceJSONResult graceJSONResult = workMSFeign.init(users.getId());
        if (graceJSONResult.getStatus() != 200) {
            // 如果远程调用失败，抛出异常
            String xid = RootContext.getXID();// 获取当前线程的全局事务ID
            if (xid != null) {
                // 如果当前线程已经开启了Seata分布式事务，则手动回滚事务
                try {
                    GlobalTransactionContext.reload(xid).rollback();
                    log.error("Seata 分布式事务回滚成功，XID: {}", xid);
                } catch (TransactionException e) {
                    throw new RuntimeException(e);
                } finally {
                    GraceException.doException(ResponseStatusEnum.USER_REGISTER_ERROR);
                }
            }

        }

        // int i = 1 / 0; // 即使算术异常已经被ExceptionHandler给捕获了，但seata依然可以进行分布式事务的回滚，因为

        return users;
    }


    public static void main(String[] args) {
        // 测试LocalDate.now()
        System.out.println("现在的时间为(只含日期):" + LocalDate.now());
    }
}
