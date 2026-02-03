package com.shadow.aicodingsystem.ai;

import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户提示，路由到对应的代码生成类型
     * @param userPrompt 用户提示
     * @return 代码生成类型
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
