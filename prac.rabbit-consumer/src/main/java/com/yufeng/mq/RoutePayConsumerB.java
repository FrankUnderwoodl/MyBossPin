package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 14:45
 * @Description 构建路由模式的消费者，监听消费消息
 */
public class RoutePayConsumerB {

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

        //· 4. 创建队列 Queue(简单模式不需要交换机Exchange)，建议还是创建一下，因为有可能消费者比生产者先启动
        /*
          queue: 队列名
          durable: 是否持久化，true：重启之后，队列依然存在，false则不存在
          exclusive: 是否独占，true：只能有一个消费者监听这个队列，一般设置为false(因为只有一个消费者监听这个队列，所以不需要设置为true)
          autoDelete: 是否自动删除，true：当没有消费者的时候，则自动删除这个队列
          arguments: map类型的其他参数

          ⚠️注意：如果队列已经存在，则不会重复创建，也就是说每当你想创建一个队列时，RabbitMQ会检查这个队列是否已经存在，如果存在则不会再创建。
         */
        String routingQueuePay = "routing_queue_pay";   // 支付队列
        channel.queueDeclare(routingQueuePay, true, false, false, null);


        //· 5.监听消费消息
        /*
          queue: 要监听的队列名
          autoAck: 是否自动确认，true：告知mq消费者已经消费的确认通知
          当 autoAck 为 false 时，你需要手动调用 channel.basicAck() 来确认消息已被处理。如果不手动确认：
            • 消息会一直处于 "unacked" 状态
            • 当消费者断开连接时，消息会重新回到队列
            • 其他消费者可以重新消费这些未确认的消息
          callback: 回调函数，处理监听到的消息
         */
        // RabbitMQ的Java客户端使用了独立的线程池来处理这些回调函数，而不是在主线程中同步执行。
        channel.basicConsume(routingQueuePay, true, (consumerTag, delivery) -> {
            // consumerTag: 消费者标识（标签）
            // delivery: 消息的封装，包含了消息的内容和一些元数据
            String message = new String(delivery.getBody());
            // System.out.println("delivery = " + delivery);
            // System.out.println("consumerTag = " + consumerTag);

            System.out.println("routingQueuePay 消费到的信息为: " + message);
        }, consumerTag -> {
            // 处理取消消费的回调
            System.out.println("取消消费: " + consumerTag);
        });

        System.out.println("消费者已启动，等待消息...");
    }
}
