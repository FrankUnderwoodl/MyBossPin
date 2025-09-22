package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 14:45
 * @Description 构建简单模式的生产者，发送消息
 */
public class FooProducer {

    public static void main(String[] args) throws IOException, TimeoutException {

        //· 1. 创建连接工厂以及相关的参数配置
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.211.55.9"); // 1.1 设置主机地址
        factory.setPort(5672);          // 1.2 设置端口号
        factory.setVirtualHost("/");    // 1.3 设置虚拟主机(类似于数据库的schema，也就是一张逻辑表)
        factory.setUsername("Lzm");     // 1.4 设置用户名
        factory.setPassword("33321480");  // 1.5 设置密码

        //· 2. 通过工程创建连接一个Connection，也就是连接到我在虚拟机上安装的RabbitMQ服务
        Connection connection = factory.newConnection();

        //· 3. 创建一个专门服务于当前线程的管道Channel
        // 注意：在RabbitMQ中，Channel是线程不安全的，所以每个线程都应该有自己的Channel实例
        Channel channel = connection.createChannel();

        //· 4. 创建队列 Queue(简单模式不需要交换机Exchange)
        /*
          queue: 队列名
          durable: 是否持久化，true：重启之后，队列依然存在，false则不存在
          exclusive: 是否独占，true：只能有一个消费者监听这个队列，一般设置为false(因为只有一个消费者监听这个队列，所以不需要设置为true)
          autoDelete: 是否自动删除，true：当没有消费者的时候，则自动删除这个队列
          arguments: map类型的其他参数

          ⚠️注意：如果队列已经存在，则不会重复创建，也就是说每当你想创建一个队列时，RabbitMQ会检查这个队列是否已经存在，如果存在则不会再创建。
         */
        channel.queueDeclare("hello", true, false, false, null);

        //· 5. 向队列发送消息
        String msg = "Hello, RabbitMQ! I'm a FooProducer!";
        /*
        basicPublish的四个参数：
          exchange: 交换机的名称，简单模式下没有，所以直接设置为 ""
          routingKey: 路由key，映射路径，如果交换机没有，则路由key和队列名保持一致
          props: 配置参数
          body: 消息数据
         ⚠️注意：这里的msg.getBytes()是将字符串转换为字节数组，因为RabbitMQ只接受字节数组作为消息内容
         */
        channel.basicPublish("", "hello", null, msg.getBytes());
        // 打印发送的消息内容，⚠️注意：在RabbitMQ中，消息是异步发送的，所以你可能不会立即看到消息被消费。
        System.out.println("Sent message: " + msg);

        //· 6. 释放资源
        channel.close(); // 关闭Channel
        connection.close(); // 关闭Connection

        System.out.println("FooProducer is running...");
    }
}
