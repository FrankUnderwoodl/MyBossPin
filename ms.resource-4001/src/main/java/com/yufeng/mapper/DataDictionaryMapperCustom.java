package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.DataDictionary;
import com.yufeng.model.vo.DataDictionaryVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 数据字典表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository
public interface DataDictionaryMapperCustom extends BaseMapper<DataDictionary> {

    // 获取数据字典列表
    // 每个typeCode对应一个List<DataDictionaryVO>
    public List<DataDictionaryVO> getDataDictionaryListByTypeCode();
}
