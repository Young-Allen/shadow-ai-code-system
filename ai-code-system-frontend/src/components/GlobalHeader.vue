<template>
  <a-layout-header class="global-header">
    <div class="header-left" @click="handleGoHome">
      <img v-if="logoPath" src="@/assets/home.png" alt="Logo" class="logo" />
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
      <!-- 已登录：显示用户信息（带下拉菜单） -->
      <a-dropdown v-if="isLoggedIn" :trigger="['hover']" placement="bottomRight">
        <div class="user-info">
          <img
            :src="userAvatar"
            alt="用户头像"
            class="user-avatar"
            @error="handleAvatarError"
          />
          <span class="user-name">{{ loginUserStore.loginUser.userName }}</span>
        </div>
        <template #overlay>
          <a-menu @click="handleMenuClick">
            <a-menu-item key="profile">
              <template #icon>
                <UserOutlined />
              </template>
              <span>个人中心</span>
            </a-menu-item>
            <a-menu-item key="logout">
              <template #icon>
                <LogoutOutlined />
              </template>
              <span>退出登录</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
      <!-- 未登录：显示登录和注册按钮 -->
      <div v-else class="auth-buttons">
        <a-button type="primary" @click="handleLogin">登录</a-button>
        <a-button type="default" @click="handleRegister">注册</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import {
  LogoutOutlined,
  HomeOutlined,
  TeamOutlined,
  UserOutlined,
  AppstoreOutlined,
  MessageOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import { message } from 'ant-design-vue'
import hamburgerImg from '@/assets/hamburger.png'

const loginUserStore = useLoginUserStore()

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
  siteTitle: 'ShadowW ZeroCode',
  menuItems: () => [
    { key: 'home', label: '首页', path: '/' },
    { key: 'userManage', label: '用户管理', path: '/admin/userManage' },
  ],
})

const route = useRoute()
const router = useRouter()

const selectedKeys = ref<string[]>([])

// 图标映射
const iconMap: Record<string, any> = {
  home: HomeOutlined,
  userManage: TeamOutlined,
  appManage: AppstoreOutlined,
  chatHistoryManage: MessageOutlined,
}

// 将 menuItems 转换为 Ant Design Vue Menu 需要的格式
const menuItemsConfig = computed<MenuProps['items']>(() => {
  return props.menuItems.map((item) => {
    const IconComponent = iconMap[item.key]
    return {
      key: item.key,
      label: item.label,
      icon: IconComponent ? h(IconComponent) : undefined,
    }
  })
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

// 判断用户是否已登录
const isLoggedIn = computed(() => {
  const user = loginUserStore.loginUser
  return user && user.userName !== '未登录' && user.id
})

// 获取用户头像，如果为 null 则使用默认图片
const userAvatar = computed(() => {
  const avatar = loginUserStore.loginUser.userAvatar
  return avatar || hamburgerImg
})

// 处理头像加载错误
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = hamburgerImg
}

// 处理登录按钮点击
const handleLogin = () => {
  router.push('/user/login')
}

// 处理注册按钮点击
const handleRegister = () => {
  router.push('/user/register')
}

// 处理点击logo/标题跳转首页
const handleGoHome = () => {
  router.push('/')
}

// 处理下拉菜单点击
const handleMenuClick = async ({ key }: { key: string }) => {
  if (key === 'profile') {
    router.push('/user/edit')
  } else if (key === 'logout') {
    await handleLogout()
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    const res = await userLogout()
    if (res.data.code === 0) {
      // 清除登录状态
      loginUserStore.resetLoginUser()
      message.success('退出登录成功')
      // 跳转到首页
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('退出登录失败，' + res.data.message)
    }
  } catch (error) {
    console.error('退出登录失败:', error)
    message.error('退出登录失败')
    // 即使 API 调用失败，也清除本地状态
    loginUserStore.resetLoginUser()
    router.push({
      path: '/',
      replace: true,
    })
  }
}

// 组件挂载时获取登录用户信息
onMounted(() => {
  loginUserStore.fetchLoginUser()
})
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--global-header-height, 64px);
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    margin-right: 48px;
    min-width: 200px;
    cursor: pointer;
    transition: opacity 0.3s;
  }

  .header-left:hover {
    opacity: 0.8;
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
    line-height: var(--global-header-height, 64px);
    min-width: 0;
  }

  .header-right {
    display: flex;
    align-items: center;
    margin-left: 24px;
  }

  .auth-buttons {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .user-name {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.85);
    white-space: nowrap;
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

  .global-header .auth-buttons {
    gap: 8px;
  }

  .global-header .auth-buttons .ant-btn {
    font-size: 12px;
    padding: 4px 12px;
    height: 28px;
  }

  .global-header .user-info {
    gap: 6px;
  }

  .global-header .user-avatar {
    width: 28px;
    height: 28px;
  }

  .global-header .user-name {
    font-size: 12px;
  }
}
</style>
