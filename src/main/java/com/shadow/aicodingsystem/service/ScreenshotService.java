package com.shadow.aicodingsystem.service;

public interface ScreenshotService {


    /**
     * 生成并上传网页截图的方法
     * @param webUrl 需要截图的网页URL地址
     * @return 返回处理结果的字符串，可能包含截图上传状态或相关信息
     */
    String generateAndUploadScreenshot(String webUrl);
}
