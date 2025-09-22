
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.yufeng.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO工具类
 */
@Slf4j
public class OSSUtils {

    private static OSS ossClient;

    private static String endpoint;
    private static String fileHost;
    private static String bucketName;
    private static String accessKeyId;
    private static String accessKeySecret;

    private static final String SEPARATOR = "/";

    public OSSUtils() {
    }

    public OSSUtils(String endpoint, String fileHost, String bucketName, String accessKeyId, String accessKeySecret) {
        OSSUtils.endpoint = endpoint;
        OSSUtils.fileHost = fileHost;
        OSSUtils.bucketName = bucketName;
        OSSUtils.accessKeyId = accessKeyId;
        OSSUtils.accessKeySecret = accessKeySecret;
        createOSSClient();
    }

    /**
     * 创建基于Java端的OSSClient
     */
    public void createOSSClient() {
        try {
            if (null == ossClient) {
                // 创建OSSClient实例。
                log.info("开始创建 OSSClient...");
                ossClient = new OSSClientBuilder()
                        .build(
                                endpoint,
                                accessKeyId,
                                accessKeySecret);
                createBucket(bucketName);
                log.info("创建完毕 OSSClient...");
            }
        } catch (Exception e) {
            log.error("OSS阿里云服务器异常：{}", e);
        }
    }

    /**
     * 获取上传文件前缀路径
     * @return
     */
    public static String getBasisUrl() {
        return endpoint + SEPARATOR + bucketName + SEPARATOR;
    }

    /******************************  Operate Bucket Start  ******************************/

    /**
     * 启动SpringBoot容器的时候初始化Bucket
     * 如果没有Bucket则创建
     * @throws Exception
     */
    private static void createBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            ossClient.createBucket(bucketName);
        }
    }

    /**
     *  判断Bucket是否存在，true：存在，false：不存在
     * @return
     * @throws Exception
     */
    public static boolean bucketExists(String bucketName) {
        return ossClient.doesBucketExist(bucketName);
    }

    /**
     * 获得上传文件的前缀路径
     * @return
     */
    public static String getBasicUrl() {
        return fileHost + SEPARATOR;
    }

    /**
     * 通过文件流进行上传
     * @param file
     * @param objectName
     * @return
     * @throws Exception
     */
    public static String uploadFile(MultipartFile file,
                                    String objectName) throws Exception {

        try {
            ossClient.putObject(bucketName, objectName, file.getInputStream());
        } finally {
            ossClient.shutdown();
        }

        return getBasicUrl() + objectName;
    }

}
