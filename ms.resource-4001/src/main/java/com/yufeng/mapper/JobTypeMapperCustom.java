package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.JobType;
import com.yufeng.model.vo.JobTypeSecondAndThirdVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位类别 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-07-12
 */
@Repository
public interface JobTypeMapperCustom extends BaseMapper<JobType> {

    public List<JobType> getThirdJobTypeByTop(@Param("paramMap") Map<String, Object> map);

    public List<JobTypeSecondAndThirdVO> getSecondAndThirdListByTop(@Param("paramMap") Map<String, Object> map);

}
