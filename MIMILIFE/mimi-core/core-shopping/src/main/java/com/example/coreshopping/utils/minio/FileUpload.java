package com.example.coreshopping.utils.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.messages.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

@Component
public class FileUpload {

    @Autowired
    private MinioProperties minioProperties;
    private MinioClient minioClient;

    @PostConstruct
    public void init(){
        try {
            // 创建 MinIO 客户端
            minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            // 检查默认存储桶是否存在，不存在则创建
            if (!bucketExists(minioProperties.getBucketName())) {
                createBucket(minioProperties.getBucketName());
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化 MinIO 客户端失败", e);
        }
    }

    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) throws Exception {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取所有存储桶
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /**
     * 上传文件到 MinIO
     * @param file 要上传的文件（MultipartFile）
     * @param bucketName 存储桶名称（null 则使用默认存储桶）
     * @param objectName 上传后的文件名称（null 则使用原文件名）
     * @return 上传后的文件路径（存储桶/文件名）
     */
    public String uploadFile(MultipartFile file, String bucketName, String objectName) throws Exception {
        // 处理参数默认值
        String targetBucket = bucketName == null ? minioProperties.getBucketName() : bucketName;
        String targetObjectName = objectName == null ? file.getOriginalFilename() : objectName;

        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(targetObjectName)
                            .stream(inputStream, file.getSize(), -1)  // -1 表示自动检测文件大小
                            .contentType(file.getContentType())  // 设置文件 contentType
                            .build()
            );
        }

        // 返回文件路径（存储桶/文件名）
        return targetBucket + "/" + targetObjectName;
    }

    /**
     * 生成文件的公开访问 URL（需确保文件所在桶已配置公开访问权限）
     * @param bucketName 存储桶名称
     * @param objectName 文件名
     * @return 公开访问 URL
     */
    public String getPublicUrl(String bucketName, String objectName) {
        // 拼接公开访问 URL（MinIO API 端口 + 存储桶 + 文件名）
        //http://139.198.126.138:9000/api/v1/buckets/test1/objects/download?preview=true&prefix=a.png&version_id=null

        return minioProperties.getEndpoint() +"/" + minioProperties.getBucketName() + "/"+objectName;
    }


}
