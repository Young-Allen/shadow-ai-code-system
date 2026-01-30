<template>
  <div class="home-page">
    <!-- 网站标题 -->
    <div class="page-header">
      <div class="title-section">
        <h1 class="main-title">
          <span>一句话</span>
          <img src="@/assets/home.png" alt="Cat" class="cat-logo" />
          <span>呈所想</span>
        </h1>
      </div>
      <p class="subtitle">一句话轻松创建网站应用</p>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="input-section">
      <a-textarea
        v-model:value="userPrompt"
        :placeholder="inputPlaceholder"
        :rows="4"
        class="prompt-input"
        :disabled="creating"
        @keydown.enter.exact="handleEnterKey"
      />
      <div class="input-footer">
        <div class="input-actions">
          <a-button type="text" class="action-btn" :disabled="creating">
            <template #icon>
              <PaperClipOutlined />
            </template>
            上传
          </a-button>
          <a-button type="text" class="action-btn" :disabled="creating">
            <template #icon>
              <ThunderboltOutlined />
            </template>
            优化
          </a-button>
        </div>
        <a-button
          type="primary"
          shape="circle"
          :loading="creating"
          :disabled="!userPrompt.trim()"
          @click="handleCreateApp"
          class="submit-btn"
        >
          <template #icon>
            <ArrowUpOutlined />
          </template>
        </a-button>
      </div>
    </div>

    <!-- 快捷提示按钮 -->
    <div class="quick-prompts">
      <a-button
        v-for="prompt in quickPrompts"
        :key="prompt"
        class="prompt-btn"
        @click="userPrompt = prompt"
      >
        {{ prompt }}
      </a-button>
    </div>

    <!-- 我的作品 -->
    <div class="section" v-if="isLoggedIn">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
        <a-input-search
          v-model:value="myAppsSearchName"
          placeholder="搜索应用名称"
          style="width: 300px"
          allow-clear
          @search="handleMyAppsSearch"
          @clear="handleMyAppsSearch"
        />
      </div>
      <a-spin :spinning="myAppsLoading">
        <div class="app-grid" v-if="myAppsList.length > 0">
          <AppCard
            v-for="app in myAppsList"
            :key="app.id"
            :app="app"
            :user-avatar="loginUserStore.loginUser.userAvatar"
            :user-name="loginUserStore.loginUser.userName"
            :show-user-avatar="true"
            @click="handleAppClick"
            @view-chat="handleViewChat"
            @view-deploy="handleViewDeploy"
          />
        </div>
        <a-empty v-else description="暂无应用" />
      </a-spin>
      <div class="pagination-wrapper" v-if="myAppsTotal > 0">
        <a-pagination
          v-model:current="myAppsPage"
          v-model:page-size="myAppsPageSize"
          :total="myAppsTotal"
          :page-size-options="['12', '20']"
          show-size-changer
          @change="fetchMyApps"
          @showSizeChange="handleMyAppsPageSizeChange"
        />
      </div>
    </div>

    <!-- 精选案例 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">精选案例</h2>
        <a-input-search
          v-model:value="featuredAppsSearchName"
          placeholder="搜索应用名称"
          style="width: 300px"
          allow-clear
          @search="handleFeaturedAppsSearch"
          @clear="handleFeaturedAppsSearch"
        />
      </div>
      <a-spin :spinning="featuredAppsLoading">
        <div class="app-grid" v-if="featuredAppsList.length > 0">
          <AppCard
            v-for="app in featuredAppsList"
            :key="app.id"
            :app="app"
            @click="handleAppClick"
            @view-chat="handleViewChat"
            @view-deploy="handleViewDeploy"
          />
        </div>
        <a-empty v-else description="暂无精选应用" />
      </a-spin>
      <div class="pagination-wrapper" v-if="featuredAppsTotal > 0">
        <a-pagination
          v-model:current="featuredAppsPage"
          v-model:page-size="featuredAppsPageSize"
          :total="featuredAppsTotal"
          :page-size-options="['12', '20']"
          show-size-changer
          @change="fetchFeaturedApps"
          @showSizeChange="handleFeaturedAppsPageSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PaperClipOutlined,
  ThunderboltOutlined,
  ArrowUpOutlined,
} from '@ant-design/icons-vue'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)
const inputPlaceholder = '使用 NoCode 创建一个高效的小工具,帮我计算……'

// 快捷提示
const quickPrompts = [
  '波普风电商页面',
  '企业网站',
  '电商运营后台',
  '暗黑话题社区',
]

// 我的应用
const myAppsList = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsPage = ref(1)
const myAppsPageSize = ref(12)
const myAppsTotal = ref(0)
const myAppsSearchName = ref('')

// 限制每页最多20个
const MAX_PAGE_SIZE = 20

// 精选应用
const featuredAppsList = ref<API.AppVO[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsPage = ref(1)
const featuredAppsPageSize = ref(12)
const featuredAppsTotal = ref(0)
const featuredAppsSearchName = ref('')

// 是否已登录
const isLoggedIn = computed(() => {
  const user = loginUserStore.loginUser
  return user && user.userName !== '未登录' && user.id
})

/**
 * 处理回车键（Ctrl+Enter或Shift+Enter换行，Enter提交）
 */
const handleEnterKey = (e: KeyboardEvent) => {
  if (e.ctrlKey || e.shiftKey) {
    // Ctrl+Enter 或 Shift+Enter 允许换行
    return
  }
  // Enter 提交
  e.preventDefault()
  handleCreateApp()
}

/**
 * 创建应用
 */
const handleCreateApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!isLoggedIn.value) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  if (creating.value) {
    return // 防止重复提交
  }

  try {
    creating.value = true
    const res = await addApp({ initPrompt: userPrompt.value.trim() })
    if (res.data.code === 0 && res.data.data) {
      const appId = String(res.data.data)
      message.success('应用创建成功')
      // 跳转到对话页面
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建应用失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    message.error('创建应用失败')
  } finally {
    creating.value = false
  }
}

/**
 * 获取我的应用列表
 */
const fetchMyApps = async () => {
  if (!isLoggedIn.value) return

  try {
    myAppsLoading.value = true
    const queryParams: API.AppQueryRequest = {
      pageNum: myAppsPage.value,
      pageSize: Math.min(myAppsPageSize.value, MAX_PAGE_SIZE),
    }
    // 添加搜索条件
    if (myAppsSearchName.value.trim()) {
      queryParams.appName = myAppsSearchName.value.trim()
    }
    const res = await listMyAppVoByPage(queryParams)
    if (res.data.code === 0 && res.data.data) {
      myAppsList.value = res.data.data.records || []
      myAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error('获取我的应用失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取我的应用失败:', error)
    message.error('获取我的应用失败')
  } finally {
    myAppsLoading.value = false
  }
}

/**
 * 获取精选应用列表
 */
const fetchFeaturedApps = async () => {
  try {
    featuredAppsLoading.value = true
    const queryParams: API.AppQueryRequest = {
      pageNum: featuredAppsPage.value,
      pageSize: Math.min(featuredAppsPageSize.value, MAX_PAGE_SIZE),
    }
    // 添加搜索条件
    if (featuredAppsSearchName.value.trim()) {
      queryParams.appName = featuredAppsSearchName.value.trim()
    }
    const res = await listFeaturedAppVoByPage(queryParams)
    if (res.data.code === 0 && res.data.data) {
      featuredAppsList.value = res.data.data.records || []
      featuredAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error('获取精选应用失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取精选应用失败:', error)
    message.error('获取精选应用失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

/**
 * 点击应用卡片
 */
const handleAppClick = (app: API.AppVO) => {
  handleViewChat(app)
}

const handleViewChat = (app: API.AppVO) => {
  if (!app.id) return
  router.push({ path: `/app/chat/${String(app.id)}`, query: { view: '1' } })
}

const handleViewDeploy = (app: API.AppVO) => {
  // 这个功能已经在 AppCard 组件中实现了
}

/**
 * 处理我的应用搜索
 */
const handleMyAppsSearch = () => {
  myAppsPage.value = 1
  fetchMyApps()
}

/**
 * 处理精选应用搜索
 */
const handleFeaturedAppsSearch = () => {
  featuredAppsPage.value = 1
  fetchFeaturedApps()
}

/**
 * 处理我的应用分页大小变化
 */
const handleMyAppsPageSizeChange = (current: number, size: number) => {
  myAppsPage.value = 1
  myAppsPageSize.value = Math.min(size, MAX_PAGE_SIZE) // 限制最大为20
  fetchMyApps()
}

/**
 * 处理精选应用分页大小变化
 */
const handleFeaturedAppsPageSizeChange = (current: number, size: number) => {
  featuredAppsPage.value = 1
  featuredAppsPageSize.value = Math.min(size, MAX_PAGE_SIZE) // 限制最大为20
  fetchFeaturedApps()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchFeaturedApps()
  if (isLoggedIn.value) {
    fetchMyApps()
  }
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  min-height: calc(100vh - 64px);
  position: relative;
  z-index: 0; /* 可选：明确层级 */
}

/* 确保背景渐变覆盖整个视口 */
.home-page::before {
  content: '';
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, #f0f2f5 0%, #d5e8f1 50%, #94d4f7 100%);
  z-index: 0;
  pointer-events: none;
}

.home-page > * {
  position: relative;
  z-index: 1;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.main-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1a1a1a;
}

.cat-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.subtitle {
  font-size: 18px;
  color: #666;
  margin: 0;
}

.input-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.prompt-input {
  font-size: 16px;
  border: none;
  resize: none;
}

.prompt-input:focus {
  box-shadow: none;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.input-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  color: #666;
  padding: 0;
}

.submit-btn {
  width: 40px;
  height: 40px;
}

.quick-prompts {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 48px;
}

.prompt-btn {
  border-radius: 20px;
  padding: 8px 20px;
  height: auto;
}

.section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}


.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .home-page {
    padding: 24px 16px;
  }

  .main-title {
    font-size: 32px;
  }

  .cat-logo {
    width: 36px;
    height: 36px;
  }

  .subtitle {
    font-size: 16px;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .section-header .ant-input-search {
    width: 100% !important;
  }
}
</style>

<style>
/* 全局样式：覆盖 BasicLayout 的白色背景，仅对首页生效 */
body .layout-content {
  background: transparent !important;
}
</style>
