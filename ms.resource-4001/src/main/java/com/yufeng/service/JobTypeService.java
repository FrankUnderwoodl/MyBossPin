
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.service;

import com.yufeng.model.pojo.JobType;
import com.yufeng.model.vo.JobTypeSecondAndThirdVO;

import java.util.List;

/**
 * <p>
 * 职位类别 服务类
 * </p>
 *
 * @author 风间影月
 * @since 2022-07-12
 */
public interface JobTypeService {

    /**
     * 获得所有顶级(一级)分类列表
     * @return
     */
    public List<JobType> getTopJobTypeList();

    /**
     * 根据顶级分类获得第三级分类列表
     * @param topJobTypeId
     * @return
     */
    public List<JobType> getThirdListByTop(String topJobTypeId);

    /**
     * 根据顶级分类获得二级分类包含三级分类列表的集合
     * @param topJobTypeId
     * @return
     */
    public List<JobTypeSecondAndThirdVO> getSecondAndThirdListByTop(String topJobTypeId);

    /**
     * 根据分类id获得子分类列表
     * @param jobTypeId
     * @return
     */
    public List<JobType> getChildrenJobTypeList(String jobTypeId);

    /**
     * 获得节点下子节点的数量
     * @param jobTypeId
     * @return
     */
    public Long getChildrenJobTypeCounts(String jobTypeId);

    /**
     * 根据名称获得职位类型
     * @param name
     * @return
     */
    public boolean getJobTypeIsExistByName(String name);

    /**
     * 根据id查询职位类型
     * @param id
     * @return
     */
    public JobType getJobTypeById(String id);

    /**
     * 创建职位类型分类
     * @param jobType
     * @return
     */
    public void createJobType(JobType jobType);

    /**
     * 修改职位类型
     * @param jobType
     */
    public void updateJobType(JobType jobType);

    /**
     * 删除职位类型
     * @param id
     */
    public void deleteJobType(String id);
}
