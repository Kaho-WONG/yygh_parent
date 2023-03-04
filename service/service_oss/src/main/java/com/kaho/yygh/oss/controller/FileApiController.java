package com.kaho.yygh.oss.controller;

import com.kaho.yygh.common.result.Result;
import com.kaho.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: oss 文件操作api接口
 * @author: Kaho
 * @create: 2023-03-04 13:48
 **/
@RestController
@RequestMapping("/api/oss/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    //上传文件到阿里云oss
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        //获取上传文件，上传后返回一个路径
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
