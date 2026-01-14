package com.shadow.aicodingsystem.aop;

import com.shadow.aicodingsystem.annotation.AuthCheck;
import com.shadow.aicodingsystem.exception.BusinessException;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.enums.UserRoleEnum;
import com.shadow.aicodingsystem.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 环绕拦截器，用于处理带有@AuthCheck注解的方法
     * @param joinPoint 连接点，可以获取被拦截方法的信息
     * @param authCheck 自定义的权限检查注解
     * @return 方法执行结果
     * @throws Throwable 方法执行可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
    // 从注解中获取要求的权限
        String mustRole = authCheck.mustRole();
    // 获取当前请求的属性
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    // 从请求属性中获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        //当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        //不需要权限,放行
        if(mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 以下为：必须有权限才能通过
        //获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        //没有权限，拒绝
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        return joinPoint.proceed();

    }

}
