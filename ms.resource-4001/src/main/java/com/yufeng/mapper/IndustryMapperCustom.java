package com.yufeng.mapper;

import com.yufeng.model.pojo.Industry;
import com.yufeng.model.vo.TopIndustryWithThirdListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行业表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository
public interface IndustryMapperCustom {

    // 这里的@Param的作用是将方法参数绑定到SQL语句中的参数
    // 比如在SQL语句中使用#{paramMap}来引用这个参数
    @SuppressWarnings("MybatisXMapperMethodInspection")
    public List<Industry> getThirdIndustryByTop(@Param("paramMap") Map<String, String> paramMap);



    /**
     * 通过行业名称来查询是否在数据库中已经存在该行业节点
     */
    public String getTopIndustryId(@Param("paramMap") Map<String, String> paramMap);


    // 获取所有的三级行业列表with顶级行业的id
    public List<TopIndustryWithThirdListVO> getAllThirdIndustryList();

}
