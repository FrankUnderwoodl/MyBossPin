
//	此资源由 58学课资源站 收集整理
//	想要获取完整课件资料 请访问：58xueke.com
//	百万资源 畅享学习
package com.test.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;

public class MinIOTest {

    @Test
    public void testUpload() throws Exception {
        // 创建客户端
        MinioClient minioClient = MinioClient.builder()
                        .endpoint("http://localhost:9000")
                        .credentials("lzm", "33321480")
                        .build();

        // 如果没有对应的bucket则创建
        String bucketName = "localjava";
        boolean found = minioClient
                .bucketExists(
                        BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } else {
            System.out.println("Bucket " + bucketName + " already exists.");
        }

        // 上传文件
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object("kzx.png")
                        .filename("/Users/lzm/Downloads/temp/face/1754264585.89983.png")
                        .build());
    }

}
