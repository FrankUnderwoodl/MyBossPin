package com.yufeng.service.impl;

import com.yufeng.pojo.Job;
import com.yufeng.mapper.JobMapper;
import com.yufeng.service.JobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * HR发布的职位表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

}
