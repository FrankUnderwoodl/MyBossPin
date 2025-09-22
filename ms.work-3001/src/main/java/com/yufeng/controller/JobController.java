package com.yufeng.controller;

import com.yufeng.api.interceptor.JWTUserInterceptor;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.EditJobBO;
import com.yufeng.model.bo.SearchJobsBO;
import com.yufeng.enums.JobStatus;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.pojo.Job;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.JobService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.PagedGridResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/job")
public class JobController extends BaseInfoProperties {

    @Autowired
    private JobService jobService;


    /**
     * 新增或者修改职位
     */
    @PostMapping("/modify")
    public GraceJSONResult modify(@Valid @RequestBody EditJobBO editJobBO) {

        // 把数据传给service层
        jobService.modifyJobDetail(editJobBO);
        return GraceJSONResult.ok();
    }


    /**
     * HR查询自己发布的职位列表
     */
    @PostMapping("/hr/jobList")
    public GraceJSONResult hrJobList(String hrId,
                                     String companyId,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit,
                                     Integer status) {
        // 判空处理
        if (hrId == null) {
            return GraceJSONResult.errorMsg("hrId不能为空");
        }

        // 调用service查询
        PagedGridResult result = jobService.queryJobList(hrId, companyId, page, limit, status);

        return GraceJSONResult.ok(result);
    }


    /**
     * 公司查询自己发布的职位列表(供给Saas端使用)
     */
    @PostMapping("/jobList")
    public GraceJSONResult jobListCompany(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit) {

        // 通过jwt，获取companyId
        Users users = JWTUserInterceptor.currentUser.get();

        // 调用service查询
        String companyId = users.getHrInWhichCompanyId();
        PagedGridResult result = jobService.queryJobList(null, companyId, page, limit, null);

        return GraceJSONResult.ok(result);
    }


    /**
     * HR查询自己发布的职位详情页
     */
    @PostMapping("/hr/jobDetail")
    public GraceJSONResult hrJobDetail(String hrId, String companyId, String jobId) {
        // 判空处理
        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.errorMsg("参数不能为空");
        }

        Job job;
        // 先从redis中查询，如果没有再从数据库中查询
        String key = REDIS_JOB_DETAIL + ":" +
                companyId + ":" + hrId + ":" + jobId;
        String jobDetailStr = redis.get(key);

        if (StringUtils.isBlank(jobDetailStr)) { // 缓存中没有，则从数据库中查询
            // 从数据库中查询
            job = jobService.getJobDetail(hrId, companyId, jobId);
            if (job == null) {
                return GraceJSONResult.errorMsg("职位不存在");
            } else {
                // 放入缓存
                redis.set(key, GsonUtils.object2String(job), 60 * 60 * 24); // 24小时
            }
        } else { // 缓存中有，则直接从缓存中获取
            job = GsonUtils.stringToBean(jobDetailStr, Job.class);
        }

        return GraceJSONResult.ok(job);
    }


    /**
     * 管理员或者公司查询职位详情页(直接查数据库，你也可以设置进缓存)
     */
    @PostMapping("/admin/jobDetail")
    public GraceJSONResult adminOrCompanyJobDetail(String jobId) {
        // 判空处理
        if (StringUtils.isBlank(jobId)) {
            return GraceJSONResult.errorMsg("参数不能为空");
        }

        // // 通过JWT获取company_id
        // Users users = JWTUserInterceptor.currentUser.get();
        // String companyId = users.getHrInWhichCompanyId();

        // 调用service
        Job jobDetail = jobService.getJobDetail(null, null, jobId);
        return GraceJSONResult.ok(jobDetail);
    }


    /**
     * HR关闭职位
     */
    @PostMapping("/close")
    public GraceJSONResult jobClose(String hrId, String companyId, String jobId) {
        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.errorMsg("参数不能为空");
        }

        // 调用service关闭职位
        jobService.modifyJobStatus(hrId, companyId, jobId, JobStatus.CLOSE);

        return GraceJSONResult.ok();
    }


    /**
     * HR开启职位
     */
    @PostMapping("/open")
    public GraceJSONResult jobOpen(String hrId, String companyId, String jobId) {
        if (StringUtils.isBlank(hrId) || StringUtils.isBlank(companyId) || StringUtils.isBlank(jobId)) {
            return GraceJSONResult.errorMsg("参数不能为空");
        }

        // 调用service关闭职位
        jobService.modifyJobStatus(hrId, companyId, jobId, JobStatus.OPEN);

        return GraceJSONResult.ok();
    }


    /**
     * @describe 候选人搜索职位列表
     * ⚠️注意：现在用数据库进行查询，后续会使用ES进行查询
     *
     * var searchBO = {
     * "jobName": "",
     * "jobType": "",
     * "city": this.chosenLocationArea,
     * "industry": "",
     * "beginSalary": 0,
     * "endSalary": 0,
     * };
     */
    @PostMapping("/searchJobs")
    public GraceJSONResult searchJobs(@RequestBody SearchJobsBO searchJobsBO,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer limit) {

        // 调用service查询
        PagedGridResult result = jobService.searchJobs(searchJobsBO, page, limit);
        //


        return GraceJSONResult.ok(result);
    }
}
