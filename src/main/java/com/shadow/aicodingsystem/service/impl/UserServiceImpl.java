package com.shadow.aicodingsystem.genresult.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shadow.aicodingsystem.genresult.entity.User;
import com.shadow.aicodingsystem.genresult.mapper.UserMapper;
import com.shadow.aicodingsystem.genresult.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author shadow
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
