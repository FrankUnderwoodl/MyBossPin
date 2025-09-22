package com.yufeng.controller;

import com.yufeng.api.mq.DelayConfig_Industry;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.pojo.Industry;
import com.yufeng.service.IndustryService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.LocalDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年8月7日08:26:01
 *
 * 这个类是行业控制器
 * 行业数据分为三级：一级行业、二级行业、三级行业
 * 1.一级行业：互联网、金融、教育、医疗、房地产、交通运输
 * 2.二级行业：一级行业下面的子行业，比如互联网下面有：电子商务、社交网络、搜索引擎、在线广告、云计算、大数据、人工智能等
 * 3.三级行业：二级行业下面的子行业，比如电子商务下面有：B2B、B2C、C2C、O2O等
 */
@RestController
@RequestMapping("/industry")
@Slf4j
public class IndustryController extends BaseInfoProperties {

    @Autowired
    private IndustryService industryService;


// ====================================下面是给App端使用的接口==============================================

    /**
     * 初始化顶级行业列表(也就是返回所有的一级行业节点)
     * 供APP端使用
     *
     * @return 返回顶级行业列表
     */
    @GetMapping("/app/initTopList")
    public GraceJSONResult initTopList() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("查询顶级行业列表(App端接口)");

        List<Industry> topIndustryList;

        //* 先从Redis中查询，如果没有；再从数据库中查询后并且放入到Redis中
        String topIndustryListStr = redis.get(TOP_INDUSTRY_LIST);
        if (StringUtils.isNotBlank(topIndustryListStr)) { // 查Redis
            topIndustryList = GsonUtils.stringToListAnother(topIndustryListStr, Industry.class);
        } else {
            topIndustryList = industryService.getTopIndustryList(); // 查MySQL
            redis.set(TOP_INDUSTRY_LIST, GsonUtils.object2String(topIndustryList));
        }

        stopWatch.stop();
        log.info("查询顶级行业列表(App端接口)耗时：{}毫秒", stopWatch.getTotalTimeMillis());
        return GraceJSONResult.ok(topIndustryList);
    }


    /**
     * 通过顶级节点的id，获取三级行业列表
     * 供APP端使用
     *
     * @param topIndustryId 顶级行业ID
     * @return 返回三级行业列表
     */
    @GetMapping("/app/getThirdListByTop/{topIndustryId}")
    public GraceJSONResult getThirdListByTop(@PathVariable("topIndustryId") String topIndustryId) {

        List<Industry> thirdIndustryList;

        //* 先从Redis中查询，如果没有；再从数据库中查询后并且放入到Redis中
        String thirdKey = THIRD_INDUSTRY_LIST + ":byTopId:" + topIndustryId;

        String thirdIndustryListStr = redis.get(thirdKey); // 先查redis
        if (StringUtils.isNotBlank(thirdIndustryListStr)) {
            thirdIndustryList = GsonUtils.stringToListAnother(thirdIndustryListStr, Industry.class);
        } else {  // 再查数据库

            // 如果传入的topIndustryId在数据库中不存在，一定要在redis中设置一个空值，避免缓存穿透，打崩数据库
            thirdIndustryList = industryService.getThirdListByTop(topIndustryId);
            if (!CollectionUtils.isEmpty(thirdIndustryList)) {
                redis.set(thirdKey, GsonUtils.object2String(thirdIndustryList));
            } else {
                // 避免缓存穿透(向数据库中疯狂读取null值)，增加设空机制
                redis.set(thirdKey, "[]", 10 * 60); // 设置10分钟过期
            }
        }

        return GraceJSONResult.ok(thirdIndustryList);
    }


    /**
     * 重置Redis中的行业数据
     * 供给管理员端使用，因为每当新增、修改或删除行业节点时，都需要重置Redis中的行业数据，让App端能够获取到最新的行业数据
     * 每当新增、修改或删除行业节点时，都需要调用此接口来重置Redis中的行业数据
     * -------------------------------------------------------------------
     * 问题一：假如更新redis的那一瞬间，有大量的请求进来，超过了MySQL所能承受的1510个QPS，后端那就崩了，App端就会用不了(redis还没set值的时候，瞬间流量打到MySQL上)
     * 解决方案：
     * ①使用分布式锁来保证在同一时间只有一个线程可以执行重置Redis的操作。
     * ②使用异步任务来处理重置Redis的操作，避免阻塞主线程。
     * ③使用消息队列来异步处理重置Redis的操作。
     * ④redis本身就有一个‘缓存预热’的功能。
     * -------------------------------------------------------------------
     * 问题二：缓存与数据库数据不一致的问题
     * 解决方案：
     * ①将redis的操作放在数据库操作之后，确保数据一致性。
     * ②假如redis操作在事务里面，就使用缓存双删策略，先删除redis中的数据，再删除一次，确保数据一致性。
     */
    private void resetRedisIndustry(Industry industry) {

        if (industry.getLevel() == 1) { // 如果是一级节点，则删除在redis里面的顶级行业列表

            // 正式删除Redis中的顶级行业列表
            redis.del(TOP_INDUSTRY_LIST);

            // 删除之后，马上就要将最新的顶级行业列表放入到Redis中(让其他的请求可以迅速访问到)
            List<Industry> topIndustryList = industryService.getTopIndustryList();
            redis.set(TOP_INDUSTRY_LIST, GsonUtils.object2String(topIndustryList));

            // 缓存双删(其实如果redis操作在事务外面，就不需要双删了，因为事务已经提交了，redis操作在事务外面，数据已经一致了)
            /* try {
                Thread.sleep(200); // 等待200毫秒，给其他线程足够时间来把脏数据写入redis
                redis.del(TOP_INDUSTRY_LIST);
                redis.set(TOP_INDUSTRY_LIST, GsonUtils.object2String(topIndustryList));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } */

        } else if (industry.getLevel() == 3) { // 如果是三级节点，则删除在redis里面的三级行业列表

            // 通过三级节点id，用我的自定义SQL去查询该三级节点的顶级节点Id
            String topIndustryId = industryService.getTopIndustryId(industry.getId());

            // 要删除三级节点，其实就是把redis下的顶级节点下的三级节点列表全部删除(所以需要自写SQL查询该三级节点的父级ID)
            String thirdKey = THIRD_INDUSTRY_LIST + ":byTopId:" + topIndustryId;

            // 正式删除Redis中的三级行业列表
            redis.del(thirdKey);

            // 删除key之后，需要重新查询三级行业列表，并放入到redis中
            List<Industry> thirdIndustryList = industryService.getThirdListByTop(topIndustryId);
            redis.set(thirdKey, GsonUtils.object2String(thirdIndustryList));

            // 缓存双删(其实如果redis操作在事务外面，就不需要双删了，因为事务已经提交了，即使当前线程将redis删除之后，其他线程从数据库中拿到的数据也是最新的)
           /*  try {
                Thread.sleep(300); // 等待300毫秒，给其他线程来把脏数据写入redis
                redis.del(thirdKey);
                redis.set(thirdKey, GsonUtils.object2String(thirdIndustryList));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } */
        }
        // 新增与修改二级节点，没有必要重置Redis中的数据，因为在该基础上，三级节点的总数并没有增减。
    }


// ====================================下面是给管理员端使用的接口==============================================

    /**
     * 创建行业节点
     *
     * @param industry 行业信息
     * @return 返回操作结果
     */
    @PostMapping("/createNode")
    public GraceJSONResult createNode(@RequestBody Industry industry) {

        // 首先通过名称查询某行业是否已经存在
        if (industryService.getIndustryByName(industry.getName())) {
            // 如果存在，直接返回错误提示
            return GraceJSONResult.errorMsg("该行业节点已存在，请勿重复添加～");
        }

        // 通过service层，开始创建一个新的行业节点
        industryService.createIndustry(industry); // 此时事务已经commit了

        // 重置Redis中的行业数据(不采用强一致性)
        // this.resetRedisIndustry(industry);
        return GraceJSONResult.ok();
    }


    /**
     * 获取顶级行业列表
     *
     * @return 返回顶级行业列表
     */
    @GetMapping("/getTopList")
    public GraceJSONResult getTopList() {

        return GraceJSONResult.ok(industryService.getTopIndustryList());
    }


    /**
     * 获得当前顶级节点下的子行业列表
     * 如果是默认的，则查询顶级行业
     *
     * @param industryId 行业ID
     * @return 返回子行业列表
     */
    @GetMapping("/children/{industryId}")
    public GraceJSONResult getChildrenIndustryList(@PathVariable("industryId") String industryId) {

        // 调用service层来查询子行业列表，管理端是直接查MySQL，而移动端则是查redis
        List<Industry> list = industryService.getChildrenIndustryList(industryId);

        return GraceJSONResult.ok(list);
    }


    /**
     * 修改行业节点
     *
     * @param industry 某个行业信息
     * @return 返回操作结果
     */
    @PostMapping("/updateNode")
    public GraceJSONResult updateNode(@RequestBody Industry industry) {

        // 调用service层来更新行业信息
        industryService.updateIndustry(industry); // 此时事务已经commit了

        // 删除Redis中的行业数据(不采用强一致性)
        // this.resetRedisIndustry(industry);

        return GraceJSONResult.ok();
    }


    /**
     * 删除行业节点
     *
     * @param industryId 行业ID
     * @return 返回操作结果
     */
    @DeleteMapping("/deleteNode/{industryId}")
    public GraceJSONResult deleteNode(@PathVariable("industryId") String industryId) {

        // 不需要判断industryId是否为空，因为前端已经做了非空校验

        // 首先要判断如果是一级或者二级节点，则需要保证没有叶子节点才能删除；如果是三级节点，则可以直接删除
        // 通过IService来查询某节点
        Industry industry = industryService.getById(industryId);

        if (industry.getLevel() == 1 || industry.getLevel() == 2) {

            //· 如果是一级或二级节点，则需要判断是否有子行业
            /* 下面这段代码不好，因为需要把全部的子行业都查询出来的话，MySQL会进行全表扫描，效率低下
            List<Industry> childrenList = industryService.getChildrenIndustryList(industryId);
            if (childrenList != null && !childrenList.isEmpty()) {
                return GraceJSONResult.errorMsg("该行业节点下存在子行业，无法删除！");
            } */
            // 正确的做法就是使用count(*)来查询子行业的数量
            Long counts = industryService.getChildrenIndustryCounts(industryId);
            if (counts > 0) {
                return GraceJSONResult.errorMsg("请保证该节点下无任何子行业后再删除！");
            }
        }

        // 删除redis中的行业数据(不采用强一致性)
        // this.resetRedisIndustry(industry);

        // 正式删除
        industryService.removeById(industryId);

        // 删除Redis中的行业数据(放在这里有问题，因为数据库已经删除了该叶子节点，无法通过该行业节点来重置Redis中的数据)
        // this.resetRedisIndustry(industry);

        return GraceJSONResult.ok();
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;



    /**
     * 刷新行业数据(每当第二天凌晨3点时，自动调用此接口来刷新行业数据) -> 之所以这么做，是因为防止del redis的时候，正好有大量的请求打到MySQL上，导致MySQL崩掉
     * 这个接口主要是为了在新增、修改或删除行业节点后，手动刷新行业数据
     * 这个接口可以在管理员端调用，来手动刷新行业数据
     * -------------------------------------------------------------------
     * 供给管理员端使用
     * 这个接口可以用来手动刷新行业数据，通常在新增、修改或删除行业节点后调用
     *
     * @return 返回操作结果
     */
    @PostMapping("/refreshIndustry")
    public GraceJSONResult refreshIndustry() {

        // 计算凌晨三点到现在的时间
        LocalDateTime futureTime = LocalDateUtils.parseLocalDateTime(
                LocalDateUtils.getTomorrow() + " 03:00:00",
                LocalDateUtils.DATETIME_PATTERN);
        // 计算当前时间和凌晨发布的时间差
        Long publishTimes = LocalDateUtils.getChronoUnitBetween(LocalDateTime.now(),
                futureTime,
                ChronoUnit.MILLIS,
                true);
        int delayTime = publishTimes.intValue();

        int delayTime2 = 10 * 1000; // 测试10秒延迟

        // 发送消息给延迟队列
        MessagePostProcessor postProcessor = DelayConfig_Industry.setDelayTimes(delayTime2); // 创建一个新的 MessagePostProcessor
        // 正式发送延迟消息到RabbitMQ
        rabbitTemplate.convertAndSend(
                DelayConfig_Industry.EXCHANGE_DELAY_REFRESH,
                DelayConfig_Industry.DELAY_REFRESH_INDUSTRY,
                "123456", // 这里可以是任何内容，实际业务中可以是需要刷新的行业ID等信息,跟消息无关
                postProcessor
        );
        log.info("正式向MQ发送弱一致性消息，当前时间为：{}", LocalDateTime.now());

        return GraceJSONResult.ok("testOfRefreshIndustry");
    }

}
