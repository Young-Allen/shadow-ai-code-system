package com.shadow.aicodingsystem.langgraph4j.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.langgraph4j.model.ImageCollectionPlan;
import com.shadow.aicodingsystem.langgraph4j.model.ImageResource;
import com.shadow.aicodingsystem.langgraph4j.model.QualityResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessagesState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 用户原始输入的提示词
     */
    private String originalPrompt;

    /**
     * 图片资源字符串
     */
    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 质量检查结果
     */
    private QualityResult qualityResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 图片收集计划
     */
    private ImageCollectionPlan imageCollectionPlan;

    /**
     * 应用ID
     */
    private Long appId = 0L;

    /**
     * 并发图片收集的中间结果字段
     */
    private List<ImageResource> contentImages;
    private List<ImageResource> illustrations;
    private List<ImageResource> diagrams;
    private List<ImageResource> logos;

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        Object raw = state.data().get(WORKFLOW_CONTEXT_KEY);
        if (raw == null) {
            return null;
        }
        if (raw instanceof WorkflowContext workflowContext) {
            return workflowContext;
        }
        try {
            if (raw instanceof String json) {
                return OBJECT_MAPPER.readValue(json, WorkflowContext.class);
            }
            return OBJECT_MAPPER.convertValue(raw, WorkflowContext.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize WorkflowContext from state, rawType=" + raw.getClass(), e);
        }
    }

    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        if (context == null) {
            throw new IllegalArgumentException("WorkflowContext cannot be null");
        }
        try {
            return Map.of(WORKFLOW_CONTEXT_KEY, OBJECT_MAPPER.writeValueAsString(context));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize WorkflowContext", e);
        }
    }
}
