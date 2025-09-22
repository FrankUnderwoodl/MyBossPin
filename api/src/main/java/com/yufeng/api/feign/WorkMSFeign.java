package com.yufeng.api.feign;

import com.yufeng.grace.result.GraceJSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lzm
 * @CreateTime 2025年7月30日 02:39
 * @describe
 */
@FeignClient("work-service")  // 本接口为调用work-service的Feign客户端(声明式客户端远程调用)
public interface WorkMSFeign {

    @PostMapping("/resume/init")
    public GraceJSONResult init(@RequestParam("userId") String userId); // 这里一定要用@RequestParam注解，因为Feign会将参数转换为查询字符串的形式传递给远程服务，如果不加注解，Feign会将参数作为请求体传递，这样就无法正确匹配到远程服务的接口了

}
