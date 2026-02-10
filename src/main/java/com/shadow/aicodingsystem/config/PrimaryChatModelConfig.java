package com.shadow.aicodingsystem.config;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PrimaryChatModelConfig {

    @Bean
    @Primary
    @ConditionalOnBean(name = "openAiChatModel")
    public ChatModel primaryChatModel(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        return openAiChatModel;
    }
}
