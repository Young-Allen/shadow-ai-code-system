package com.shadow.aicodingsystem.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.shadow.aicodingsystem.model.dto.user.UserQueryRequest;
import com.shadow.aicodingsystem.model.entity.User;
import com.shadow.aicodingsystem.model.vo.LoginUserVO;
import com.shadow.aicodingsystem.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author shadow
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 注册结果
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 根据用户信息获取登录用户视图对象
     * 该方法用于将用户实体对象转换为登录用户视图对象，通常用于前端展示
     *
     * @param user 用户实体对象，包含用户的完整信息
     * @return LoginUserVO 登录用户视图对象，包含前端需要的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param request       HTTP请求对象，用于获取请求信息
     * @return LoginUserVO 登录用户视图对象，包含前端需要的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求对象，用于获取请求信息
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request HTTP请求对象，用于获取请求信息
     * @return 登出结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据User实体对象获取UserVO视图对象
     * UserVO通常用于前端展示，可能包含与User实体不同或经过处理的数据
     *
     * @param user 用户实体对象，包含用户的基本信息
     * @return UserVO 视图对象，用于前端展示的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 根据用户列表获取用户视图对象列表
     * UserVO是User的视图对象，通常用于前端展示，可能只包含用户的部分信息
     *
     * @param userList 用户实体列表，包含完整的用户信息
     * @return 用户视图对象列表，包含用于展示的用户信息
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 根据用户查询请求参数构建QueryWrapper对象
     * QueryWrapper是MyBatis-Plus中用于构建查询条件的工具类
     *
     * @param userQueryRequest 用户查询请求参数对象，包含查询条件
     * @return 返回一个包含查询条件的QueryWrapper对象，可用于数据库查询操作
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
