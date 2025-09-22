package com.yufeng.service;

import com.yufeng.enums.DealStatus;
import com.yufeng.model.bo.SearchReportJobBO;
import com.yufeng.model.mo.ReportMO;
import com.yufeng.utils.PagedGridResult;

/**
 * @author Lzm
 * @CreateTime 2025年9月08日 22:03
 */
public interface ReportService {


    /**
     * 新增举报记录
     * @param reportMO 举报信息MO
     */
    public void saveReport(ReportMO reportMO);


    /**
     * 判断是否已经举报过(也就是不能让用户重复举报)
     * @param jobId 职位ID
     * @param userId 用户ID
     * @return true-已举报，false-未举报
     */
    public boolean isReportRecordExist(String userId, String jobId);





    /**
     * 分页查询举报记录列表，提给给admin端使用
     * @param reportJobBO 查询条件
     * @param page 第几页
     * @param pageSize 每页显示多少条
     * @return PagedGridResult
     */
    PagedGridResult pagedReportRecordList(SearchReportJobBO reportJobBO, Integer page, Integer pageSize);



    /**
     * 修改举报状态
     * @param reportId 举报ID
     * @param status 处理状态
     */
    void updateReportRecordStatus(String reportId, DealStatus status);
}
