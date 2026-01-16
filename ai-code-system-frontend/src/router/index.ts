import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../pages/HomePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomeView,
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN, // 主页公开
      },
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('../pages/user/UserLoginPage.vue'),
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
        hideInMenu: true,
      },
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('../pages/user/UserRegisterPage.vue'),
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
        hideInMenu: true,
      },
    },
    {
      path: '/user/edit',
      name: '个人中心',
      component: () => import('../pages/user/UserEditPage.vue'),
      meta: {
        access: ACCESS_ENUM.USER, // 需要登录
      },
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('../pages/admin/UserManagerPage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN, // 管理员
      },
    },
    {
      path: '/noAuth',
      name: '无权限',
      component: () => import('../pages/NoAuthPage.vue'),
      meta: {
        access: ACCESS_ENUM.NOT_LOGIN,
        hideInMenu: true,
      },
    },
  ],
})

export default router
