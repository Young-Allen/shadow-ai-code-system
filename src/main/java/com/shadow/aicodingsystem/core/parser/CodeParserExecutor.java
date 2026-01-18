package com.shadow.aicodingsystem.core.parser;

import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;

public class CodeParserExecutor {
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析器，根据不同的代码生成类型解析代码内容
     * @param codeContent 需要解析的代码内容
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return 解析后的结果对象
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        // 使用switch表达式根据不同的代码生成类型执行相应的解析器
        return switch (codeGenTypeEnum) {
            // 当类型为HTML时，使用HTML代码解析器解析代码内容
            case HTML -> htmlCodeParser.parseCode(codeContent);
            // 当类型为MULTI_FILE时，使用多文件代码解析器解析代码内容
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            // 默认情况下，抛出业务异常，提示不支持的代码生成类型
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型" + codeGenTypeEnum);
        };
    }
}
