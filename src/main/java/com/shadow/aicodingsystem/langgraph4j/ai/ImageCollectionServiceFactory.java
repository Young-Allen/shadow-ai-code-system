package com.shadow.aicodingsystem.langgraph4j.ai;

import com.shadow.aicodingsystem.langgraph4j.tools.ImageSearchTool;
import com.shadow.aicodingsystem.langgraph4j.tools.LogoGeneratorTool;
import com.shadow.aicodingsystem.langgraph4j.tools.MermaidDiagramTool;
import com.shadow.aicodingsystem.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    /**
     * 创建图片收集服务
     * @return
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,
                        undrawIllustrationTool,
                        logoGeneratorTool,
                        mermaidDiagramTool
                )
                .build();
    }
}
