package com.shadow.aicodingsystem.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.shadow.aicodingsystem.model.dto.app.AppQueryRequest;
import com.shadow.aicodingsystem.model.entity.App;
import com.shadow.aicodingsystem.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author shadow
 */
public interface AppService extends IService<App> {

    /**
     * 根据App实体对象获取AppVO视图对象
     *
     * @param app 应用实体对象
     * @return AppVO 视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 根据应用列表获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据应用查询请求参数构建QueryWrapper对象
     *
     * @param appQueryRequest 应用查询请求参数对象
     * @return QueryWrapper 查询条件对象
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
