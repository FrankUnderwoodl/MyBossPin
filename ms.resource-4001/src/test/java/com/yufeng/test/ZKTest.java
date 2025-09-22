// package com.yufeng.test;
//
// import lombok.extern.slf4j.Slf4j;
// import org.apache.zookeeper.CreateMode;
// import org.apache.zookeeper.ZooDefs;
// import org.apache.zookeeper.ZooKeeper;
// import org.junit.Before;
// import org.junit.Test;
//
// import java.util.List;
// import java.util.concurrent.CountDownLatch;
//
// /**
//  * @author Lzm
//  * @CreateTime 2025年9月16日 06:18
//  */
// @Slf4j
// public class ZKTest {
//
//     public static ZooKeeper zooKeeper = null;
//
//     CountDownLatch countDownLatch = new CountDownLatch(1);
//
//     @Before
//     public void initZK() throws  Exception {
//
//         /**
//          * connectString: 连接服务器的ip地址和端口号
//          * sessionTimeout: 会话超时时间，单位毫秒，就是所谓的Java客户端和ZK服务端之间的心跳时间
//          * watcher: 监听器对象，其实就是一个回调函数(比如其他的客户端修改了数据，这里的watcher就会得到一个值，然后进行回调)
//          * sessionId: 会话id
//          * sessionPasswd: 会话密码
//          * canBeReadOnly: 是否只读(当zookeeper断开连接的时候，当前客户端是否可以进行读操作，但这个读操作是'脏数据'，因为没有和zookeeper服务端进行连接)
//          */
//         zooKeeper = new ZooKeeper("localhost:2181", 5000, null);
//     }
//
//
//     @Test
//     public void createNode() throws  Exception {
//
//         // 创建节点
//         //    参数，1.节点的路径 2.节点的数据 3.节点的权限 4.节点的类型
//         //    ZooKeeper的ACL控制谁可以对节点执行什么操作：
//         //
//         //    权限类型：
//         //    CREATE - 创建子节点
//         //    READ - 读取节点数据和子节点列表
//         //    WRITE - 设置节点数据
//         //    DELETE - 删除子节点
//         //    ADMIN - 设置节点ACL权限
//         //    常用预定义ACL：
//         //    OPEN_ACL_UNSAFE - 完全开放，任何人都有所有权限
//         //    CREATOR_ALL_ACL - 创建者拥有所有权限
//         //    READ_ACL_UNSAFE - 任何人只有读权限
//         zooKeeper.create("/mybosspin/java", "hello kzr".getBytes(),
//                         ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//     }
//
//     @Test
//     public void getChildNodesAndWatcher() throws  Exception {
//
//         // getChildren 方法
//         // 用途：获取指定节点的子节点列表
//         List<String> children = zooKeeper.getChildren("/mybosspin", watched -> {
//             log.info("监听到节点变化了...");
//             log.info("监听到的路径为: {}, 监听类型为: {}", watched.getPath(), watched.getType());
//         });
//
//
//         // getData 方法
//         // 用途：获取指定节点的数据内容
//         // zooKeeper.getData()
//
//         // await的意思是等待计数器归零，然后继续往下执行
//         countDownLatch.await();
//     }
//
//
// }
