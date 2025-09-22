package com.yufeng.repository;

import com.yufeng.model.mo.ReportMO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lzm
 * @CreateTime 2025年9月08日 22:06
 * 理解：
 * 这里你其实可以看成是一个Mapper
 * 这里的MongoRepository你可以看成mybatis-plus中的BaseMapper
 */
@Repository
public interface ReportJobRepository extends MongoRepository<ReportMO, String> { // 这里的泛型分别是MO和主键ID类型


    /**
     * 向MongoDB中，查询用户是否已经举报过该职位(通过用户ID和职位ID联合查询)
     * @param userId 用户ID
     * @param jobId 职位ID
     * @return 举报记录，如果没有则返回null
     */
    public ReportMO findByReportUserIdAndJobId(String userId, String jobId);
}
