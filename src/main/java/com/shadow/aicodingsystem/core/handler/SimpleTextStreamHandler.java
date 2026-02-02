package com.shadow.aicodingsystem.core.handler;

import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.enums.MessageTypeEnum;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器，用于处理AI生成的文本流响应
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理AI文本流响应的方法
     * 直接收集完整的文本响应
     * @param originFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @return
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser){
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 保存AI响应内容到数据库
                    chatHistoryService.addChatMessage(appId, aiResponseBuilder.toString(), MessageTypeEnum.AI.getValue(),loginUser.getId() );
                })
                .doOnError(error -> {
                    String errorMsg = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMsg, MessageTypeEnum.AI_ERROR.getValue(),loginUser.getId() );
                    log.error(errorMsg, error);
                });
    }
}
