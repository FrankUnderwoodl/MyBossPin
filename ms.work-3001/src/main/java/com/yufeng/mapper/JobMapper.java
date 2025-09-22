package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.Job;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * HR发布的职位表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository
public interface JobMapper extends BaseMapper<Job> {

}
