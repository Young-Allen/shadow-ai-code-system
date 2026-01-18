package com.shadow.aicodingsystem;

import com.shadow.aicodingsystem.ai.AiCodeGeneratorService;
import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiCodeGeneratorServiceTest {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    public void generateHtmlCode(){
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode("请生成一个登录页面");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    public void generateMultiFileCode(){
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("请生成一个用户管理系统");
        Assertions.assertNotNull(multiFileCode);
    }


}
