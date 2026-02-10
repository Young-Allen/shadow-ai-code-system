package com.shadow.aicodingsystem.ai;

import com.shadow.aicodingsystem.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {
    /**
     * 获取AI代码生成类型路由服务
     * @return AI代码生成类型路由服务
     */
    @Bean
    public AiCodeGenTypeRoutingService getAiCodeGenTypeRoutingService() {
        ChatModel routingChatModelPrototype = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModelPrototype)
                .build();
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return getAiCodeGenTypeRoutingService();
    }
}
