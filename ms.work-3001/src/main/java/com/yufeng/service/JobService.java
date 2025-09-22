package com.yufeng.service;

import com.yufeng.model.bo.EditJobBO;
import com.yufeng.model.bo.SearchJobsBO;
import com.yufeng.enums.JobStatus;
import com.yufeng.model.pojo.Job;
import com.yufeng.utils.PagedGridResult;

/**
 * <p>
 * HR发布的职位表 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface JobService {



    /**
     * 修改职位信息
     */
    public void modifyJobDetail(EditJobBO editJobBO);




    /**
     * HR查询自己发布的职位列表
     */
    public PagedGridResult queryJobList(String hrId, String companyId, Integer page, Integer pageSize, Integer status);




    /**
     * 查询职位详情
     */
    public Job getJobDetail(String hrId, String companyId, String jobId);



    /**
     * 修改职位状态
     */
    public void modifyJobStatus(String hrId, String companyId, String jobId, JobStatus jobStatus);




    /**
     * 候选人搜索职位列表
     */
    public PagedGridResult searchJobs(SearchJobsBO searchJobsBO, Integer page, Integer limit);




}
