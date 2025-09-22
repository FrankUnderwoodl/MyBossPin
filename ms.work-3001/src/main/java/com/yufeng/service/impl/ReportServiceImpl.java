package com.yufeng.service.impl;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.DealStatus;
import com.yufeng.enums.JobStatus;
import com.yufeng.mapper.JobMapper;
import com.yufeng.model.bo.SearchReportJobBO;
import com.yufeng.model.mo.ReportMO;
import com.yufeng.model.pojo.Job;
import com.yufeng.repository.ReportJobRepository;
import com.yufeng.service.ReportService;
import com.yufeng.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Lzm
 * @CreateTime 2025年9月08日 22:04
 */
@Service
public class ReportServiceImpl extends BaseInfoProperties implements ReportService {

    @Autowired
    private ReportJobRepository reportJobRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JobMapper jobMapper;

    /**
     * 新增举报记录
     *
     * @param reportMO 举报信息MO
     */
    @Override
    public void saveReport(ReportMO reportMO) {
       /*  问:以前通过mapper，现在通过什么呢？
        答:通过MongoTemplate，Spring Data MongoDB(其实就是类似mybatis-plus中的BaseMapper)
        问:为什么这里不直接用MongoTemplate，而是用MongoRepository呢？
        答:因为MongoRepository是对MongoTemplate的进一步封装，使用起来更方便
        问:那如果是复杂的查询呢？
        答:可以直接注入MongoTemplate来使用 */

        // 设置更新时间和创建时间
        reportMO.setCreatedTime(java.time.LocalDateTime.now());
        reportMO.setUpdatedTime(java.time.LocalDateTime.now());

        // 设置admin端的处理状态为待处理
        reportMO.setDealStatus(DealStatus.WAITING.type);

        reportJobRepository.save(reportMO);
    }




    /**
     * 判断是否已经举报过(也就是不能让用户重复举报)
     *
     * @param jobId  职位ID
     * @param userId 用户ID
     * @return true-已举报，false-未举报
     */
    @Override
    public boolean isReportRecordExist(String userId, String jobId) {

        // 查询MongoDB中，是否存在该用户对该职位的举报记录
        ReportMO reportMO = reportJobRepository.findByReportUserIdAndJobId(userId, jobId);

        return reportMO == null ? false : true;
    }




    /**
     * 分页查询举报记录列表，提给给admin端使用
     *
     * @param reportJobBO 查询条件
     * @param page         第几页
     * @param pageSize     每页显示多少条
     * @return PagedGridResult
     */
    @Override
    public PagedGridResult pagedReportRecordList(SearchReportJobBO reportJobBO, Integer page, Integer pageSize) {

        // 先把BO里面的查询条件拿出来
        String jobName = reportJobBO.getJobName();
        String companyName = reportJobBO.getCompanyName();
        String reportUserName = reportJobBO.getReportUserName();
        Integer dealStatus = reportJobBO.getDealStatus();
        LocalDateTime beginDate = reportJobBO.getBeginDateTime();
        LocalDateTime endDate = reportJobBO.getEndDateTime();

        //* 这里不能JPA的方式去查询，因为起始时间和结束时间是动态的，JPA不支持动态查询，使用MongoTemplate来实现
        // 1. 创建查询对象
        Query query = new Query();

        // 2. 创建条件对象
        Criteria criteria = new Criteria();

        // 3. 设置查询条件参数
        if (StringUtils.isNotBlank(jobName)) {
            query = addLikeByValue(query, "job_name", jobName);
        }
        if (StringUtils.isNotBlank(companyName)) {
            query = addLikeByValue(query, "company_name", companyName);
        }
        if (StringUtils.isNotBlank(reportUserName)) {
            query = addLikeByValue(query, "report_user_name", reportUserName);
        }

        if (dealStatus != null) {
            query.addCriteria(Criteria.where("deal_status").is(dealStatus));
        }

        if (beginDate != null && endDate == null) {
            query.addCriteria(Criteria.where("created_time").gte(beginDate));
        } else if (beginDate == null && endDate != null) {
            query.addCriteria(Criteria.where("created_time").lte(endDate));
        } else if (beginDate != null && endDate != null) {
            query.addCriteria(Criteria.where("created_time").gte(beginDate).lte(endDate));
        }

        // 4. 查询记录总数，必须在分页前查询，否则总数不对
        long counts = mongoTemplate.count(query, ReportMO.class);

        // 5. 设置分页(注意：MongoDB的分页是从0开始的)
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "created_time");
        query.with(pageable); // with的意思是使用

        // 6. 执行查询
        List<ReportMO> list = mongoTemplate.find(query, ReportMO.class);

        // 7. 封装分页grid信息数据
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(counts);

        return gridResult;
    }


    // 模糊查询
    private Query addLikeByValue(Query query, String key, String value) {

        // ^ - 匹配字符串的开始位置
        // .* - 匹配任意字符（.）零次或多次（*）
        // value - 你要搜索的具体字符串（动态插入的值）
        // .* - 再次匹配任意字符零次或多次
        // $ - 匹配字符串的结束位置

        // 拼接 正则表达式和查询参数
        Pattern pattern = Pattern.compile("^.*" + value + ".*$");
        // 指定要查询的属性
        query.addCriteria(Criteria.where(key).regex(pattern)); // 这里就类似于 SQL 语句中的 LIKE '%value%'
        return query;
    }



    /**
     * 更新举报记录的处理状态
     *
     * @param reportId   举报记录ID
     * @param status 处理状态
     */
    @Override
    @Transactional
    public void updateReportRecordStatus(String reportId, DealStatus status) {
        // 给MongoDB创建查询对象
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(reportId));

        Update update = new Update();
        update.set("deal_status", status.type);
        update.set("updated_time", LocalDateTime.now());

        // 如果是删除职位，就需要把职位状态改为删除，并且把Redis中的职位详情给删除掉
        if (status == DealStatus.DONE) {
            // 从MongoDB中拿到举报记录
            ReportMO tmp = reportJobRepository.findById(reportId).get();

            // 修改职位状态(同步到MySQL数据库中)
            String jobId = tmp.getJobId();
            Job pending = new Job();
            pending.setId(jobId);
            pending.setStatus(JobStatus.DELETE.type);
            pending.setViolateReason(tmp.getReportReason());
            pending.setUpdatedTime(LocalDateTime.now());
            jobMapper.updateById(pending);

            // 删除Redis中的职位详情
            Job job = jobMapper.selectById(jobId);
            redis.del(REDIS_JOB_DETAIL +
                    ":" + job.getCompanyId() +
                    ":" + job.getHrId() +
                    ":" + jobId);
        }

        // 正式向MongoDB中执行更新
        mongoTemplate.updateFirst(query, update, ReportMO.class);
    }
}
