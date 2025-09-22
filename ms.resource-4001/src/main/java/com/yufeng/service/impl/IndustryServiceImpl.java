package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yufeng.mapper.IndustryMapper;
import com.yufeng.mapper.IndustryMapperCustom;
import com.yufeng.model.pojo.Industry;
import com.yufeng.service.IndustryService;
import com.yufeng.model.vo.TopIndustryWithThirdListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 行业表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class IndustryServiceImpl extends ServiceImpl<IndustryMapper, Industry> implements IndustryService {

    @Autowired
    private IndustryMapperCustom industryMapperCustom;

// ====================================下面是给APP端使用的接口==============================================

    /**
     * 获取三级行业列表
     * 供APP端使用
     *
     * @param topIndustryId 顶级行业ID
     * @return 返回三级行业列表
     */
    @Override
    public List<Industry> getThirdListByTop(String topIndustryId) {

        HashMap<String, String> map = new HashMap<>();
        map.put("topIndustryId", topIndustryId);

        // 调用自定义的Mapper方法来获取三级行业列表
        List<Industry> industryList = industryMapperCustom.getThirdIndustryByTop(map);

        return industryList != null ? industryList : Collections.emptyList();
    }


    // 给你一个三级行业ID，返回它的顶级行业
    @Override
    public String getTopIndustryId(String thirdIndustryId) {

        HashMap<String, String> map = new HashMap<>();
        map.put("thirdIndustryId", thirdIndustryId);

        return industryMapperCustom.getTopIndustryId(map);
    }


    // ====================================下面是给后台管理端使用的接口==============================================

    /**
     * 通过行业名称来查询是否在数据库中已经存在该行业节点
     *
     * @param nodeName 行业名称
     * @return true表示存在，false表示不存在
     */
    @Override
    public boolean getIndustryByName(String nodeName) {

        // 使用MyBatis-Plus的查询方法来检查行业名称是否存在
        Industry industry = baseMapper.selectOne(new QueryWrapper<Industry>()
                .eq("name", nodeName));

        return industry != null;
    }


    /**
     * 创建一个新的行业节点
     *
     * @param industry 行业信息
     */
    @Override
    @Transactional
    public void createIndustry(Industry industry) {
        baseMapper.insert(industry);
    }


    /**
     * 获取顶级行业列表
     *
     * @return 返回顶级行业列表Î
     */
    @Override
    public List<Industry> getTopIndustryList() {

        // 查询father_id为0的顶级行业，并按sort字段升序排列
        // List<Industry> list = baseMapper.selectList(new QueryWrapper<Industry>()
        //         .eq("father_id", 0)
        //         .orderByAsc("sort")
        // );

        List<Industry> list = this.getChildrenIndustryList("0");

        return list;
    }


    /**
     * 获取当前分类下的子行业列表
     * 如果是默认的，则查询顶级行业
     *
     * @param industryId 行业ID
     * @return 返回子行业列表
     */
    @Override
    public List<Industry> getChildrenIndustryList(String industryId) {

        // 这段SQL如果并发量大的话，会有什么问题呢？
       /*  father_id没加索引的话：
        ①MySQL会进行全表扫描，O(n)去找到father_id==industryId的信息
        ②找到之后，还需要返回到MySQL的缓存中进行排序 */
        List<Industry> list = baseMapper.selectList(new QueryWrapper<Industry>()
                .eq("father_id", industryId)
                .orderByAsc("sort")
        );

        return list;
    }


    /**
     * 更新行业信息
     *
     * @param industry 行业信息
     */
    @Override
    @Transactional
    public void updateIndustry(Industry industry) {

        // 这里不用判空处理，因为前端已经做了非空校验
        // 使用MyBatis-Plus的更新方法来更新行业信息
        baseMapper.updateById(industry);
    }



    /**
     * 获取子行业数量
     *
     * @param industryId 行业ID
     * @return 返回子行业数量
     */
    @Override
    public Long getChildrenIndustryCounts(String industryId) {

        // 底层的SQL代码：select count(*) from industry where father_id = industryId;
        // 理解：MySQL会对count(*)进行优化，COUNT(*) 只统计行数，MySQL 只需要知道这行存在即可，不会传输完整数据
        Long counts = baseMapper.selectCount(new QueryWrapper<Industry>()
                .eq("father_id", industryId)
        );

        return counts;
    }


    @Override
    public List<TopIndustryWithThirdListVO> getAllThirdIndustryList() {

        return industryMapperCustom.getAllThirdIndustryList();
    }
}
