package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.mapper.*;
import com.yufeng.model.bo.*;
import com.yufeng.model.pojo.*;
import com.yufeng.service.ResumeService;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.ResumeVO;
import com.yufeng.model.vo.SearchResumesVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Service // 这个注解的作用是将当前类标识为一个服务层的组件，Spring会自动扫描并注册到容器中
public class ResumeServiceImpl extends BaseInfoProperties implements ResumeService {


    // 简历表
    @Autowired
    private ResumeMapper resumeMapper;

    // 教育经历表
    @Autowired
    private ResumeEducationMapper resumeEducationMapper;

    // 求职意向表
    @Autowired
    private ResumeExpectMapper resumeExpectMapper;

    // 项目经历表
    @Autowired
    private ResumeProjectExpMapper resumeProjectExpMapper;

    // 工作经历表
    @Autowired
    private ResumeWorkExpMapper resumeWorkExpMapper;

    // 自定义mapper(主要用来做复杂查询)
    @Autowired
    private ResumeMapperCustom resumeMapperCustom;


    /**
     * 初始化简历
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void initResume(@RequestParam String userId) {

        // 初始化一个pojo对象
        Resume resume = new Resume();
        // 设置用户id
        resume.setUserId(userId);

        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdatedTime(LocalDateTime.now());

        // 这里故意制造一个异常，测试分布式事务的回滚
        // int i = 1 / 0;

        // 调用mapper层的方法，插入数据
        resumeMapper.insert(resume);
    }


    /**
     * 修改简历
     *
     * @param editResumeBO 编辑简历业务对象，包含用户ID、姓名、性别、出生日期、联系方式、电子邮箱、教育经历、工作经历等信息
     */
    @Override
    @Transactional
    public void modifyResume(EditResumeBO editResumeBO) {

        // 看到BO，就要想到转成一个POJO
        Resume resume = new Resume();
        // 将BO中的数据拷贝到POJO中
        BeanUtils.copyProperties(editResumeBO, resume);
        // 设置更新时间
        resume.setUpdatedTime(LocalDateTime.now());

        // 调用mapper层的方法，更新数据
        resumeMapper.updateById(resume);

        // 更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + editResumeBO.getUserId()); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }


    /**
     * 根据用户ID查询简历信息
     *
     * @param userId 用户ID
     * @return ResumeVO 返回简历视图对象，包含用户的简历信息
     */
    @Override
    public ResumeVO getResumeInfo(String userId) {

        // 先创建需要返回给前端的VO对象
        ResumeVO resumeVO = new ResumeVO();

        //* 1. 查询简历信息
        Resume resume = resumeMapper.selectOne(new QueryWrapper<Resume>().eq("user_id", userId));
        // 先判断是否为空
        if (resume == null) {
            return null;
        }
        // 将POJO中的数据拷贝到VO中
        BeanUtils.copyProperties(resume, resumeVO);

        //* 2.查询工作经验
        List<ResumeWorkExp> resumeWorkExps = resumeWorkExpMapper.selectList(new QueryWrapper<ResumeWorkExp>()
                .eq("user_id", userId)
                .eq("resume_id", resume.getId())
                .orderByDesc("begin_date"));
        // 这里没有进行多表关联查询，单独查表后再组装，避免数据库压力过大
        resumeVO.setWorkExpList(resumeWorkExps);

        //* 3.查询项目经验
        List<ResumeProjectExp> resumeProjectExps = resumeProjectExpMapper.selectList(new QueryWrapper<ResumeProjectExp>()
                .eq("user_id", userId)
                .eq("resume_id", resume.getId())
                .orderByDesc("begin_date"));
        // 这里没有进行多表关联查询，单独查表后再组装，避免数据库压力过大
        resumeVO.setProjectExpList(resumeProjectExps);

        //* 4.查询我的教育经历
        List<ResumeEducation> resumeEducations = resumeEducationMapper.selectList(new QueryWrapper<ResumeEducation>()
                .eq("user_id", userId)
                .eq("resume_id", resume.getId())
                .orderByDesc("begin_date"));
        // 这里没有进行多表关联查询，单独查表后再组装，避免数据库压力过大
        resumeVO.setEducationList(resumeEducations);

        return resumeVO;
    }


    /**
     * 修改工作经历
     *
     * @param editWorkExpBO 编辑工作经历业务对象，包含用户ID、简历ID、工作经历等信息
     */
    @Override
    @Transactional
    public void editWorkExperience(EditWorkExpBO editWorkExpBO) {

        // 看到BO，就要想到转成一个POJO
        ResumeWorkExp resumeWorkExp = new ResumeWorkExp();
        // 将BO中的数据拷贝到POJO中
        BeanUtils.copyProperties(editWorkExpBO, resumeWorkExp);
        // 设置更新时间
        resumeWorkExp.setUpdatedTime(LocalDateTime.now());


        // 通过判断ID是否为空，来决定是新增还是修改
        if (StringUtils.isBlank(editWorkExpBO.getId())) {
            // 设置创建时间
            resumeWorkExp.setCreateTime(LocalDateTime.now());
            // 这里是新增
            resumeWorkExpMapper.insert(resumeWorkExp);
        } else {
            // 这里是修改(这里底层的SQL语句是：UPDATE resume_work_exp SET ... WHERE id = ? AND user_id = ? AND resume_id = ?)
            resumeWorkExpMapper.update(resumeWorkExp, new QueryWrapper<ResumeWorkExp>()
                    .eq("id", editWorkExpBO.getId())
                    .eq("user_id", editWorkExpBO.getUserId())
                    .eq("resume_id", editWorkExpBO.getResumeId()));
        }


        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + editWorkExpBO.getUserId()); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }



    /**
     * 查询工作经历
     *
     * @param workExpId 工作经历ID
     * @param userId    用户ID
     * @return ResumeWorkExp 返回工作经历对象，包含用户的工作经历信息
     */
    @Override
    public ResumeWorkExp getWorkExp(String workExpId, String userId) {

        return resumeWorkExpMapper.selectOne(new QueryWrapper<ResumeWorkExp>()
                .eq("id", workExpId)
                .eq("user_id", userId));
    }



    /**
     * 删除工作经历
     *
     * @param workExpId 工作经历ID
     * @param userId    用户ID
     * @return ResumeWorkExp 返回被删除的工作经历对象，包含用户的工作经历信息
     */
    @Override
    @Transactional
    public void deleteWorkExp(String workExpId, String userId) {

        // 操作数据库，删除工作经历
        resumeWorkExpMapper.delete(new QueryWrapper<ResumeWorkExp>()
                .eq("id", workExpId)
                .eq("user_id", userId));

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + userId); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩

    }



    /**
     * 修改项目经历
     *
     * @param editProjectExpBO 编辑项目经历业务对象，包含用户ID、简历ID、项目经历等信息
     */
    @Override
    @Transactional
    public void editProjectExp(EditProjectExpBO editProjectExpBO) {

        // 看到BO，就要想到转成一个POJO
        ResumeProjectExp resumeProjectExp = new ResumeProjectExp();
        // 将BO中的数据拷贝到POJO中
        BeanUtils.copyProperties(editProjectExpBO, resumeProjectExp);
        // 设置更新时间
        resumeProjectExp.setUpdatedTime(LocalDateTime.now());

        // 通过判断ID是否为空，来决定是新增还是修改
        if (StringUtils.isBlank(editProjectExpBO.getId())) {
            // 设置创建时间
            resumeProjectExp.setCreateTime(LocalDateTime.now());
            // 这里是新增
            resumeProjectExpMapper.insert(resumeProjectExp);
        } else {
            // 这里是修改(这里底层的SQL语句是：UPDATE resume_project_exp SET ... WHERE id = ? AND user_id = ? AND resume_id = ?)
            resumeProjectExpMapper.update(resumeProjectExp, new QueryWrapper<ResumeProjectExp>()
                    .eq("id", editProjectExpBO.getId())
                    .eq("user_id", editProjectExpBO.getUserId())
                    .eq("resume_id", editProjectExpBO.getResumeId()));
        }

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + editProjectExpBO.getUserId()); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }



    /**
     * 查询项目经历
     *
     * @param projectExpId 项目经历ID
     * @param userId       用户ID
     * @return ResumeProjectExp 返回项目经历对象，包含用户的项目经历信息
     */
    @Override
    public ResumeProjectExp getProjectExp(String projectExpId, String userId) {

        ResumeProjectExp projectExp = resumeProjectExpMapper.selectOne(
                new QueryWrapper<ResumeProjectExp>()
                        .eq("id", projectExpId)
                        .eq("user_id", userId)
        );
        return projectExp;
    }



    /**
     * 删除项目经历
     *
     * @param projectExpId 项目经历ID
     * @param userId       用户ID
     */
    @Override
    @Transactional
    public void deleteProjectExp(String projectExpId, String userId) {
        // 操作数据库，删除项目经历
        resumeProjectExpMapper.delete(new QueryWrapper<ResumeProjectExp>()
                .eq("id", projectExpId)
                .eq("user_id", userId));

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + userId); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }




    /**
     * 修改教育经历
     *
     * @param editEducationBO 编辑教育经历业务对象，包含用户ID、简历ID、教育经历等信息
     */
    @Override
    @Transactional
    public void editEducation(EditEducationBO editEducationBO) {

        // 看到BO，就要想到转成一个POJO
        ResumeEducation resumeEducation = new ResumeEducation();
        // 将BO中的数据拷贝到POJO中
        BeanUtils.copyProperties(editEducationBO, resumeEducation);
        // 设置更新时间
        resumeEducation.setUpdatedTime(LocalDateTime.now());

        // 通过判断ID是否为空，来决定是新增还是修改
        if (StringUtils.isBlank(editEducationBO.getId())) {
            // 设置创建时间
            resumeEducation.setCreateTime(LocalDateTime.now());
            // 这里是新增
            resumeEducationMapper.insert(resumeEducation);
        } else {
            // 这里是修改(这里底层的SQL语句是：UPDATE resume_education SET ... WHERE id = ? AND user_id = ? AND resume_id = ?)
            resumeEducationMapper.update(resumeEducation, new QueryWrapper<ResumeEducation>()
                    .eq("id", editEducationBO.getId())
                    .eq("user_id", editEducationBO.getUserId())
                    .eq("resume_id", editEducationBO.getResumeId()));
        }

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + editEducationBO.getUserId()); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }





    /**
     * 查询教育经历
     *
     * @param eduId  教育经历ID
     * @param userId 用户ID
     * @return ResumeEducation 返回教育经历对象，包含用户的教育经历信息
     */
    @Override
    public ResumeEducation getEducation(String eduId, String userId) {

        ResumeEducation education = resumeEducationMapper.selectOne(
                new QueryWrapper<ResumeEducation>()
                        .eq("id", eduId)
                        .eq("user_id", userId)
        );
        return education;
    }



    /**
     * 删除教育经历
     *
     * @param eduId  教育经历ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void deleteEducation(String eduId, String userId) {
        // 操作数据库，删除教育经历
        resumeEducationMapper.delete(new QueryWrapper<ResumeEducation>()
                .eq("id", eduId)
                .eq("user_id", userId));

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + userId); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }




    /**
     * 修改求职意向
     *
     * @param editResumeExpectBO 编辑求职意向业务对象，包含用户ID、简历ID、求职意向等信息
     */
    @Override
    @Transactional
    public void editJobExpect(EditResumeExpectBO editResumeExpectBO) {
        // 看到BO，就要想到转成一个POJO
        ResumeExpect resumeExpect = new ResumeExpect();
        // 将BO中的数据拷贝到POJO中
        BeanUtils.copyProperties(editResumeExpectBO, resumeExpect);
        // 设置更新时间
        resumeExpect.setUpdatedTime(LocalDateTime.now());

        // 通过判断ID是否为空，来决定是新增还是修改
        if (StringUtils.isBlank(editResumeExpectBO.getId())) {
            // 设置创建时间
            resumeExpect.setCreateTime(LocalDateTime.now());
            // 这里是新增
            resumeExpectMapper.insert(resumeExpect);
        } else {
            // 这里是修改(这里底层的SQL语句是：UPDATE resume_expect SET ... WHERE id = ? AND user_id = ? AND resume_id = ?)
            resumeExpectMapper.update(resumeExpect, new QueryWrapper<ResumeExpect>()
                    .eq("id", editResumeExpectBO.getId())
                    .eq("user_id", editResumeExpectBO.getUserId())
                    .eq("resume_id", editResumeExpectBO.getResumeId()));
        }

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_EXPECT + ":" + editResumeExpectBO.getUserId()); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }




    /**
     * 查询求职意向
     *
     * @param resumeId 简历ID
     * @param userId   用户ID
     * @return List<ResumeExpect> 返回求职意向列表，包含用户的求职意向信息
     */
    @Override
    public List<ResumeExpect> getMyResumeExpectList(String resumeId, String userId) {

        // 这里是查询最新的一条求职意向信息
        List<ResumeExpect> resumeExpectList = resumeExpectMapper.selectList(
                new QueryWrapper<ResumeExpect>()
                        .eq("user_id", userId)
                        .eq("resume_id", resumeId)
                        .orderByDesc("updated_time")
        );
        return resumeExpectList;
    }




    /**
     * 删除求职意向
     *
     * @param resumeExpectId 求职意向ID
     * @param userId   用户ID
     */
    @Override
    @Transactional
    public void deleteMyResumeExpect(String resumeExpectId, String userId) {
        // 操作数据库，删除求职意向
        resumeExpectMapper.delete(new QueryWrapper<ResumeExpect>()
                .eq("id", resumeExpectId)
                .eq("user_id", userId));

        // 最后，更新完用户简历后，就得删除缓存，因为数据已经变了
        redis.del(REDIS_RESUME_INFO + ":" + userId); // 这里还能优化，通过Canal来做数据同步来防止缓存雪崩
    }



    /**
     * 刷新简历
     *
     * @param resumeId 简历ID
     * @param userId   用户ID
     */
    @Override
    @Transactional
    public void refreshResume(String resumeId, String userId) {

        // 构建一个BO，去进行修改
        EditResumeBO resumeBO = new EditResumeBO();
        resumeBO.setId(resumeId);
        resumeBO.setUserId(userId);
        resumeBO.setRefreshTime(LocalDateTime.now());

        // 调用修改简历的方法
        this.modifyResume(resumeBO);
    }




    /**
     * 搜索简历
     *
     * @param searchResumesBO 搜索简历业务对象，包含搜索条件如关键字、工作经验、学历、期望薪资等信息
     * @return PagedGridResult 返回分页结果，包含符合搜索条件的简历列表及分页信息
     */
    @Override
    public PagedGridResult searchResumes(SearchResumesBO searchResumesBO, Integer page, Integer pageSize) {
        // 开始分页
        PageHelper.startPage(page, pageSize);

        // 将BO里面的参数取出来
        String basicTitle = searchResumesBO.getBasicTitle();
        String jobType = searchResumesBO.getJobType();
        Integer beginAge = searchResumesBO.getBeginAge();
        Integer endAge = searchResumesBO.getEndAge();
        Integer sex = searchResumesBO.getSex();
        Integer activeTimes = searchResumesBO.getActiveTimes();
        Integer beginWorkExpYears = searchResumesBO.getBeginWorkExpYears();
        Integer endWorkExpYears = searchResumesBO.getEndWorkExpYears();
        String edu = searchResumesBO.getEdu();
        List<String> eduList = searchResumesBO.getEduList();
        Integer beginSalary = searchResumesBO.getBeginSalary();
        Integer endSalary = searchResumesBO.getEndSalary();
        String jobStatus = searchResumesBO.getJobStatus();

        // 将参数放入map中
        Map<String, Object> map = new HashMap<>();
        map.put("basicTitle", basicTitle);
        map.put("jobType", jobType);
        map.put("beginAge", beginAge);
        map.put("endAge", endAge);
        map.put("sex", sex);
        map.put("activeTimes", activeTimes);
        map.put("beginWorkExpYears", beginWorkExpYears);
        map.put("endWorkExpYears", endWorkExpYears);
        map.put("edu", edu);
        map.put("eduList", eduList);
        map.put("beginSalary", beginSalary);
        map.put("endSalary", endSalary);
        map.put("jobStatus", jobStatus);


        List<SearchResumesVO> resumesVOList = resumeMapperCustom.searchResumesList(map);

        return setterPagedGrid(resumesVOList, page);
    }
}
