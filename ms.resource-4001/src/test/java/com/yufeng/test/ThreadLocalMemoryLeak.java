package com.yufeng.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ThreadLocal内存泄露示例
 * 运行时设置小堆空间: java -Xmx64m -Xms64m ThreadLocalMemoryLeak
 */
public class ThreadLocalMemoryLeak {

    // 创建一个ThreadLocal，存储大量数据
    private static ThreadLocal<List<byte[]>> threadLocalData = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("开始ThreadLocal内存泄露演示...");
        System.out.println("初始内存使用: " + getMemoryInfo());

        // 创建固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 提交大量任务
        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    // 在ThreadLocal中存储大量数据
                    createLargeDataInThreadLocal(taskId);

                    // 模拟一些工作
                    Thread.sleep(100);

                    System.out.println("任务 " + taskId + " 完成，当前内存: " + getMemoryInfo());

                    // 注意：这里故意不调用threadLocalData.remove()
                    // 这会导致内存泄露！

                } catch (Exception e) {
                    System.err.println("任务 " + taskId + " 执行出错: " + e.getMessage());
                }
            });

            // 稍微延迟，观察内存增长
            Thread.sleep(50);
        }

        // 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        System.out.println("所有任务完成，最终内存使用: " + getMemoryInfo());

        // 尝试强制垃圾回收
        System.gc();
        Thread.sleep(1000);

        System.out.println("GC后内存使用: " + getMemoryInfo());
        System.out.println("注意：由于ThreadLocal没有调用remove()，内存可能无法完全释放！");

        // 演示正确的做法（可选）
        // demonstrateCorrectUsage();
    }

    /**
     * 在ThreadLocal中创建大量数据
     */
    private static void createLargeDataInThreadLocal(int taskId) {
        List<byte[]> dataList = new ArrayList<>();

        // 每个线程创建约1MB的数据
        for (int i = 0; i < 10; i++) {
            // 创建100KB的字节数组
            byte[] largeData = new byte[100 * 1024];
            // 填充一些数据，防止JVM优化
            for (int j = 0; j < largeData.length; j += 1000) {
                largeData[j] = (byte) (taskId + i + j);
            }
            dataList.add(largeData);
        }

        // 将数据存储到ThreadLocal中
        threadLocalData.set(dataList);

        System.out.println("线程 " + Thread.currentThread().getName() +
                " 创建了 " + dataList.size() + " 个数据块");
    }

    /**
     * 获取当前内存使用信息
     */
    private static String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        return String.format("已用: %dMB, 总计: %dMB, 最大: %dMB",
                usedMemory, totalMemory, maxMemory);
    }

    /**
     * 演示正确使用ThreadLocal的方法
     */
    private static void demonstrateCorrectUsage() throws InterruptedException {
        System.out.println("\n--- 演示正确的ThreadLocal使用方式 ---");

        ThreadLocal<String> correctThreadLocal = new ThreadLocal<>();

        Thread testThread = new Thread(() -> {
            try {
                // 设置ThreadLocal值
                correctThreadLocal.set("正确使用的数据");

                // 使用数据
                String data = correctThreadLocal.get();
                System.out.println("获取到数据: " + data);

            } finally {
                // 重要：在finally块中清理ThreadLocal
                correctThreadLocal.remove();
                System.out.println("ThreadLocal已清理");
            }
        });

        testThread.start();
        testThread.join();
    }
}

/**
 * 另一个更极端的内存泄露例子
 */
class ExtremeLeak {
    private static ThreadLocal<byte[]> hugeData = new ThreadLocal<>();

    public static void createMemoryLeak() {
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                // 每个线程存储10MB数据
                hugeData.set(new byte[10 * 1024 * 1024]);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // 故意不调用remove()，造成严重内存泄露
                // hugeData.remove(); // 这行被注释掉了！
            });
        }

        executor.shutdown();
    }
}

/**
 * ThreadLocal使用最佳实践示例
 */
class ThreadLocalBestPractice {
    private static final ThreadLocal<StringBuilder> STRING_BUILDER =
            ThreadLocal.withInitial(() -> new StringBuilder(1024));

    public static String processString(String input) {
        StringBuilder sb = STRING_BUILDER.get();
        try {
            // 清空之前的内容
            sb.setLength(0);

            // 使用StringBuilder处理字符串
            sb.append("处理: ").append(input);

            return sb.toString();
        } finally {
            // 清空内容，但不remove ThreadLocal（因为会复用）
            sb.setLength(0);

            // 如果确定不再使用，可以调用remove()
            // STRING_BUILDER.remove();
        }
    }
}
