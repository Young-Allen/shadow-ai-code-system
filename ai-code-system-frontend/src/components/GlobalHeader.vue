<template>
  <a-layout-header class="global-header">
    <div class="header-left">
      <img v-if="logoPath" :src="logoPath" alt="Logo" class="logo" />
      <h1 class="site-title">{{ siteTitle }}</h1>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="horizontal"
      :items="menuItemsConfig"
      class="header-menu"
      @select="handleMenuSelect"
    />
    <div class="header-right">
      <a-button type="primary" @click="handleLogin">登录</a-button>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

interface MenuItem {
  key: string
  label: string
  path?: string
}

interface Props {
  logoPath?: string
  siteTitle?: string
  menuItems?: MenuItem[]
}

const props = withDefaults(defineProps<Props>(), {
  logoPath: '/logo.png',
  siteTitle: 'ShadowW AI',
  menuItems: () => [
    { key: 'home', label: '首页', path: '/' },
    { key: 'about', label: '关于', path: '/about' },
  ],
})

const route = useRoute()
const router = useRouter()

const selectedKeys = ref<string[]>([])

// 将 menuItems 转换为 Ant Design Vue Menu 需要的格式
const menuItemsConfig = computed<MenuProps['items']>(() => {
  return props.menuItems.map((item) => ({
    key: item.key,
    label: item.label,
  }))
})

// 根据当前路由设置选中的菜单项
watch(
  () => route.path,
  (path) => {
    const matchedItem = props.menuItems.find((item) => item.path === path)
    if (matchedItem) {
      selectedKeys.value = [matchedItem.key]
    }
  },
  { immediate: true }
)

const handleMenuSelect = ({ key }: { key: string }) => {
  const menuItem = props.menuItems.find((item) => item.key === key)
  if (menuItem?.path) {
    router.push(menuItem.path)
  }
}

const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('登录')
}
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;

  .header-left {
    display: flex;
    align-items: center;
    margin-right: 48px;
    min-width: 200px;
  }

  .header-left .logo {
    width: 40px;
    height: 40px;
    margin-right: 12px;
    object-fit: contain;
  }

  .header-left .site-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1890ff;
    white-space: nowrap;
  }

  .header-menu {
    flex: 1;
    border-bottom: none;
    line-height: 64px;
    min-width: 0;
  }

  .header-right {
    display: flex;
    align-items: center;
    margin-left: 24px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
  }

  .global-header .header-left {
    margin-right: 16px;
    min-width: auto;
  }

  .global-header .header-left .site-title {
    font-size: 16px;
  }

  .global-header .header-left .logo {
    width: 32px;
    height: 32px;
    margin-right: 8px;
  }

  .global-header .header-menu {
    flex: 1;
    min-width: 0;
  }

  .global-header .header-right {
    margin-left: 12px;
  }
}
</style>