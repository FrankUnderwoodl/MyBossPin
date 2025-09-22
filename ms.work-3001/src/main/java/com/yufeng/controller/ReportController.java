package com.yufeng.controller;

import com.yufeng.enums.DealStatus;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.bo.SearchReportJobBO;
import com.yufeng.model.mo.ReportMO;
import com.yufeng.service.ReportService;
import com.yufeng.utils.LocalDateUtils;
import com.yufeng.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;


    /**
     * 用户举报职位
     * @param reportMO 举报信息
     * @return JSONResult
     */
    @PostMapping("/create")
    public GraceJSONResult create(@RequestBody @Valid ReportMO reportMO) {
        // 如果MongoDB性能遇到瓶颈，可以放在redis中，定时任务同步到MongoDB

        // 先他妈判断用户是否已经举报过该职位
        boolean isExist = reportService.isReportRecordExist(reportMO.getReportUserId(), reportMO.getJobId());
        if (isExist) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.REPORT_RECORD_EXIST_ERROR);
        }
        // 调用service，保存举报信息
        reportService.saveReport(reportMO);
        return GraceJSONResult.ok();
    }





    /**
     * 分页查询举报记录列表，提给给admin端使用
     * @param reportJobBO 查询条件
     * @param page 第几页
     * @param pageSize 每页显示多少条
     * @return JSONResult
     */
    @PostMapping("/pagedReportRecordList")
    public GraceJSONResult pagedReportRecordList(@RequestBody SearchReportJobBO reportJobBO,
                                                 @RequestParam(defaultValue = "0")Integer page,
                                                 @RequestParam(defaultValue = "10")Integer pageSize) {

        // 先把起始时间和结束时间拿出来，将起始和结束时间的时分秒设置好(例如将 2023-10-01 - 2023-10-07，转成 2023-10-01 00:00:00 - 2023-10-07 23:59:59)
        LocalDate beginDate = reportJobBO.getBeginDate();
        LocalDate endDate = reportJobBO.getEndDate();
        if (beginDate != null) {
            // 开始日期加上时间: 00:00:00
            String beginDateTimeStr = LocalDateUtils.format(beginDate,
                    LocalDateUtils.DATE_PATTERN) + " 00:00:00";

            LocalDateTime beginDateTime = LocalDateUtils.parseLocalDateTime(beginDateTimeStr,
                    LocalDateUtils.DATETIME_PATTERN);
            reportJobBO.setBeginDateTime(beginDateTime);
        }

        if (endDate != null) {
            // 结束日期加上时间: 23:59:59
            String endDateTimeStr = LocalDateUtils.format(endDate,
                    LocalDateUtils.DATE_PATTERN) + " 23:59:59";

            LocalDateTime endDateTime = LocalDateUtils.parseLocalDateTime(endDateTimeStr,
                    LocalDateUtils.DATETIME_PATTERN);
            reportJobBO.setEndDateTime(endDateTime);
        }

        // 调用service，分页查询
        PagedGridResult gridResult = reportService.pagedReportRecordList(reportJobBO, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }



    /**
     * 删除职位
     * @param reportId 举报记录ID
     * @return JSONResult
     */
    @PostMapping("deal/delete")
    public GraceJSONResult delete(String reportId) {
        reportService.updateReportRecordStatus(reportId, DealStatus.DONE);
        return GraceJSONResult.ok();
    }

    /**
     * 忽略职位(也就是该职位暂时没有问题)
     * @param reportId 举报记录ID
     * @return JSONResult
     */
    @PostMapping("deal/ignore")
    public GraceJSONResult ignore(String reportId) {
        reportService.updateReportRecordStatus(reportId, DealStatus.IGNORE);
        return GraceJSONResult.ok();
    }
}
