
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EditProjectExpBO {

    private String id;
    private String userId;
    private String resumeId;
    private String projectName;
    private String roleName;
    private String beginDate;
    private String endDate;
    private String content;
    private String contentHtml;

}
