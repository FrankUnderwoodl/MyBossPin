package com.yufeng.canal;


import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.co.DataDictionaryCO;
import com.yufeng.model.pojo.DataDictionary;
import com.yufeng.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年8月14日 08:46
 * 问：EntryHandler怎么理解呢？
 * 答：EntryHandler是Canal客户端的一个接口，用于处理从Canal接收到的数据库变更事件，数据库变更作为事件发布，EntryHandler作为订阅者处理，
 *     当Canal监听到指定表（这里是data_dictionary）的数据变更时，
 *     会调用实现了EntryHandler接口的类（这里是DataDictSyncHelper）
 *     的方法来处理这些变更事件。
 *     通过实现EntryHandler接口，可以自定义如何处理数据变更事件，比如同步到其他系统、记录日志等。
 */
@Slf4j
@Component
@CanalTable("data_dictionary") // 指定Canal监听的表名，这里是 "data_dictionary"
public class DataDictSyncHelper extends BaseInfoProperties implements EntryHandler<DataDictionaryCO> {

    // 数据字典列表的Redis键前缀
    private static final String DDKEY_PREFIX = DATA_DICTIONARY_LIST_TYPECODE + ":";


    /* 理解：每当数据字典表（data_dictionary）发生数据变更的时候，这里的参数就是那条新插入的数据字典记录，
         通过实现EntryHandler接口的insert方法，可以自定义如何处理这条
         新插入的数据字典记录，比如同步到其他系统、记录日志等 */
    @Override
    public void insert(DataDictionaryCO dataDictionaryCO) {
        // log.info("数据字典表插入数据：{}", dataDictionaryCO);

        // 定义缓存key
        String key = DDKEY_PREFIX + dataDictionaryCO.getType_code();

        List<DataDictionary> redisDDList;
        // 先查询redis中是否含有该数据字典List的json数组
        String dataDictionaryList = redis.get(key);
        if (StringUtils.isBlank(dataDictionaryList)) {
            // 如果不存在，则先new一个List，准备添加新key存入redis中
            redisDDList = new ArrayList<>();
        } else {
            // 如果存在该数据字典List，则将该json数组转成List<DataDictionary>
            redisDDList = GsonUtils.stringToListAnother(dataDictionaryList, DataDictionary.class);
        }

        // 将新插入的数据字典项添加到List中
        DataDictionary dataDictionary = convertCOToPOJO(dataDictionaryCO);
        redisDDList.add(dataDictionary);

        // 最后将更新后的List存入redis中
        redis.set(key, GsonUtils.object2String(redisDDList));
    }



    // 每当数据字典表（data_dictionary）发生更新，这里的两个参数分别表示更新前和更新后的数据字典记录
    @Override
    public void update(DataDictionaryCO before, DataDictionaryCO after) {

        // 定义缓存key
        String key = DDKEY_PREFIX + after.getType_code();

        // 将redis里面json数组转成List<DataDictionary>(注意redis里可能没有该List)
        String dataDictionaryList = redis.get(key);
        if (StringUtils.isBlank(dataDictionaryList)) { // isBlank三种情况：""、null、"   "都返回true，
            // 如果redis中都没有该数据字典List，直接返回
            return;
        }
        List<DataDictionary> redisDDList = GsonUtils.stringToListAnother(dataDictionaryList, DataDictionary.class);

        // 如果List为空，直接返回
        if (redisDDList.isEmpty()) {
            return;
        }

        // 正式更新数据字典项(根据主键id进行更新)
        DataDictionary dataDictionary = convertCOToPOJO(after);
        for (int i = 0; i < redisDDList.size(); i++) {
            if (redisDDList.get(i).getId().equals(dataDictionary.getId())) {
                redisDDList.set(i, dataDictionary); // 更新对应的元素
                break; // 找到后退出循环
            }
        }

        // 把新的List存入redis中 // 注意：如果没有找到对应的元素，redisDDList将保持不变，不会进行任何更新操作
        redis.set(key, GsonUtils.object2String(redisDDList));

    }




    @Override
    public void delete(DataDictionaryCO dataDictionaryCO) {

        // 将redis里面json数组转成List<DataDictionary>(注意redis里可能没有该List)
        String key = DDKEY_PREFIX + dataDictionaryCO.getType_code();
        String dataDictionaryList = redis.get(key);
        if (StringUtils.isBlank(dataDictionaryList)) { // isBlank三种情况：""、null、"   "都返回true，
            // 如果redis中都没有该数据字典List，直接返回
            return;
        }
        List<DataDictionary> redisDDList = GsonUtils.stringToListAnother(dataDictionaryList, DataDictionary.class);

        // 如果List为空，直接返回
        if (redisDDList.isEmpty()) {
            return;
        }

        // 正式删除数据字典项
        DataDictionary dataDictionary = convertCOToPOJO(dataDictionaryCO);
        redisDDList.removeIf(dd -> dd.getId().equals(dataDictionary.getId())); // removeIf：如果List中没有该元素，则不会删除任何元素

        // 把新的List存入redis中
        redis.set(key, GsonUtils.object2String(redisDDList));
    }




    // 将一个CO转成pojo
    private DataDictionary convertCOToPOJO(DataDictionaryCO dataDictionaryCO) {
        DataDictionary dataDictionary = new DataDictionary();
        dataDictionary.setId(dataDictionaryCO.getId());
        dataDictionary.setTypeCode(dataDictionaryCO.getType_code());
        dataDictionary.setItemKey(dataDictionaryCO.getItem_key());
        dataDictionary.setItemValue(dataDictionaryCO.getItem_value());
        dataDictionary.setSort(dataDictionaryCO.getSort());
        dataDictionary.setEnable(dataDictionaryCO.getEnable());
        return dataDictionary;
    }
}
