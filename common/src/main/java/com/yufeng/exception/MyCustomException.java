package com.yufeng.exception;

import com.yufeng.grace.result.ResponseStatusEnum;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 06:40
 * @Description 自定义异常类，这里就不用Lombok来创造构造方法了，因为这个扩展性比较强，可以直接在其他项目中使用
 */
public class MyCustomException extends  RuntimeException {

    // 异常状态码枚举
    private ResponseStatusEnum responseStatusEnum;

    // 构造方法，传入异常状态码枚举
    public MyCustomException(ResponseStatusEnum responseStatusEnum) {

        // 调用父类构造方法，传入异常相关信息(其实这里其实底层就是new RuntimeException("xxx"))
        super("异常状态码为:" + responseStatusEnum.status() + " 异常信息为:" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }

    // Getter和Setter方法
    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }

    // 设置异常状态码枚举
    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }


}
