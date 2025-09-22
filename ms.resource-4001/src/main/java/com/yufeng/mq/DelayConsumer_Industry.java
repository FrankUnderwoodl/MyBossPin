package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.yufeng.api.mq.DelayConfig_Industry;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.pojo.Industry;
import com.yufeng.service.IndustryService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.model.vo.TopIndustryWithThirdListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年7月18日 02:41
 * @Description RabbitMQ延迟消息消费者-行业分类
 */
@Slf4j
@Component
public class DelayConsumer_Industry extends BaseInfoProperties {

    @Autowired
    private IndustryService industryService;

    /**
     * 监听SMS_QUEUE这个队列，并处理接收到的消息
     *
     * @param message 消息对象
     * @param channel 跟RabbitMQ的通道对象，底层是一个线程来处理消息
     */
    @RabbitListener(queues = DelayConfig_Industry.QUEUE_DELAY_REFRESH) // 这里底层就是帮你创建了一个线程来处理消息，也就是帮你封装好了Connection、Channel等对象
    public void receiveIndustryMessage(Message message, Channel channel) throws IOException {

        try {
            // 打印一下消息内容、routingKey等信息
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();

            // 打印接收到的消息内容
            if (routingKey.equalsIgnoreCase(DelayConfig_Industry.DELAY_REFRESH_INDUSTRY)) {

                log.info("10s后，" + "接收到RabbitMQ发送的延迟消息，routingKey：{}，消息内容：{}", routingKey, new String(message.getBody()));
                log.info("正式开始刷新Redis中的行业分类数据");

                // 从Redis中删除一级分类
                redis.del(TOP_INDUSTRY_LIST);
                // 查询一级分类列表
                List<Industry> list = industryService.getTopIndustryList();
                // 设置一级分类到Redis
                redis.set(TOP_INDUSTRY_LIST, GsonUtils.object2String(list));

                // 从Redis中删除所有的三级分类键值对
                String thirdKeyMulti = THIRD_INDUSTRY_LIST + ":byTopId:";
                redis.allDel(thirdKeyMulti); // 删除所有以THIRD_INDUSTRY_LIST:byTopId:开头的键值对
                // 从MySQL中查询所有的三级分类列表
                List<TopIndustryWithThirdListVO> listVOS = industryService.getAllThirdIndustryList();
                // 设置三级分类到Redis
                for (TopIndustryWithThirdListVO thirdVO : listVOS) {
                    String topIndustryId = thirdVO.getTopId();
                    String thirdKey = THIRD_INDUSTRY_LIST + ":byTopId:" + topIndustryId;
                    redis.set(thirdKey, GsonUtils.object2String(thirdVO.getThirdIndustryList()));
                }
            }

            /* 向RabbitMQ发送确认消息，也就是告诉RabbitMQ这条消息已经被成功消费了
            参数说明：
            deliveryTag：RabbitMQ给每条消息分配的唯一标识符，用来确认消息是否被消费成功
            multiple：参数表示是否批量确认，如果为true，则会确认所有小于等于deliveryTag的消息 */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);

        } catch (IOException e) {

        }
    }

}

