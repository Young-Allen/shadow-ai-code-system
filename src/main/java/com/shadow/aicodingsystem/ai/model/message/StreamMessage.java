package com.shadow.aicodingsystem.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 * 注意封装对象必须有无参构造器，否则反序列化会失败
 */
@Data
@AllArgsConstructor //生成一个“全参数构造器”
@NoArgsConstructor // 生成一个无参构造器
public class StreamMessage {
    private String type;
}
