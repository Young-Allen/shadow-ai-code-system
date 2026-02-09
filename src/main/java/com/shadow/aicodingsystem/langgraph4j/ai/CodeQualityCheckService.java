package com.shadow.aicodingsystem.langgraph4j.ai;

import com.shadow.aicodingsystem.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface CodeQualityCheckService {

    /**
     * 检查代码质量
     *
     * @param codeContent 代码内容
     * @return 代码质量结果
     */
    @SystemMessage(fromResource = "prompt/code-quality-check-prompt.txt")
    QualityResult checkCodeQuality(@UserMessage String codeContent);
}
