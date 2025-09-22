package com.yufeng.api.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 23:09
 * @Description RabbitMQ延迟队列的配置类(专门针对行业分类数据刷新的延迟队列)
 *
 */
@Configuration // 这个Configuration注解就是专门用来创建Bean对象的
public class DelayConfig_Industry {

    // 这里可以添加RabbitMQ的相关配置，比如交换机、队列等
    // 例如：创建交换机、队列、绑定关系等

    // exchange的名称
    public static final String EXCHANGE_DELAY_REFRESH = "exchange_delay_refresh";

    // 队列的名称
    public static final String QUEUE_DELAY_REFRESH = "queue_delay_refresh";

    //· 统一定义路由Key
    public static final String DELAY_REFRESH_INDUSTRY = "delay.refresh.industry";



    //· 向RabbitMQ声明一个交换机(如果交换机不存在，会自动创建；如果已存在，会验证配置是否匹配)
    @Bean(EXCHANGE_DELAY_REFRESH)
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_DELAY_REFRESH) // 声明一个主题交换机
                .durable(true) // true表示交换机是持久化的(也就是重启RabbitMQ服务器后仍然存在)
                .delayed() // 声明这是一个延迟交换机(也就是支持延迟消息)
                .build();
    }


    //· 向RabbitMQ声明一个队列(如果队列不存在，会自动创建；如果已存在，会验证配置是否匹配)
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
    @Bean(QUEUE_DELAY_REFRESH)
    public Queue queue() {
        // return new Queue(SMS_QUEUE, true); // true表示队列是持久化的
        return QueueBuilder
                .durable(QUEUE_DELAY_REFRESH)  // 声明一个持久化队列，也就是重启RabbitMQ服务器后仍然存在
                .build();
    }


    //· 创建绑定关系(将交换机和队列通过路由键绑定在一起)
    /**
     * 问：为什么要使用@Qualifier注解？
     * 答：因为在IOC中可能会有多个Exchange和Queue对象，使用@Qualifier可以指定注入哪个Bean
     * 问: binding这个Bean的底层是什么？
     * 答：其实类似于这句代码 -> channel.queueBind(topicQueueOrder, topicExchange, "order.*"),只不过Spring 会自动处理连接管理和绑定操作
     */
    @Bean
    public Binding delayBindingIndustry(@Qualifier(EXCHANGE_DELAY_REFRESH) Exchange exchange,
                                        @Qualifier(QUEUE_DELAY_REFRESH) Queue queue)  {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("delay.refresh.*") // 路由键，#表示匹配任意字符，比如 delay.refresh.industry；*表示匹配一个字符，比如 delay.refresh.industry
                .noargs(); // 不需要额外参数
    }



    /**
     * 设置延迟时间，也就是消息在交换机中等待的时间，过了这个时间后才会被投递到队列中，消费者才能消费
     * @param times 延迟时间，单位为毫秒
     * @return 返回一个MessagePostProcessor对象，用于设置消息的延迟时间
     */
    public static MessagePostProcessor setDelayTimes(Integer times) {

        // 创建一个MessagePostProcessor对象，用于设置每一条消息的延迟时间
        return message -> {
            // 设置每一条消息为持久化的(也就是重启RabbitMQ服务器后仍然存在)
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            // 设置每一条消息的延迟时间(单位为毫秒)
            message.getMessageProperties().setDelay(times);
            return message;
        };
    }

}
