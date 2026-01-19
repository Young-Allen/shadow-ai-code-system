package com.shadow.aicodingsystem.genresult.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.shadow.aicodingsystem.genresult.entity.App;
import com.shadow.aicodingsystem.genresult.mapper.AppMapper;
import com.shadow.aicodingsystem.genresult.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author shadow
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
