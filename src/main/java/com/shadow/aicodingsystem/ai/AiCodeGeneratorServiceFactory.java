package com.shadow.aicodingsystem.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shadow.aicodingsystem.ai.guardrail.PromptSafetyInputGuardrail;
import com.shadow.aicodingsystem.ai.guardrail.RetryOutputGuardrail;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.ai.tools.*;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import com.shadow.aicodingsystem.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;


    @Resource
    private ToolManager toolManager;


    /**
     * AI 服务实例缓存
     *  缓存策略：
     *  - 最大缓存数量：1000 个实例
     *  - 写入后30分钟过期
     *  - 访问后10分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
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
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据应用ID和代码生成类型获取AI代码生成器服务
     * 该方法会使用缓存机制，如果缓存中不存在则创建新的服务实例
     *
     * @param appId 应用ID，用于标识特定的应用
     * @param codeGenType 代码生成类型，指定生成的代码类型
     * @return 返回对应的AI代码生成器服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 根据应用ID和代码生成类型构建缓存键
        String cacheKey = buildCacheKey(appId, codeGenType);
    // 从缓存中获取服务，如果不存在则使用lambda表达式创建新的服务实例
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 构建缓存键
     * @param appId
     * @param codeGenType
     * @return
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }


    /**
     * 创建AI代码生成器服务实例
     * @param appId 应用ID，用于标识和区分不同的服务实例
     * @return 返回一个配置好的AI代码生成器服务实例
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
    // 记录创建服务实例的日志，包含appId信息
        log.info("创建新的 AI 服务实例, appId：{}", appId);
        // 构建一个基于Redis的聊天记忆窗口，用于存储对话上下文(这个appId是记忆标识)
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)                    // 设置应用ID作为记忆标识
                .chatMemoryStore(redisChatMemoryStore)  // 使用Redis作为记忆存储
                .maxMessages(20)              // 设置最大记忆消息数为20条
                .build();
        // 从数据库加载聊天历史记录到内存中，最多加载20条记录
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 根据代码生成类型选择合适的流式聊天模型并创建AI服务实例
        return switch (codeGenType) {
            //Vue 项目生成使用推理模型
            case VUE_PROJECT -> {
                // 使用多例模式的 StreamingChatModel 解决并发问题
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools(toolManager.getAllTools())
                        // 处理工具调用幻觉问题
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())
                        )
                        .maxSequentialToolsInvocations(20)  // 最多连续调用 20 次工具
                        .inputGuardrails(new PromptSafetyInputGuardrail()) // 添加输入护轨
                        .outputGuardrails(new RetryOutputGuardrail()) // 添加输出护轨，为了流式输出，这里不使用
                        .build();
            }
            // 使用建造者模式创建并配置AI服务实例
            case HTML, MULTI_FILE ->{
                // 使用多例模式的 StreamingChatModel 解决并发问题
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail()) // 添加输入护轨
                        .outputGuardrails(new RetryOutputGuardrail()) // 添加输出护轨，为了流式输出，这里不使用
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.getValue());
        };
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}
