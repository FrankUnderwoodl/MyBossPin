package com.yufeng.service.impl;

import com.yufeng.mapper.StuMapper;
import com.yufeng.pojo.Stu;
import com.yufeng.service.StuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class StuServiceImpl implements StuService {

    @Autowired
    StuMapper stuMapper;

    @Transactional
    @Override
    public void save(Stu stu) {
        stuMapper.insert(stu);
    }
}
