package com.yufeng.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SysParamsVO {

    private Integer id;
    private Integer maxResumeRefreshCounts;

    // 这个版本号是用来做乐观锁的
    private Integer version;

}
