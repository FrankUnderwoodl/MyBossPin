package com.yufeng.service.impl;

import com.yufeng.api.mq.DelayConfig_MaxCounts;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.DelayTimes;
import com.yufeng.mapper.SysParamsMapper;
import com.yufeng.model.pojo.SysParams;
import com.yufeng.model.vo.SysParamsVO;
import com.yufeng.service.SysParamsService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 系统参数配置表，本表仅有一条记录 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class SysParamsServiceImpl extends BaseInfoProperties implements SysParamsService {

    @Autowired
    private SysParamsMapper sysParamsMapper;

    @Resource(name = "curatorClient") // 根据名称注入
    private CuratorFramework zkClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * Updates the maximum count for resume refreshes along with its version.
     *
     * @param maxCounts the new maximum count for resume refreshes
     * @param version   the version of the record to ensure optimistic locking
     * @return
     */
    @Override
    @Transactional
    public int updateMaxResumeRefreshCounts(Integer maxCounts, Integer version) throws Exception {
        // 如果前端人员传了一个老数据(老版本号)过来的话，就抛错

        // 1.把数据写入到MySQL中
        SysParams sysParams = new SysParams(); // 要想CRUD，就先创建一个实体类对象
        sysParams.setId(1001);
        sysParams.setMaxResumeRefreshCounts(maxCounts);
        // 调用mapper
        sysParamsMapper.updateById(sysParams);
        // 2.把数据携带版本号，写入到zookeeper中(这里可能会有异常，因为可能版本号有错)
        String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;
        Stat stat = zkClient.setData().withVersion(version).forPath(path, (maxCounts + "").getBytes());
        // 3.更新数据到redis缓存中
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");



        // =========== 这里有‘数据一致性’的问题，假如有异常、运行到某一行断电，那么MySQL可以回滚，但是zookeeper和redis就没法回滚了 ===========
        // 所以怎么保证那么多中间件(zookeeper、redis、es)的最终一致性呢？ -> 延迟队列来进行校验(假如不能用canal的话)

        // 发送延迟消息到MQ中，db、zookeeper、redis这三者的数据需要保持一致性
        int delayTime2 = DelayTimes.getDelayTimes(1); // 第一次延迟的时间，这里是3秒

        // 发送消息给延迟队列
        MessagePostProcessor postProcessor = DelayConfig_MaxCounts.setDelayTimes(delayTime2); // 创建一个新的 MessagePostProcessor
        // 正式发送延迟消息到RabbitMQ
        rabbitTemplate.convertAndSend(
                DelayConfig_MaxCounts.EXCHANGE_DELAY_MAX_COUNTS, // 要发送的交换机
                DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH, // 使用正确的路由键
                "123456", // 这里可以是任何内容，实际业务中可以是需要刷新的行业ID等信息,跟消息无关
                postProcessor
        );

        // 记得把版本号返回给前端存着，下次更新的时候还要用到
        return stat.getVersion();
    }


    /**
     * 获取简历刷新的最大次数(通过查数据库的方式)
     */
    @Override
    public SysParamsVO getSysParams() {

        // 调用mapper层查询得到pojo(SYS_PARAMS_PK:1001)
        SysParams sysParams = sysParamsMapper.selectById(SYS_PARAMS_PK);

        // 将pojo转成一个vo
        SysParamsVO sysParamsVO = new SysParamsVO();
        BeanUtils.copyProperties(sysParams, sysParamsVO);
        sysParamsVO.setVersion(0); // 这里先写死，后续会结合zookeeper实现乐观锁



        return sysParamsVO;
    }
}
