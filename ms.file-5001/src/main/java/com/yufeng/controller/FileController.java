package com.yufeng.controller;

import com.yufeng.model.bo.Base64FileBO;
import com.yufeng.config.MinIOConfig;
import com.yufeng.config.MinIOUtils;
import com.yufeng.config.OSSUtils;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.Base64ToFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Lzm
 * @CreateTime 2025年6月03日 00:36
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

    // @Autowired
    // private OSSConfig ossConfig;

    // public static final String HOST = "http://localhost:8000/";


    /**
     * 上传头像接口
     *
     * @param file 头像文件
     * @return 返回上传成功的头像地址
     */
    /* @PostMapping("/uploadFace1")
    public GraceJSONResult uploadFace(@RequestParam("file") MultipartFile file, String userId) {
        log.info("开始上传头像，用户ID: {}", userId);
        // 判断文件是否为空
        if (file == null || file.isEmpty()) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 获取文件名并截取后缀名，然后生成新的文件名
        String filename = file.getOriginalFilename();
        String suffixName = filename.substring(filename.lastIndexOf("."));
        String newFileName = userId + suffixName;

        // 设置文件存储路径
        String rootPath = "/Users/lzm/Downloads/temp"; // 注意：这里的路径需要根据实际情况修改
        String filePath = rootPath + File.separator + "face" + File.separator + newFileName;
        File newFile = new File(filePath);

        // 🔥 关键：先创建目录，再写入文件
        if (!newFile.getParentFile().exists()) {
            boolean created = newFile.getParentFile().mkdirs();
            if (!created) {
                // 如果目录创建失败，返回错误，
                log.error("创建目录失败: {}", newFile.getParentFile().getAbsolutePath());
            }
        }

        try {
            // 然后再写入文件
            file.transferTo(newFile);
            log.info("文件上传成功，存储路径: {}", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 生成可以被访问的URL地址，并返还给前端
        String userFaceUrl = HOST + "static/face/" + newFileName;

        return GraceJSONResult.ok(userFaceUrl);
    } */


    /**
     * 上传头像接口
     * 供用户在APP端上传头像文件
     *
     * @param file   头像文件
     * @param userId 用户ID
     * @return 返回上传成功的头像地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("/uploadFace")
    public GraceJSONResult uploadFace(@RequestParam("file") MultipartFile file, String userId) throws Exception {

        // 判断用户ID是否为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        // 获得文件原始名称
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        // 生成新的文件名，使用用户ID作为前缀
        // filename = userId + File.separator + filename;
        String suffixName = filename.substring(filename.lastIndexOf("."));
        filename = "app/face/" + userId + suffixName;
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());


        String imageUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + filename;

        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 使用阿里云OSS上传头像
     *
     * @param file   头像文件
     * @param userId 用户ID
     * @return 返回上传成功的头像地址
     */
    @PostMapping("uploadFace2")
    public GraceJSONResult uploadFace4(@RequestParam("file") MultipartFile file,
                                       @RequestParam("userId") String userId) throws Exception {
        // 获得文件原始名称
        String filename = file.getOriginalFilename();

        filename = userId + File.separator + filename;
        String imageUrl = OSSUtils.uploadFile(file, filename);

        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 管理员上传头像接口
     * 供管理员在运营管理平台上传头像文件
     *
     * @param base64FileBO 包含Base64编码的文件内容和管理员ID
     * @return 返回上传成功的头像地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("uploadAdminFace")
    public GraceJSONResult uploadAdminFace(@RequestBody @Valid Base64FileBO base64FileBO) throws Exception {

        // 获取Base64编码的文件内容
        String base64 = base64FileBO.getBase64File();

        // 设置文件后缀名和生成的对象名
        String suffixName = ".png"; // 这里用png，是因为占用空间小
        String uuid = UUID.randomUUID().toString(); // 生成唯一文件名
        String objectName = uuid + suffixName;  // 对象名

        String rootPath = "/Users/lzm/Downloads/Manual Library/temp" + File.separator;
        String filePath = rootPath
                + File.separator
                + "adminFace"
                + File.separator
                + objectName;

        // 将Base64编码转换为文件，暂存到本地
        Base64ToFile.Base64ToFile(base64, filePath);

        objectName = "admin/face/" + objectName;

        // 上传文件到MinIO
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), objectName, filePath);

        String imageUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + objectName;

        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 上传公司Logo接口
     * 供企业在创建公司时上传Logo文件
     *
     * @param file Logo文件
     * @return 返回上传成功的Logo地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("uploadLogo")
    public GraceJSONResult uploadLogo(@RequestParam("file") MultipartFile file) throws Exception {

        // 获得文件原始名称
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        filename = "company/logo/" + dealFilename(filename);
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());

        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);
        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 上传营业执照接口
     * 供企业在创建公司时上传营业执照文件
     *
     * @param file 营业执照文件
     * @return 返回上传成功的营业执照地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("uploadBizLicense")
    public GraceJSONResult uploadBizLicense(@RequestParam("file") MultipartFile file) throws Exception {

        // 获得文件原始名称
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        // 生成新的文件名，使用UUID来避免重复
        filename = "company/bizLicense/" + dealFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);
        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 上传授权函接口
     * 供企业在创建公司时上传授权函文件
     *
     * @param file 授权函文件
     * @return 返回上传成功的授权函地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("uploadAuthLetter")
    public GraceJSONResult uploadAuthLetter(@RequestParam("file") MultipartFile file) throws Exception {

        // 获得文件原始名称
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        // 生成新的文件名，使用UUID来避免重复
        filename = "company/AuthLetter/" + dealFilename(filename);
        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);
        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 处理文件名，添加UUID以避免重复
     *
     * @param filename 原始文件名
     * @return 处理后的文件名
     */
    private String dealFilename(String filename) {
        String suffixName = filename.substring(filename.lastIndexOf("."));
        String fName = filename.substring(0, filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return fName + "-" + uuid + suffixName;
    }




    /**
     * 上传照片接口
     * 供用户在APP端上传照片文件：针对企业的相关图片
     *
     * @param files 照片文件数组(注意：这里的files是一个数组，可能是多张照片)
     * @param companyId 公司ID
     * @return 返回上传成功的照片地址
     * @throws Exception 可能抛出异常
     */
    @PostMapping("uploadPhoto")
    public GraceJSONResult uploadPhoto(@RequestParam("files") MultipartFile[] files, String companyId) throws Exception {

        // 判断公司ID是否为空
        if (StringUtils.isBlank(companyId)) companyId = "";

        // 创建一个列表来存储上传成功的照片地址
        List<String> fileList = new ArrayList<>();

        // 遍历上传的文件数组
        for (MultipartFile f : files) {
            // 获得文件原始名称
            String filename = f.getOriginalFilename();

            // 组装文件路径
            filename = "company/" + companyId + "/photo/" + dealFilename(filename);
            String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                    filename,
                    f.getInputStream(),
                    true);
            fileList.add(imageUrl);
        }

        return GraceJSONResult.ok(fileList);
    }

}
