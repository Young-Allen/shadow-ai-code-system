package com.shadow.aicodingsystem.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate <T>{
    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存代码结果到文件系统
     * 该方法是一个模板方法，定义了保存代码的基本流程，具体文件保存逻辑由子类实现
     *
     * @param result 需要保存的代码结果对象
     * @return 保存结果的File对象，指向保存的目录
     */
    public final File saveCode(T result){
        //1. 验证输入参数的有效性
        validateInput(result);
        //2. 构建目录
        String baseDirPath = buildUniqueDir();
        //3. 保存文件（具体实现由子类提供）
        saveFiles(result, baseDirPath);
        //4. 返回文件
        return new File(baseDirPath);
    }

    /**
     * 验证输入结果对象的有效性
     * @param result 需要验证的结果对象
     * @throws BusinessException 当结果对象为空时抛出业务异常
     */
    protected void validateInput(T result){
        // 判断结果对象是否为空
        if(result == null)
            // 如果为空，抛出带有系统错误码和错误信息的业务异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
    }

    /**
     * 构建一个唯一的目录路径
     * 该方法会根据业务类型和生成的唯一ID创建一个新的目录
     * @return 返回创建好的目录路径
     */
    protected final String buildUniqueDir(){
        // 获取业务类型的值
        String codeType = getCodeType().getValue();
        // 使用业务类型和雪花算法生成的唯一ID组合成目录名称
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        // 拼接完整的目录路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        // 创建目录（如果不存在）
        FileUtil.mkdir(dirPath);
        // 返回创建的目录路径
        return dirPath;
    }

    /**
     * 将内容写入指定文件
     * @param dirPath 文件所在目录路径
     * @param fileName 文件名
     * @param content 要写入文件的内容
     */
    protected final void writeToFile(String dirPath, String fileName, String content){
        // 检查内容是否为空或空白字符串
        if(StrUtil.isNotBlank(content)){
            // 拼接完整的文件路径
            String filePath = dirPath + File.separator + fileName;
            // 使用FileUtil工具类将内容以UTF-8编码写入文件
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型（由子类实现）
     * @return  代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（由子类实现）
     * @param result    代码结果对象
     * @param baseDirPath   基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}