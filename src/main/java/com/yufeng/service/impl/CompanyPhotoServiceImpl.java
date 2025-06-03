package com.yufeng.service.impl;

import com.yufeng.pojo.CompanyPhoto;
import com.yufeng.mapper.CompanyPhotoMapper;
import com.yufeng.service.CompanyPhotoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 企业相册表，本表只存企业上传的图片 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class CompanyPhotoServiceImpl extends ServiceImpl<CompanyPhotoMapper, CompanyPhoto> implements CompanyPhotoService {

}
