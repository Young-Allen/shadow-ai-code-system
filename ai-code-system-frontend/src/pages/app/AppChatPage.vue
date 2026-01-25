<template>
  <div class="app-chat-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <a-select
          v-model:value="appName"
          :options="[]"
          style="width: 200px"
          :loading="appLoading"
          disabled
        >
          <template #suffixIcon>
            <span>{{ appName || '加载中...' }}</span>
          </template>
        </a-select>
        <a-input
          v-model:value="appName"
          placeholder="应用名称"
          style="width: 200px; margin-left: 12px"
          :disabled="appLoading"
        />
      </div>
      <div class="top-bar-right">
        <a-button type="primary" :loading="deploying" @click="handleDeploy">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署
        </a-button>
      </div>
    </div>

    <!-- 核心内容区域 -->
    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-panel">
        <div class="chat-header">
          <h3>生成个人博客</h3>
        </div>

        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainerRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message-item', message.type === 'user' ? 'message-user' : 'message-ai']"
          >
            <div class="message-avatar">
              <img
                v-if="message.type === 'user'"
                :src="userAvatar"
                alt="用户"
                class="avatar-img"
                @error="handleAvatarError"
              />
              <RobotOutlined v-else class="bot-icon" />
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div v-if="message.type === 'ai' && message.version" class="message-meta">
                <span>v{{ message.version }} 已保存 {{ formatTime(message.timestamp) }}</span>
                <a-button type="link" size="small" @click="handleAiReply">AI 回复</a-button>
              </div>
            </div>
          </div>

          <!-- 优化按钮 -->
          <div v-if="showOptimizeBtn" class="optimize-section">
            <a-button type="default" @click="handleOptimize">优化</a-button>
          </div>

          <!-- 加载中提示 -->
          <div v-if="streaming" class="streaming-indicator">
            <a-spin size="small" />
            <span style="margin-left: 8px">AI 正在生成中...</span>
          </div>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-section">
          <a-textarea
            v-model:value="userMessage"
            :placeholder="inputPlaceholder"
            :rows="4"
            class="message-input"
            :disabled="streaming || appLoading"
            @pressEnter="handleSendMessage"
          />
          <div class="input-footer">
            <div class="input-actions">
              <a-button type="text" class="action-btn" :disabled="streaming || appLoading">
                <template #icon>
                  <PaperClipOutlined />
                </template>
                上传
              </a-button>
              <a-button type="text" class="action-btn" :disabled="streaming || appLoading">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button type="text" class="action-btn" :disabled="streaming || appLoading">
                <template #icon>
                  <ThunderboltOutlined />
                </template>
                优化
              </a-button>
            </div>
            <a-button
              type="primary"
              shape="circle"
              :loading="streaming"
              :disabled="!userMessage.trim() || appLoading"
              @click="handleSendMessage"
              class="submit-btn"
            >
              <template #icon>
                <ArrowUpOutlined />
              </template>
            </a-button>
          </div>
        </div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-panel">
        <div class="preview-header">
          <h3>生成后的网页展示</h3>
        </div>
        <div class="preview-content" v-if="previewUrl">
          <iframe :src="previewUrl" frameborder="0" class="preview-iframe"></iframe>
        </div>
        <div v-else class="preview-placeholder">
          <a-empty description="网站生成完成后将在此展示" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  PaperClipOutlined,
  ThunderboltOutlined,
  ArrowUpOutlined,
  EditOutlined,
  CloudUploadOutlined,
  RobotOutlined,
} from '@ant-design/icons-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import hamburgerImg from '@/assets/hamburger.png'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>('')
const appName = ref('')
const appLoading = ref(false)
const appInfo = ref<API.AppVO | null>(null)

// 消息列表
interface Message {
  type: 'user' | 'ai'
  content: string
  timestamp?: number
  version?: number
}

const messages = ref<Message[]>([])
const streaming = ref(false)
const currentAiMessage = ref('')
const showOptimizeBtn = ref(false)
const messagesContainerRef = ref<HTMLElement>()

// 用户输入
const userMessage = ref('')
const inputPlaceholder =
  '描述越详细,页面越具体,可以一步一步完善生成效果'

// 预览
const previewUrl = ref('')
const codeGenType = ref('')

// 部署
const deploying = ref(false)

// 用户头像
const userAvatar = computed(() => {
  return loginUserStore.loginUser.userAvatar || hamburgerImg
})

/**
 * 初始化应用信息
 */
const initApp = async () => {
  const id = String(route.params.id || '')
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    appLoading.value = true
    const res = await getAppVoById({ id: id as any })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      appName.value = res.data.data.appName || '未命名应用'
      codeGenType.value = res.data.data.codeGenType || ''

      // 如果有初始提示词，自动发送
      if (res.data.data.initPrompt) {
        // 添加用户消息
        messages.value.push({
          type: 'user',
          content: res.data.data.initPrompt,
          timestamp: Date.now(),
        })

        // 自动发送给AI
        await sendMessageToAI(res.data.data.initPrompt)
      }
    } else {
      message.error('获取应用信息失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取应用信息失败:', error)
    message.error('获取应用信息失败')
  } finally {
    appLoading.value = false
  }
}

/**
 * 发送消息给AI（SSE流式）- 使用fetch确保携带cookie
 */
const sendMessageToAI = async (messageText: string) => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  try {
    streaming.value = true
    currentAiMessage.value = ''
    showOptimizeBtn.value = false

    // 添加AI消息占位
    const aiMessageIndex = messages.value.length
    messages.value.push({
      type: 'ai',
      content: '',
      timestamp: Date.now(),
    })

    // 构建SSE请求URL
    const baseURL = 'http://localhost:8123/api'
    const url = `${baseURL}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(messageText)}`

    // 使用fetch处理SSE，确保携带cookie（withCredentials）
    // EventSource在某些情况下可能无法正确携带cookie，使用fetch更可靠
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include', // 确保携带cookie，这对Session认证至关重要
      headers: {
        'Accept': 'text/event-stream',
      },
    })

      if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法读取响应流')
    }

    let version = 1
    let currentEvent = 'message' // 当前事件类型
    let isDone = false
    let buffer = ''

    // 处理SSE流
    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          // 流结束
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留最后一个不完整的行

        for (const line of lines) {
          if (line.startsWith('event:')) {
            // 事件类型
            currentEvent = line.substring(6).trim()
          } else if (line.startsWith('data:')) {
            // 数据
            const data = line.substring(5).trim()
            
            if (currentEvent === 'done') {
              // 收到完成事件
              isDone = true
              await reader.cancel()
              await handleStreamComplete()
              return
            }

            if (data && data !== '[DONE]') {
              try {
                // 尝试解析JSON（后端返回格式：{"d": "chunk内容"}）
                const parsed = JSON.parse(data)
                if (parsed.d) {
                  // 后端返回的格式是 {"d": "chunk内容"}
                  currentAiMessage.value += parsed.d
                } else if (parsed.content) {
                  // 兼容其他格式
                  currentAiMessage.value += parsed.content
                } else if (typeof parsed === 'string') {
                  currentAiMessage.value += parsed
                }
              } catch {
                // 如果不是JSON，直接作为文本内容
                currentAiMessage.value += data
              }

              // 更新消息内容
              messages.value[aiMessageIndex].content = currentAiMessage.value
              messages.value[aiMessageIndex].version = version

              // 滚动到底部
              nextTick(() => {
                scrollToBottom()
              })
            } else if (data === '[DONE]') {
              // 收到完成信号
              isDone = true
              await reader.cancel()
              await handleStreamComplete()
              return
            }
          } else if (line === '') {
            // 空行，重置事件类型
            currentEvent = 'message'
          }
        }
      }

      // 流正常结束
      if (!isDone) {
        if (currentAiMessage.value) {
          // 有内容，正常结束
          await handleStreamComplete()
        } else {
          // 没有内容，可能是错误
          message.error('发送消息失败：连接已关闭')
          streaming.value = false
          if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'ai') {
            messages.value.pop()
          }
          throw new Error('SSE连接关闭且无内容')
        }
      }
    } catch (error) {
      console.error('处理SSE流失败:', error)
      streaming.value = false
      message.error('发送消息失败')
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'ai') {
        messages.value.pop()
      }
      throw error
    }
  } catch (error) {
    console.error('创建SSE连接失败:', error)
    message.error('发送消息失败')
    streaming.value = false
    // 移除失败的AI消息
    if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'ai') {
      messages.value.pop()
    }
    throw error
  }
}

/**
 * 处理流式响应完成
 */
const handleStreamComplete = async () => {
  streaming.value = false
  showOptimizeBtn.value = true

  // 更新应用信息以获取最新的codeGenType
  await refreshAppInfo()

  // 如果生成了代码，显示预览
  if (codeGenType.value && appId.value) {
    // 等待一小段时间确保文件已生成
    setTimeout(() => {
      updatePreview()
    }, 1000)
  }
}

/**
 * 刷新应用信息（获取最新的codeGenType）
 */
const refreshAppInfo = async () => {
  if (!appId.value) return

  try {
    const res = await getAppVoById({ id: appId.value as any })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      codeGenType.value = res.data.data.codeGenType || ''
    }
  } catch (error) {
    console.error('刷新应用信息失败:', error)
  }
}

/**
 * 更新预览
 */
const updatePreview = () => {
  if (codeGenType.value && appId.value) {
    // 构建预览URL: http://localhost:8123/api/static/{codeGenType}_{appId}/
    previewUrl.value = `http://localhost:8123/api/static/${codeGenType.value}_${appId.value}/`
  }
}

/**
 * 处理发送消息
 */
const handleSendMessage = async () => {
  const messageText = userMessage.value.trim()
  if (!messageText) {
    message.warning('请输入消息')
    return
  }

  if (streaming.value) {
    message.warning('AI正在生成中，请稍候...')
    return
  }

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: messageText,
    timestamp: Date.now(),
  })

  // 清空输入框
  userMessage.value = ''

  // 发送给AI
  await sendMessageToAI(messageText)
}

/**
 * 处理优化
 */
const handleOptimize = async () => {
  if (streaming.value) {
    message.warning('AI正在生成中，请稍候...')
    return
  }

  const optimizeMessage = '优化当前应用，提升用户体验和视觉效果'
  userMessage.value = optimizeMessage
  await handleSendMessage()
}

/**
 * 处理AI回复
 */
const handleAiReply = () => {
  // 可以添加重新生成或其他功能
  message.info('AI回复功能')
}

/**
 * 处理部署
 */
const handleDeploy = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  try {
    deploying.value = true
    const res = await deployApp({ appId: appId.value as any })
    if (res.data.code === 0 && res.data.data) {
      const deployUrl = res.data.data
      message.success('部署成功！')
      // 可以打开新窗口或复制链接
      if (deployUrl) {
        Modal.info({
          title: '部署成功',
          content: `部署地址：${deployUrl}`,
          okText: '复制链接',
          onOk: () => {
            navigator.clipboard.writeText(deployUrl)
            message.success('链接已复制到剪贴板')
          },
        })
      }
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败:', error)
    message.error('部署失败')
  } finally {
    deploying.value = false
  }
}

/**
 * 格式化消息内容（支持代码高亮等）
 */
const formatMessage = (content: string) => {
  if (!content) return ''
  // 简单的格式化，可以后续增强
  return content
    .replace(/\n/g, '<br>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}

/**
 * 格式化时间
 */
const formatTime = (timestamp?: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)
  const weeks = Math.floor(days / 7)

  if (weeks > 0) {
    return `${weeks}周前`
  } else if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else {
    return '刚刚'
  }
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
  }
}

/**
 * 处理头像加载错误
 */
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = hamburgerImg
}

// 组件挂载时初始化
onMounted(() => {
  initApp()
})
</script>

<style scoped>
.app-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  background: #f5f5f5;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.top-bar-left {
  display: flex;
  align-items: center;
}

.top-bar-right {
  display: flex;
  align-items: center;
}

.content-area {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-panel {
  width: 50%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e8e8e8;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-user .message-content {
  background: #1890ff;
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}

.message-ai .message-content {
  background: #f5f5f5;
  color: #333;
  border-radius: 12px 12px 12px 4px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.bot-icon {
  width: 32px;
  height: 32px;
  font-size: 24px;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
}

.message-text {
  word-wrap: break-word;
  line-height: 1.6;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.optimize-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  padding: 12px;
  color: #999;
  font-size: 14px;
}

.input-section {
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.message-input {
  font-size: 14px;
}

.message-input:focus {
  box-shadow: none;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  color: #666;
  padding: 0;
}

.submit-btn {
  width: 36px;
  height: 36px;
}

.preview-panel {
  width: 50%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.preview-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-area {
    flex-direction: column;
  }

  .chat-panel,
  .preview-panel {
    width: 100%;
    height: 50%;
  }

  .chat-panel {
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }
}
</style>
