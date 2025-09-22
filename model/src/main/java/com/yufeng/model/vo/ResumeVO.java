package com.yufeng.model.vo;

import com.yufeng.model.pojo.ResumeEducation;
import com.yufeng.model.pojo.ResumeProjectExp;
import com.yufeng.model.pojo.ResumeWorkExp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResumeVO {

    private String id;
    private String userId;
    private String advantage;
    private String advantageHtml;
    private String credentials;
    private String skills;
    private String status;
    private LocalDateTime refreshTime;

    private List<ResumeWorkExp> workExpList;         // 工作经验
    private List<ResumeProjectExp> projectExpList;   // 项目经验
    private List<ResumeEducation> educationList;     // 教育经历

}
