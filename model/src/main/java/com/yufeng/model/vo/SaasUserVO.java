package com.yufeng.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Lzm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SaasUserVO {
    private String username;
    private String name;
    private String face;
}
