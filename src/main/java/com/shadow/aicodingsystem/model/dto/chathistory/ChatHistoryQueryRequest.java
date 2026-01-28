package com.shadow.aicodingsystem.model.dto.chathistory;

import com.shadow.aicodingsystem.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai/ai_error）
     */
    private String messageType;

    /**
     * 发送用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询时，查询最后一条记录的创建时间之后的记录
     */
    private LocalDateTime lastCreateTime;

    private static final long serialVersionUID = 1L;
}
