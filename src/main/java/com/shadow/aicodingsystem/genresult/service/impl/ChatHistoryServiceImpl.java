package com.shadow.aicodingsystem.genresult.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shadow.aicodingsystem.genresult.entity.ChatHistory;
import com.shadow.aicodingsystem.genresult.mapper.ChatHistoryMapper;
import com.shadow.aicodingsystem.genresult.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author shadow
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
