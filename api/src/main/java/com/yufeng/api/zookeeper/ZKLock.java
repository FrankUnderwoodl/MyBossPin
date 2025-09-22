package com.yufeng.api.zookeeper;

import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Lzm
 * @CreateTime 2025年9月16日 20:12
 */
@Slf4j
public class ZKLock {

    // zookeeper的客户端
    private ZooKeeper zooKeeper;

    // 分布式锁的命名空间
    private final String lockSpace = "/locks";

    // 创建锁的名称，用户可以自定义
    private final String lockName;

    // 创建了一个分布式锁
    String selfLock;

    // JVM本地锁(放心，底层也是用的AQS)
    CountDownLatch countDownLatch = new CountDownLatch(1);


    public ZKLock(ZooKeeper zooKeeper, String lockName) {
        this.zooKeeper = zooKeeper;
        this.lockName = lockName;

        /**
         * 尝试创建一下命名空间 /locks
         * 所有的锁都在这个命名空间下
         */
        try {
            // 检查会话状态
            if (!this.zooKeeper.getState().isAlive()) {
                log.error("ZooKeeper会话已失效，状态：{}", this.zooKeeper.getState());
                throw new RuntimeException("ZooKeeper会话已失效");
            }

            if (this.zooKeeper.exists(lockSpace, false) == null) { // 如果 /locks 不存在，则创建
                this.zooKeeper.create(lockSpace, "this is zk lock's namespace!".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // 因为是命名空间，所以用永久节点

                log.info("创建命名空间 {} 成功", lockSpace);
            }
        } catch (KeeperException e) {
            if (e.code() == KeeperException.Code.SESSIONEXPIRED) {
                log.error("ZooKeeper会话过期，请重新连接");
                throw new RuntimeException("ZooKeeper会话过期", e);
            } else {
                log.error("创建命名空间 {} 失败，失败原因：{}", lockSpace, e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("创建命名空间 {} 失败，失败原因：{}", lockSpace, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 获得一个节点锁
     */
    public void get() {

        try {
            this.selfLock = zooKeeper.create(lockSpace + "/" + lockName, null,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            log.info("创建一个节点锁 {} 成功，开始进入判断逻辑", selfLock);

            //* 判断当前的节点，是不是最小的节点
            //* 如果不是最小的节点，则需要等待上一个节点释放锁
            List<String> subLocks = zooKeeper.getChildren(lockSpace, false); // 获得所有的分布式锁节点
            if (CollectionUtils.isEmpty(subLocks)) { // 此处不可能为空，因为上面才他妈的创建了一个节点
                GraceException.doException(ResponseStatusEnum.SYSTEM_ERROR);
            }

            // 如果子节点数量只有是一个的话，说明他妈的就是最小的节点
            if (subLocks.size() == 1) {
                log.info("当前 {} 是最小的节点，获得锁成功", selfLock);
                return;
            } else {
                // 000001 <- 000002 <- 000003，每一个节点都需要监听上一个节点
                // 获得锁的下标进行排序(这里底层是快速排序，根据字符串的ASCII码进行排序)
                Collections.sort(subLocks);

                // 举个例子，节点的名称有可能是: /locks/order/mybosspin00001(按照最后一个斜杠进行分割，也就是mybosspin00001作为key)
                String key = StringUtils.substringAfterLast(selfLock, "/");// 进行字符串的截取，获得锁的名称

                int index = Collections.binarySearch(subLocks, key);// 进行二分查找，返回下标(前提是已经排序好了)

                if (index < 0) {
                    GraceException.doException(ResponseStatusEnum.SYSTEM_ERROR);
                } else if (index == 0) { // 说明当前节点是最小的节点
                    log.info("当前 {} 是最小的节点，获得锁成功", selfLock);
                    return;
                } else { // 说明不是最小的节点 index > 0
                    String preNode = subLocks.get(index - 1); // 获得上一个节点

                    // 监听上一个节点(监听上一个节点是否被删除了，如果被删除了，则说明获得锁成功，如果没有被删除，则继续等待)
                    zooKeeper.getData(lockSpace + "/" + preNode, event -> {
                        // 监听节点被删除的事件
                        if (event.getType() == null) {
                            return;
                        }
                        switch (event.getType()) {
                            case NodeDeleted: // 上一个节点被删除了
                                log.info("监听到上一个节点 {} 被删除了，当前 {} 获得锁成功", preNode, selfLock);
                                countDownLatch.countDown(); // 唤醒当前线程
                                break;
                            default:
                                break;
                        }
                    }, null);

                    // 调用park
                    log.info("当前 {} 不是最小的节点，进入等待", selfLock);
                    countDownLatch.await();
                }
            }


        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 释放锁
     */
    public void release() {
        log.info("释放锁 {}", selfLock);
        try {
            zooKeeper.delete(selfLock, -1);
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }

}
