package org.mimi.controller;


import lombok.SneakyThrows;
import org.mimi.result.Result;
import org.mimi.utils.minio.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/minio")
public class FileUploadController {
    @Autowired
    private FileUpload fileUpload;

    @SneakyThrows
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file){
        fileUpload.uploadFile(file,null,null);
        return Result.ok();
    }

}
