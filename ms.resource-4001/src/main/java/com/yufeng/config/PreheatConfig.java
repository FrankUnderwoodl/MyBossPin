package com.yufeng.config;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.vo.SysParamsVO;
import com.yufeng.service.SysParamsService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Lzm
 * @CreateTime 2025年9月09日 22:36
 * @describe 这个类的目的是：项目启动完毕的时候，自动就把最大刷新次数这个数据放到Redis缓存中(也就是所谓的：缓存预热)
 *
 * 问：CommandLineRunner怎么理解？
 * 答：Spring Boot提供的一个接口，专门用来在项目启动完成之后，执行一些特定的业务逻辑
 */
@Configuration
public class PreheatConfig extends BaseInfoProperties implements CommandLineRunner {

    @Autowired
    private SysParamsService sysParamsService;

    @Resource(name = "curatorClient") // 根据名称注入
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {

        // 项目启动完毕之后，调用service层去查询MySQL中简历的最大刷新次数
        SysParamsVO sysParams = sysParamsService.getSysParams();

        if (sysParams == null) {
            GraceException.doException(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        this.dealMaxResumeRefreshCounts(sysParams.getMaxResumeRefreshCounts());
    }


    /**
     * 处理简历的最大刷新次数
     *
     * @param counts 简历的最大刷新次数
     */
    private void dealMaxResumeRefreshCounts(Integer counts) {

        // 1.把数据预热，存储到redis中
        redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, counts + "");

        // 2.不仅放在redis中，还放在zookeeper中的配置节点中
        String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;
        String data = counts+"";
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                // 节点不存在，则创建
                zkClient.create()
                        .creatingParentContainersIfNeeded()     // 这里可以递归创建节点路径，比如/imooc/abc/jack
                        .withMode(CreateMode.PERSISTENT)        // 持久类型的节点
                        .forPath(path, data.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
