package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.model.bo.ModifyUserBO;
import com.yufeng.enums.UserRole;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mapper.UsersMapper;
import com.yufeng.model.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-03
 */
@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {


    @Autowired
    private UsersMapper usersMapper;



    /**
     * 修改用户信息
     * 供给APP端调用
     *
     * @param userBO 用户信息修改对象
     */
    @Override
    @Transactional
    public void modifyUserInfo(ModifyUserBO userBO) {

        // 对BO进行校验
        String userId = userBO.getUserId();
        if (StringUtils.isBlank(userId)) {
            GraceException.doException(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }

        // 执行更新操作，创建一个新的Users对象，并将BO的属性复制到该对象中，然后调用mapper进行更新操作
        Users pendingUser = new Users();
        BeanUtils.copyProperties(userBO, pendingUser); // 将BO的属性复制到实体类中
        pendingUser.setId(userId);
        pendingUser.setUpdatedTime(LocalDateTime.now());

        // 正式调用mapper层来对MySQL进行更新操作
        usersMapper.updateById(pendingUser);
    }




    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 返回用户信息
     */
    @Override
    public Users getById(String userId) {
        // 对用户ID进行校验
        if (StringUtils.isBlank(userId)) {
            GraceException.doException(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 调用mapper来查询用户信息
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            GraceException.doException(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        return user;
    }



    /**
     * 根据公司ID查询HR统计信息
     *
     * @param companyId 公司ID
     * @return 返回公司HR统计信息
     */
    @Override
    public Long getHRCountsByCompanyId(String companyId) {

        // 直接调用mapper来查询HR数量
        // 底层SQL语句：SELECT COUNT(*) FROM users WHERE hr_in_which_company_id = ?
        Long count = usersMapper.selectCount(new QueryWrapper<Users>()
                .eq("hr_in_which_company_id", companyId));

        return count != null ? count : 0L; // 如果count为null，返回0
    }



    /**
     * 绑定公司与HR的关系
     *
     * @param userId    用户ID
     * @param companyId 公司ID
     */
    @Override
    @Transactional
    public void updateUserCompanyId(String userId, String realname, String companyId) {

        // 创建一个新的Users对象，并设置新的用户ID和公司ID
        Users pendingUser = new Users();
        pendingUser.setId(userId);
        pendingUser.setRealName(realname);
        pendingUser.setHrInWhichCompanyId(companyId);
        pendingUser.setUpdatedTime(LocalDateTime.now());

        // 调用mapper层来更新用户信息(不用担心pendingUser对象中有null值，因为MyBatis-Plus的updateById方法会忽略null值)
        usersMapper.updateById(pendingUser);
    }



    /**
     * 刷新用户信息成为HR(其实就是创建一个POJO对象，然后设置用户字段中的role字段为2，2表示能求职又能招聘)
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional // 其实不用加，因为是分布式事务调用，不用担心会回滚不了
    public void changeUserToHR(String userId) {

        Users hrUser = new Users();
        hrUser.setId(userId);
        hrUser.setRole(UserRole.RECRUITER.type);
        hrUser.setUpdatedTime(LocalDateTime.now());

        // 调用mapper层来更新用户信息
        usersMapper.updateById(hrUser);
    }


    @Override
    @Transactional
    public void changeUserToCand(String hrUserId) {

        // 想要修改数据库的数据，都先创建一个新的POJO对象，然后设置需要修改的字段
        Users candUser = new Users();
        candUser.setId(hrUserId);
        candUser.setRole(UserRole.CANDIDATE.type);

        // update-strategy: not_empty  # 当更新时，忽略空值，比如null和空字符串不会覆盖原先的值
        //* 这句代码有问题，因为我的mybatis-plus的更新策略是not_empty，null值不会更新到数据库中
        // candUser.setHrInWhichCompanyId(null);
        candUser.setHrInWhichCompanyId("0");

        candUser.setUpdatedTime(LocalDateTime.now());

        // 调用mapper层来更新用户信息
        usersMapper.updateById(candUser);
    }

    /**
     * 获取某个公司的HR列表
     *
     * @param companyId 公司ID
     * @param page      页码
     * @param limit     每页条数
     * @return 返回公司HR列表
     */
    @Override
    public PagedGridResult getHRList(String companyId, Integer page, Integer limit) {

        // 开始分页
        PageHelper.startPage(page, limit);

        // 调用mapper层来获取HR列表
        // 底层SQL语句：SELECT * FROM users WHERE hr_in_which_company
        List<Users> usersList = usersMapper.selectList(new QueryWrapper<Users>()
                .eq("hr_in_which_company_id", companyId));


        return setterPagedGrid(usersList, page);
    }



    /**
     * 根据用户ID列表查询用户信息列表
     *
     * @param userIds 用户ID列表
     * @return 返回用户信息列表
     */
    @Override
    public List<Users> getByIdList(List<String> userIds) {
        // 判空处理
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }

        // 调用mapper层来查询用户信息列表
        // 这里底层的SQL语句是： SELECT * FROM users WHERE id IN (?, ?, ?, ...)， MySQL 会将 IN 列表中的所有 ID 值进行排序，然后通过主键索引（B+ 树）批量查找
        return usersMapper.selectBatchIds(userIds);
    }
}
