package com.shadow.aicodingsystem.core;

import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeParser {
    private static final Pattern HEML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析HTML代码的方法
     * @param codeContent 需要解析的内容字符串
     * @return 返回一个HtmlCodeResult对象，包含解析后的HTML代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent){
        // 创建HtmlCodeResult对象用于存储结果
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        //提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if(htmlCode != null && !htmlCode.trim().isEmpty()){
            htmlCodeResult.setHtmlCode(htmlCode);
        }else{
            //如果没有找到代码块，将整个内容作为html
            htmlCodeResult.setHtmlCode(codeContent.trim());
        }
        return htmlCodeResult;
    }

    /**
     * 解析多文件代码内容，提取HTML、CSS和JavaScript代码
     * @param codeContent 包含多文件代码的字符串内容
     * @return MultiFileCodeResult 包含提取出的HTML、CSS和JavaScript代码的结果对象
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        // 创建多文件代码结果对象
        MultiFileCodeResult mutiFileCodeResult = new MultiFileCodeResult();

        // 使用正则表达式模式提取HTML代码
        String htmlCode = extractCodeByPattern(codeContent, HEML_CODE_PATTERN);
        // 使用正则表达式模式提取CSS代码
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        // 使用正则表达式模式提取JavaScript代码
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        // 检查并设置HTML代码（非空时）
        if(htmlCode != null && !htmlCode.trim().isEmpty()){
            mutiFileCodeResult.setHtmlCode(htmlCode.trim());
        }
        // 检查并设置CSS代码（非空时）
        if(cssCode != null && !cssCode.trim().isEmpty()){
            mutiFileCodeResult.setCssCode(cssCode.trim());
        }
        // 检查并设置JavaScript代码（非空时）
        if (jsCode != null && !jsCode.trim().isEmpty()){
            mutiFileCodeResult.setJsCode(jsCode.trim());
        }
        // 返回包含提取代码的结果对象
        return mutiFileCodeResult;
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

    /**
     * 根据正则表达式模式从内容字符串中提取代码片段
     * @param content 要处理的原始内容字符串
     * @param pattern 用于匹配的正则表达式模式
     * @return 匹配到的第一个分组的内容（去除前后空格），如果没有匹配到则返回null
     */
    private static String extractCodeByPattern(String content, Pattern pattern){
        // 使用给定的模式创建一个Matcher对象
        Matcher matcher = pattern.matcher(content);
        // 尝试查找匹配的内容
        if(matcher.find()){
            // 返回第一个分组的内容，并去除前后空格
            return matcher.group(1).trim();
        }
        // 如果没有找到匹配项，返回null
        return null;
    }

}
