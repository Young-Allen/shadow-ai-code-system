<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" />
    <a-layout-content class="layout-content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import router from '@/router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { useLoginUserStore } from '@/stores/loginUser'
import checkAccess from '@/access/checkAccess'
import ACCESS_ENUM from '@/access/accessEnum'

interface MenuItem {
  key: string
  label: string
  path: string
}

// 你原来的菜单定义保留（也可以未来改成从 routes 自动生成）
const allMenuItems: MenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'userManage', label: '用户管理', path: '/admin/userManage' },
  { key: 'appManage', label: '应用管理', path: '/admin/appManage' },
]

const loginUserStore = useLoginUserStore()

const menuItems = computed(() => {
  return allMenuItems.filter((menu) => {
    const routeRecord = router.getRoutes().find((r) => r.path === menu.path)
    if (!routeRecord) return true

    // hideInMenu 强制隐藏
    if ((routeRecord.meta as any)?.hideInMenu) return false

    const needAccess = ((routeRecord.meta as any)?.access as string) ?? ACCESS_ENUM.NOT_LOGIN
    return checkAccess(loginUserStore.loginUser, needAccess as any)
  })
})
</script>

<style scoped>
.basic-layout {
  --global-header-height: 64px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  .layout-content {
    flex: 1;
    padding: 24px;
    padding-top: calc(24px + var(--global-header-height));
    background: #fff;
    min-height: 0;
  }
}

@media (max-width: 768px) {
  .basic-layout .layout-content {
    padding: 16px;
    padding-top: calc(16px + var(--global-header-height));
  }
}
</style>
