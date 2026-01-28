package com.shadow.aicodingsystem.service;

import com.mybatisflex.core.service.IService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.shadow.aicodingsystem.model.dto.chathistory.ChatHistoryQueryRequest;
import com.shadow.aicodingsystem.model.entity.ChatHistory;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.vo.ChatHistoryVO;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author shadow
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 添加聊天消息。
     * @param appId   应用id
     * @param userId  用户id
     * @param message 消息内容
     * @param messageType 消息类型
     */
    void addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据查询条件构建查询器。
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 对象转 VO。
     *
     * @param chatHistory 历史
     * @return VO
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 列表转 VO。
     *
     * @param chatHistoryList 列表
     * @return VO 列表
     */
    java.util.List<ChatHistoryVO> getChatHistoryVOList(java.util.List<ChatHistory> chatHistoryList);

    /**
     * 根据应用删除历史。
     *
     * @param appId 应用id
     * @return 删除结果
     */
    boolean removeByAppId(Long appId);

    /**
     * 分页查询应用聊天历史记录
     *
     * @param appId 应用ID，用于指定查询哪个应用的聊天记录
     * @param pageSize 每页大小，控制返回结果的数量
     * @param lastCreateTime 上一次查询的最后创建时间，用于分页查询，获取该时间点之后的记录
     * @param loginUser 当前登录用户，用于权限验证
     * @return 返回一个包含聊天历史记录视图对象(Page<ChatHistoryVO>)的分页结果，其中ChatHistoryVO可能包含聊天记录的相关信息
     */
    Page<ChatHistoryVO> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);
}
