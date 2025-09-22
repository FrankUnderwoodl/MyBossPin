package com.yufeng.api.bean_config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 * @CreateTime 2025年7月15日 20:33
 * @describe 日志切面，用来记录每一个微服务里面的service所执行的时间
 */
@Slf4j
@Component
@Aspect
public class SeataTXAspect {


    /**
     * 调用service之前，手动开启 Seata 分布式事务
     * 这里使用@Before注解表示这是一个前置通知，它会在方法执行
     */
    // @SuppressWarnings("AopLanguageInspection")
    // @Before("execution(* com.yufeng.service.impl..*.*(..))")
    // public void beginTransaction(JoinPoint joinPoint) throws TransactionException {
    //
    //     log.info("Seata 分布式事务手动开始，方法为: {}", joinPoint.getSignature().toShortString());
    //
    //     // 手动开启 Seata 分布式事务
    //     GlobalTransaction gt = GlobalTransactionContext.getCurrentOrCreate();
    //     gt.begin();
    //
    // }



    /**
     * 实现 Seata 分布式事务的手动回滚
     * 这里使用@AfterThrowing注解表示这是一个异常通知，它会在方法抛出异常时执行这个方法会在任何一个service方法抛出异常时被调用，
     * 主要是为了在发生异常时，手动回滚 Seata 分布式事务，以确保数据的一致性和完整性。
     */
    // @SuppressWarnings("AopLanguageInspection")
    // @AfterThrowing(
    //         throwing = "throwable",
    //         pointcut = "execution(* com.yufeng.service.impl..*.*(..))")
    // public void seataRollback(Throwable throwable) throws Throwable {
    //
    //     log.error("Seata 分布式事务开始回滚，异常信息为: {}", throwable.getMessage());
    //
    //     // 从当前线程获取全局事务ID
    //     String xid = RootContext.getXID();
    //     if (StringUtils.isNotBlank(xid)) {
    //
    //         // 如果全局事务ID不为空，说明当前线程已经开启了 Seata 分布式事务
    //         GlobalTransaction gt = GlobalTransactionContext.getCurrentOrCreate();
    //         // 回滚事务
    //         gt.rollback();
    //         log.info("Seata 分布式事务回滚成功，XID: {}", xid);
    //     } else {
    //         log.warn("当前线程没有开启 Seata 分布式事务，无法回滚");
    //     }
    // }

}
