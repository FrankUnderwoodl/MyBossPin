package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.yufeng.api.mq.SMSConfig;
import com.yufeng.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Lzm
 * @CreateTime 2025年7月18日 02:41
 * @Description 短信消费者类，用来接收RabbitMQ发送的短信内容(底层就是一个线程，用来处理消息)
 */
@Slf4j
@Component
public class SMSConsumer {

    @Autowired
    private SMSUtils smsUtils;


    /**
     * 监听SMS_QUEUE这个队列，并处理接收到的消息
     *
     * @param message 消息对象
     * @param channel 跟RabbitMQ的通道对象，底层是一个线程来处理消息
     */
    @RabbitListener(queues = SMSConfig.SMS_QUEUE) // 这里底层就是帮你创建了一个线程来处理消息，也就是帮你封装好了Connection、Channel等对象
    public void receiveSMSMessage(Message message, Channel channel) throws IOException {

        try {
            // 打印一下消息内容、routingKey等信息
            String msg = new String(message.getBody());
            log.info("获取到的payload为: {}", msg);
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            // log.info("获取到的routingKey为: {}", routingKey);

            // int i = 1 / 0; // 模拟异常，测试消息重试机制

            // 向RabbitMQ发送确认消息，也就是告诉RabbitMQ这条消息已经被成功消费了
            // 参数说明：
            // deliveryTag：RabbitMQ给每条消息分配的唯一标识符，用来确认消息是否被消费成功
            // multiple：参数表示是否批量确认，如果为true，则会确认所有小于等于deliveryTag的消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);

        } catch (Exception e) {

            // 这里的requeue参数表示是否将消息重新入队，如果为true，则会将消息重新放回队列中，等待下次消费
            // 如果为false，你可以将消息丢弃，或者发送到死信队列中
            log.error("处理短信消息失败，消息内容: {}，将放入死信队列", new String(message.getBody()));

            channel.basicNack(message.getMessageProperties().getDeliveryTag(), true, false);
        }
    }

}

