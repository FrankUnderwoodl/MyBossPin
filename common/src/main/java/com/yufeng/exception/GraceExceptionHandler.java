package com.yufeng.exception;

import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 12:13
 */
// @ControllerAdvice是什么: @Component 的特殊化，专门用于定义全局异常处理、数据绑定和数据预处理等功能
// 存在的意义是: 结合 @ExceptionHandler 实现全局异常处理，避免在每个控制器中重复编写异常处理代码
@Slf4j
@ControllerAdvice
public class GraceExceptionHandler {


    /**
     * 捕获自定义的异常
     */
    @ExceptionHandler(MyCustomException.class)
    @ResponseBody // @ResponseBody的作用是:将方法的返回值直接作为HTTP响应体返回，而不是解析为视图
    public GraceJSONResult returnMyCustomException(MyCustomException e) {
        // 返回自定义异常信息
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }



    /**
     * 捕获zookeeper乐观锁相关的异常(也就是版本号的不匹配)
     */
    @ExceptionHandler(KeeperException.BadVersionException.class)
    @ResponseBody // @ResponseBody的作用是:将方法的返回值直接作为HTTP响应体返回，而不是解析为视图
    public GraceJSONResult returnBadVersionException(KeeperException.BadVersionException e) {
        log.error("zookeeper乐观锁异常，版本号不匹配", e);
        // 返回自定义异常信息
        return GraceJSONResult.exception(ResponseStatusEnum.ZOOKEEPER_BAD_VERSION_ERROR);
    }


    /**
     * 捕获文件上传超过500KB的异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody // @ResponseBody的作用是:将方法的返回值直接作为HTTP响应体返回，而不是解析为视图
    public GraceJSONResult returnMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件上传失败，超过最大限制: 500KB", e);
        e.printStackTrace();
        // 返回自定义异常信息
        return GraceJSONResult.exception(ResponseStatusEnum.FILE_MAX_SIZE_500KB_ERROR);
    }



    /**
     * 捕获JWT相关的异常
     */
    @ExceptionHandler({SignatureException.class, ExpiredJwtException.class, UnsupportedJwtException.class, MalformedJwtException.class, io.jsonwebtoken.security.SignatureException.class})
    @ResponseBody
    public GraceJSONResult returnSignatureException(SignatureException e) {
        e.printStackTrace();
        return GraceJSONResult.exception(ResponseStatusEnum.JWT_SIGNATURE_ERROR);
    }


    /**
     * 捕获算术异常
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public GraceJSONResult returnArithmeticException(ArithmeticException e) {
        e.printStackTrace();
        // 返回自定义异常信息
        return GraceJSONResult.errorMsg(e.getMessage());
    }


    /**
     * 捕获前端传来的BO参数是否合法，如果不合法就会抛出：MethodArgumentNotValidException异常，这里就进行捕获处理
     * 例如: @NotBlank(message = "手机号不能为空")
     *     ‘@Length(min = 11, max = 11, message = "手机号长度必须为11位")’
     *     private String mobile;
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public GraceJSONResult returnArgusNotValid(MethodArgumentNotValidException e) {
        e.printStackTrace();
        // 返回自定义异常信息
        BindingResult result = e.getBindingResult();
        Map<String, String> errors = getErrors(result);
        return GraceJSONResult.errorMap(errors);
    }
    public Map<String, String> getErrors(BindingResult result) {

        Map<String, String> map = new HashMap<>();

        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fe : errorList) {
            // 错误所对应的属性字段名
            String field = fe.getField();
            // 错误信息
            String message = fe.getDefaultMessage();
            // 将错误信息存入map中
            map.put(field, message);
        }
        return map;
    }

}
