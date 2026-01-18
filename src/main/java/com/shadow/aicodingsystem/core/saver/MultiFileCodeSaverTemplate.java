package com.shadow.aicodingsystem.core.saver;

import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;

public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML 代码内容不能为空");
        }
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }
}
