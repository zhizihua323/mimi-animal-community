package com.example.coreshopping.controller;

import com.example.coreshopping.utils.minio.FileUpload;
import lombok.SneakyThrows;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/shopping/minio")
public class MinioShoppingController {

 @Autowired
 private FileUpload fileUpload;

 @SneakyThrows
 @PostMapping("fileUpload")
 public Result fileUpload(MultipartFile file){

  String originalFileName = file.getOriginalFilename();
  System.out.println("商品上传的原始文件名：" + originalFileName);

  String fileSuffix = originalFileName.substring(originalFileName.lastIndexOf("."));
  // 可以加个 "goods_" 前缀，方便在 MinIO 后台区分
  String newFileName = "goods_" + System.currentTimeMillis() + fileSuffix;

  // 这里调用你的工具类上传，如果你的工具类支持传 bucketName，可以在这里指定
  String fileURl= fileUpload.getPublicUrl(null, newFileName);
  fileUpload.uploadFile(file, null, newFileName);

  return Result.ok("商品图片上传成功", fileURl);
 }
}