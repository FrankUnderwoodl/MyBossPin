package com.yufeng.model.vo;

import com.yufeng.model.pojo.Industry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author Lzm
 * 解释一下：顶级行业，with三级列表的VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TopIndustryWithThirdListVO {

    private String topId;
    private List<Industry> thirdIndustryList;
}
