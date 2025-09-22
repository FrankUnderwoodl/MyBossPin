package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.DataDictionaryBO;
import com.yufeng.model.bo.QueryDictItemsBO;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.pojo.DataDictionary;
import com.yufeng.service.DataDictionaryService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.CompanyPointsVO;
import com.yufeng.model.vo.DataDictionaryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@Slf4j
@RestController
@RequestMapping("/dataDict")
public class DataDictController extends BaseInfoProperties {

    // 数据字典列表的Redis键前缀
    private static final String DDKEY_PREFIX = DATA_DICTIONARY_LIST_TYPECODE + ":";

    @Autowired
    private DataDictionaryService dataDictionaryService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

// ==============================下面是给移动端使用的接口===================================

    /**
     * 根据字典码获得该分类下的所有数据字典项列表(主要用于创建公司的时候选择行业类型、选择公司的优势、公司福利、薪资福利、补助津贴等)
     *
     * @param typeCode 数据字典码
     * @return 包含数据字典项列表的GraceJSONResult对象
     */
    @PostMapping("/app/getDataByCode")
    public GraceJSONResult getDataByCode(String typeCode) {

        // 参数判空
        if (StringUtils.isBlank(typeCode)) {
            return GraceJSONResult.errorMsg("数据字典码不能为空");
        }

        // 调用服务层方法获取数据字典项列表(不走数据库，走缓存)
        // List<DataDictionary> dataByCode = dataDictionaryService.getDataByCode(typeCode);

        // 需要返回的List
        List<DataDictionary> dataDictionaryList = null;

        // 获取redis key
        String key = DDKEY_PREFIX + typeCode;
        // 从Redis中获取数据字典项列表
        String dataDictionaryRedisList = redis.get(key);

        if (StringUtils.isNotBlank(dataDictionaryRedisList)) {
            // 如果Redis中存在该数据字典项列表，则将其转换为List<DataDictionaryBO>
            dataDictionaryList = GsonUtils.stringToListAnother(dataDictionaryRedisList, DataDictionary.class);
        }

        return GraceJSONResult.ok(dataDictionaryList);
    }




    /**
     * 根据字典key，找到对应的字典value(线程池+异步编排)
     * 供App端使用
     * 例如，前端传来五个字典项的key的数组：
     * var bo = {
     *         "advantage": advantageArr,
     *         "benefits": benefitsArr,
     *         "bonus": bonusArr,
     *         "subsidy": subsidyArr
     *       }
     *
     * @param itemsBO 查询字典项的业务对象
     * @return 包含数据字典项列表的GraceJSONResult对象
     */
    @PostMapping("/app/getItemsByKeys")
    public GraceJSONResult getItemsByKeys(@RequestBody QueryDictItemsBO itemsBO) {

        // 将各个字典项的列表封装到一个VO对象中
        CompanyPointsVO list = new CompanyPointsVO();

        // 将业务封装成CF对象
        CompletableFuture<List<DataDictionary>> advantageFuture = CompletableFuture.supplyAsync(() -> {
            String[] advantage = itemsBO.getAdvantage();
            List<DataDictionary> advantageList = dataDictionaryService.getItemsByKeys(advantage);
            list.setAdvantageList(advantageList);
            return advantageList;
        }, threadPoolExecutor);

        CompletableFuture<List<DataDictionary>> benefitsFuture = CompletableFuture.supplyAsync(() -> {
            String[] benefits = itemsBO.getBenefits();
            List<DataDictionary> benefitsList = dataDictionaryService.getItemsByKeys(benefits);
            list.setBenefitsList(benefitsList);
            return benefitsList;
        }, threadPoolExecutor);

        CompletableFuture<List<DataDictionary>> bonusFuture = CompletableFuture.supplyAsync(() -> {
            String[] bonus = itemsBO.getBonus();
            List<DataDictionary> bonusList = dataDictionaryService.getItemsByKeys(bonus);
            list.setBonusList(bonusList);
            return bonusList;
        }, threadPoolExecutor);

        CompletableFuture<List<DataDictionary>> subsidyFuture = CompletableFuture.supplyAsync(() -> {
            String[] subsidy = itemsBO.getSubsidy();
            List<DataDictionary> subsidyList = dataDictionaryService.getItemsByKeys(subsidy);
            list.setSubsidyList(subsidyList);
            return subsidyList;
        }, threadPoolExecutor);

        // 等待所有的异步任务完成
        CompletableFuture.allOf(advantageFuture, benefitsFuture, bonusFuture, subsidyFuture).join();

        return GraceJSONResult.ok(list);
    }



    /**
     * 根据字典key，找到对应的字典value
     * 供App端使用
     * 例如，前端传来五个字典项的key的数组：
     * var bo = {
     *         "advantage": advantageArr,
     *         "benefits": benefitsArr,
     *         "bonus": bonusArr,
     *         "subsidy": subsidyArr
     *       }
     *
     * @param itemsBO 查询字典项的业务对象
     * @return 包含数据字典项列表的GraceJSONResult对象
     */
    @PostMapping("/app/getItemsByKeys2")
    public GraceJSONResult getItemsByKeys2(@RequestBody QueryDictItemsBO itemsBO) {

        // 设置一个秒表
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("获取数据字典项列表计时开始");

        // 从业务对象中获取各个字典项的key数组
        String[] advantage = itemsBO.getAdvantage(); // 获取优势的字典项key数组(比如技能培训、管理规范)
        String[] benefits = itemsBO.getBenefits();   // 获取福利的字典项key数组(比如五险一金、定期体检)
        String[] bonus = itemsBO.getBonus();         // 获取奖金的字典项key数组(比如年底双薪、带薪年假)
        String[] subsidy = itemsBO.getSubsidy();     // 获取补助津贴的字典项key数组(比如午餐补助、外派津贴)

        // 调用服务层方法获取各个字典项的列表
        List<DataDictionary> advantageList = dataDictionaryService.getItemsByKeys(advantage);
        List<DataDictionary> benefitsList = dataDictionaryService.getItemsByKeys(benefits);
        List<DataDictionary> bonusList = dataDictionaryService.getItemsByKeys(bonus);
        List<DataDictionary> subsidyList = dataDictionaryService.getItemsByKeys(subsidy);

        // 将各个字典项的列表封装到一个VO对象中
        CompanyPointsVO list = new CompanyPointsVO();
        list.setAdvantageList(advantageList);
        list.setBenefitsList(benefitsList);
        list.setBonusList(bonusList);
        list.setSubsidyList(subsidyList);

        stopWatch.stop();
        log.info("获取数据字典项列表耗时: {} ms", stopWatch.getTotalTimeMillis());

        return GraceJSONResult.ok(list);
    }


// ==============================下面是给管理端使用的接口===================================
    /**
     * 创建一个数据字典记录
     */
    @PostMapping("/create")
    public GraceJSONResult create(@RequestBody @Valid DataDictionaryBO dataDictionaryBO) {

        dataDictionaryService.createOrUpdateDataDictionary(dataDictionaryBO);
        return GraceJSONResult.ok();
    }


    /**
     * 通过数据类型名称和项值分页查询数据字典列表
     *
     * @param typeName 数据类型名称
     * @param itemValue 项值，用于过滤查询
     * @param page 请求的页码，默认为1
     * @param limit 每页显示的记录数，默认为10
     * @return 包含分页数据字典列表的GraceJSONResult对象
     */
    @PostMapping("/list")
    public GraceJSONResult list(String typeName, String itemValue,
                                @RequestParam(defaultValue = "1")Integer page,
                                @RequestParam(defaultValue = "10")Integer limit) {

        PagedGridResult dataDictionaryList = dataDictionaryService.getDataDictionaryList(typeName, itemValue, page, limit);

        return GraceJSONResult.ok(dataDictionaryList);
    }



    /**
     * 修改数据字典记录
     *
     * @param dataDictionaryBO 数据字典业务对象
     * @return 成功响应
     */
    @PostMapping("/modify")
    public GraceJSONResult modify(@RequestBody @Valid DataDictionaryBO dataDictionaryBO) {

        // 防止黑客恶意传入空ID进行修改
        if (StringUtils.isBlank(dataDictionaryBO.getId())) {
            return GraceJSONResult.errorMsg("数据字典ID不能为空");
        }

        dataDictionaryService.createOrUpdateDataDictionary(dataDictionaryBO);
        return GraceJSONResult.ok();
    }


    /**
     * 根据数据字典的主键ID获取数据字典项的信息。
     *
     * @param dictId 数据字典项的ID
     * @return 包含请求结果的GraceJSONResult对象
     */
    @PostMapping("/item")
    public GraceJSONResult item(String dictId) {

        return GraceJSONResult.ok(
                dataDictionaryService.getDataDictionaryById(dictId)
        );
    }

    /**
     * 删除数据字典记录
     *
     * @param dictId 数据字典ID
     * @return 成功响应
     */
    @PostMapping("/delete")
    public GraceJSONResult delete(String dictId) {

        // 防止黑客恶意传入空ID进行删除
        if (StringUtils.isBlank(dictId)) {
            return GraceJSONResult.errorMsg("数据字典ID不能为空");
        }

        // 调用服务层方法删除数据字典记录
        // 如果删除失败，服务层会抛出异常，控制器会捕获
        // 并返回错误响应
        // 如果删除成功，则返回成功响应
        // 注意：这里假设服务层已经处理了异常情况
        // 如果没有处理异常，可能会导致500错误
        dataDictionaryService.deleteDataDictionaryById(dictId);
        return GraceJSONResult.ok();
    }


    /**
     * 缓存预热，将MySQL中的数据字典项列表存入Redis中，
     * 以便后续查询时直接从Redis中获取，减少数据库压力。
     * 该方法通常在应用启动时调用，确保缓存中有最新的数据字典项列表。
     */
    @PostMapping("/warmUp")
    public GraceJSONResult warmUp() {

        // 从数据库中得到最新的数据字典项列表，然后就循环放入redis
        List<DataDictionaryVO> dataDictionaryVOS = dataDictionaryService.warmUpDataDictionaryCache();
        for (DataDictionaryVO dataDictionaryVO : dataDictionaryVOS) {
            // 定义缓存key
            String key = DDKEY_PREFIX + dataDictionaryVO.getTypeCode();
            // 将数据字典项列表转换为JSON字符串存入Redis
            redis.set(key, GsonUtils.object2String(dataDictionaryVO.getDataDictionaryList()));
        }

        return GraceJSONResult.ok("这里是数据字典缓存预热接口，已完成缓存预热");
    }

}
