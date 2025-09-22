package com.yufeng.mq;

import com.rabbitmq.client.Channel;
import com.yufeng.api.mq.DelayConfig_MaxCounts;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.DelayTimes;
import com.yufeng.service.SysParamsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Lzm
 * @CreateTime 2025年7月18日 02:41
 * @Description RabbitMQ延迟消息消费者-简历能力刷新次数的最大值
 */
@Slf4j
@Component
public class DelayConsumer_MaxCounts extends BaseInfoProperties {

    @Autowired
    private SysParamsService sysParamsService;

    @Resource(name = "curatorClient")
    private CuratorFramework zkClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 监听
     *
     * @param message 消息对象
     * @param channel 跟RabbitMQ的通道对象，底层是一个线程来处理消息
     */
    @RabbitListener(queues = DelayConfig_MaxCounts.QUEUE_DELAY_MAX_COUNTS)
    // 这里底层就是帮你创建了一个线程来处理消息，也就是帮你封装好了Connection、Channel等对象
    public void receiveIndustryMessage(Message message, Channel channel) {

        try {
            // 打印一下消息内容、routingKey等信息
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();

            // 打印接收到的消息内容
            if (routingKey.equalsIgnoreCase(DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH)) {
                log.info("接收到RabbitMQ发送的延迟消息，routingKey：{}，消息内容：{}", routingKey, new String(message.getBody()));
                log.info("正式开始刷新简历能力的最大值");

                // 1.查询数据库
                Integer maxResumeRefreshCounts = sysParamsService.getSysParams().getMaxResumeRefreshCounts();
                // 2.查询zookeeper
                String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;
                Integer maxCountsZK = Integer.valueOf(new String(zkClient.getData().forPath(path)));
                // 3.查询Redis
                Integer maxCountsRedis = Integer.valueOf(redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS));

                // 4.对比数据是否一致，如果不一致，则进行数据的修复
                this.resetMaxCounts(maxResumeRefreshCounts, maxCountsZK, maxCountsRedis, path);
            }

            // 模拟除零异常
            //int a = 1 / 0;

            // 删除redis中的重试标记
            redis.del(DELAY_ERROR_RETRY_COUNTS);

            // channel.basicAck(message.getMessageProperties().getDeliveryTag(), true); 这里交给finally去确认
        } catch (Exception e) {
            // 假设发生异常的话，则再发送消息给RabbitMQ，让它重新投递(这里可以结合redis，实现不同间隔时间的重试)
            log.error("处理RabbitMQ延迟消息出现异常，异常的原因是：{}，开始进行重试", e.getMessage());

            // 默认的重试次数
            Integer delayErrorRetryCounts = 1;

            // 1.从redis中拿出重试次数
            String delayErrorRetryCountsStr = redis.get(DELAY_ERROR_RETRY_COUNTS);
            if (StringUtils.isNotBlank(delayErrorRetryCountsStr)) {
                delayErrorRetryCounts = Integer.valueOf(delayErrorRetryCountsStr);
            }
            // 获取重试次数所对应的延迟时间
            int delayTimes = DelayTimes.getDelayTimes(delayErrorRetryCounts);
            MessagePostProcessor processor = DelayConfig_MaxCounts.setDelayTimes(delayTimes);
            rabbitTemplate.convertAndSend(
                    DelayConfig_MaxCounts.EXCHANGE_DELAY_MAX_COUNTS,
                    DelayConfig_MaxCounts.DELAY_MAX_COUNTS_REFRESH,
                    "123456",
                    processor);

            log.info("发生异常，这是第{}次发送延迟队列进行校验...", delayErrorRetryCounts);

            // 每次异常失败，则累加 DELAY_ERROR_RETRY_COUNTS
            redis.increment(DELAY_ERROR_RETRY_COUNTS, 1);

        } finally {
            try {
                /**
                 * 无论如何都要确认消息，避免消息一直堆积在队列中
                 * 向RabbitMQ发送确认消息，也就是告诉RabbitMQ这条消息已经被成功消费了
                 * 参数说明：
                 * deliveryTag：RabbitMQ给每条消息分配的唯一标识符，用来确认消息是否被消费成功
                 * multiple：参数表示是否批量确认，如果为true，则会确认所有小于等于deliveryTag的消息，如果为false，则只确认当前消息
                 */
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception ex) {
                log.error("向RabbitMQ确认消息失败，失败的原因是：{}", ex.getMessage());
            }
        }
    }

    private void resetMaxCounts(Integer maxResumeRefreshCounts, Integer maxCountsZK, Integer maxCountsRedis, String path) throws Exception {
        if (maxResumeRefreshCounts.equals(maxCountsZK) && maxResumeRefreshCounts.equals(maxCountsRedis)) {
            log.info("数据库、Zookeeper、Redis三方数据一致，无需修复");
        } else {
            log.info("数据库、Zookeeper、Redis三方数据不一致，开始进行数据修复");
            // 4.1 如果ZK数据不一致，则进行修复
            if (!maxResumeRefreshCounts.equals(maxCountsZK)) {
                // 进行数据修复
                zkClient.setData().forPath(path, (maxResumeRefreshCounts + "").getBytes());
                log.info("Zookeeper数据修复成功，修复后的数据为：{}", maxResumeRefreshCounts);
            }
            // 4.2 如果Redis数据不一致，则进行修复
            if (!maxResumeRefreshCounts.equals(maxCountsRedis)) {
                redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxResumeRefreshCounts + "");
                log.info("Redis数据修复成功，修复后的数据为：{}", maxResumeRefreshCounts);
            }
        }
    }

}

