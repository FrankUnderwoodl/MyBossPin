package com.yufeng.exception;

import com.yufeng.grace.result.ResponseStatusEnum;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 06:40
 * @Description 自定义异常类，这里就不用Lombok了，因为这个扩展性比较强，可以直接在其他项目中使用
 */
public class MyCustomException extends  RuntimeException {

    private ResponseStatusEnum responseStatusEnum;

    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        // 调用父类构造方法，传入异常相关信息
        super("异常状态码为:" + responseStatusEnum.status() + " 异常信息为:" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }
    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }
    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }


}
