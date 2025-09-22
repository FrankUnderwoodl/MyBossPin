package com.yufeng.service;

import com.yufeng.model.vo.SysParamsVO;

/**
 * <p>
 * 系统参数配置表，本表仅有一条记录 服务类
 * </p>
 *
 * @author Frank Underwood
 * @since 2025-06-16
 */
public interface SysParamsService {



    /**
     * 修改简历刷新最大次数
     *
     * @param maxCounts 允许用户刷新的最大次数
     * @param version   当前版本号(会结合乐观锁机制，比如zookeeper)
     * @return
     */
    public int updateMaxResumeRefreshCounts(Integer maxCounts, Integer version) throws Exception;




    /**
     * 获得刷新简历的最大次数(通过查数据库的方式)
     *
     * @return a SysParamsVO object containing system parameters
     */
    public SysParamsVO getSysParams();
}
