package com.shadow.aicodingsystem.controller;

import com.mybatisflex.core.paginate.Page;
import com.shadow.aicodingsystem.common.BaseResponse;
import com.shadow.aicodingsystem.common.ResultUtils;
import com.shadow.aicodingsystem.exception.ErrorCode;
import com.shadow.aicodingsystem.exception.ThrowUtils;
import com.shadow.aicodingsystem.model.dto.user.UserLoginRequest;
import com.shadow.aicodingsystem.model.dto.user.UserRegisterRequest;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.vo.LoginUserVO;
import com.shadow.aicodingsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author shadow
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param userRegisterRequest 用户注册请求参数，包含用户账号、密码和确认密码
     * @return BaseResponse<Long> 返回注册成功的用户ID
     */
    @PostMapping("register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    // 检查请求参数是否为空，若为空则抛出参数错误异常
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

    // 获取用户注册请求中的账号、密码和确认密码
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
    // 调用用户服务进行用户注册操作，获取注册结果
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
    // 返回注册成功的用户ID
        return ResultUtils.success(result);
    }


    /**
     * 用户登录接口
     * @param userLoginRequest 用户登录请求参数，包含用户账号和密码
     * @param request HTTP请求对象，用于获取请求相关信息
     * @return BaseResponse<LoginUserVO> 返回登录结果，包含登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

    // 从请求参数中获取用户账号和密码
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
    // 调用userService的userLogin方法进行用户登录验证，并获取登录用户信息
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

    // 返回成功响应，包含登录用户信息
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户信息的接口方法
     * 通过HTTP GET请求访问，路径为 "/get/login"
     *
     * @param request HttpServletRequest对象，用于获取HTTP请求信息
     * @return BaseResponse<LoginUserVO> 包含登录用户信息的响应对象
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
    // 调用userService的getLoginUser方法从request中获取登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 使用ResultUtils.success方法包装并返回用户信息的VO对象
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }


    /**
     * 用户登出接口
     * 处理用户登出请求，验证请求参数并调用服务层方法执行登出操作
     *
     * @param request HTTP请求对象，用于获取会话信息
     * @return BaseResponse<Boolean> 返回操作结果，成功时返回true
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    // 参数校验：检查请求对象是否为空
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    // 调用服务层方法执行用户登出操作
        boolean result = userService.userLogout(request);
    // 返回操作成功结果
        return ResultUtils.success(result);
    }

    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户。
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

}
