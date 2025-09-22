
package com.yufeng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yufeng.model.pojo.CompanyPhoto;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 企业相册表，本表只存企业上传的图片 Mapper 接口
 * </p>
 *
 * @author 风间影月
 * @since 2022-09-04
 */
@Repository
public interface CompanyPhotoMapper extends BaseMapper<CompanyPhoto> {

}
