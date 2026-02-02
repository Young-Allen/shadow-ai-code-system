<template>
  <div class="app-card" @click="handleCardClick">
    <div class="app-cover">
      <a-image
        v-if="app.cover"
        :src="app.cover"
        :alt="app.appName"
        :preview="false"
        class="app-cover-image"
        @error="handleImageError"
      />
      <a-image
        v-else
        :src="defaultCover"
        :alt="app.appName"
        :preview="false"
        class="app-cover-image"
      />
    </div>
    <div class="app-info">
      <img
        v-if="app.user"
        :src="app.user.userAvatar || defaultAvatar"
        :alt="app.user.userName"
        class="app-info-avatar"
        @error="handleAvatarError"
      />
      <img
        v-else-if="showUserAvatar"
        :src="userAvatar || defaultAvatar"
        :alt="userName"
        class="app-info-avatar"
        @error="handleAvatarError"
      />
      <img
        v-else
        :src="defaultAvatar"
        alt="匿名"
        class="app-info-avatar"
        @error="handleAvatarError"
      />
      <div class="app-info-right">
        <h3 class="app-name">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author-name">{{ app.user?.userName || userName || '匿名' }}</p>
      </div>
    </div>
    <div class="app-card-actions" @click.stop>
      <a-button size="large" type="primary" @click.stop="handleViewChat">查看对话</a-button>
      <a-button v-if="app.deployKey" size="large" @click.stop="handleViewDeploy">
        查看作品
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { handleAvatarError, handleImageError, DEFAULT_AVATAR, DEFAULT_COVER } from '@/utils/image'

interface Props {
  app: API.AppVO
  userAvatar?: string
  userName?: string
  showUserAvatar?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showUserAvatar: false,
})

const emit = defineEmits<{
  click: [app: API.AppVO]
  viewChat: [app: API.AppVO]
  viewDeploy: [app: API.AppVO]
}>()

const router = useRouter()
const defaultAvatar = DEFAULT_AVATAR
const defaultCover = DEFAULT_COVER

const handleCardClick = () => {
  emit('click', props.app)
  handleViewChat()
}

const handleViewChat = () => {
  if (!props.app.id) return
  emit('viewChat', props.app)
  router.push({ path: `/app/chat/${String(props.app.id)}`, query: { view: '1' } })
}

const handleViewDeploy = () => {
  if (!props.app.deployKey) return
  const deployKey = String(props.app.deployKey || '').replace(/^\//, '')
  const deployDomain = import.meta.env.VITE_APP_DEPLOY_DOMAIN || 'http://localhost'
  // 如果是vue项目，需要在URL后面添加 /dist 后缀
  const distSuffix = props.app.codeGenType === 'vue_project' ? '/dist' : ''
  const deployUrl = `${deployDomain}/${deployKey}${distSuffix}`
  window.open(deployUrl, '_blank')
  emit('viewDeploy', props.app)
}
</script>

<style scoped>
.app-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.app-card-actions {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
  z-index: 2;
}

.app-card:hover .app-card-actions {
  opacity: 1;
  pointer-events: auto;
}

.app-cover {
  width: 100%;
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.app-cover-image {
  width: 100%;
  height: 100%;
}

.app-cover-image :deep(.ant-image-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-cover-image :deep(.ant-image) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-info {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-info-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-author-name {
  font-size: 12px;
  color: #666;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .app-cover {
    height: 120px;
  }
}
</style>
