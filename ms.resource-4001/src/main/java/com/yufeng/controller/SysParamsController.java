package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.model.vo.SysParamsVO;
import com.yufeng.service.SysParamsService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 * 这个的类的作用是：系统参数相关的接口
 */
@RestController
@RequestMapping("/sys")
public class SysParamsController extends BaseInfoProperties {

    @Autowired
    private SysParamsService sysParamsService;

    // @Autowired
    // private ZKConnector zkConnector;


    // @Autowired()  根据类型注入
    @Resource(name = "curatorClient") // 根据名称注入
    private CuratorFramework zkClient;


    /**
     * 修改简历刷新最大次数，提供给admin端使用
     *
     * @param maxCounts 允许用户刷新的最大次数
     * @param version   当前版本号(会结合乐观锁机制，比如zookeeper)
     * @return a GraceJSONResult indicating the success of the operation
     */
    @PostMapping("/modifyMaxResumeRefreshCounts2")
    public GraceJSONResult modifyMaxResumeRefreshCounts2(Integer maxCounts, Integer version) throws Exception {
        // 判空处理
        if (maxCounts == null || maxCounts < 1) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_PARAMS_SETTINGS_ERROR);
        }

        // 因为可能他妈的有多个admin用户同时去修改这个值，所以需要用到分布式锁
        // ZKLock zkLock = zkConnector.getZKLock("mybosspin-lock"); // 获取命名空间
        // zkLock.get(); // 获得锁

        // 获取zookeeper的分布式锁(可重入锁)
        InterProcessMutex processMutex = new InterProcessMutex(zkClient, "/mutex-locks");
        processMutex.acquire();

        // 调用service层去修改简历的最大刷新次数
        try {
            sysParamsService.updateMaxResumeRefreshCounts(maxCounts, version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            processMutex.release(); // 释放锁
        }

        // zkLock.release(); // 释放锁
        // TODO version zookeeper的乐观锁机制(后面再补充)
        return GraceJSONResult.ok(0);
    }


    @PostMapping("/modifyMaxResumeRefreshCounts")
    public GraceJSONResult modifyMaxResumeRefreshCounts(Integer maxCounts, Integer version) throws Exception {
        // 判空处理
        if (maxCounts == null || maxCounts < 1) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_PARAMS_SETTINGS_ERROR);
        }

        // 调用service层去修改简历的最大刷新次数
        int updateVersion = sysParamsService.updateMaxResumeRefreshCounts(maxCounts, version);
        return GraceJSONResult.ok(updateVersion);
    }


    /**
     * 获得刷新简历的最大次数
     *
     * @return a GraceJSONResult indicating the success of the operation
     */
    @PostMapping("/params2")
    public GraceJSONResult params2() throws Exception {

        // 可以加一个zookeeper读写锁
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(zkClient, "/read-write-locks");
        readWriteLock.readLock().acquire();

        // 调用service层去查询简历的最大刷新次数
        SysParamsVO sysParamsVO = null;
        try {
            sysParamsVO = sysParamsService.getSysParams();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readWriteLock.readLock().release();
        }
        return GraceJSONResult.ok(sysParamsVO);
    }


    @PostMapping("/params")
    public GraceJSONResult params() throws Exception {
        // 调用service层去查询简历的最大刷新次数
        // SysParamsVO sysParamsVO = sysParamsService.getSysParams();  // server层已经将POJO转成了VO

        // 改成从zookeeper中读取
        String path = "/" + ZK_MAX_RESUME_REFRESH_COUNTS;

        // 获取数值
        String dataString = new String(zkClient.getData().forPath(path)); // 因为已经进行了缓存预热，所以你吃不用担心空指针异常
        Integer maxCounts = Integer.valueOf(dataString);
        // 获取版本号
        int version = zkClient.checkExists().forPath(path).getVersion();

        // 封装到VO中
        SysParamsVO sysParamsVO = new SysParamsVO();
        sysParamsVO.setMaxResumeRefreshCounts(maxCounts);
        sysParamsVO.setVersion(version);

        return GraceJSONResult.ok(sysParamsVO);
    }
}
