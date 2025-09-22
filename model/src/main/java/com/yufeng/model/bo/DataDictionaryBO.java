package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

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
@NoArgsConstructor
@AllArgsConstructor
public class DataDictionaryBO {

    private String id;

    @NotBlank(message = "字典类型编码不能为空")
    private String typeCode;

    @NotBlank(message = "字典类型名称不能为空")
    private String typeName;

    @NotBlank(message = "字典项键不能为空")
    private String itemKey;

    @NotBlank(message = "字典项值不能为空")
    private String itemValue;

    private Integer sort;

    private String icon;
    private Boolean enable;

}
