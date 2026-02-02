package com.shadow.aicodingsystem.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebScreenshotUtils {
    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        // 初始化WebDriver，例如使用ChromeDriver
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void destroy(){
        webDriver.quit();
    }

    /**
     * 初始化 Chrome 浏览器驱动
     * @param width
     * @param height
     * @return
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try{
            //自动管理 ChromeDriver
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            //禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            //禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            //禁用扩张
            options.addArguments("--disable-extensions");
            //设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");            // 创建 WebDriver 实例
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        }catch (Exception e){
            log.error("初始化 ChromeDriver 失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片到文件
     * @param imageBytes
     * @param outputPath
     */
    private static void saveImage(byte[] imageBytes, String outputPath){
        //实现保存图片的逻辑
        try{
            // 保存图片到指定路径
            FileUtil.writeBytes(imageBytes, outputPath);
        }catch (Exception e){
            log.error("保存图片失败: {}", outputPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     * @param originalImagePath
     * @param compressedImagePath
     */
    private static void compressImage(String originalImagePath, String compressedImagePath){
        //压缩图片质量（0.3 = 30%质量）
        final float COMPRESSION_QUALITY = 0.3f;
        try{
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
            //实现图片压缩的逻辑
        }catch (Exception e){
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     * @param driver
     */
    private static void waitForPageLoad(WebDriver driver){
        try{
            //创建登记页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            //等待页面加载完成
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            //额外等待2秒，确保页面完全加载
            Thread.sleep(2000);
            log.info("页面加载完成");
        }catch (Exception e)  {
            log.error("等待页面加载失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "等待页面加载失败");
        }
    }

    /**
     * 生成网页截图并保存
     * @param webUrl
     * @return
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if(StrUtil.isBlank(webUrl)){
            log.error("网页URL不能为空");
            return null;
        }
        try{
            //创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0,8);
            FileUtil.mkdir(rootPath);
            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //原始截图文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            //访问网页
            webDriver.get(webUrl);
            //等待页面加载完成
            waitForPageLoad(webDriver);
            //截图并保存到文件
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            //保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("网页截图保存成功: {}", imageSavePath);
            //压缩后图片文件路径
            final String COMPRESSED_IMAGE_SUFFIX = "_compressed.jpg";
            String compressedImageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSED_IMAGE_SUFFIX;
            compressImage(imageSavePath, compressedImageSavePath);
            log.info("网页截图压缩成功: {}", compressedImageSavePath);
            //删除原始图片，只保留压缩后的图片
            FileUtil.del(imageSavePath);
            return compressedImageSavePath;
        }catch (Exception e){
            log.error("保存网页截图失败: {}", webUrl, e);
            return null;
        }
    }
}
