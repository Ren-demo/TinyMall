package com.mall.tinymall.util;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${spring.minio.bucket-name}")
    private String defaultBucket;
    @Value("${spring.minio.endpoint}")
    private String endpoint;

    /**
     * 上传文件到MinIO
     * @param file 上传的文件（MultipartFile）
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, Integer flag) {
        try {
            String objectName = file.getOriginalFilename();
            objectName = UUID.randomUUID().toString().replace("-", "") + objectName;
            if(flag.equals(PicType.USER_PICTURE.getCode())) objectName = "user/" + objectName;
            else if(flag.equals(PicType.GOODS_PICTURE.getCode())) objectName = "goods/" + objectName;
            else if(flag.equals(PicType.STORE_PICTURE.getCode())) objectName = "store/" + objectName;
            else objectName = "other/" + objectName;
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            // 返回文件访问路径
            return endpoint + ":9000/"+ defaultBucket + "/" + objectName;
        } catch (Exception e) {
            return "服务器出错";
        }
    }

    public String uploadComFile(MultipartFile file, List<String> prefix) {
        try {
            StringBuilder objectName = new StringBuilder(file.getOriginalFilename());
            objectName.insert(0, UUID.randomUUID().toString().replace("-", ""));
            for (String s : prefix) {
                objectName.insert(0, s);
            }
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(objectName.toString())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            // 返回文件访问路径
            return endpoint + ":9000/"+ defaultBucket + "/" + objectName;
        } catch (Exception e) {
           throw new RuntimeException();
        }
    }

    /**
     * 从MinIO下载文件（通过文件URL解析bucket和objectName）
     * @param fileUrl 文件的完整访问URL
     * @return 文件的输入流
     */
    public InputStream downloadFile(String fileUrl) {
        try {
            // 1. 使用URL类解析完整地址，避免手动截取导致的协议/端口问题
            URL url = new URL(fileUrl);
            // 2. 获取URL的路径部分（即endpoint之后的路径，如 /bucketName/objectName.txt）
            String path = url.getPath();
            // 3. 去除路径开头的/，并按第一个/分割，得到bucket和objectName
            if (path == null || path.isEmpty() || path.equals("/")) {
                throw new IllegalArgumentException("文件URL路径为空，格式错误");
            }
            String[] pathParts = path.replaceFirst("^/", "").split("/", 2);

            // 4. 校验路径格式是否合法
            if (pathParts.length < 2) {
                throw new IllegalArgumentException("文件访问路径格式错误，缺少文件名信息：" + path);
            }

            // 5. 提取解析后的bucket和object名称
            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            // 6. 执行MinIO文件下载操作
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("删除 {} 出错", fileUrl);
        }
        return null;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            log.info("deletePic: {}", fileUrl);
            // 1. 使用URL类解析完整地址，统一路径解析逻辑
            URL url = new URL(fileUrl);
            // 2. 获取纯路径部分（自动剔除协议、域名、端口）
            String path = url.getPath();

            // 3. 校验路径非空
            if (path == null || path.isEmpty() || path.equals("/")) {
                throw new IllegalArgumentException("文件URL路径为空，格式错误");
            }
            // 4. 去除开头的/，并按第一个/分割
            String[] pathParts = path.replaceFirst("^/", "").split("/", 2);

            if (pathParts.length < 2) {
                throw new IllegalArgumentException("文件访问路径格式错误，缺少文件名信息：" + path);
            }

            String bucketName = pathParts[0];
            String objectName = pathParts[1];

            // 执行删除操作
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    * 判断文件是否为图片
    * */
    public boolean isImage(String name){
        String suffix = name.substring(name.lastIndexOf(".")).toLowerCase();
        String[] imageSuffixes = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".tiff"};
        boolean isSuffixValid = false;
        for (String imageSuffix : imageSuffixes) {
            if (suffix.equals(imageSuffix)) {
                isSuffixValid = true;
                break;
            }
        }
        return isSuffixValid;
    }

}