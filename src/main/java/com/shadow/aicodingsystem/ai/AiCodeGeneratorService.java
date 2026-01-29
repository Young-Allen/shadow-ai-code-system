package com.shadow.aicodingsystem.ai;

import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 使用系统消息注解标记的方法，用于生成HTML代码
     * 该方法从资源文件"prompt/codegen-html-system-prompt.txt"加载系统提示信息
     *
     * @param userMessage 用户输入的消息，将用于生成相应的HTML代码
     * @return 生成的HTML代码字符串
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(@MemoryId long memoryId, @UserMessage String userMessage);


    /**
     * 生成多文件代码的系统方法
     * 该方法使用从资源文件中加载的系统提示，来生成多文件代码
     *
     * @param userMessage 用户输入的消息，作为生成代码的输入
     * @return 生成的多文件代码，以字符串形式返回
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成HTML代码流的方法
     * 该方法接收用户消息并返回一个响应流，用于逐步生成HTML代码
     *
     * @param userMessage 用户输入的消息，将用于生成相应的HTML代码
     * @return 返回一个Flux<String>类型的响应流，包含逐步生成的HTML代码内容
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 使用系统消息生成多文件代码流
     * 该注解 @SystemMessage 指定了从资源文件 "prompt/codegen-multi-file-file-system-prompt.txt" 中加载系统提示
     *
     * @param userMessage 用户输入的消息内容，用于生成代码
     * @return 返回一个 Flux<String> 类型的响应流，表示生成的多文件代码内容流
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
