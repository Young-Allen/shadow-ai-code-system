package com.shadow.aicodingsystem.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.shadow.aicodingsystem.annotation.AuthCheck;
import com.shadow.aicodingsystem.common.BaseResponse;
import com.shadow.aicodingsystem.common.ResultUtils;
import com.shadow.aicodingsystem.constant.UserConstant;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.exception.ThrowUtils;
import com.shadow.aicodingsystem.model.dto.chathistory.ChatHistoryQueryRequest;
import com.shadow.aicodingsystem.model.entity.App;
import com.shadow.aicodingsystem.model.entity.ChatHistory;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.vo.ChatHistoryVO;
import com.shadow.aicodingsystem.service.AppService;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import com.shadow.aicodingsystem.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author shadow
 */
@RestController
@RequestMapping("/chat/history")
public class ChatHistoryController {
    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    @GetMapping("/app/{appId}")
    @AuthCheck(mustRole = UserConstant.USER_LOGIN_STATE)
    public BaseResponse<Page<ChatHistoryVO>> listAppChatHistory(@PathVariable Long appId,
                                                               @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                               @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 【管理员】查看所有应用的对话历史，按时间倒序。
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 历史分页
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryVO>> listByAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        if (chatHistoryQueryRequest.getPageSize() == null || chatHistoryQueryRequest.getPageSize() <= 0) {
            chatHistoryQueryRequest.setPageSize(10);
        } else if (chatHistoryQueryRequest.getPageSize() > 100) {
            chatHistoryQueryRequest.setPageSize(100);
        }

        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();

        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistoryVO> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper)
                .map(chatHistoryService::getChatHistoryVO);
        return ResultUtils.success(result);
    }
}
