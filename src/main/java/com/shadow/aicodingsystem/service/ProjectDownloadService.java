package com.shadow.aicodingsystem.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {


    /**
     * 将指定项目路径下的内容打包成ZIP文件并下载
     *
     * @param projectPath 要下载的项目路径
     * @param downloadFileName 下载时显示的文件名
     * @param response HTTP响应对象，用于将ZIP文件写入输出流
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
