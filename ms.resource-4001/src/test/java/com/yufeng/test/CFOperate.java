package com.yufeng.test;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

/**
 * @author Lzm
 * @CreateTime 2025年8月23日 09:56
 */
public class CFOperate {


    @Test
    public void testOfCF() throws Exception {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("111");
            return "111";
        }).thenApplyAsync((result) -> {
            System.out.println("222");
            return result + "222";
        }).thenApplyAsync((result) -> {
            System.out.println("333");
            return result + "333";
        }).thenAcceptAsync((result) -> {
            System.out.println("end:" + result);
        }).get();
    }

    @Test
    public void testLocalDateTime(){

        // 输出的格式为yyyy-MM-ddTHH:mm:ss.SSS
        System.out.println(java.time.LocalDateTime.now());
    }
}
