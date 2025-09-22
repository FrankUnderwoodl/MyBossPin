package com.yufeng.api.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 23:09
 * @Description RabbitMQ的配置类
 * 问：将exchange、queue、blinding声明为Bean的目的是什么？
 * 答：这样做的好处是：可以每当SpringBoot服务启动的时候，就能自动向RabbitMQ中间件中定义交换机、队列以及路由key
 *
 */
@Configuration // 这个Configuration注解就是专门用来创建Bean对象的
public class SMSConfig {

    // 这里可以添加RabbitMQ的相关配置，比如交换机、队列等
    // 例如：创建交换机、队列、绑定关系等

    // exchange的名称
    public static final String SMS_EXCHANGE = "sms_exchange";

    // 队列的名称
    public static final String SMS_QUEUE = "sms_queue";

    //· 统一定义路由Key
    // 发送短信验证码的路由Key
    public static final String ROUTING_KEY_SMS_SEND_LOGIN = "yufeng.sms.send.login";



    //· 向RabbitMQ声明一个交换机
    // 因为有可能有多个交换机，所以需要指定Bean的id(这里的SMS_EXCHANGE就是Bean的id)，如果不指定，默认Bean的名称是方法名
    @Bean(SMS_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(SMS_EXCHANGE)
                .durable(true)
                .build();
    }


    //· 向RabbitMQ声明一个队列
    /*
    这个@Bean注解做了什么？
      • 向Spring容器注册一个Queue对象
      • Spring Boot会自动将这个Queue声明到RabbitMQ服务器
      • 如果队列不存在，会自动创建；如果已存在，会验证配置是否匹配
    就像这样的代码：
       Spring Boot启动时自动执行类似操作
       Channel channel = connection.createChannel();
       channel.queueDeclare(SMS_QUEUE, true, false, false, null);
     */
    @Bean(SMS_QUEUE)
    public Queue queue() {
        // return new Queue(SMS_QUEUE, true); // true表示队列是持久化的
        return QueueBuilder
                .durable(SMS_QUEUE)
                .withArgument("x-max-length", 100) // 设置队列的最大长度为100条消息
                .withArgument("x-message-ttl", 60000) // 设置队列里面的消息过期时间为60秒
                .withArgument("x-dead-letter-exchange", SMSDeadConfig.SMS_EXCHANGE_DEAD)       // 设置死信交换机
                .withArgument("x-dead-letter-routing-key", SMSDeadConfig.ROUTING_KEY_SMS_DEAD) // 设置死信路由键
                .build();
    }


    //· 创建绑定关系
    /**
     * 问：为什么要使用@Qualifier注解？
     * 答：因为在IOC中可能会有多个Bean对象，如果不使用@Qualifier注解，Spring会不知道使用哪个Bean对象
     * 问: binding这个Bean的底层是什么？
     * 答：其实类似于这句代码 -> channel.queueBind(topicQueueOrder, topicExchange, "order.*"),只不过Spring 会自动处理连接管理和绑定操作
     */
    @Bean
    public Binding smsBinding(@Qualifier(SMS_EXCHANGE) Exchange exchange,  @Qualifier(SMS_QUEUE) Queue queue)  {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("yufeng.sms.#") // 路由键，#表示匹配任意字符，比如yufeng.sms.send.login
                .noargs(); // 不需要额外参数
    }

}
