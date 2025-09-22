package com.yufeng.model.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * @author Lzm
 * @describe 用于admin传递过来的查询公司信息的业务对象
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QueryCompanyBO {

    private String companyName;    // 公司名称
    private String commitUser;     // 提交人
    private Integer reviewStatus;  // 审核状态

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate commitDateStart; // 提交开始日期

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private LocalDate commitDateEnd; // 提交结束日期

}
