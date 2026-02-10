package com.shadow.aicodingsystem.langgraph4j.node;

import com.shadow.aicodingsystem.ai.AiCodeGenTypeRoutingService;
import com.shadow.aicodingsystem.ai.AiCodeGenTypeRoutingServiceFactory;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.langgraph4j.state.WorkflowContext;
import com.shadow.aicodingsystem.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create(){
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点：智能路由");
            CodeGenTypeEnum generationType;
            try{
                AiCodeGenTypeRoutingServiceFactory routingServiceFactory = SpringContextUtil.getBean(AiCodeGenTypeRoutingServiceFactory.class);
                AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = routingServiceFactory.getAiCodeGenTypeRoutingService();
                generationType = aiCodeGenTypeRoutingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("AI 智能路由完成，生成类型为：{}", generationType);
            }catch (Exception e ){
                log.error("AI 智能路由失败， 使用默认HTML类型：{}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新上下文
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            return WorkflowContext.saveContext(context);
        });
    }
}
