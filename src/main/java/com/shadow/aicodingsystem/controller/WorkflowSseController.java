package com.shadow.aicodingsystem.controller;

import com.shadow.aicodingsystem.langgraph4j.CodeGenWorkflow;
import com.shadow.aicodingsystem.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * 工作流SSE控制器
 * langgraph4j 的工作流的流式输出功能
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
public class WorkflowSseController {

    /**
     * 执行工作流
     * @param originalPrompt
     * @return
     */
    @PostMapping("/execute")
    public WorkflowContext executeWorkflow(@RequestParam String originalPrompt) {
        log.info("收到同步工作流执行请求: {}", originalPrompt);
        return new CodeGenWorkflow().executeWorkflow(originalPrompt);
    }

    /**
     *  Flux 流式执行工作流
     */
    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowWithFlux(@RequestParam String originalPrompt) {
        log.info("收到 Flux 工作流执行请求: {}", originalPrompt);
        return new CodeGenWorkflow().executeWorkflowWithFlux(originalPrompt);
    }


    /**
     * SSE 流式执行工作流
     */
    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeWorkflowWithSse(@RequestParam String originalPrompt) {
        log.info("收到 SSE 工作流执行请求: {}", originalPrompt);
        return new CodeGenWorkflow().executeWorkflowWithSse(originalPrompt);
    }
}
