package com.kaho.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @description:
 * @author: Kaho
 * @create: 2023-03-04 13:50
 **/
public interface FileService {

    // 上传文件到阿里云oss
    String upload(MultipartFile file);
}
