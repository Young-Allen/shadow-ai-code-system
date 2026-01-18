package com.shadow.aicodingsystem.core.saver;

import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;

import java.io.File;

public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();

    private static final  MultiFileCodeSaverTemplate multiFileCodeSaverTemplate = new MultiFileCodeSaverTemplate();

    /**
     * 根据代码生成类型执行相应的保存操作
     * @param codeResult 代码生成结果对象
     * @param codeGenTypeEnum 代码生成类型枚举
     * @return File 保存后的文件对象
     * @throws BusinessException 当遇到不支持的代码生成类型时抛出业务异常
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
    // 使用switch表达式根据不同的代码生成类型执行相应的保存逻辑
        return switch (codeGenTypeEnum) {
        // 当代码类型为HTML时，将结果强制转换为HtmlCodeResult并使用HTML代码保存模板进行保存
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult);
        // 当代码类型为MULTI_FILE时，将结果强制转换为MultiFileCodeResult并使用多文件代码保存模板进行保存
            case MULTI_FILE -> multiFileCodeSaverTemplate.saveCode((MultiFileCodeResult) codeResult);
        // 默认情况下，抛出系统错误异常，提示不支持的代码生成类型
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
