package com.shadow.aicodingsystem.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WebScreenshotUtils {
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;

    private static final AtomicBoolean CHROME_DRIVER_MANAGER_READY = new AtomicBoolean(false);
    private static final Set<WebDriver> ALL_WEB_DRIVERS = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<WebDriver> THREAD_LOCAL_DRIVER = new ThreadLocal<>();

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(WebScreenshotUtils::closeAllWebDrivers, "web-screenshot-webdriver-shutdown")
        );
    }

    /**
     * ThreadLocal 模式：同一线程复用同一个 WebDriver，不同线程互不干扰。
     */
    public static WebDriver getWebDriver() {
        WebDriver driver = THREAD_LOCAL_DRIVER.get();
        if (driver != null) {
            return driver;
        }
        driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        THREAD_LOCAL_DRIVER.set(driver);
        ALL_WEB_DRIVERS.add(driver);
        return driver;
    }

    @PreDestroy
    public void destroy() {
        closeAllWebDrivers();
    }

    private static void ensureChromeDriverManagerReady() {
        if (CHROME_DRIVER_MANAGER_READY.get()) {
            return;
        }
        synchronized (WebScreenshotUtils.class) {
            if (CHROME_DRIVER_MANAGER_READY.get()) {
                return;
            }
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();
            CHROME_DRIVER_MANAGER_READY.set(true);
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动。
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            ensureChromeDriverManagerReady();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            options.addArguments("--disable-extensions");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("Init ChromeDriver failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Initialize Chrome browser failed");
        }
    }

    /**
     * 释放当前线程绑定的 WebDriver（适用于线程生命周期短的场景）。
     * 线程池场景通常不需要调用；应用关闭时会统一清理。
     */
    public static void quitCurrentThreadWebDriver() {
        WebDriver driver = THREAD_LOCAL_DRIVER.get();
        if (driver == null) {
            return;
        }
        THREAD_LOCAL_DRIVER.remove();
        ALL_WEB_DRIVERS.remove(driver);
        safeQuit(driver);
    }

    private static void closeAllWebDrivers() {
        for (WebDriver driver : ALL_WEB_DRIVERS) {
            safeQuit(driver);
        }
        ALL_WEB_DRIVERS.clear();
        THREAD_LOCAL_DRIVER.remove();
    }

    private static void safeQuit(WebDriver driver) {
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (Exception e) {
            log.warn("Quit WebDriver failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String outputPath) {
        try {
            FileUtil.writeBytes(imageBytes, outputPath);
        } catch (Exception e) {
            log.error("Save image failed: {}", outputPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Save image failed");
        }
    }

    /**
     * 压缩图片
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("Compress image failed: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Compress image failed");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            Thread.sleep(2000);
            log.info("Page loaded");
        } catch (Exception e) {
            log.error("Wait page load failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Wait page load failed");
        }
    }

    /**
     * 生成网页截图并保存（同线程复用 WebDriver）。
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("webUrl is blank");
            return null;
        }
        try {
            String rootPath = System.getProperty("user.dir")
                    + File.separator + "tmp"
                    + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            final String IMAGE_SUFFIX = ".png";
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            WebDriver driver = getWebDriver();
            driver.get(webUrl);
            waitForPageLoad(driver);

            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, imageSavePath);
            log.info("Screenshot saved: {}", imageSavePath);

            final String COMPRESSED_IMAGE_SUFFIX = "_compressed.jpg";
            String compressedImageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSED_IMAGE_SUFFIX;
            compressImage(imageSavePath, compressedImageSavePath);
            log.info("Screenshot compressed: {}", compressedImageSavePath);

            FileUtil.del(imageSavePath);
            return compressedImageSavePath;
        } catch (Exception e) {
            log.error("Save webpage screenshot failed: {}", webUrl, e);
            return null;
        }
    }
}
