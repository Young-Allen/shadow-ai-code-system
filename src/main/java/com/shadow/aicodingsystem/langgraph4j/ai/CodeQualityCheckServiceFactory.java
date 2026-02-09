package com.shadow.aicodingsystem.langgraph4j.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CodeQualityCheckServiceFactory {
    @Resource
    private ChatModel chatModel;

    /**
     * 创建代码质量检查服务
     * @return
     */
    @Bean
    public CodeQualityCheckService createCodeQualityCheckService() {
        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(chatModel)
                .build();
    }
}
