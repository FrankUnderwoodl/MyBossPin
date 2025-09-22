
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.model.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Lzm
 * @CreateTime 2025年7月17日 23:09
 * @Description 短信内容的传输对象，用来给RabbitMQ发送短信内容
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SMSContentQO {
    private String mobile;
    private String content;
}
