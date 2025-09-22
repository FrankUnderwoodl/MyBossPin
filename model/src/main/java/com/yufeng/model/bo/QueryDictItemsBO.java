
package com.yufeng.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Lzm
 * 这是有关于接收前端所传来的查询字典项的业务对象
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QueryDictItemsBO {
    private String[] advantage;  // 优势
    private String[] benefits;   // 福利
    private String[] bonus;      // 奖金
    private String[] subsidy;    // 补贴
}
