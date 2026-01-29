package com.shadow.aicodingsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shadow.aicodingsystem.constant.UserConstant;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.exception.ThrowUtils;
import com.shadow.aicodingsystem.mapper.ChatHistoryMapper;
import com.shadow.aicodingsystem.model.dto.chathistory.ChatHistoryQueryRequest;
import com.shadow.aicodingsystem.model.entity.App;
import com.shadow.aicodingsystem.model.entity.ChatHistory;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.enums.MessageTypeEnum;
import com.shadow.aicodingsystem.model.vo.ChatHistoryVO;
import com.shadow.aicodingsystem.service.AppService;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author shadow
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    /**
     * 添加聊天消息方法
     * @param appId 应用ID，必须大于0
     * @param message 聊天消息内容，不能为空
     * @param messageType 消息类型，不能为空且必须是合法的消息类型
     * @param userId 用户ID，必须大于0
     */
    @Override
    public void addChatMessage(Long appId, String message, String messageType, Long userId) {
        // 校验应用ID是否合法
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        // 校验消息内容是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        // 校验消息类型是否为空
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        // 校验用户ID是否合法
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        
        // 验证消息类型是否合法
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型不合法");

        // 构建聊天历史记录对象并保存
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        this.save(chatHistory);
    }

    /**
     * 根据聊天历史查询请求条件构建查询包装器
     * @param chatHistoryQueryRequest 聊天历史查询请求对象，包含查询条件
     * @return QueryWrapper 构建好的查询包装器，用于数据库查询
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
    // 创建基础查询包装器
        QueryWrapper queryWrapper = QueryWrapper.create();
    // 如果查询请求为空，直接返回空查询包装器
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
    // 从请求对象中获取查询条件
        Long id = chatHistoryQueryRequest.getId();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

    // 设置查询条件：精确匹配ID、应用ID、用户ID、消息类型；模糊匹配消息内容
        queryWrapper.eq(ChatHistory::getId, id)
                .eq(ChatHistory::getAppId, appId)
                .eq(ChatHistory::getUserId, userId)
                .eq(ChatHistory::getMessageType, messageType)
                .like(ChatHistory::getMessage, message);

        // 按创建时间倒序，默认获取最新消息
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 将ChatHistory对象转换为ChatHistoryVO对象
     * 这是一个重写的方法，用于实现对象属性的复制和转换
     *
     * @param chatHistory 需要转换的ChatHistory对象
     * @return 转换后的ChatHistoryVO对象，如果输入为null则返回null
     */
    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
    // 如果输入对象为null，直接返回null
        if (chatHistory == null) {
            return null;
        }
    // 创建ChatHistoryVO对象
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
    // 使用BeanUtil工具类将chatHistory的属性复制到chatHistoryVO中
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
    // 返回转换后的对象
        return chatHistoryVO;
    }

    /**
     * 将聊天历史记录列表转换为聊天历史视图对象列表
     * @param chatHistoryList 聊天历史记录列表
     * @return 聊天历史视图对象列表，如果输入列表为空则返回空列表
     */
    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
    // 检查输入列表是否为空，如果为空则返回新的空列表
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
    // 使用Stream流将聊天历史记录列表转换为视图对象列表
        return chatHistoryList.stream().map(this::getChatHistoryVO).toList();
    }

    /**
     * 根据应用ID删除聊天历史记录
     *
     * @param appId 应用ID，必须为大于0的Long类型
     * @return 删除成功返回true，否则返回false
     */
    @Override
    public boolean removeByAppId(Long appId) {
    // 参数校验：如果appId为null或小于等于0，抛出参数错误异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
    // 创建查询条件，删除指定appId的所有聊天历史记录，并返回删除结果
        return this.remove(QueryWrapper.create().eq(ChatHistory::getAppId, appId));
    }

    /**
     * 分页查询应用聊天记录
     * @param appId 应用ID
     * @param pageSize 每页记录数
     * @param lastCreateTime 上一次查询的最后创建时间，用于分页
     * @param loginUser 当前登录用户
     * @return 分页后的聊天记录视图对象
     */
    @Override
    public Page<ChatHistoryVO> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
    // 校验应用ID参数合法性
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
    // 校验每页记录数参数合法性，限制在1-50之间
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在0-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
    // 获取应用信息
        App app = appService.getById(appId);
    // 校验应用是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        boolean isAdmin =  UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());;
        ThrowUtils.throwIf(!isAdmin && !app.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR, "没有权限查看该应用历史");

        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);

        return this.page(Page.of(1, pageSize), queryWrapper)
                .map(this::getChatHistoryVO);
    }

    /**
     * 加载聊天历史到内存中的方法
     * @param appId 应用ID
     * @param chatMemory 聊天记忆窗口对象
     * @param maxCount 最大加载数量
     * @return 实际加载的消息数量
     */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount){
        try{
            // 直接构造查询条件，起始点为1而不是0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)  // 设置应用ID条件
                    .orderBy(ChatHistory::getCreateTime, false)      // 按创建时间降序排列
                    .limit(1, maxCount);               // 设置查询范围

            // 执行查询获取聊天历史列表
            List<ChatHistory> historyList = this.list(queryWrapper);
            // 如果列表为空，直接返回0
            if(CollUtil.isEmpty(historyList)){
                return 0;
            }
            // 逆序添加到内存，使消息按时间正序排列
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;  // 记录成功加载的消息数量
            chatMemory.clear();    // 清空现有聊天记忆
            // 遍历历史消息列表
            for(ChatHistory history : historyList){
                // 判断消息类型并添加到记忆中
                if(MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));  // 添加用户消息
                    loadedCount++;
                }else if(MessageTypeEnum.AI.getValue().equals(history.getMessageType())){
                    chatMemory.add(AiMessage.from(history.getMessage()));    // 添加AI回复消息
                    loadedCount++;
                }
            }
            // 记录加载成功的日志
            log.info("加载历史对话成功，appId：{}, loadedCount: {}", appId, loadedCount);
            return loadedCount;
        }catch (Exception e){
            // 记录加载失败的日志
            log.error("加载历史对话失败，appId：{}, error: {}", appId, e.getMessage());
            return 0;
        }
    }
}