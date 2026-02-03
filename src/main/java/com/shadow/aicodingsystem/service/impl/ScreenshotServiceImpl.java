package com.shadow.aicodingsystem.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.exception.ThrowUtils;
import com.shadow.aicodingsystem.manager.CosManager;
import com.shadow.aicodingsystem.service.ScreenshotService;
import com.shadow.aicodingsystem.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;


    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isEmpty(webUrl), ErrorCode.PARAMS_ERROR, "webUrl不能为空");
        log.info("开始生成截图，webUrl:{}", webUrl);
        //1. 生成本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isEmpty(localScreenshotPath), ErrorCode.SYSTEM_ERROR, "生成截图失败");
        try{
            //2. 上传到cos
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isEmpty(cosUrl), ErrorCode.SYSTEM_ERROR, "上传截图到COS失败");
            log.info("截图上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        }finally {
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到COS并返回URL
     * @param localScreenshotPath
     * @return
     */
    private String uploadScreenshotToCos(String localScreenshotPath){
        if(StrUtil.isEmpty(localScreenshotPath)){
            log.error("本地截图路径不能为空");
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if(!screenshotFile.exists()){
            log.error("本地截图文件不存在，路径:{}", localScreenshotPath);
            return null;
        }
        //生成COS对象建
        String fileName = UUID.randomUUID().toString().substring(0,8) + "_compressed.jpg";
        String cosKey = generateCosKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成COS对象建
     * 格式：/screenshots/2026/02/02/filename.jpg
     * @param fileName
     * @return
     */
    private String generateCosKey(String fileName){
        String dataPath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", dataPath, fileName);
    }

    /**
     * 清理本地截图文件及其父目录
     * @param localFilePath
     */
    private void cleanupLocalFile(String localFilePath){
        File localFile = new File(localFilePath);
        if(localFile.exists()){
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("已删除本地截图文件及其父目录，路径: {}", localFilePath);
        }
    }
}
