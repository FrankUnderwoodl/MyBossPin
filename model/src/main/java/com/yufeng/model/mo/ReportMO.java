package com.yufeng.model.mo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @author Lzm
 * @CreateTime 2025年9月08日 21:15
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document("report_jop") // 表映射
public class ReportMO {

    @Id
    private String id;

    @Field("job_id")
    private String jobId;

    @Field("job_name")
    private String jobName;

    @Field("company_name")
    private String companyName;

    @Field("report_user_id")
    private String reportUserId;

    @Field("report_user_name")
    private String reportUserName;

    @Field("report_reason")
    private String reportReason;

    @Field("deal_status")
    private Integer dealStatus;

    @Field("remark")
    private String remark;

    @Field("created_time")
    private LocalDateTime createdTime;
    @Field("updated_time")
    private LocalDateTime updatedTime;
}
