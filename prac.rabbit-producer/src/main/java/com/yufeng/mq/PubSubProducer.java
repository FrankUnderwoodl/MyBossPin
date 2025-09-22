package com.yufeng.mq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 14:45
 * @Description 构建发布订阅模式的生产者，发送消息
 */
public class PubSubProducer {

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

        //· 4. 创建交换机 Exchange
        /*
          exchange: 交换机的名称
          type: 交换机的类型 {
            FANOUT("fanout"): 广播模式，发布订阅，把消息发送给所有的绑定的队列
            DIRECT("direct"): 定向投递模式，把消息发送给指定的“routing key”的队列
            TOPIC("topic"): 通配符模式，把消息发送给符合的“routing pattern”的队列
            HEADERS("headers"): 使用率不多，参数匹配 }
          durable: 是否持久化  true：交换机本身在 RabbitMQ 重启后仍然存在 false：交换机在 RabbitMQ 重启后会消失
          autoDelete: 是否自动删除
          internal: 内部意思，true：表示当前exchange是rabbitmq内部使用的，用户创建的队列不会消费该类型交换机下的消息，所以我们一般使用false即可
          arguments: map类型的参数
         */
        String fanoutExchange = "fanout_exchange";
        channel.exchangeDeclare(fanoutExchange, BuiltinExchangeType.FANOUT,
                true, false, false, null);


        //· 5. 定义两个队列
        String fanoutQueueA = "fanout_queue_a";
        String fanoutQueueB = "fanout_queue_b";
        // 参数含义：队列名、是否持久化、是否被一个consumer独占、是否自动删除、其他参数
        channel.queueDeclare(fanoutQueueA, true, false, false, null);
        channel.queueDeclare(fanoutQueueB, true, false, false, null);

        //· 6. 绑定交换机和队列(我只需发一次消息给交换机即可，交换机会将消息发送到所有绑定的队列，如果没有交换机，我就需要将消息发送到每个队列)
        // 如果routingKey为空字符串，则这个队列所绑定的交换机会将消息发送到所有绑定的队列
        channel.queueBind(fanoutQueueA, "fanout_exchange", "");
        channel.queueBind(fanoutQueueB, "fanout_exchange", "");

        //· 7. 向交换机发送消息(然后交换机会根据你提供的routingKey将消息发送到指定的队列，这里是空字符串，所以会发送到所有绑定的队列)
        for (int i = 0 ; i < 10 ; i ++) {
            String task = "开始上班，搬砖喽~ 开启任务[" + i + "]";
            channel.basicPublish(fanoutExchange, "", null, task.getBytes());
        }

        //· 8. 释放资源
        channel.close(); // 关闭Channel
        connection.close(); // 关闭Connection

        System.out.println("FooProducer is running...");
    }
}
