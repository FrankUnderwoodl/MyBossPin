package com.yufeng.filter;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.properties.IncludeUrlProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年7月9日14:35:40
 */
@Slf4j
@Component
public class BlackIPLimitFilter extends BaseInfoProperties implements GlobalFilter, Ordered {

    // 注入需要进行IP限流校验的路由配置
    @Autowired
    private IncludeUrlProperties includeUrlProperties;

    // 从yaml文件中导入相关的配置：
    // 一个IP可以连续请求的最大次数
    @Value("${blackIP.continueCounts}")
    private Integer continueCounts;
    // 连续请求的时间间隔
    @Value("${blackIP.timeInterval}")
    private Integer timeInterval;
    // 获取限制时间
    @Value("${blackIP.limitTimes}")
    private Integer limitTimes;



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1️⃣获取当前的请求路径
        String url = exchange.getRequest().getURI().getPath();

        // 2️⃣获取所有需要进行IP限流校验的URL列表
        List<String> ipLimitList = includeUrlProperties.getIpLimitUrls();

        // 3️⃣校验并判断
        if (ipLimitList.contains(url)) {
           // 如果匹配到，则表明需要进行IP的拦截校验
            log.info("BlackIPLimitFilter - 拦截到需要进行IP限流校验的方法：URL = " + url);
            // 4️⃣调用doLimit方法进行IP限流校验
            return doLimit(exchange, chain);
        }

        // 4️⃣默认直接放行
        return chain.filter(exchange);
    }


    /**
     * 其实这个需求贼简单，就在redis定义两个键就好了：一个是记录某IP请求次数的键，另一个是记录某IP黑名单剩余时间的键
     * 需求：
     * 判断ip在20s内是否连续请求超过3次
     * 如果超过3次，则将该ip加入黑名单，也就是限制该ip在30s内不能再请求(关小黑屋)
     * 等待30s后，才能再请求
     */
    public Mono<Void> doLimit(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1️⃣根据exchange获取请求的IP地址
        ServerHttpRequest request = exchange.getRequest();
        String ip = IPUtil.getIP(request);

        // 2️⃣定义两个Redis Key
        // 获取该IP请求总次数的key
        final String ipRedisKey = "gateway-ip:count:" + ip;
        // 获取该ip在黑名单中的key
        final String ipRedisLimitedKey = "gateway-ip:limit:" + ip;

        // 3️⃣判断该IP是否在黑名单中，也就是redis中是否存在ipRedisLimitedKey这个键(通过获取这个键的ttl值来判断)
        long limitLeftTimes = redis.ttl(ipRedisLimitedKey); // ttl是获取键的剩余时间，单位是秒，如果没有定义这个键，则返回-2
        if (limitLeftTimes > 0) {
            // 如果在黑名单中，终止请求，返回错误信息
            log.warn("IP {} is in the black list, remaining limit time: {} seconds", ip, limitLeftTimes);
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }

        // 4️⃣将该IP的访问次数存入Redis，并获得当前的访问次数
        // ⚠️注意：这里的increment方法会自动处理键不存在的情况，如果键不存在，则会创建一个新的键并将其值设置为1返回给我们；如果键存在，则会将其值加1并返回新的值。
        long currentCount = redis.increment(ipRedisKey,1);
        log.info("IP {} has made {} requests", ip, currentCount);

        // 5️⃣判断如果该IP是第一次访问，则需要给这个ipRedisKey设置过期时间，因为是限制同一ip在10s，注意是10内连续访问某个接口达到3次以上嘛
        if (currentCount == 1) {
            // 主动设置该IP的访问次数键的过期时间为timeInterval秒
            redis.expire(ipRedisKey, timeInterval);
            log.info("IP {} is making its first request, setting expiration time to {} seconds", ip, timeInterval);
        }

        // 6️⃣判断当前的访问次数是否超过了限制次数，如果超过了，则将该IP加入黑名单并不放行
        if (currentCount > continueCounts) {
            // 如果超过了限制次数，则将该IP加入黑名单
            log.warn("IP {} has exceeded the limit, adding to black list", ip);
            // 将该IP加入黑名单，其实就是设置过期时间为30秒
            redis.set("gateway-ip:limit:" + ip, "1", limitTimes);
            // 返回错误信息，也就是不放行该请求
            return renderErrorMsg(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP);
        }

        return chain.filter(exchange);
    }



    public Mono<Void> renderErrorMsg(ServerWebExchange exchange, ResponseStatusEnum status) {

        // 1.获得response
        ServerHttpResponse response = exchange.getResponse();
        // 2.构建jsonResult
        GraceJSONResult graceJSONResult = GraceJSONResult.exception(status);
        // 3.手动修改response的响应码为500
        response.setStatusCode(HttpStatus.BAD_GATEWAY);

        // 4.设置response的content-type为application/json，也就是告诉前端响应的内容是JSON格式
        // 如果之前的代码或者中间件已经设置了Content-Type，就不会覆盖它，避免在响应头中出现重复的Content-Type键，这可能会导致某些客户端解析错误
        if (!response.getHeaders().containsKey("Content-Type")) {
            // 第一个参数 "Content-Type"：HTTP响应头的键名，用于指定响应内容的媒体类型
            // 第二个参数 MimeTypeUtils.APPLICATION_JSON_VALUE：HTTP响应头的值，这是一个常量，值为 "application/json"
            // 这行代码的作用是告诉客户端（浏览器或前端应用）返回的内容是JSON格式，这样客户端就知道如何正确解析响应数据。
            response.getHeaders().add("Content-Type", MimeTypeUtils.APPLICATION_JSON_VALUE);
        }

        // 5.将jsonResult写入response的body中
        String resultJson = new Gson().toJson(graceJSONResult);

        // 创建一个DataBuffer对象，用于将JSON字符串转换为字节数组
        // 这里的DataBuffer类似于NIO中的ByteBuffer，用于在响应中传输数据
        DataBuffer dataBuffer = response.bufferFactory().wrap(resultJson.getBytes(StandardCharsets.UTF_8));
        // 这里的Mono.just(dataBuffer)表示异步地将数据写入响应体中，类似于JavaScript中的Promise.resolve(dataBuffer)
        return response.writeWith(Mono.just(dataBuffer));
    }


    // 这里要比jwt验证要慢执行，因为你都没有进行jwt验证，直接就拦截了
    @Override
    public int getOrder() {
        return 1;
    }
}
