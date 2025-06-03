package com.yufeng.service;

import com.yufeng.pojo.Interview;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 面试邀约表
本表为次表，可做冗余，可以用mongo或者es替代 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
public interface InterviewService extends IService<Interview> {

}
