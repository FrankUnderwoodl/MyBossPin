package com.yufeng.service.impl;

import com.yufeng.pojo.Interview;
import com.yufeng.mapper.InterviewMapper;
import com.yufeng.service.InterviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 面试邀约表
本表为次表，可做冗余，可以用mongo或者es替代 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

}
