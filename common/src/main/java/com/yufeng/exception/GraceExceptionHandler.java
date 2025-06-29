package com.yufeng.exception;

import com.yufeng.grace.result.GraceJSONResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 12:13
 */
// @ControllerAdvice是什么: @Component 的特殊化，专门用于定义全局异常处理、数据绑定和数据预处理等功能
// 存在的意义是: 结合 @ExceptionHandler 实现全局异常处理，避免在每个控制器中重复编写异常处理代码
@ControllerAdvice
public class GraceExceptionHandler {

    /**
     * ExceptionHandler的作用是:指定当抛出特定异常时(这里指的是MyCustomException，你也可以指定RuntimeException)，执行该方法进行捕获处理(给前端返回错误信息)
     * 例如: 当你在Controller中抛出MyCustomException异常时，Spring会自动调用这个方法来处理异常，并将错误信息返回给前端
     */
    @ExceptionHandler(MyCustomException.class)
    @ResponseBody // @ResponseBody的作用是:将方法的返回值直接作为HTTP响应体返回，而不是解析为视图
    public GraceJSONResult returnMyCustomException(MyCustomException e) {
        // 返回自定义异常信息
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }


    /**
     * 捕获前端传来的BO参数是否合法，如果不合法就会抛出MethodArgumentNotValidException异常，这里就进行捕获处理
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
