package com.yufeng.service;

import com.yufeng.model.bo.DataDictionaryBO;
import com.yufeng.model.pojo.DataDictionary;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.DataDictionaryVO;

import java.util.List;

/**
 * <p>
 * 数据字典表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface DataDictionaryService {

// ==============================下面是给移动端使用的接口===================================


    /**
     * 根据字典码获得该分类下的所有数据字典项列表
     *
     * @param typeCode 数据字典码
     * @return 包含数据字典项列表的GraceJSONResult对象
     */
    public List<DataDictionary> getDataByCode(String typeCode);


    /**
     * 根据字典项的keys获取数据字典项列表
     *
     * @param keys 字典项的keys(可以传一个或多个字符串)
     * @return 包含数据字典项列表的List对象
     */
    public List<DataDictionary> getItemsByKeys(String... keys);

// ==============================下面是给管理端使用的接口===================================

    /**
     * 根据主键id获取数据字典
     */
    public DataDictionary getDataDictionaryById(String id);


    /**
     * 创建或更新数据字典
     */
    public void createOrUpdateDataDictionary(DataDictionaryBO dataDictionaryBO);


    /**
     * 获取数据字典列表
     *
     * @param typeName  字典类型名称
     * @param itemValue 字典项值
     * @param page      页码
     * @param limit     每页条数
     */
    public PagedGridResult getDataDictionaryList(String typeName, String itemValue,
                                                 Integer page, Integer limit);




    /**
     * 删除数据字典记录
     *
     * @param dictId 数据字典ID
     */
    public void deleteDataDictionaryById(String dictId);


    /**
     * 缓存预热
     */
    public List<DataDictionaryVO> warmUpDataDictionaryCache();
}
