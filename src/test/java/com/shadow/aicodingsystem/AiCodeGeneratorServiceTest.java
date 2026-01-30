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
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode( "请生成一个登录页面");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    public void generateMultiFileCode(){
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("请生成一个用户管理系统");
        Assertions.assertNotNull(multiFileCode);
    }

//    @Test
//    public void testChatMemory(){
//        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(1, "做一个程序员鱼皮的工具网站，总代码不超过20行");
//        Assertions.assertNotNull(htmlCodeResult);
//        htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(1, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(htmlCodeResult);
//
//        htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(2, "做一个程序员鱼皮的工具网站，总代码不超过20行");
//        Assertions.assertNotNull(htmlCodeResult);
//        htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(2, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(htmlCodeResult);
//    }
}
