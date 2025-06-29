package com.yufeng.exception;

import com.yufeng.grace.result.ResponseStatusEnum;

/**
 * @author Lzm
 * @CreateTime 2025年6月16日 12:31
 * @Description 这个类存在的意义是: 在需要抛出异常的地方，直接调用doException方法即可，而不用throw new MyCustomException(ResponseStatusEnum.xxx)这种方式，这种方式会让代码看起来更简洁
 */
public class GraceException {

    public static void doException(ResponseStatusEnum responseStatusEnum) {
        // 抛出异常
        throw new MyCustomException(responseStatusEnum);
    }
}
