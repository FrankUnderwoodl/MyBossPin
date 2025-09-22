package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.yufeng.api.mq.SMSDeadConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Lzm
 * @CreateTime 2025年7月18日 02:41
 * @Description 死信队列消费者类，用来接收RabbitMQ发送的短信内容(底层就是一个线程，用来处理消息)
 */
@Slf4j
@Component
public class SMSDeadConsumer {

    /**
     * 监听SMS_QUEUE这个队列，并处理接收到的消息
     *
     * @param message 消息对象
     * @param channel 跟RabbitMQ的通道对象，底层是一个线程来处理消息
     */
    @RabbitListener(queues = SMSDeadConfig.SMS_QUEUE_DEAD) // 这里底层就是帮你创建了一个线程来处理消息，也就是帮你封装好了Connection、Channel等对象
    public void receiveSMSMessage(Message message, Channel channel) throws IOException {

        try {
            // 打印一下消息内容、routingKey等信息
            String msg = new String(message.getBody());
            log.info("这里是死信队列，获取到的payload为: {}", msg);
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            // log.info("这里是死信队列，获取到的routingKey为: {}", routingKey);

            // int i = 1 / 0; // 模拟异常，测试消息重试机制

            // 向RabbitMQ发送确认消息，也就是告诉RabbitMQ这条消息已经被成功消费了
            // 参数说明：
            // deliveryTag：RabbitMQ给每条消息分配的唯一标识符，用来确认消息是否被消费成功
            // multiple：参数表示是否批量确认，如果为true，则会确认所有小于等于deliveryTag的消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);

        } catch (IOException e) {

        }
    }

}

