package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.api.feign.CompanyMSFeign;
import com.yufeng.api.feign.UserInfoMSFeign;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.EditJobBO;
import com.yufeng.model.bo.SearchBO;
import com.yufeng.model.bo.SearchJobsBO;
import com.yufeng.enums.JobStatus;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.mapper.JobMapper;
import com.yufeng.model.pojo.Job;
import com.yufeng.service.JobService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.CompanyInfoVO;
import com.yufeng.model.vo.SearchJobsVO;
import com.yufeng.model.vo.UsersVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * HR发布的职位表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service
public class JobServiceImpl extends BaseInfoProperties implements JobService {


    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private UserInfoMSFeign userInfoMSFeign;

    @Autowired
    private CompanyMSFeign companyMSFeign;


    /**
     * Modifies the details of a job based on the provided EditJobBO object.
     *
     * @param editJobBO the EditJobBO object containing the updated job details, including
     *                  id, hrId, companyId, jobName, jobType, expYears, edu, beginSalary,
     *                  endSalary, monthlySalary, jobDesc, tags, city, and address
     */
    @Override
    @Transactional
    public void modifyJobDetail(EditJobBO editJobBO) {

        // 看到BO就要转成一个PO
        Job job = new Job();
        BeanUtils.copyProperties(editJobBO, job);
        job.setUpdatedTime(LocalDateTime.now());

        // 需要判断一下id，是insert还是update
        if (StringUtils.isBlank(editJobBO.getId())) {
            // 插入数据
            job.setStatus(JobStatus.OPEN.type);  // 设置简历的状态为：开放状态
            job.setCreateTime(LocalDateTime.now());
            jobMapper.insert(job);
        } else {
            // 更新数据
            jobMapper.update(job, new QueryWrapper<Job>()
                    .eq("id", editJobBO.getId())
                    .eq("hr_id", editJobBO.getHrId())
                    .eq("company_id", editJobBO.getCompanyId()
                    ));
        }

        // 因为数据已经改变了，所以需要删除缓存
        redis.del(REDIS_JOB_DETAIL + ":" +
                editJobBO.getCompanyId() + ":" + editJobBO.getHrId() + ":" + editJobBO.getId());
    }


    /**
     * HR查询自己发布的职位列表
     */
    @Override
    public PagedGridResult queryJobList(String hrId, String companyId, Integer page, Integer pageSize, Integer status) {

        // 开启分页功能，其实就是将参数存入ThreadLocal中，后续的Mybatis的查询会自动获取
        PageHelper.startPage(page, pageSize);

        // 构建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(hrId)) { // HR的ID有可能为空，因为管理员也可以查询
            queryWrapper.eq("hr_id", hrId);
        }
        queryWrapper.eq("company_id", companyId); // 公司ID不能为空
        if (status != null) { // 职位状态有可能为空
            if (status == JobStatus.OPEN.type || status == JobStatus.CLOSE.type || status == JobStatus.DELETE.type) {
                queryWrapper.eq("status", status);
            }
        }
        queryWrapper.orderByDesc("updated_time");

        // 执行查询
        return setterPagedGrid(jobMapper.selectList(queryWrapper), page);
    }


    /**
     * 查询职位详情
     */
    @Override
    public Job getJobDetail(String hrId, String companyId, String jobId) {
        Integer[] statusArr = {JobStatus.OPEN.type, JobStatus.CLOSE.type, JobStatus.DELETE.type};

        // 构建查询条件
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("id", jobId);

        // 这里为啥要增加判断？
        // 可以让admin/company来调用查询，只需增加接口即可，提高service的公用性
        if (StringUtils.isNotBlank(hrId)) {
            wrapper.eq("hr_id", hrId);
        }
        if (StringUtils.isNotBlank(companyId)) {
            wrapper.eq("company_id", companyId);
        }

        // 状态只能是开放、关闭、删除
        wrapper.in("status", statusArr);

        return jobMapper.selectOne(wrapper);
    }


    /**
     * 修改职位状态
     */
    @Override
    @Transactional
    public void modifyJobStatus(String hrId, String companyId, String jobId, JobStatus jobStatus) {

        // 你他妈就记住了：凡是要进行CRUD，都要pojo对象！
        Job job = new Job();
        job.setStatus(jobStatus.type);
        job.setUpdatedTime(LocalDateTime.now());

        jobMapper.update(job, new QueryWrapper<Job>()
                .eq("id", jobId)
                .eq("hr_id", hrId)
                .eq("company_id", companyId)
        );

        // 因为数据已经改变了，所以需要删除缓存
        redis.del(REDIS_JOB_DETAIL + ":" +
                companyId + ":" + hrId + ":" + jobId);
    }


    /**
     * 候选人搜索职位列表
     *
     * @return
     */
    @Override
    public PagedGridResult searchJobs(SearchJobsBO searchJobsBO, Integer page, Integer limit) {
        // 先开启分页
        PageHelper.startPage(page, limit);

        //* 1.简历、职位相关的
        // 把BO里面的数据先全部取出来
        String jobName = searchJobsBO.getJobName();
        String jobType = searchJobsBO.getJobType();
        String city = searchJobsBO.getCity();
        Integer beginSalary = searchJobsBO.getBeginSalary();
        Integer endSalary = searchJobsBO.getEndSalary();

        // 构建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        // 第一个条件：只能查询开放的职位
        queryWrapper.eq("status", JobStatus.OPEN.type);
        // 拼接职位名称
        if (StringUtils.isNotBlank(jobName)) {
            queryWrapper.like("job_name", jobName);
        }
        // 拼接职位类型
        if (StringUtils.isNotBlank(jobType)) {
            queryWrapper.like("job_type", jobType);
        }
        // 拼接城市
        if (StringUtils.isNotBlank(city)) {
            queryWrapper.like("city", city);
        }
        // 拼接薪资范围
        if (beginSalary > 0 && endSalary > 0) {
            // queryWrapper.ge("end_salary", beginSalary); // 用户所期望的最低薪资符合职位的最高薪资即可
            // queryWrapper.and(
            //     qw -> qw.or(
            //             subQW -> subQW.ge("end_salary", beginSalary)
            //     ).or(
            //             subQW -> subQW.between("begin_salary", beginSalary, endSalary)
            //     ).or(
            //             subQW -> subQW.le("begin_salary", endSalary)
            //     ));

            // WHERE status = 1
            //   AND (end_salary >= 5000
            //        OR begin_salary BETWEEN 5000 AND 8000
            //        OR begin_salary <= 8000)

            // 这里底层的SQL语句是：

            // 构建期望的薪资查询：
            queryWrapper.and(
                    qw -> qw.or(
                                    // 职位最低薪资begin ＜= 求职薪资begin ＜= 职位最高薪资end
                                    subQW -> subQW.le("begin_salary", beginSalary)
                                                                     .ge("end_salary", beginSalary)
                            )
                            .or(
                                    // 职位最低薪资begin ＜= 求职薪资end ＜= 职位最高薪资end
                                    subQW -> subQW.le("begin_salary", endSalary)
                                                                    .ge("end_salary", beginSalary)
                            )
                            .or(
                                    // 求职薪资begin <= 职位最低薪资begin && 求职薪资end >= 职位最高薪资end
                                    subQW -> subQW.ge("begin_salary", beginSalary)
                                                                    .le("end_salary", endSalary)
                            )

            );
        }
        // 按照更新时间倒序排序
        queryWrapper.orderByDesc("updated_time");
        // 执行查询
        List<Job> jobList = jobMapper.selectList(queryWrapper);
        // 判断从MySQL查询出来的结果，如果为空，直接返回。
        if (jobList == null || jobList.isEmpty()) {
            return new PagedGridResult();
        }


        //* 遍历每个job，获取每个job的hrId、companyId，进而查出对应job的头像、公司logo等信息
        List<String> hrIdList = new ArrayList<>();
        List<String> companyIdList = new ArrayList<>();
        List<SearchJobsVO> jobsVOList = new ArrayList<>();

        // 遍历jobList，将每个Job的hrId、companyId存入List中，并转换成jobsVO
        for (Job job : jobList) {
            // 取出每个job的hrId、companyId
            hrIdList.add(job.getHrId());
            companyIdList.add(job.getCompanyId());

            // 把job转换成jobsVO
            SearchJobsVO jobsVO = new SearchJobsVO();
            BeanUtils.copyProperties(job, jobsVO);
            jobsVOList.add(jobsVO);
        }

        //* 构建一个SearchBO对象，用于传递给用户微服务、企业微服务
        SearchBO searchBO = new SearchBO();
        searchBO.setUserIds(hrIdList);
        searchBO.setCompanyIds(companyIdList);

        // 1.通过feign远程调用，获得hrIdList所对应的信息
        GraceJSONResult usersResult = userInfoMSFeign.getHRList(searchBO);
        String userListStr = (String) usersResult.getData();
        List<UsersVO> userList = GsonUtils.stringToListAnother(userListStr, UsersVO.class);

        // 将userList转换成一个map，key是hrId，value是UsersVO，如果不借助map，则每次将VO放入jobsVO时，都要遍历一遍userList，效率太低
        /* for (SearchJobsVO j : jobsVOList) {
            for (UsersVO u : hrUsersList) {
                if (j.getHrId().equals(u.getId())) {
                    j.setUsersVO(u);
                }
            }
        } */
        Map<String, UsersVO> usersMap = new HashMap<>();
        for (UsersVO usersVO : userList) {
            usersMap.put(usersVO.getId(), usersVO);
        }
        // 遍历jobsVOList，给每个jobsVO设置UsersVO
        jobsVOList.forEach(jobsVO -> jobsVO.setUsersVO(usersMap.get(jobsVO.getHrId())));

        // 2.通过feign远程调用，获得companyIdList所对应的信息
        GraceJSONResult companyResult = companyMSFeign.getCompanyList(searchBO);
        String companyListStr = (String) companyResult.getData();
        List<CompanyInfoVO> companyList = GsonUtils.stringToListAnother(companyListStr, CompanyInfoVO.class);

        // 将companyList转换成一个map，key是companyId，value是CompanyInfoVO
        Map<String, CompanyInfoVO> companyMap = new HashMap<>();
        for (CompanyInfoVO companyInfoVO : companyList) {
            companyMap.put(companyInfoVO.getCompanyId(), companyInfoVO);
        }
        // 遍历jobsVOList，给每个jobsVO设置CompanyInfoVO
        jobsVOList.forEach(jobsVO -> jobsVO.setCompanyInfoVO(companyMap.get(jobsVO.getCompanyId())));


        // 返回分页对象
        return setterPagedGrid(jobsVOList, page);
    }
}
