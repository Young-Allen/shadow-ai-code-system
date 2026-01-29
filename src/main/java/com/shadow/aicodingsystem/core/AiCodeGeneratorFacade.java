package com.shadow.aicodingsystem.core;

import com.shadow.aicodingsystem.ai.AiCodeGeneratorService;
import com.shadow.aicodingsystem.ai.AiCodeGeneratorServiceFactory;
import com.shadow.aicodingsystem.ai.model.HtmlCodeResult;
import com.shadow.aicodingsystem.ai.model.MultiFileCodeResult;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.core.parser.CodeParserExecutor;
import com.shadow.aicodingsystem.core.saver.CodeFileSaverExecutor;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

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
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(appId, userMessage);
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
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);

        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum;
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成HTML代码并保存为文件的方法
     *
     * @param userMessage 用户输入的消息，用于生成对应的HTML代码
     * @return File 返回生成的HTML文件对象
     */
//    private File generateAndSaveHtmlCode(String userMessage) {
//    // 调用AI代码生成服务生成HTML代码
//        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(1, userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//    }

    /**
     * 生成并保存多文件代码
     * 该方法通过AI代码生成服务生成多文件代码，并将其保存到指定位置
     *
     * @param userMessage 用户输入的消息，用于生成代码的依据
     * @return File 返回生成的多文件代码保存后的文件对象
     */
//    private File generateAndSaveMultiFileCode(String userMessage) {
//    // 调用AI代码生成服务生成多文件代码
//        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//    // 使用代码文件保存服务保存生成的多文件代码结果，并返回保存后的文件对象
//        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//    }


    /**
     * 生成并保存HTML代码流
     * @param userMessage
     * @return 保存的目录
     */
//    public Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//        StringBuilder codeBuilder = new StringBuilder();
//        return result
//                .doOnNext(chunk -> {
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    try{
//                        String completeHtmlCode = codeBuilder.toString();
//                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
//                        File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                        log.info("HTML代码生成完成，保存路径为：{}", saveDir.getAbsolutePath());
//                    }catch (Exception e){
//                        log.error("HTML代码生成失败", e.getMessage());
//                    }
//                });
//    }

    /**
     * 生成并保存多文件代码流
     * @param userMessage
     * @return 保存的目录
     */
//    public Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        //当流式返回生成代码完成后，再保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        return result
//                .doOnNext(chunk -> {
//                    //实时收集代码片段
//                    codeBuilder.append(chunk);
//                })
//                .doOnComplete(() -> {
//                    try{
//                        String completeMultiFileCode = codeBuilder.toString();
//                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
//                        File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//                        log.info("多文件代码生成完成，保存路径为：{}", saveDir.getAbsolutePath());
//                    }catch (Exception e){
//                        log.error("多文件代码生成失败", e.getMessage());
//                    }
//                });
//    }

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
}
