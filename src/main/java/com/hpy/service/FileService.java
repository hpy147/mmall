package com.hpy.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Author: hpy
 * Date: 2019-09-30
 * Description: <描述>
 */
public interface FileService {
    String upload(MultipartFile file, String path);
}
