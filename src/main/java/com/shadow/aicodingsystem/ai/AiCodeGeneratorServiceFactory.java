package com.shadow.aicodingsystem.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     *  缓存策略：
     *  - 最大缓存数量：1000 个实例
     *  - 写入后30分钟过期
     *  - 访问后10分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务示例被移除, appId：{}, 原因：{}", key, cause);
            })
            .build();

    /**
     * 根据应用ID获取AI代码生成器服务
     * 该方法会优先从缓存中获取服务，如果缓存中没有，则通过创建函数创建新的服务实例
     *
     * @param appId 应用ID，用于标识特定的应用
     * @return AiCodeGeneratorService 返回对应的AI代码生成器服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
    // 使用serviceCache的get方法，如果缓存中不存在则通过createAiCodeGeneratorService创建新实例
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }


    /**
     * 创建AI代码生成器服务实例
     * @param appId 应用ID，用于标识和区分不同的服务实例
     * @return 返回一个配置好的AI代码生成器服务实例
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
    // 记录创建服务实例的日志，包含appId信息
        log.info("创建新的 AI 服务实例, appId：{}", appId);
    // 构建一个基于Redis的聊天记忆窗口，用于存储对话上下文
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)                    // 设置应用ID作为记忆标识
                .chatMemoryStore(redisChatMemoryStore)  // 使用Redis作为记忆存储
                .maxMessages(20)              // 设置最大记忆消息数为20条
                .build();
        // 从数据库加载聊天历史记录到内存中，最多加载20条记录
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
    // 使用建造者模式创建并配置AI服务实例
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)                    // 设置聊天模型
                .streamingChatModel(streamingChatModel)  // 设置流式聊天模型
                .chatMemory(chatMemory)                  // 设置聊天记忆
                .build();
    }

    // TODO: 2023/5/26 优化为配置文件
//    @Bean
//    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
//                .builder()
//                .id(appId)
//                .chatMemoryStore(redisChatMemoryStore)
//                .maxMessages(20)
//                .build();
//
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .chatMemory(chatMemory)
//                .build();
//    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}
