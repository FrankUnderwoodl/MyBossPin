package com.yufeng.service;

import com.yufeng.model.bo.*;
import com.yufeng.model.pojo.ResumeEducation;
import com.yufeng.model.pojo.ResumeExpect;
import com.yufeng.model.pojo.ResumeProjectExp;
import com.yufeng.model.pojo.ResumeWorkExp;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.model.vo.ResumeVO;

import java.util.List;

/**
 * @author Frank Underwood
 * @since 2025-07-29 07:19:20
 * @describe 简历服务service
 * @deep 你说为什么需要这个接口呢？ 因为假如没有这个接口，那你想要开启一个事务的话，Spring就无法帮你生成代理对象，
 *         也就无法开启事务了。 这个接口的作用就是为了让Spring能够识别这是一个服务层的组件，
 *         并且可以在这个接口上使用事务相关的注解。
 *         这样一来，当你在实现类中调用这个接口的方法时，Spring就会自动帮你开启事务，
 *         并且在方法执行完毕后提交事务或回滚事务。
 *         这就是为什么需要这个接口的原因。

 */
public interface ResumeService {


    /**
     * 初始化简历
     */
    public void initResume(String userId);



    /**
     * 修改简历
     */
    public void modifyResume(EditResumeBO editResumeBO);



    /**
     * 根据用户ID查询简历信息
     */
    public ResumeVO getResumeInfo(String userId);



    /**
     * 修改工作经历
     */
    public void editWorkExperience(EditWorkExpBO editWorkExpBO);



    /**
     * 查询工作经历
     */
    public ResumeWorkExp getWorkExp(String workExpId, String userId);



    /**
     * 删除工作经历
     */
    public void deleteWorkExp(String workExpId, String userId);




    /**
     * 修改项目经历
     */
    public void editProjectExp(EditProjectExpBO editProjectExpBO);




    /**
     * 查询项目经历
     */
    public ResumeProjectExp getProjectExp(String projectExpId, String userId);



    /**
     * 删除项目经历
     */
    public void deleteProjectExp(String projectExpId, String userId);



    /**
     * 修改教育经历
     */
    public void editEducation(EditEducationBO editEducationBO);



    /**
     * 查询教育经历
     */
    public ResumeEducation getEducation(String eduId, String userId);



    /**
     * 删除教育经历
     */
    public void deleteEducation(String eduId, String userId);



    /**
     * 修改求职意向
     */
    public void editJobExpect(EditResumeExpectBO editResumeExpectBO);




    /**
     * 查询求职意向
     */
    List<ResumeExpect> getMyResumeExpectList(String resumeId, String userId);




    /**
     * 删除求职意向
     */
    void deleteMyResumeExpect(String resumeExpectId, String userId);



    /**
     * 刷新简历
     */
    void refreshResume(String resumeId, String userId);



    /**
     * 搜索简历
     */
    PagedGridResult searchResumes(SearchResumesBO searchResumesBO, Integer page, Integer pageSize);
}
