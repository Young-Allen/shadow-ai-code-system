package com.shadow.aicodingsystem.core;

import cn.hutool.json.JSONUtil;
import com.shadow.aicodingsystem.ai.AiCodeGeneratorService;
import com.shadow.aicodingsystem.ai.AiCodeGeneratorServiceFactory;
import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.ai.model.message.AiResponseMessage;
import com.shadow.aicodingsystem.ai.model.message.ToolExecutedMessage;
import com.shadow.aicodingsystem.ai.model.message.ToolRequestMessage;
import com.shadow.aicodingsystem.core.parser.CodeParserExecutor;
import com.shadow.aicodingsystem.core.saver.CodeFileSaverExecutor;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.tokenize.TokenSample;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 根据用户输入的消息和生成类型生成代码并保存为文件的方法
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        //从工厂中通过appId加载对应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);

        return switch (codeGenTypeEnum){
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 根据用户输入的消息和生成类型生成代码并保存为文件的方法(流式）
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        if(codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        //从工厂中通过appId加载对应的AI服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                // yield 用于“返回当前 case 的值”。
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(codeStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 处理代码流，生成代码并保存
     * @param codeStream
     * @param codeGenType
     * @param appId
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                String completeCode = codeBuilder.toString();
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                log.info("{}代码生成完成，保存路径为：{}", codeGenType, saveDir.getAbsolutePath());
            }catch (Exception e){
                log.error("{}代码生成失败", codeGenType, e.getMessage());
            }
        });
    }

    /**
     * 将 TokenStream 处理为 Flux<String> 流
     * @param tokenStream
     * @return
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    // 1. 处理普通 AI 文本响应
                    .onPartialResponse((String partialResponse) -> {
                        AiResponseMessage msg = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    // 2. 处理流式生成的工具参数 (1.11.0 特有)
                    //如果你想让前端实时看到 JSON 参数在一点点崩出来，可以使用这个钩子
//                    .onPartialToolCall((PartialToolCall partialToolCall) -> {
//                        ToolRequestMessage msg = new ToolRequestMessage(partialToolCall);
//                        sink.next(JSONUtil.toJsonStr(msg));
//                    })
                    // 4. 处理工具执行完毕后的结果
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage msg = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(msg));
                    })
                    // 5. 生命周期结束
                    .onCompleteResponse((ChatResponse chatResponse) -> {
                        sink.complete();
                    })
                    // 6. 异常处理
                    .onError((Throwable error) -> {
                        log.error("LangChain4j streaming error: ", error);
                        sink.error(error);
                    })
                    // 7. 必须手动启动流
                    .start();
        });
    }
}
