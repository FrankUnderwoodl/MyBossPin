package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @description: 编辑工作经历业务对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EditWorkExpBO {

    private String id;
    private String userId;
    private String resumeId;
    private String companyName;
    private String industry;
    private String beginDate;
    private String endDate;
    private String position;
    private String department;
    private String content;
    private String contentHtml;
}
