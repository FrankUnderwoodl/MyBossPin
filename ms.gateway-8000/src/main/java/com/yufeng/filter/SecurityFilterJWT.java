package com.yufeng.filter;

import com.google.gson.Gson;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.exception.GraceException;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年6月18日 02:31
 * @Description JWT安全过滤器，主要就是用来验证前端传来的JWT是否有效
 * 问:GlobalFilter怎么理解？
 * 答:GlobalFilter是Spring Cloud Gateway提供的一个接口，用于定义全局过滤器。全局过滤器会在每个请求处理之前执行，可以用于实现跨切面逻辑，比如认证、日志记录等。
 */
@Slf4j
@Component // 有了Spring容器，您不需要手动使用 new SecurityFilterJWT() 来创建对象，也可以在其他Spring组件中使用 @Autowired 注入这个对象
public class SecurityFilterJWT extends BaseInfoProperties implements GlobalFilter, Ordered {

    // 注入不需要token验证地址数据Bean(比如登录、注册、获取短信验证码等接口)
    @Autowired
    private ExcludeUrlProperties excludeUrlProperties;

    // 注入JWT工具类(用来验证jwt的有效性)
    @Autowired
    private JWTUtil jwtUtil;


    // 路径匹配的规则器(这是com.springframework.util包下的AntPathMatcher类，主要用来匹配路径模式)
    // 例如：可以用来匹配"/login/**"这样的路径模式，表示匹配以"/login/"开头的所有路径
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 前端传过来的JWT令牌的header名称
    public static final String HEADER_USER_TOKEN = "headerUserToken";


    /**
     * 过滤器的核心逻辑
     * @param exchange 包含请求和响应信息的容器, 你可以类比成HttpServletRequest和HttpServletResponse的组合体
     * @param chain 过滤器链，用来传递给下一个过滤器
     * @return Mono<Void>，这是响应式编程的概念，表示异步操作,这里<Void>表示没有返回值
     * 举例： 就像接力赛跑 🏃‍♂️，你拿到接力棒（exchange），处理完后传给下一个人（chain.filter）
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取传过来的请求路径
        String getUrl = exchange.getRequest().getURI().getPath();
        log.info("查看一下getRequest、getURI、getPath的值分别为: {},{},{}",
                exchange.getRequest(), // ServerHttpRequest [org.springframework.http.server.reactive.ReactorServerHttpRequest@1c236df8]
                exchange.getRequest().getURI(), // URI [http://localhost:8000/passport/getSMSCode?mobile=13961880025]
                exchange.getRequest().getURI().getPath()); // 路径部分 [ /passport/getSMSCode]

        // 2.获取不需要token验证的地址列表
        List<String> excludeList = excludeUrlProperties.getUrls();

        /*
          3.判断当前请求路径是否在不需要验证的地址列表中
          有两种可以对比URL的方式实现：
          方式一: excludeList.contains(getUrl)，适用于精确匹配
          例如：只有当请求路径完全等于"/login"时才匹配，而"/login/user"则不匹配
          ---
          方式二: antPathMatcher.match(excludeUrl, getUrl)，适用于通配符匹配
          例如：如果排除列表中有"/login/**"，那么"/login"、"/login/user"、"/login/admin/dashboard"等都会匹配
         */
        if(excludeList != null && !excludeList.isEmpty()) {
            if (excludeList.contains(getUrl)) {
                log.info("当前请求路径 ‘{}’ 在不需要验证的地址列表中，直接放行请求！", getUrl);
                return chain.filter(exchange); // 如果在列表中，直接放行请求
            }

          /*   for (String excludeUrl : excludeList) {
                if (antPathMatcher.match(excludeUrl, getUrl)) {
                    // 如果匹配到了exclude path，则直接放行请求
                    log.info("当前请求路径 '{}' 在不需要验证的地址列表中，直接放行请求!", getUrl);
                    return chain.filter(exchange);
                }
            } */
        }
        // 到达此处表示被拦截了
        log.info("当前请求路径 {} 不在不需要验证的地址列表中，下面将进行JWT验证啦！", getUrl);


        // 4.如果请求路径不在不需要验证的地址列表中，下面就进行JWT验证逻辑
        // 例如：从请求头中获取JWT，解析并验证它的有效性
        // 前后端规约：从header中获取用户的token
        HttpHeaders headers = exchange.getRequest().getHeaders();
        // getFirst：获取指定名称的header的第一个值
        String userToken = headers.getFirst(HEADER_USER_TOKEN);// 这里的headerUserToken就是前端传过来的JWT令牌
        log.info("当前请求路径 {} 的header中传过来的JWT令牌为: {}", getUrl, userToken);

        // 判空header中的令牌
        if (StringUtils.isNotBlank(userToken)) {
            String[] tokenArr = userToken.split(JWTUtil.AT);
            // 验证tokenArr的长度是否为2，表示前端传过来的JWT格式正确
            if(tokenArr.length < 2) {
                log.error("当前请求路径 {} 的header中传过来的JWT格式不正确，长度小于2！", getUrl);
                // 返回错误信息给前端
                return renderErrorMsg(exchange, ResponseStatusEnum.UN_LOGIN);
            }

            // 获得jwt的令牌与前缀
            String prefix = tokenArr[0]; // 前缀(实现的目标是：给header设定不同的key名，方便微服务们区分用户类型)
            String jwt = tokenArr[1]; // JWT令牌
            // 这里可能会抛出异常，如果JWT无效或者过期(所以得为这句代码专门封装一个try-catch块的方法：dealJWT)
            if (prefix.equalsIgnoreCase(TOKEN_APP_PREFIX)) {
                // 如果前缀是TOKEN_APP_PREFIX，则表示是APP端用户
                return dealJWT(jwt, exchange, chain, APP_USER_JSON); // 处理JWT令牌，设置新的header，将用户信息放入请求头中

            } else if (prefix.equalsIgnoreCase(TOKEN_SAAS_PREFIX)) {
                // 如果前缀是TOKEN_SAAS_PREFIX，则表示是SaaS端用户
                return dealJWT(jwt, exchange, chain, SAAS_USER_JSON);

            } else if (prefix.equalsIgnoreCase(TOKEN_ADMIN_PREFIX)) {
                // 如果前缀是TOKEN_ADMIN_PREFIX，则表示是管理员用户
                return dealJWT(jwt, exchange, chain, ADMIN_USER_JSON);

            } else {
                log.error("当前请求路径 {} 的header中传过来的JWT前缀不正确！", getUrl);
                // 返回错误信息给前端
                return renderErrorMsg(exchange, ResponseStatusEnum.UN_LOGIN);
            }
        }


        // 默认不放行(抛出错误信息给前端)
        // GraceException.doException(ResponseStatusEnum.UN_LOGIN); // 这种方式可不行，因为网关过滤器比@ExceptionHandler(MyCustomException.class)更早执行，所以无法捕获到这个异常
        return renderErrorMsg(exchange, ResponseStatusEnum.UN_LOGIN);
    }


    /**
     * 作用：处理JWT令牌的验证逻辑
     * @param jwt JWT令牌字符串，通常包含用户信息等内容
     * @param exchange 包含请求和响应信息的容器, 类似于HttpServletRequest和HttpServletResponse的组合体
     * @param chain 过滤器链，用来传递给下一个过滤器
     * @param key    请求头的键名，用于存储用户信息
     * @return Mono<Void>，表示异步操作，没有返回值
     */
    public Mono<Void> dealJWT(String jwt, ServerWebExchange exchange, GatewayFilterChain chain, String key) {
        try {
            // 这里调用JWT工具类的checkJWT方法来验证JWT的有效性
            String userJson = jwtUtil.checkJWT(jwt);
            // 将用户信息放入请求头中，让其他业务可以不用再去数据库查询用户信息
            exchange = setNewHeader(exchange, key, userJson); // 设置新的header，将用户信息放入请求头中

            // 如果不抛出异常，则交给下一个过滤器处理
            log.info("JWT验证通过啦，用户信息: {}", userJson);
            return chain.filter(exchange); // 继续执行下一个过滤器

        } catch (ExpiredJwtException e) { // 如果JWT过期，则抛出这个ExpiredJwtException异常
            // 如果验证失败，抛出自定义异常
            log.error("JWT过期了，错误信息: {}", e.getMessage());
            return renderErrorMsg(exchange, ResponseStatusEnum.JWT_EXPIRE_ERROR);
        } catch (Exception e) {
            // 如果验证失败，抛出自定义异常
            log.error("JWT验证失败，错误信息: {}", e.getMessage());
            e.printStackTrace();
            return renderErrorMsg(exchange, ResponseStatusEnum.JWT_SIGNATURE_ERROR);
        }

    }


    /**
     * 作用：设置新的header，将用户信息放入Request头中
     * @param exchange 包含请求和响应信息的容器, 类似于HttpServletRequest和HttpServletResponse的组合体
     * @param key 请求头的键名，用于存储用户信息
     * @param userJson 用户信息的JSON字符串，通常包含用户ID、用户名等信息
     * @return ServerWebExchange，修改后的请求和响应信息容器
     */
    public ServerWebExchange setNewHeader(ServerWebExchange exchange, String key, String userJson) {

       /*  // 这里的exchange是一个容器，包含了请求和响应的信息
        // 我们可以在这个容器中添加新的header(这里的代码是错的，因为exchange.getRequest().getHeaders()返回的是一个不可变的HttpHeaders对象，不能直接修改它)
        HttpHeaders headers = exchange.getRequest().getHeaders();
        // 创建一个新的header，将用户信息放入其中
        headers.add(key, userJson);
        // 返回修改后的exchange对象
        return exchange.mutate().request(exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.putAll(headers)).build()).build(); */

        // 🔹 正确做法：通过mutate()创建新的请求对象，而不是直接修改原有的headers
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(key, userJson)  // 直接设置header
                .build();

        // 🔹 返回新的exchange对象
        return exchange.mutate()
                .request(mutatedRequest)
                .build();
    }



    /**
     * 作用：封装网关的异常处理器，返回自定义的JSON格式错误信息
     * @param exchange 包含请求和响应信息的容器
     * @param status 错误状态枚举
     * @return Mono<Void>，表示异步操作，没有返回值
     * 举例：就像在餐厅点餐时，如果菜品有问题，服务员会给你一个错误信息的菜单 🍽️
     */
    public Mono<Void> renderErrorMsg(ServerWebExchange exchange, ResponseStatusEnum status) {

        // 1.获得response
        ServerHttpResponse response = exchange.getResponse();
        // 2.构建jsonResult
        GraceJSONResult graceJSONResult = GraceJSONResult.exception(status);
        // 3.修改response的响应码为500
        response.setStatusCode(HttpStatus.BAD_GATEWAY);

        // 4.设置response的content-type为application/json
        // 如果之前的代码或者中间件已经设置了Content-Type，就不会覆盖它，避免在响应头中出现重复的Content-Type键，这可能会导致某些客户端解析错误
        if (!response.getHeaders().containsKey("Content-Type")) {
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


    /**
     * 作用： 返回执行优先级，0表示最高优先级
     * 举例： VIP客户排队号码是0，普通客户是1、2、3... 👑
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
