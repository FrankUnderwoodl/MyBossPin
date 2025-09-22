package com.yufeng.controller;

import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.model.pojo.JobType;
import com.yufeng.service.JobTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("jobType")
public class JobTypeController {

    @Autowired
    private JobTypeService jobTypeService;


    /**
     * 获得顶级分类列表
     * @return
     */
    @GetMapping("app/initTopList")
    public GraceJSONResult initTopList() {
        // TODO 先从redis中查询，如果没有，再从db查询并且放入redis中

        return GraceJSONResult.ok(jobTypeService.getTopJobTypeList());
    }

    /**
     * 根据顶级分类获得第三级分类列表(app端懒加载)
     * @return
     */
    @GetMapping("app/getThirdListByTop/{topJobTypeId}")
    public GraceJSONResult getThirdListByTop(@PathVariable("topJobTypeId") String topJobTypeId) {

        // TODO 先从redis中查询，如果没有，再从db查询并且放入redis中

        return GraceJSONResult.ok(jobTypeService.getThirdListByTop(topJobTypeId));
    }

    /**
     * 根据顶级分类获得二级分类包含三级分类列表的集合(app端懒加载)
     * @return
     */
    @GetMapping("app/getSecondAndThirdListByTop/{topJobTypeId}")
    public GraceJSONResult getSecondAndThirdListByTop(@PathVariable("topJobTypeId") String topJobTypeId) {

        // TODO 先从redis中查询，如果没有，再从db查询并且放入redis中

        return GraceJSONResult.ok(jobTypeService.getSecondAndThirdListByTop(topJobTypeId));
    }

    // ****************************** 以上为app用户端所用，查询的时候涉及缓存 *************************************************


    // ****************************** 以下为运营平台所用，操作的时候不涉及缓存操作，以此可以直接入库 ******************************


    /**
     * 获得顶级分类列表
     * @return
     */
    @GetMapping("getTopList")
    public GraceJSONResult getTopList(HttpServletRequest request) {
        return GraceJSONResult.ok(jobTypeService.getTopJobTypeList());
    }

    /**
     * 获得当前分类下的子分类
     * @param jobTypeId
     * @return
     */
    @GetMapping("children/{jobTypeId}")
    public GraceJSONResult getChildrenIndustryList(@PathVariable("jobTypeId") String jobTypeId) {

        return GraceJSONResult.ok(jobTypeService.getChildrenJobTypeList(jobTypeId));
    }

    /**
     * 创建节点
     * @param jobType
     * @return
     */
    @PostMapping("createNode")
    public GraceJSONResult createNode(@RequestBody JobType jobType) {

        // 判断节点名是否已经存在
        if (jobTypeService.getJobTypeIsExistByName(jobType.getName())) {
            return GraceJSONResult.errorMsg("该分类名已存在！请重新取名吧~");
        }

        // 节点创建
        jobTypeService.createJobType(jobType);
        return GraceJSONResult.ok();
    }

    /**
     * 更新节点
     * @param jobType
     * @return
     */
    @PostMapping("updateNode")
    public GraceJSONResult updateNode(@RequestBody JobType jobType) {
        jobTypeService.updateJobType(jobType);
        return GraceJSONResult.ok();
    }

    @DeleteMapping("deleteNode/{jobTypeId}")
    public GraceJSONResult deleteNode(@PathVariable("jobTypeId") String jobTypeId) {
        // 思考：能不能传一个industry对象进来，这样更方便判断，不需要再查数据库

        // 判断如果是一级二级节点，则要保证没有子节点才能删除，三级节点可以直接删
        JobType temp = jobTypeService.getJobTypeById(jobTypeId);
        if (temp.getLevel() == 1 || temp.getLevel() == 2) {
            // 查询该节点下是否有子节点
            Long counts = jobTypeService.getChildrenJobTypeCounts(jobTypeId);
            System.out.println("counts = " + counts);
            if (counts > 0) {
                return GraceJSONResult.errorMsg("请保证该节点下无任何子节点后再删除！");
            }
        }

        jobTypeService.deleteJobType(jobTypeId);

        return GraceJSONResult.ok();
    }

}
