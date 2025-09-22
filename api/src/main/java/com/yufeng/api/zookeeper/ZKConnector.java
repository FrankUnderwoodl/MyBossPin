package com.yufeng.api.zookeeper;

import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.ZooKeeper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author Lzm
 * @CreateTime 2025年9月16日 18:53
 */
// @Component
public class ZKConnector {

    private static final String host = "localhost:2181"; // Zookeeper服务器的地址和端口
    private final Integer timeout = 60 * 1000 * 30; // 30分钟的超时时间

    private ZooKeeper zooKeeper;

    /**
     * 正式初始化Bean的时候，会进行这么一个前置操作，类似于Tomcat的init方法
     * Bean 创建流程：
     * 实例化 Bean（调用构造函数）
     * 依赖注入（设置属性）
     * 执行 @PostConstruct 方法
     * Bean 放入容器（如 ConcurrentHashMap）
     */
    @PostConstruct
    public void init() {
        // 先他妈的连接到Zookeeper服务器！
        try {
            this.zooKeeper = new ZooKeeper(host, timeout, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 销毁Bean之前，进行这么一个后置操作，类似于Tomcat的destroy方法
     */
    @PreDestroy
    public void close() {
        if (this.zooKeeper != null) {
            try {
                this.zooKeeper.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 获得锁
     * 当业务层调用这个方法的时候，就获得锁
     */
    public ZKLock getZKLock(String lockName) {
        // 先他妈的进行一个判空，防止业务层传递过来的锁名称为空
        if (StringUtils.isBlank(lockName)) {
            GraceException.doException(ResponseStatusEnum.SYSTEM_ERROR_NOT_BLANK);
        }


        // 把ZooKeeper客户端和锁的名称传递给ZKLock
        return new ZKLock(this.zooKeeper, lockName);
    }
}
