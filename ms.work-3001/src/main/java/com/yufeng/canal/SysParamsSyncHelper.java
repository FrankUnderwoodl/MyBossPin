


// 这里通过Canal监听的方式，现在改为通过监听zookeeper的方式来实现







// package com.yufeng.canal;
//
//
// import com.github.benmanes.caffeine.cache.Cache;
// import com.yufeng.base.BaseInfoProperties;
// import com.yufeng.model.co.SysParamsCO;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.BeanUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import top.javatool.canal.client.annotation.CanalTable;
// import top.javatool.canal.client.handler.EntryHandler;
//
// /**
//  * @author Lzm
//  * @CreateTime 2025年8月14日 08:46
//  * 问：EntryHandler怎么理解呢？
//  * 答：EntryHandler是Canal客户端的一个接口，用于处理从Canal接收到的数据库变更事件，数据库变更作为事件发布，EntryHandler作为订阅者处理，
//  *     当Canal监听到指定表（这里是data_dictionary）的数据变更时，
//  *     会调用实现了EntryHandler接口的类（这里是DataDictSyncHelper）
//  *     的方法来处理这些变更事件。
//  *     通过实现EntryHandler接口，可以自定义如何处理数据变更事件，比如同步到其他系统、记录日志等。
//  */
// @Slf4j
// @Component
// @CanalTable("sys_params") // 指定Canal监听的表名，这里是 "data_dictionary"
// public class SysParamsSyncHelper extends BaseInfoProperties implements EntryHandler<SysParamsCO> {
//
//
//     @Autowired
//     private Cache<String, Integer> resumeRefreshCountsCache;
//
//
//     // 数据字典列表的Redis键前缀
//     private static final String DDKEY_PREFIX = DATA_DICTIONARY_LIST_TYPECODE + ":";
//
//
//     /* 理解：每当数据字典表（data_dictionary）发生数据变更的时候，这里的参数就是那条新插入的数据字典记录，
//          通过实现EntryHandler接口的insert方法，可以自定义如何处理这条
//          新插入的数据字典记录，比如同步到其他系统、记录日志等 */
//     @Override
//     public void insert(SysParamsCO sysParamsCO) {
//     }
//
//
//
//     // 每当数据字典表（data_dictionary）发生更新，这里的两个参数分别表示更新前和更新后的数据字典记录
//     @Override
//     public void update(SysParamsCO before, SysParamsCO after) {
//
//         // 获取更新后的'最大简历刷新次数'
//         Integer maxCounts = after.getMax_resume_refresh_counts();
//
//         // 先更新到caffeine这个本地缓存中
//         resumeRefreshCountsCache.put(CACHE_MAX_RESUME_REFRESH_COUNTS, maxCounts);
//         log.info("=========== SysParamsSyncHelper监听到数据变更，更新最大简历刷新次数到本地缓存中 maxCounts={} ===========", resumeRefreshCountsCache.getIfPresent(CACHE_MAX_RESUME_REFRESH_COUNTS));
//
//         // 再更新到redis缓存中
//         redis.set(REDIS_MAX_RESUME_REFRESH_COUNTS, maxCounts + "");
//     }
//
//
//
//
//     @Override
//     public void delete(SysParamsCO sysParamsCO) {
//
//     }
//
//
//
//
//     // 将CO转成POJO
//     private SysParamsCO convertCOToPOJO(SysParamsCO sysParamsCO) {
//         if (sysParamsCO == null) {
//             return null;
//         }
//         SysParamsCO newSysParamsCO = new SysParamsCO();
//         // 使用Spring的BeanUtils工具类进行属性复制
//         BeanUtils.copyProperties(sysParamsCO, newSysParamsCO);
//         return newSysParamsCO;
//     }
// }
