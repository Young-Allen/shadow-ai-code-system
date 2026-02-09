package com.shadow.aicodingsystem.langgraph4j.tools;

import com.shadow.aicodingsystem.langgraph4j.model.ImageResource;
import com.shadow.aicodingsystem.langgraph4j.model.enums.ImageCategoryEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    @Test
    void searchContentImages() {
        List<ImageResource> images = imageSearchTool.searchContentImages("technology");
        assertNotNull(images);
    }

    @Test
    void testSearchIllustrations() {
        // 测试正常搜索插画
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("happy");
        assertNotNull(illustrations);
        // 验证返回的插画资源
        Assumptions.assumeTrue(!illustrations.isEmpty(), "No results returned from Undraw (network/API may be unavailable)");
        ImageResource firstIllustration = illustrations.get(0);
        assertEquals(ImageCategoryEnum.ILLUSTRATION, firstIllustration.getCategory());
        assertNotNull(firstIllustration.getDescription());
        assertNotNull(firstIllustration.getUrl());
        assertTrue(firstIllustration.getUrl().startsWith("http"));
        System.out.println("搜索到 " + illustrations.size() + " 张插画");
        illustrations.forEach(illustration ->
                System.out.println("插画: " + illustration.getDescription() + " - " + illustration.getUrl())
        );
    }

    @Test
    void testGenerateMermaidDiagram() {
        // 测试生成 Mermaid 架构图
        String mermaidCode = """
                flowchart LR
                    Start([开始]) --> Input[输入数据]
                    Input --> Process[处理数据]
                    Process --> Decision{是否有效?}
                    Decision -->|是| Output[输出结果]
                    Decision -->|否| Error[错误处理]
                    Output --> End([结束])
                    Error --> End
                """;

        String description = "简单系统架构图";
        List<ImageResource> diagrams = mermaidDiagramTool.generateMermaidDiagram(mermaidCode, description);
        assertNotNull(diagrams);
        // 如果有结果，验证图表资源
        Assumptions.assumeTrue(!diagrams.isEmpty(), "No diagrams generated (network/API may be unavailable)");
        ImageResource firstDiagram = diagrams.get(0);
        assertEquals(ImageCategoryEnum.ARCHITECTURE, firstDiagram.getCategory());
        assertEquals(description, firstDiagram.getDescription());
        assertNotNull(firstDiagram.getUrl());
//        assertTrue(firstDiagram.getUrl().startsWith("https"));
        System.out.println("生成了架构图: " + firstDiagram.getUrl());
    }

    @Test
    void testGenerateLogos() {
        // 测试生成Logo
        List<ImageResource> logos = logoGeneratorTool.generateLogos("技术公司现代简约风格Logo");
        assertNotNull(logos);
        ImageResource firstLogo = logos.getFirst();
        assertEquals(ImageCategoryEnum.LOGO, firstLogo.getCategory());
        assertNotNull(firstLogo.getDescription());
        assertNotNull(firstLogo.getUrl());
        logos.forEach(logo ->
                System.out.println("Logo: " + logo.getDescription() + " - " + logo.getUrl())
        );
    }
}
