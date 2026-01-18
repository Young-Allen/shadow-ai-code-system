package com.shadow.aicodingsystem.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {

    //文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存HTML代码结果到指定目录
     * @param htmlCodeResult 包含HTML代码的结果对象
     * @return 返回保存HTML文件的目录File对象
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult){
        // 构建基于HTML类型的唯一目录路径
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        // 将HTML代码写入到index.html文件中
        writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        // 返回保存文件的目录
        return new File(baseDirPath);
    }

    /**
     * 保存多文件代码结果到指定目录
     * @param multiFileCodeResult 包含HTML、CSS和JS代码的多文件代码结果对象
     * @return 返回保存代码的File对象，指向创建的目录
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult){
        // 构建唯一目录路径，使用CodeGenTypeEnum.MULTI_FILE的值作为参数
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 将HTML代码写入到index.html文件
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        // 将CSS代码写入到style.css文件
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        // 将JS代码写入到script.js文件
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        // 返回创建的目录File对象
        return new File(baseDirPath);
    }


    /**
     * 根据业务类型创建唯一目录
     * @param bizType 业务类型标识
     * @return 返回创建的完整目录路径
     */
    private static String buildUniqueDir(String bizType){
    // 使用业务类型和雪花算法生成的唯一ID组合成目录名称
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
    // 拼接完整的目录路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
    // 创建目录（如果不存在）
        FileUtil.mkdir(dirPath);
    // 返回创建的目录路径
        return dirPath;
    }

    /**
     * 将指定内容写入到文件中
     * @param dirPath 文件所在目录路径
     * @param fileName 文件名
     * @param content 要写入文件的内容
     */
    private static void writeToFile(String dirPath, String fileName, String content){
    // 拼接完整的文件路径，使用File.separator确保跨平台兼容性
        String filePath = dirPath + File.separator + fileName;
    // 调用FileUtil工具类的方法将内容以UTF-8编码写入指定文件
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
