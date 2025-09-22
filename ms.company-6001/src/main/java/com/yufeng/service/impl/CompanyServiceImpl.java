package com.yufeng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.CompanyReviewStatus;
import com.yufeng.enums.YesOrNo;
import com.yufeng.mapper.CompanyMapper;
import com.yufeng.mapper.CompanyMapperCustom;
import com.yufeng.mapper.CompanyPhotoMapper;
import com.yufeng.model.bo.CreateCompanyBO;
import com.yufeng.model.bo.ModifyCompanyInfoBO;
import com.yufeng.model.bo.QueryCompanyBO;
import com.yufeng.model.bo.ReviewCompanyBO;
import com.yufeng.model.pojo.Company;
import com.yufeng.model.pojo.CompanyPhoto;
import com.yufeng.model.vo.CompanyInfoVO;
import com.yufeng.service.CompanyService;
import com.yufeng.utils.LocalDateUtils;
import com.yufeng.utils.PagedGridResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业表 服务实现类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
@Slf4j
@Service
public class CompanyServiceImpl extends BaseInfoProperties implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private CompanyMapperCustom companyMapperCustom;

    @Autowired
    private CompanyPhotoMapper companyPhotoMapper;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public Company getByFullName(String fullName) {

        // 向MySQL中查询公司全称对应的公司信息
        // 这里的queryWrapper相当于SQL中的WHERE条件
        return companyMapper.selectOne(new QueryWrapper<Company>().eq("company_name", fullName));
    }


    @Override
    @Transactional
    public String createNewCompany(CreateCompanyBO createCompanyBO) {

        // 一看到BO，就想到要将BO转换为PO(BeanUtils.copyProperties)
        Company company = new Company();
        BeanUtils.copyProperties(createCompanyBO, company);

        // 将PO信息完整化
        company.setIsVip(YesOrNo.NO.type); // 默认不是VIP
        company.setReviewStatus(CompanyReviewStatus.NOTHING.type); // 默认未发起审核认证
        company.setCreatedTime(LocalDateTime.now()); // 设置创建时间为当前时间
        company.setUpdatedTime(LocalDateTime.now()); // 设置更新时间为当前时间

        // 正式插入到MySQL中
        companyMapper.insert(company);

        // 返回新创建的公司的ID
        return company.getId();
    }


    @Override
    @Transactional
    public String resetNewCompany(CreateCompanyBO createCompanyBO) {

        // 一看到BO，就想到要将BO转换为PO(BeanUtils.copyProperties)
        Company company = new Company();
        BeanUtils.copyProperties(createCompanyBO, company);

        // 将PO信息完整化
        company.setId(createCompanyBO.getCompanyId()); // BO中的companyId是PO的主键ID，需要设置进去
        company.setReviewStatus(CompanyReviewStatus.NOTHING.type); // 从审核失败状态重置为未发起审核认证
        company.setUpdatedTime(LocalDateTime.now()); // 设置更新时间为当前时间

        // 正式更新到MySQL中
        companyMapper.updateById(company);

        return company.getId();
    }


    @Override
    public Company getCompanyById(String companyId) {

        // 向MySQL中查询公司ID对应的公司信息
        // 这里的queryWrapper相当于SQL中的WHERE条件
        return companyMapper.selectOne(new QueryWrapper<Company>().eq("id", companyId));
    }


    /**
     * 提交审核的企业信息(其实底层就是更新记录到MySQL中)
     *
     * @param reviewCompanyBO 审核公司业务对象
     */
    @Override
    @Transactional
    public void commitReviewCompanyInfo(ReviewCompanyBO reviewCompanyBO) {

        // 一看到BO，就想到要将BO转换为PO
        Company pendingCompany = new Company();
        pendingCompany.setId(reviewCompanyBO.getCompanyId());                // 设置要更新的公司ID

        // 设置提交审核的相关信息
        pendingCompany.setReviewStatus(CompanyReviewStatus.REVIEW_ING.type); // 设置审核状态为审核中
        pendingCompany.setReviewReplay("");                                  // 如果有内容，则重置覆盖之前的审核意见
        pendingCompany.setAuthLetter(reviewCompanyBO.getAuthLetter());       // 设置企业授权书

        // 设置提交审核的用户信息
        pendingCompany.setCommitUserId(reviewCompanyBO.getHrUserId());       // 设置提交审核的HR用户ID
        pendingCompany.setCommitUserMobile(reviewCompanyBO.getHrMobile());   // 设置提交审核的HR用户手机号
        pendingCompany.setCommitDate(LocalDate.now());                       // 设置提交审核的日期

        // 设置更新时间为当前时间
        pendingCompany.setUpdatedTime(LocalDateTime.now());                  // 设置更新时间为当前时间


        // 正式更新记录到MySQL中
        companyMapper.updateById(pendingCompany);
    }


    /**
     * 查询公司列表
     *
     * @param companyBO 查询公司业务对象
     * @param page      当前页码
     * @param limit     每页记录数
     * @return 返回分页结果
     */
    @Override
    public PagedGridResult queryCompanyList(QueryCompanyBO companyBO, Integer page, Integer limit) {

        // 开启分页
        PageHelper.startPage(page, limit);

        // 构建一个Map，将前端需要的查询条件放入Map中(这里的Map相当于SQL中的WHERE条件)
        Map<String, Object> map = new HashMap<>();
        map.put("companyName", companyBO.getCompanyName());
        map.put("realName", companyBO.getCommitUser());
        map.put("reviewStatus", companyBO.getReviewStatus());
        map.put("commitDateStart", companyBO.getCommitDateStart());
        map.put("commitDateEnd", companyBO.getCommitDateEnd());

        // 调用自定义Mapper查询公司列表
        List<CompanyInfoVO> list = companyMapperCustom.queryCompanyInfoList(map);

        // 将查询结果转换为分页格式
        return setterPagedGrid(list, page);
    }




    /**
     * 根据企业ID查询数据库获得最新企业信息
     *
     * @param companyId 企业ID
     * @return 返回查询结果
     */
    @Override
    public CompanyInfoVO getCompanyInfo(String companyId) {

        return companyMapperCustom.getCompanyInfo(companyId);
    }



    /**
     * 更新审核后的信息
     *
     * @param reviewCompanyBO 审核公司业务对象
     */
    @Override
    @Transactional
    public void updateReviewInfo(ReviewCompanyBO reviewCompanyBO) {

        // 看见BO，就想到要将BO转换为PO
        Company pendingCompany = new Company();
        pendingCompany.setId(reviewCompanyBO.getCompanyId()); // 设置要更新的公司ID
        pendingCompany.setReviewStatus(reviewCompanyBO.getReviewStatus()); // 设置审核状态
        pendingCompany.setReviewReplay(reviewCompanyBO.getReviewReplay()); // 设置审核意见
        pendingCompany.setUpdatedTime(LocalDateTime.now()); // 设置更新时间为当前时间

        // 正式更新记录到MySQL中
        companyMapper.updateById(pendingCompany);
    }



    /**
     * 更新企业信息
     *
     * @param modifyCompanyInfoBO 企业对象
     */
    @Override
    @Transactional
    public void updateCompanyInfo(ModifyCompanyInfoBO modifyCompanyInfoBO) {

        // 定义锁的名称
        String distLock = "company_modify_lock:" + modifyCompanyInfoBO.getCompanyId();
        // 当前线程，通过Redisson来获取分布式锁
        RLock rLock = redissonClient.getLock(distLock);
        log.info("开始获取分布式锁，锁名称：{}", distLock);

        rLock.lock(); // 如果获取不到锁，则等待，直到获取到锁为止
        // 业务处理
        try {
            rLock.lock();
            log.info("开始更新企业信息，企业ID：{}", modifyCompanyInfoBO.getCompanyId());
            Thread.sleep(40000); // 这里设置40s的睡眠时间，是因为Redisson的看门狗默认是30s过期
            this.doModify(modifyCompanyInfoBO);
        } catch (Exception e) {
            log.error("更新企业信息异常，企业ID：{}", modifyCompanyInfoBO.getCompanyId(), e);
        } finally {
            // 最终释放锁
            log.info("结束更新企业信息，企业ID：{}", modifyCompanyInfoBO.getCompanyId());
            rLock.unlock(); // 如果超过了30s，Redisson的看门狗会自动续期，不用担心锁自动过期被释放
            rLock.unlock(); // 因为上面加了两次锁，所以这里要解锁两次
        }
    }

    private void doModify(ModifyCompanyInfoBO modifyCompanyInfoBO) {
        // 一看到BO，就想到要将BO转换为PO
        Company company = new Company();
        BeanUtils.copyProperties(modifyCompanyInfoBO, company);

        // 设置要更新的公司ID
        String companyId = modifyCompanyInfoBO.getCompanyId();
        company.setId(companyId);

        // 设置更新时间为当前时间
        company.setUpdatedTime(LocalDateTime.now());

        // 正式更新记录到MySQL中
        companyMapper.updateById(company);

        // del掉企业的redis缓存
        redis.del(REDIS_COMPANY_MORE_INFO + ":" + companyId);
        redis.del(REDIS_COMPANY_BASE_INFO + ":" + companyId);
    }


    /**
     * 保存企业的照片信息
     *
     * @param modifyCompanyInfoBO 企业信息业务对象
     */
    @Override
    @Transactional
    public void savePhotos(ModifyCompanyInfoBO modifyCompanyInfoBO) {

        // 凡是涉及到数据的更新操作，都需要new一个POJO对象出来先
        CompanyPhoto companyPhoto = new CompanyPhoto();

        // 将前端传递过来的BO对象的属性值设置到POJO对象中
        String companyId = modifyCompanyInfoBO.getCompanyId();
        companyPhoto.setCompanyId(companyId);
        companyPhoto.setPhotos(modifyCompanyInfoBO.getPhotos());

        // 得先判断企业相册是否存在，存在：更新、不存在：插入
        CompanyPhoto tmpPhotos = this.getPhotos(companyId);
        if (tmpPhotos == null) {
            companyPhotoMapper.insert(companyPhoto);
        } else {
            companyPhotoMapper.update(companyPhoto,
                new UpdateWrapper<CompanyPhoto>().eq("company_id", companyId));
        }

    }


    // 从数据库中查询企业照片
    @Override
    public CompanyPhoto getPhotos(String companyId) {

        // 直接调用mapper层来查询该公司ID对应的企业照片信息
        QueryWrapper<CompanyPhoto> wrapper = new QueryWrapper<CompanyPhoto>()
                .eq("company_id", companyId);
        return companyPhotoMapper.selectOne(wrapper);
    }



    /**
     * 我给你一堆的companyId，你帮我把这些用户都变更为企业pojo
     * 供给user服务调用
     *
     * @param companyIds 公司ID列表
     * @return 返回公司列表
     */
    @Override
    public List<Company> getCompanyList(List<String> companyIds) {
        // 判空处理
        if (companyIds == null || companyIds.isEmpty()) {
            return null;
        }

        // 调用mapper层来查询用户信息列表
        // 这里底层的SQL语句是： SELECT * FROM users WHERE id IN (?, ?, ?, ...)， MySQL 会将 IN 列表中的所有 ID 值进行排序，然后通过主键索引（B+ 树）批量查找
        return companyMapper.selectBatchIds(companyIds);
    }



    /**
     * 判断企业是否是VIP(提给给网页端、App端，还有下单的时候调用)
     * 因为获取企业是否是vip，是一个高频操作，所以要结合redis
     *
     * @param companyId 企业ID
     * @return true/false
     */
    @Override
    public Boolean getIsVip(String companyId) {

        boolean vipCompany = false; // 默认不是vip

        // 先从redis中获取，如果没有，再从MySQL中获取，然后放入redis
        String vipStr = redis.get(REDIS_COMPANY_IS_VIP + ":" + companyId);
        if (StringUtils.isNotBlank(vipStr)) {
            // redis中有数据
            vipCompany = Boolean.parseBoolean(vipStr);
        } else {
            // redis中没有数据
            Company company = this.getCompanyById(companyId);
            if (company != null) {
                // 两个条件：
                Integer isVip = company.getIsVip(); // ①isVip=1
                LocalDate vipExpireDate = company.getVipExpireDate(); // ②vip_expire_date > 当前日期

                if (vipExpireDate != null) {
                    long expireDays = LocalDateUtils.getChronoUnitBetween(LocalDate.now(),
                            vipExpireDate,
                            ChronoUnit.DAYS,
                            false);
                    // isVip == 1 && 过期时间 >= 当前日期
                    if (isVip == YesOrNo.YES.type && expireDays >= 0) {
                        vipCompany = true;
                    }
                }
            }
        }
        // 将企业的vip状态放入redis，设置过期时间12小时
        redis.set(REDIS_COMPANY_IS_VIP + ":" + companyId, String.valueOf(vipCompany), 12 * 60 * 60);
        return vipCompany;
    }


    /**
     * 设置企业为VIP
     * @param companyId 企业ID
     * @param date  过期时间
     */
    @Override
    @Transactional
    public void setCompanyVip(String companyId, LocalDate date) {

        // 想要进行CRUD，你得来一个POJO
        Company company = new Company();
        company.setId(companyId);
        company.setIsVip(YesOrNo.YES.type); // 设置为VIP
        company.setVipExpireDate(date);    // 设置过期时间
        company.setUpdatedTime(LocalDateTime.now()); // 设置更新时间为当前时间

        // 正式更新到MySQL中
        companyMapper.updateById(company);
    }
}
