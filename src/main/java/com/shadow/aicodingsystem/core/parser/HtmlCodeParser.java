package com.shadow.aicodingsystem.core.parser;

import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeParser implements CodeParser<HtmlCodeResult>{
    private static final Pattern HEML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析代码内容并返回HTML格式的结果
     * @param codeContent 需要解析的代码内容字符串
     * @return 返回一个HtmlCodeResult对象，包含解析后的HTML代码
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
    // 创建一个新的HtmlCodeResult对象用于存储结果
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
    // 从输入的代码内容中提取HTML代码
        String htmlCode = extractHtmlCode(codeContent);
    // 检查提取的HTML代码是否有效（不为null且不为空字符串）
        if(htmlCode != null && !htmlCode.trim().isEmpty()){
        // 如果HTML代码有效，将其设置到结果对象中
            htmlCodeResult.setHtmlCode(htmlCode);
        }else{
        // 如果HTML代码无效，则使用原始代码内容
            htmlCodeResult.setHtmlCode(codeContent);
        }
    // 返回处理后的结果
        return htmlCodeResult;
    }

    /**
     * 从给定的代码内容中提取HTML代码
     * 该方法使用正则表达式匹配并提取HTML代码内容
     * @param codeContent 包含可能HTML代码的字符串内容
     * @return 如果找到HTML代码则返回去除首尾空格的HTML代码字符串，否则返回null
     */
    private static String extractHtmlCode(String codeContent){
        // 使用预定义的正则表达式模式HEML_CODE_PATTERN匹配codeContent
        Matcher matcher = HEML_CODE_PATTERN.matcher(codeContent);
        // 检查是否找到匹配项
        if(matcher.find()){
            // 返回第一个捕获组的内容，并去除首尾空格
            return matcher.group(1).trim();
        }
        // 如果未找到匹配项，返回null
        return null;
    }
}
