package com.shadow.aicodingsystem.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具调用消息
 */
@Data
@EqualsAndHashCode(callSuper = true) //在生成 equals() 和 hashCode() 时，把“父类的字段”也一起算进去
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage{

    private String id;

    private String name;

    private String arguments;

    // 1. 用于 onPartialToolCall 的流式构造 (1.11.0+)
    // 新增的：用于 onPartialToolCall 发送流式片段
    public ToolRequestMessage(PartialToolCall partialToolCall) {
        // 依然使用 tool_request 类型，或者你可以定义一个新的枚举值以便前端区分
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = partialToolCall.id();
        this.name = partialToolCall.name();
        // 这里的关键是 partialArguments()，它代表当前流出的那一个字符/片段
        this.arguments = partialToolCall.partialArguments();
    }

    // 3. Full constructor for beforeToolExecution
    public ToolRequestMessage(ToolExecutionRequest toolExecutionRequest) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}
