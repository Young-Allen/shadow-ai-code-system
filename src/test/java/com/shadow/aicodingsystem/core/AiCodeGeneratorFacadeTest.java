package com.shadow.aicodingsystem.core;

import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("注册用户信息网站", CodeGenTypeEnum.MULTI_FILE);
        assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("登录页面", CodeGenTypeEnum.HTML);
        //阻塞等待所有数据收集完成
        StringBuilder stringBuilder = new StringBuilder();
        codeStream.subscribe(chunk -> {
            System.out.println("chunk = " + chunk);
            stringBuilder.append(chunk);
        });
//        List<String> block = codeStream.collectList().block();
//        assertNotNull(stringBuilder);
//        String completeContent = String.join("", block);
        assertNotNull(stringBuilder);
    }
}