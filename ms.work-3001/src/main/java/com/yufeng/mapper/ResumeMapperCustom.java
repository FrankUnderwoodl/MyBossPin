package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.Resume;
import com.yufeng.model.vo.SearchResumesVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 简历表 Mapper 接口
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Repository
public interface ResumeMapperCustom extends BaseMapper<Resume> {

    public List<SearchResumesVO> searchResumesList(@Param("paramMap")Map<String, Object> paramMap);
}
