package com.yufeng.model.vo;

import com.yufeng.model.pojo.DataDictionary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * <p>
 * 数据字典表
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DataDictionaryVO {

    private String typeCode;
    private List<DataDictionary> dataDictionaryList;

}
