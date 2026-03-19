package org.mimi.corecommunity.controller;

import lombok.SneakyThrows;
import org.mimi.corecommunity.utils.minio.FileUpload;
import org.mimi.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/community/minio")
public class MinioArchivalController {
    @Autowired
    private FileUpload fileUpload;

    @SneakyThrows
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file){

        String originalFileName = file.getOriginalFilename();
        System.out.println("用户上传的原始文件名：" + originalFileName); // 示例输出：cat.jpg

        String fileSuffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = System.currentTimeMillis() + fileSuffix;

        String fileURl= fileUpload.getPublicUrl(null,newFileName);
        fileUpload.uploadFile(file,null,newFileName);

        return Result.ok("上传成功",fileURl);
    }
}
