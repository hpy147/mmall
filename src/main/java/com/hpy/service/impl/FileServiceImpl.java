package com.hpy.service.impl;

import com.google.common.collect.Lists;
import com.hpy.service.FileService;
import com.hpy.util.FTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Author: hpy
 * Date: 2019-09-30
 * Description: <描述>
 */
@Service(value = "fileService")
public class FileServiceImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    @Override
    public String upload(MultipartFile file, String path) {
        // 原始文件名
        String fileName = file.getOriginalFilename();
        // 文件扩展名
        String fileExtentionName = fileName.substring(fileName.lastIndexOf("."));
        // 要上传的文件名
        String uploadFileName = UUID.randomUUID().toString() + fileExtentionName;

        // 创建文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        // 上传
        File targetFile = new File(path, uploadFileName);
        try {
            file.transferTo(targetFile);
            // 文件已经上传成功了

            FTPUtils.uploadFile(Lists.newArrayList(targetFile));
            // 文件已经上传到FTP服务器

            targetFile.delete();
            // 删除上传到项目中的临时文件

            return targetFile.getName();

        } catch (IOException e) {
            logger.error("文件上传异常", e);
            return null;
        }
    }
}
