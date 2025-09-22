package com.yufeng.service;

import com.yufeng.model.bo.ModifyUserBO;
import com.yufeng.model.pojo.Users;
import com.yufeng.utils.PagedGridResult;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-08-02 19:56:30
 */
public interface UserService {


    /**
     * 修改用户信息
     * @param userBO 用户信息的业务对象
     */
    public void modifyUserInfo(ModifyUserBO userBO);


    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 返回用户信息
     */
    public Users getById(String userId);




    /**
     * 根据公司ID查询HR统计信息
     * @param companyId 公司ID
     * @return 返回公司HR统计信息
     */
    public Long getHRCountsByCompanyId(String companyId);



    /**
     * 绑定公司与HR的关系
     * @param userId 用户ID
     * @param companyId 公司ID
     */
    public void updateUserCompanyId(String userId, String realname, String companyId);



    /**
     * 刷新用户信息成为HR
     * @param userId 用户ID
     */
    public void changeUserToHR(String userId);



    /**
     * 刷新用户信息成为候选人
     * @param hrUserId HR用户ID
     */
    public void changeUserToCand(String hrUserId);



    /**
     * 获取某个公司的HR列表
     * @param companyId 公司ID
     * @param page 页码
     * @param pageSize 每页条数
     */
    public PagedGridResult getHRList(String companyId, Integer page, Integer pageSize);




    /**
     * 根据用户ID列表查询用户信息列表
     * @param userIds 用户ID列表
     * @return 返回用户信息列表
     */
    public List<Users> getByIdList(List<String> userIds);
}
