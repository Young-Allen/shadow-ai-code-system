package com.shadow.aicodingsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shadow.aicodingsystem.ai.AiCodeGenTypeRoutingService;
import com.shadow.aicodingsystem.ai.model.enums.CodeGenTypeEnum;
import com.shadow.aicodingsystem.constant.AppConstant;
import com.shadow.aicodingsystem.core.AiCodeGeneratorFacade;
import com.shadow.aicodingsystem.core.builder.VueProjectBuilder;
import com.shadow.aicodingsystem.core.handler.StreamHandlerExecutor;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.exception.ThrowUtils;
import com.shadow.aicodingsystem.model.dto.app.AppAddRequest;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.vo.UserVO;
import com.shadow.aicodingsystem.service.ScreenshotService;
import com.shadow.aicodingsystem.service.UserService;
import com.shadow.aicodingsystem.mapper.AppMapper;
import com.shadow.aicodingsystem.model.dto.app.AppQueryRequest;
import com.shadow.aicodingsystem.model.entity.App;
import com.shadow.aicodingsystem.model.vo.AppVO;
import com.shadow.aicodingsystem.service.AppService;
import com.shadow.aicodingsystem.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import com.shadow.aicodingsystem.model.enums.MessageTypeEnum;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author shadow
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginuser) {
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示不能为空");
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginuser.getId());
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());

        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("创建应用成功, 应用ID: {}, 类型: {}", app.getId(), app.getCodeGenType());
        return app.getId();
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        //关联查询用户信息
        Long userId = app.getUserId();
        if(userId != null){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        //批量获取用户信息，避免 N+1 查询问题
        Set<Long> userId = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userId).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));

        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Long userId = appQueryRequest.getUserId();
        String userName = appQueryRequest.getUserName();
        Integer priority = appQueryRequest.getPriority();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // ① 先根据 userName 模糊查询出 userIds
        List<Long> userIdList = null;
        if (StrUtil.isNotBlank(userName)) {
            userIdList = userService.list(
                    QueryWrapper.create().select("id").from("user")
                            .like("userName", userName)
            ).stream().map(User::getId).toList();

            // 如果用户昵称有条件，但一个都没匹配到，则直接让 app 查询返回空
            if (userIdList.isEmpty()) {
                // 返回一个永远不成立的条件
                return QueryWrapper.create().eq("id", -1);
            }
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                // 精确匹配
                .eq(App::getId, id, id != null && id > 0)
                .eq(App::getUserId, userId)
                .eq(App::getPriority, priority, priority != null)
                // 模糊匹配
                .like("appName", appName, StrUtil.isNotBlank(appName))
                .like("cover", cover, StrUtil.isNotBlank(cover))
                .like("initPrompt", initPrompt, StrUtil.isNotBlank(initPrompt))
                // 枚举 / 标识类字段
                .eq("codeGenType", codeGenType,  StrUtil.isNotBlank(codeGenType))
                .eq("deployKey", deployKey, StrUtil.isNotBlank(deployKey));

        // ③ 如果 userName 条件存在，就用 userId in (...) 过滤
        if (userIdList != null) {
            queryWrapper.in(App::getUserId, userIdList);
        }

        // 排序：如果有排序字段，则按排序字段排序，否则按优先级降序、创建时间降序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("priority", false);
            queryWrapper.orderBy("createTime", false);
        }

        return queryWrapper;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginuser) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "message不能为空");
        //2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //3. 验证用户是否有权限访问改应用，仅本人可以生成代码
        if(!app.getUserId().equals(loginuser.getId())){
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "没有权限访问该应用");
        }
        //4. 记录用户输入,存数据库
        chatHistoryService.addChatMessage(appId, message, MessageTypeEnum.USER.getValue(), loginuser.getId());
        //5. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum =  CodeGenTypeEnum.getEnumByValue(codeGenType);
        if(codeGenTypeEnum == null){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        //6. 调用 AI 生成代码流
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        //7. 处理 AI 生成代码流
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService,  appId, loginuser, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null || appId < 0, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "登录用户不能为空");
        //2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        //3. 验证用户是否有权限部署该应用，仅本人可以部署
        if(!app.getUserId().equals(loginUser.getId())){
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "没有权限部署该应用");
        }
        //4. 查询是否已有deployKey，如果没有则生成新的
        String deployKey = app.getDeployKey();
        if(StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        //5. 获取代码生成类型，构建原目录路径，检查是否存在
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), 
                ErrorCode.PARAMS_ERROR, "代码目录不存在，请先生成代码");
        //6. 如果是VUE项目，则构建项目
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if(codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT){
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "VUE项目构建失败，无法部署");
            //检查dist目录是否存在
            File distDir = new File(sourceDirPath + File.separator + "dist");
            ThrowUtils.throwIf(!distDir.exists() || !distDir.isDirectory(),
                ErrorCode.SYSTEM_ERROR, "VUE项目构建失败，无法部署");
            sourceDir = distDir;
            log.info("VUE项目构建成功，准备部署，目录：{}", sourceDir.getAbsolutePath());
        }

        //6. 复制文件到部署目录
        // 部署目录：tmp/code_deploy/{deployKey}
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try{
            FileUtil.copyContent(sourceDir, new File(deployDirPath) , true);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败" + e.getMessage());
        }

        //7. 更新应用的deployKey和部署时间，并返回url
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updatedById = this.updateById(updateApp);
        ThrowUtils.throwIf(!updatedById, ErrorCode.SYSTEM_ERROR, "部署失败");

        //8. 返回部署url
        String appDeployUrl =  String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        //9. 异步生成应用截图并更新封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程并执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新数据库的封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    @Override
    public boolean removeById(Serializable id) {
        if(id == null){
            return false;
        }
        Long appId = Long.valueOf(id.toString());
        if(appId < 0){
            return false;
        }
        try{
            chatHistoryService.removeByAppId(appId);
        }catch(Exception e){
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        return super.removeById(appId);
    }
}