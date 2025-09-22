package com.yufeng.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yufeng.model.pojo.Industry;
import com.yufeng.model.vo.TopIndustryWithThirdListVO;

import java.util.List;

/**
 * <p>
 * 行业表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface IndustryService extends IService<Industry> {


    /**
     * 获取三级行业列表
     * 供APP端使用
     *
     * @return 返回三级行业列表
     */
    public List<Industry> getThirdListByTop(String topIndustryId);


    /**
     * 通过行业名称来查询是否在数据库中已经存在该行业节点
     *
     * @param nodeName 行业名称
     * @return true表示存在，false表示不存在
     */
    public boolean getIndustryByName(String nodeName);


    /**
     * 创建一个新的行业节点
     *
     * @param industry 行业信息
     */
    public void createIndustry(Industry industry);



    /**
     * 获取顶级行业列表
     *
     * @return 返回顶级行业列表
     */
    public List<Industry> getTopIndustryList();



    /**
     * 根据行业ID获取子行业列表(如果id为0，则返回所有顶级行业)
     *
     * @param industryId 行业ID
     * @return 返回子行业列表
     */
    public List<Industry> getChildrenIndustryList(String industryId);



    /**
     * 更新行业信息
     *
     * @param industry 行业信息
     */
    public void updateIndustry(Industry industry);



    /**
     * 获取子行业数量
     *
     * @param industryId 行业ID
     * @return 返回子行业数量
     */
    public Long getChildrenIndustryCounts(String industryId);



    /**
     * 通过三级行业ID获取顶级行业ID
     *
     * @param thirdIndustryId 三级行业ID
     * @return 返回顶级行业ID
     */
    public String getTopIndustryId(String thirdIndustryId);



    /**
     * 获取所有的三级行业列表with顶级行业的id
     *
     * @return 返回所有的三级行业列表with顶级行业的id
     */
    public List<TopIndustryWithThirdListVO> getAllThirdIndustryList();
}
