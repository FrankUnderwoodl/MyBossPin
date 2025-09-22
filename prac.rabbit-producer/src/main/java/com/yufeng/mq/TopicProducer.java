package com.yufeng.mq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 14:45
 * @Description 构建通配符模式的生产者，发送消息
 */
public class TopicProducer {

    public static void main(String[] args) throws IOException, TimeoutException {

        //· 1. 创建连接工厂以及相关的参数配置
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.211.55.9");  // 1.1 设置主机地址
        factory.setPort(5672);           // 1.2 设置端口号
        factory.setVirtualHost("/");     // 1.3 设置虚拟主机(类似于数据库的schema，也就是一张逻辑表)
        factory.setUsername("Lzm");      // 1.4 设置用户名
        factory.setPassword("33321480"); // 1.5 设置密码

        //· 2. 通过工程创建连接一个Connection，也就是连接到我在虚拟机上安装的RabbitMQ服务
        Connection connection = factory.newConnection();

        //· 3. 创建一个专门服务于当前线程的管道Channel
        // 注意：在RabbitMQ中，Channel是线程不安全的，所以每个线程都应该有自己的Channel实例
        // 这里你可以多创几个线程，然后每个线程都创建自己的Channel，这样就可以实现多线程并发发送消息
        Channel channel = connection.createChannel();

        //· 4. 创建交换机 Exchange，也就是通过channel向RabbitMQ中间件声明一个交换机(你其实可以将channel类比成socket类)
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

          返回值：如果交换机已经存在或者声明成功，则返回一个Exchange.DeclareOk对象，否则抛出异常
         */
        String topicExchange = "topic_exchange";
        AMQP.Exchange.DeclareOk exchanged = channel.exchangeDeclare(topicExchange, BuiltinExchangeType.TOPIC,
                true, false, false, null);


        //· 5. 定义两个队列，也就是向RabbitMQ中间件声明两个队列(如果队列已经存在，则不会重复创建)
        String topicQueueOrder = "topic_queue_order"; // 订单队列
        String topicQueuePay = "topic_queue_pay";   // 支付队列
        // 参数含义：队列名、是否持久化、是否被一个consumer独占、是否自动删除、其他参数
        AMQP.Queue.DeclareOk queued = channel.queueDeclare(topicQueueOrder, true, false, false, null);
        channel.queueDeclare(topicQueuePay, true, false, false, null);

        //· 6. 通过channel和RabbitMQ进行通信，绑定交换机和队列的关系(我只需发一次消息给交换机即可，交换机会将消息发送到所有绑定的队列，如果没有交换机，我就需要将消息发送到每个队列)
        // 交换机要带上什么样的routingKey，才能将消息发送到对应的队列
        // ‘*’的意思是匹配一个单词，‘#’的意思是匹配0个或多个单词
        channel.queueBind(topicQueueOrder, topicExchange, "order.*"); // 订单相关的消息
        channel.queueBind(topicQueuePay, topicExchange, "*.pay.#"); // 支付相关的消息

        //· 7. 向交换机发送消息
        String msg1 = "创建订单A";
        String msg2 = "创建订单B";
        String msg3 = "删除订单C";
        String msg4 = "修改订单D";
        String msg5 = "支付订单E";
        String msg6 = "超市支付订单F";
        String msg7 = "御风支付订单G";

        channel.basicPublish(topicExchange, "order.create", null, msg1.getBytes()); // 发送创建订单A消息
        channel.basicPublish(topicExchange, "order.create", null, msg2.getBytes()); // 发送创建订单B消息
        channel.basicPublish(topicExchange, "order.delete", null, msg3.getBytes()); // 发送删除订单C消息
        channel.basicPublish(topicExchange, "order.update", null, msg4.getBytes()); // 发送修改订单D消息
        channel.basicPublish(topicExchange, "order.pay", null, msg5.getBytes());    // 发送支付订单E消息
        channel.basicPublish(topicExchange, "yufeng.pay.market", null, msg6.getBytes()); // 发送超市支付订单F消息
        channel.basicPublish(topicExchange, "yufeng.pay.yufeng", null, msg7.getBytes()); // 发送御风支付订单G消息

        //· 8. 释放资源
        channel.close(); // 关闭Channel
        connection.close(); // 关闭Connection

        System.out.println("RoutingProducer is running...");
    }
}
