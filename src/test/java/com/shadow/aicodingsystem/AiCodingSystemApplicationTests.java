package com.shadow.aicodingsystem;

import com.shadow.aicodingsystem.utils.WebScreenshotUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class AiCodingSystemApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshotPath);
    }
}
