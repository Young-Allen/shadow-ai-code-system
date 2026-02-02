package com.shadow.aicodingsystem.ai.model.message;

import lombok.Getter;

@Getter
public enum StreamMessageTypeEnum {
    AI_RESPONSE("ai_response", "AI响应消息"),
    TOOL_REQUEST("tool_request", "工具请求消息"),
    TOOL_EXECUTED("tool_executed", "工具执行结果消息"),;

    private final String value;
    private final String text;

    StreamMessageTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据类型字符串获取对应的枚举值
     * @param value 类型字符串
     * @return 匹配的枚举值，如果没有匹配则返回null
     */
    public static StreamMessageTypeEnum getEnumByValue(String value) {
    // 遍历所有枚举值
        for (StreamMessageTypeEnum messageTypeEnum : values()) {
        // 检查当前枚举值的type是否与传入的type匹配
            if (messageTypeEnum.getValue().equals(value)) {
            // 如果匹配，返回当前枚举值
                return messageTypeEnum;
            }
        }
    // 如果没有匹配的枚举值，返回null
        return null;
    }
}
