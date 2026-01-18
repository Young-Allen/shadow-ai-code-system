package com.shadow.aicodingsystem.core.parser;

import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {
    private static final Pattern HEML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析代码内容，提取HTML、CSS和JavaScript代码
     * @param codeContent 包含完整代码内容的字符串，可能包含HTML、CSS和JavaScript代码
     * @return MultiFileCodeResult 包含解析出的HTML、CSS和JavaScript代码的对象
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        // 创建MultiFileCodeResult对象用于存储解析结果
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 使用正则表达式分别提取HTML、CSS和JavaScript代码
        String htmlCode = extractCodeByPattern(codeContent, HEML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        // 检查HTML代码是否为空，不为空则设置到结果对象中
        if(htmlCode != null && !htmlCode.trim().isEmpty()){
            result.setHtmlCode(htmlCode);
        }
        // 检查CSS代码是否为空，不为空则设置到结果对象中
        if(cssCode != null && !cssCode.trim().isEmpty()){
            result.setCssCode(cssCode);
        }
        // 检查JavaScript代码是否为空，不为空则设置到结果对象中
        if(jsCode != null && !jsCode.trim().isEmpty()){
            result.setJsCode(jsCode);
        }
        // 返回包含所有解析出的代码的结果对象
        return result;
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
