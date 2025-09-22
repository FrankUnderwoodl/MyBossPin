package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.DataDictionaryBO;
import com.yufeng.enums.YesOrNo;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.DataDictionaryMapper;
import com.yufeng.mapper.DataDictionaryMapperCustom;
import com.yufeng.model.pojo.DataDictionary;
import com.yufeng.service.DataDictionaryService;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.DataDictionaryVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 数据字典表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Slf4j
@Service
public class DataDictionaryServiceImpl extends BaseInfoProperties implements DataDictionaryService {

    @Autowired
    private DataDictionaryMapper dataDictionaryMapper;

    @Autowired
    private DataDictionaryMapperCustom dataDictionaryMapperCustom;


// ==============================下面是给移动端使用的接口===================================


    /**
     * 根据字典码获得该分类下的所有数据字典项列表
     *
     * @param typeCode 数据字典码
     * @return 包含数据字典项列表的GraceJSONResult对象
     */
    @Override
    public List<DataDictionary> getDataByCode(String typeCode) {

        // 使用MyBatis-Plus的selectList方法查询数据字典项列表
        return dataDictionaryMapper.selectList(
                new QueryWrapper<DataDictionary>()
                        .eq("type_code", typeCode)
                        .eq("enable", YesOrNo.YES.type) // 只查询启用的字典项
                        .orderByAsc("sort")
                        .orderByAsc("item_key")
        );
    }


    /**
     * 根据字典项的keys获取数据字典项的value(比如通过‘Salary_Money’获取到‘年底双薪’)
     *
     * @param keys 字典项的keys(可以传一个或多个字符串)
     * @return 包含数据字典项列表的List对象
     */
    @Override
    public List<DataDictionary> getItemsByKeys(String... keys) {

        // SELECT * FROM data_dictionary
        // WHERE enable = ?
        // AND item_key IN (?, ?, ...)
        return dataDictionaryMapper.selectList(
                new QueryWrapper<DataDictionary>()
                        .eq("enable", YesOrNo.YES.type) // 只查询启用的字典项
                        .in("item_key", keys) // 根据传入的keys查询
        );
    }

// ==============================下面是给管理端使用的接口===================================

    /**
     * 根据主键id获取数据字典
     *
     * @param id 数据字典的主键ID
     * @return 数据字典对象
     */
    @Override
    public DataDictionary getDataDictionaryById(String id) {
        // 使用MyBatis-Plus的selectById方法根据ID查询数据字典
        return dataDictionaryMapper.selectById(id);
    }


    /**
     * 删除数据字典记录
     *
     * @param dictId 数据字典ID
     */
    @Override
    @Transactional
    public void deleteDataDictionaryById(String dictId) {

        // 使用MyBatis-Plus的deleteById方法删除数据字典记录
        int rowsAffected = dataDictionaryMapper.deleteById(dictId);

        // 如果没有删除任何记录，抛出异常
        if (rowsAffected == 0) {
            GraceException.doException(ResponseStatusEnum.DATA_DICT_DELETE_ERROR);
        }
    }

    /**
     * 创建或更新数据字典
     *
     * @param dataDictionaryBO 数据字典业务对象
     */
    @Override
    @Transactional
    public void createOrUpdateDataDictionary(DataDictionaryBO dataDictionaryBO) {

        // 创建一个pojo(你可以当做是在MySQL中的一条记录)，将业务对象的属性复制到数据字典pojo对象中
        DataDictionary dataDictionary = new DataDictionary();
        BeanUtils.copyProperties(dataDictionaryBO, dataDictionary);

        // 判断前端是要创建记录还是更新记录
        if (StringUtils.isBlank(dataDictionary.getId())) { // 如果传过来BO的ID为空，说明是创建操作

            // 需要先判断传过来的字典类型是否在数据库中是否已经存在(通过item_key和item_value来判断)
            DataDictionary existingDict = dataDictionaryMapper.selectOne(
                    new QueryWrapper<DataDictionary>()
                            .eq("item_key", dataDictionary.getItemKey())
                            .eq("item_value", dataDictionary.getItemValue())
            );

            if (existingDict != null) { // 如果已经存在，抛出异常或返回错误信息
                GraceException.doException(ResponseStatusEnum.DATA_DICT_EXIST_ERROR);
            }

            // 正式把记录插入到数据库中
            dataDictionaryMapper.insert(dataDictionary);

        } else { // 如果ID不为空，说明是更新操作
            // 正式更新数据字典记录
            dataDictionaryMapper.updateById(dataDictionary);
        }
    }


    @Override
    public PagedGridResult getDataDictionaryList(String typeName, String itemValue, Integer page, Integer limit) {

        PageHelper.startPage(page, limit);

        // 底层的SQL语句：
        /*
        SELECT * FROM data_dictionary
        WHERE type_name LIKE CONCAT('%', ?, '%')
        AND item_value LIKE CONCAT('%', ?, '%')
        ORDER BY type_code ASC, sort ASC
        · 举个例子：参数最后会拼接成'%typeName%'、'%itemValue%'
        · 如果传入的参数为空或null，MyBatis-Plus会自动忽略相应的查询条件。
        */
        List<DataDictionary> adminList = dataDictionaryMapper.selectList(
                new QueryWrapper<DataDictionary>()
                        .like("type_name", typeName)
                        .like("item_value", itemValue)
                        .orderByAsc("type_code")
                        .orderByAsc("sort")
                        .orderByAsc("item_key")
        );

        return setterPagedGrid(adminList, page); // 将查询结果封装成PagedGridResult对象并返回
    }


    @Override
    public List<DataDictionaryVO> warmUpDataDictionaryCache() {

        return dataDictionaryMapperCustom.getDataDictionaryListByTypeCode();
    }
}
