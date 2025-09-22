package com.yufeng.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.github.benmanes.caffeine.cache.Cache;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.ActiveTime;
import com.yufeng.enums.EduEnum;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.bo.*;
import com.yufeng.model.pojo.ResumeEducation;
import com.yufeng.model.pojo.ResumeExpect;
import com.yufeng.model.pojo.ResumeProjectExp;
import com.yufeng.model.pojo.ResumeWorkExp;
import com.yufeng.model.vo.ResumeVO;
import com.yufeng.service.ResumeService;
import com.yufeng.utils.GsonUtils;
import com.yufeng.utils.LocalDateUtils;
import com.yufeng.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@Slf4j
@RestController
@RequestMapping("/resume")
public class ResumeController extends BaseInfoProperties {

    @Autowired
    ResumeService resumeService;

    @Autowired
    private Cache<String, Integer> resumeRefreshCountsCache;

    @Resource(name = "curatorClient") // 根据名称注入
    private CuratorFramework zkClient;


    /**
     * 初始化用户简历，在用户注册成功后自动分布式调用
     *
     * @param userId 用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/init")
    public GraceJSONResult init(@RequestParam("userId") String userId) {

        // 这里不需要对userId进行校验，因为这是微服务之间的调用，肯定不为null
        resumeService.initResume(userId);

        return GraceJSONResult.ok();
    }


    /**
     * 修改简历(适用于App端用户点击‘保存简历’按钮后，调用此接口修改简历信息)
     *
     * @param editResumeBO 编辑简历业务对象，包含用户ID、姓名、性别、出生日期、联系方式、电子邮箱、教育经历、工作经历等信息
     * @return GraceJSONResult 返回操作结果
     * <p>
     * var pendingInfo = {
     * "id": this.resumeId,
     * "userId": userId,
     * "advantage": advantageText,
     * "advantageHtml": advantageHtml,
     * };
     */
    @PostMapping("/modify")
    public GraceJSONResult modify(@RequestBody @Valid EditResumeBO editResumeBO) {

        // TODO 这里需要对BO里面的简历Id进行一个校验，确保当前用户只能修改自己的简历
        // 比如：resumeService.checkResumeId(editResumeBO.getUserId(), editResumeBO.getId());

        // 调用service层，修改简历
        resumeService.modifyResume(editResumeBO);
        return GraceJSONResult.ok();
    }


    /**
     * 根据用户ID查询简历信息(适用于每次用户点击‘在线简历’后，自动查询并展示简历信息)
     *
     * @param userId 用户ID
     * @return GraceJSONResult 返回操作结果，包含简历信息
     */
    @PostMapping("/queryMyResume")
    public GraceJSONResult queryMyResume(String userId) {
        // 对userId进行非空校验，减少对MySQL的访问
        if (StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        ResumeVO resumeVO;
        // 先从redis中看看有没有该用户的缓存信息
        String resumeJson = redis.get(REDIS_RESUME_INFO + ":" + userId);
        if (StringUtils.isBlank(resumeJson)) {
            // 如果没有，就从数据库中，分批查询并缓存到redis中，缓存时间为24小时
            resumeVO = resumeService.getResumeInfo(userId);
            redis.set(REDIS_RESUME_INFO + ":" + userId, GsonUtils.object2String(resumeVO), 60 * 60 * 24);
        } else {
            // 如果有，就直接从redis中获取
            resumeVO = GsonUtils.stringToBean(resumeJson, ResumeVO.class);
        }

        /**
         * 由于用户的简历在修改完之后，一般不会去做太多的修改，
         * 而且每次查询又是会涉及到多张表的查询，所以可以考虑使用Redis进行缓存，提升查询性能。
         */
        // resumeVO = resumeService.getResumeInfo(userId);

        return GraceJSONResult.ok(resumeVO);
    }


    /**
     * 修改用户的工作经历信息。
     *
     * @param editWorkExpBO 包含要修改的工作经历信息的业务对象，包括但不限于用户ID、简历ID以及具体的工作经历详情
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/editWorkExp")
    public GraceJSONResult editWorkExp(@RequestBody EditWorkExpBO editWorkExpBO) {

        // TODO 这里需要对BO里面的简历Id进行一个校验，确保当前用户只能修改自己的简历
        // 比如：resumeService.checkResumeId(editWorkExpBO.getUserId(), editWorkExpBO.getResumeId());

        // 调用service层，修改工作经历
        resumeService.editWorkExperience(editWorkExpBO);

        return GraceJSONResult.ok();
    }


    /**
     * 获取并处理用户的工作经历信息(适用于点击修改页面的时候，自动查询并展示工作经历信息)
     *
     * @param workExpId 工作经历ID
     * @param userId    用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/getWorkExp")
    public GraceJSONResult getWorkExp(String workExpId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(workExpId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，查询工作经历
        ResumeWorkExp exp = resumeService.getWorkExp(workExpId, userId);

        return GraceJSONResult.ok(exp);
    }


    /**
     * 删除用户的工作经历信息。
     *
     * @param workExpId 工作经历ID
     * @param userId    用户ID
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/deleteWorkExp")
    public GraceJSONResult deleteWorkExp(String workExpId, String userId) {

        // 判空处理
        resumeService.deleteWorkExp(workExpId, userId);
        return GraceJSONResult.ok();
    }


    /**
     * 修改用户的项目经历信息。
     *
     * @param editProjectExpBO 包含要修改的项目经历信息的业务对象，包括但不限于用户ID、简历ID以及具体的项目经历详情
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/editProjectExp")
    public GraceJSONResult editProjectExp(@RequestBody EditProjectExpBO editProjectExpBO) {

        resumeService.editProjectExp(editProjectExpBO);
        return GraceJSONResult.ok();
    }


    /**
     * 获取并处理用户的项目经历信息(适用于点击修改页面的时候，自动查询并展示项目经历信息)
     *
     * @param projectExpId 项目经历ID
     * @param userId       用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/getProjectExp")
    public GraceJSONResult getProjectExp(String projectExpId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(projectExpId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，查询项目经历
        ResumeProjectExp projectExp = resumeService.getProjectExp(projectExpId, userId);

        return GraceJSONResult.ok(projectExp);
    }


    /**
     * 删除用户的项目经历信息。
     *
     * @param projectExpId 项目经历ID
     * @param userId       用户ID
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/deleteProjectExp")
    public GraceJSONResult deleteProjectExp(String projectExpId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(projectExpId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，删除项目经历
        resumeService.deleteProjectExp(projectExpId, userId);
        return GraceJSONResult.ok();
    }


    /**
     * 修改用户的教育经历信息(新增或者修改)。
     *
     * @param editEducationBO 包含要修改的教育经历信息的业务对象，包括但不限于用户ID、简历ID以及具体的教育经历详情
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/editEducation")
    public GraceJSONResult editEducation(@RequestBody EditEducationBO editEducationBO) {

        // 调用service层，删除项目经历
        resumeService.editEducation(editEducationBO);
        return GraceJSONResult.ok();
    }


    /**
     * 获取并处理用户的教育经历信息(适用于点击修改页面的时候，自动查询并展示教育经历信息)
     *
     * @param eduId  教育经历ID
     * @param userId 用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/getEducation")
    public GraceJSONResult getEducation(String eduId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(eduId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，查询教育经历
        ResumeEducation education = resumeService.getEducation(eduId, userId);

        return GraceJSONResult.ok(education);
    }


    /**
     * 删除用户的教育经历信息。
     *
     * @param eduId  教育经历ID
     * @param userId 用户ID
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/deleteEducation")
    public GraceJSONResult deleteEducation(String eduId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(eduId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，删除教育经历
        resumeService.deleteEducation(eduId, userId);
        return GraceJSONResult.ok();
    }


    /**
     * 修改用户的求职意向信息(新增或者修改)。
     *
     * @param editResumeExpectBO 包含要修改的求职意向信息的业务对象，包括但不限于用户ID、简历ID以及具体的求职意向详情
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/editJobExpect")
    public GraceJSONResult editJobExpect(@RequestBody EditResumeExpectBO editResumeExpectBO) {

        // 调用service层，更新求职意向
        resumeService.editJobExpect(editResumeExpectBO);
        return GraceJSONResult.ok();
    }


    /**
     * 获取并处理用户的求职意向信息
     *
     * @param resumeId 简历ID
     * @param userId   用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/getMyResumeExpectList")
    public GraceJSONResult getMyResumeExpectList(String resumeId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(resumeId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 因为该接口属于二级页面，点击的次数比较多，所以这里需要Redis进行缓存
        String myResumeExpectListJson = redis.get(REDIS_RESUME_EXPECT + ":" + userId);
        List<ResumeExpect> expectList;

        // 如果没有，就从数据库中查询，并缓存到redis中，缓存时间为24小时
        if (StringUtils.isBlank(myResumeExpectListJson)) {
            expectList = resumeService.getMyResumeExpectList(resumeId, userId);
            // 放入缓存
            redis.set(REDIS_RESUME_EXPECT + ":" + userId, GsonUtils.object2String(expectList), 60 * 60 * 24);
        } else { // 如果有，就直接从redis中获取
            expectList = GsonUtils.stringToList(myResumeExpectListJson, ResumeExpect.class);
        }

        return GraceJSONResult.ok(expectList);
    }


    /**
     * 删除用户的求职意向信息。
     *
     * @param resumeExpectId 求职意向ID
     * @param userId         用户ID
     * @return GraceJSONResult 返回操作结果，成功时返回ok状态
     */
    @PostMapping("/deleteMyResumeExpect")
    public GraceJSONResult deleteMyResumeExpect(String resumeExpectId, String userId) {
        // 判空处理
        if (StringUtils.isEmpty(resumeExpectId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 调用service层，删除求职意向
        resumeService.deleteMyResumeExpect(resumeExpectId, userId);
        return GraceJSONResult.ok();
    }


    /**
     * 当资源: test/refresh 被限流或者降级时，调用此方法
     */
    public GraceJSONResult blockHandlerForRefresh(String resumeId, String userId, BlockException ex) {
        log.error("触发了限流或者降级，异常信息：", ex);
        return GraceJSONResult.errorMsg("触发了限流或者降级！！");
    }


    /**
     * 当资源: test/refresh 发生业务异常时(也就是所谓的兜底处理、降级)，也就是返回一个友好的提示
     */
    public GraceJSONResult fallbackHandlerForRefresh(String resumeId, String userId) {

        String today = LocalDateUtils.getLocalDateStr();
        redis.increment(USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId, 1);

       return GraceJSONResult.ok("刷新成功～(兜底处理)");
    }

    /**
     * 刷新简历时间(适用于用户点击‘刷新简历’按钮后，调用此接口刷新简历时间)
     *
     * @param resumeId 简历ID
     * @param userId   用户ID
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/refresh")
    @SentinelResource(value = "test/refresh", blockHandler = "blockHandlerForRefresh", fallback = "fallbackHandlerForRefresh")
    public GraceJSONResult refresh(String resumeId, String userId) {

        // 判空处理
        if (StringUtils.isEmpty(resumeId) || StringUtils.isEmpty(userId)) {
            return GraceJSONResult.error();
        }

        // 模拟降级处理
        // int a = 1 / 0;

        // 查询最大允许的刷新次数(这里可以用open_feign调用Resource服务，为了简化代码，就先写死)
        // Integer maxResumeRefreshCounts = 3;/*  */

        // 从redis中获取最大允许的刷新次数
        // int maxResumeRefreshCounts = Integer.parseInt(redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS));

        // 从本地缓存中获取最大允许的刷新次数，如果没有命中，就从redis中获取
        Integer maxResumeRefreshCounts = resumeRefreshCountsCache.get(CACHE_MAX_RESUME_REFRESH_COUNTS, key -> {
            log.info("本地缓存Caffeine中没有命中[简历最大刷新次数]，从Redis中获取并放入本地缓存...");

            // 因为使用了缓存预热(也就是容器初始化完成后，就会把最大刷新次数放入到Redis中)，所以这里直接从Redis中获取就可以了，不需要再去查数据库
            String maxRefreshCount2Str = redis.get(REDIS_MAX_RESUME_REFRESH_COUNTS);
            int maxRefreshCounts2;

            // 如果redis崩了，那就试着从zookeeper中获取
            if (StringUtils.isNotBlank(maxRefreshCount2Str)) {
                String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;
                try {
                    String maxCountsStr = new String(zkClient.getData().forPath(path));
                    maxRefreshCounts2 = Integer.parseInt(maxCountsStr);
                } catch (Exception e) {
                    log.error("从zookeeper中获取最大简历刷新次数失败，错误信息：", e);
                    // 如果zookeeper也获取不到，那就只能写死一个值了
                    maxRefreshCounts2 = 3;
                }
            } else {
                maxRefreshCounts2 = Integer.parseInt(maxRefreshCount2Str);
            }

            return maxRefreshCounts2;
        });


        // 正式进入业务判断处理
        // 通过redis获得当前用户在当天已经刷新了多少次简历，如果超过了最大允许刷新次数，就直接返回
        String today = LocalDateUtils.getLocalDateStr();  // 这里获得的日期格式为：2023-10-01(年月日)
        int userAlreadyRefreshedCounts = 1; // 默认值为1
        // redis的key设计：user_resume_refresh_counts:2023-10-01:userId
        String key = USER_ALREADY_REFRESHED_COUNTS + ":" + today + ":" + userId;
        // 从redis中获取用户今天已经刷新的次数
        String userAlreadyRefreshedCountsStr = redis.get(key);

        if (StringUtils.isBlank(userAlreadyRefreshedCountsStr)) {
            // 如果为空，说明是今天第一次刷新
            redis.set(key, userAlreadyRefreshedCounts + "", 24 * 60 * 60);
        } else {
            // 如果不为空，说明不是今天第一次刷新
            userAlreadyRefreshedCounts = Integer.parseInt(userAlreadyRefreshedCountsStr);

            // 如果已经超过了最大允许刷新次数，就直接返回
            if (userAlreadyRefreshedCounts >= maxResumeRefreshCounts) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.RESUME_MAX_LIMIT_ERROR);

            } else {
                // 如果没有超过，就把已经刷新的次数+1，并更新到redis中(同时把用户的简历进行刷新)
                resumeService.refreshResume(resumeId, userId); // 调用service层，刷新简历时间
                redis.increment(key, 1);
            }
        }

        return GraceJSONResult.ok();
    }


    /**
     * 搜索简历(适用于HR在‘搜索简历’页面，输入关键字搜索简历)
     * TODO 后续可以考虑用ElasticSearch来实现(检索查询)
     *
     * @param searchResumesBO 搜索简历业务对象，包含关键字、工作城市、薪资范围、工作经验、学历等信息
     * @param page            当前页
     * @param limit           每页显示的条数
     * @return GraceJSONResult 返回操作结果
     */
    @PostMapping("/searchResumes")
    public GraceJSONResult searchResumes(@RequestBody SearchResumesBO searchResumesBO,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer limit) {

        // 将App端选择的String类型的活跃时间，转成数据库中的数字类型
        String activeTime = searchResumesBO.getActiveTime();
        Integer activeTimes = ActiveTime.getActiveTimes(activeTime);
        searchResumesBO.setActiveTimes(activeTimes);

        // 将App端选择的String类型的学历，转成数据库中的数字类型
        String edu = searchResumesBO.getEdu();
        Integer eduIndex = EduEnum.getEduIndex(edu);
        List<String> eduList = EduEnum.getEduList(eduIndex);
        searchResumesBO.setEduList(eduList);

        // 调用service层，搜索简历
        PagedGridResult pagedGridResult = resumeService.searchResumes(searchResumesBO, page, limit);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
