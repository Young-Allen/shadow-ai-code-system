package com.shadow.aicodingsystem.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shadow.aicodingsystem.ai.model.message.*;
import com.shadow.aicodingsystem.constant.AppConstant;
import com.shadow.aicodingsystem.core.builder.VueProjectBuilder;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.enums.MessageTypeEnum;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;


/**
 * JSON消息流处理器，用于处理AI生成的JSON格式流响应
 * 处理 VUE_PROJECT 类型的流式响应
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser){
        //收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤掉空的消息块
                .doOnComplete(() -> {
                    // 保存AI响应内容到数据库
                    chatHistoryService.addChatMessage(appId, chatHistoryStringBuilder.toString(), MessageTypeEnum.AI.getValue(), loginUser.getId() );
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    String errorMsg = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMsg, MessageTypeEnum.AI_ERROR.getValue(), loginUser.getId() );
                    log.error(errorMsg);
                });

    };

    /**
     * 处理JSON消息块的方法，根据不同的消息类型执行不同的处理逻辑
     * @param chunk 接收到的JSON格式消息块
     * @param chatHistoryStringBuilder 用于构建聊天历史记录的StringBuilder对象
     * @param seenToolIds 已处理过的工具ID集合，用于避免重复处理
     * @return 返回处理后的消息内容，用于显示或进一步处理
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 这里可以添加对JSON消息块的处理逻辑
        // 例如，解析JSON，提取信息等
        // 将JSON字符串转换为StreamMessage对象
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        // 根据消息类型获取对应的枚举值
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        // 根据不同的消息类型执行不同的处理逻辑
        switch (typeEnum) {
            // AI响应消息类型的处理
            case AI_RESPONSE -> {
                // 将JSON字符串转换为AiResponseMessage对象
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                // 获取AI响应的数据内容
                String data = aiMessage.getData();
                // 将数据追加到聊天历史记录中
                chatHistoryStringBuilder.append(data);
                return data;
            }
            // 工具请求消息类型的处理
            case TOOL_REQUEST -> {
                // 将JSON字符串转换为ToolRequestMessage对象
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                // 获取工具请求的ID
                String toolId = toolRequestMessage.getId();
                // 检查工具ID是否为空且是否未处理过
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 第一次调用该工具，执行相关逻辑
                    seenToolIds.add(toolId);
                    return "\n\n[选择工具] 写入文件\n\n";
                }else{
                    //不是第一次调用这个工具
                    return "";
                }
            }
            // 工具执行结果消息类型的处理
            case TOOL_EXECUTED -> {
                // 将JSON字符串转换为ToolRequestMessage对象
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                // 解析工具执行参数
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                // 获取相对文件路径
                String relativeFilePath = jsonObject.getStr("relativeFilePath");
                // 获取文件后缀
                String suffix = FileUtil.getSuffix(relativeFilePath);
                // 获取文件内容
                String content = jsonObject.getStr("content");
                // 全量将代码内容流式传递给前端会导致页面卡顿，因此这里只取前300字符作为预览
                int len = content != null ? content.length() : 0;
                String preview = StrUtil.sub(content, 0, 300);
                String result = String.format("""
                        [工具调用] 写入文件: %s (len=%d)
                        ```%s
                        %s%s
                        ```
                        """,
                        relativeFilePath,
                        len,
                        suffix,
                        preview,
                        len > 300 ? "\n... (truncated)" : ""
                );

                // 格式化工具执行结果
//                String result = String.format("""
//                        [工具调用] 写入文件: %s
//                        ```%s
//                        %s
//                        ```
//                        """, relativeFilePath, suffix, content);
                // 格式化输出内容，添加前后换行
                String output = String.format("\n\n%s\n\n", result);
                // 将输出内容追加到聊天历史记录中
                chatHistoryStringBuilder.append(output);
                return output;
            }
            // 默认情况，处理未知消息类型
            default -> {
                // 记录警告日志，提示未知消息类型
                log.warn("未知的消息类型: {}", streamMessage.getType());
                return "";
            }
        }
    }
}
