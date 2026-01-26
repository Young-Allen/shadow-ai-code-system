<template>
  <div class="app-chat-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <a-button type="text" class="back-btn" @click="handleGoHome">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <span class="app-title" :title="appName">{{ appName || '未命名应用' }}</span>
      </div>
      <div class="top-bar-right">
        <a-button v-if="canManage" @click="openDetailModal">
          <template #icon>
            <InfoCircleOutlined /> 
          </template>
          应用详情
        </a-button>
        <a-button v-if="canManage" type="primary" :loading="deploying" @click="handleDeploy">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          应用部署
        </a-button>
      </div>
    </div>

    <!-- 核心内容区域 -->
    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-panel">
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
              <div
                v-if="message.type === 'ai'"
                class="message-text markdown-body"
                v-html="renderMarkdown(message.content)"
              ></div>
              <div v-else class="message-text message-text-plain">{{ message.content }}</div>
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
          <a-tooltip :title="inputTooltip">
            <div class="message-input-wrapper">
              <a-textarea
                v-model:value="userMessage"
                :placeholder="inputPlaceholder"
                :rows="4"
                class="message-input"
                :disabled="inputDisabled"
                @pressEnter="handleSendMessage"
              />
            </div>
          </a-tooltip>
          <div class="input-footer">
            <div class="input-actions">
              <a-button type="text" class="action-btn" :disabled="inputDisabled">
                <template #icon>
                  <PaperClipOutlined />
                </template>
                上传
              </a-button>
              <a-button type="text" class="action-btn" :disabled="inputDisabled">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-button type="text" class="action-btn" :disabled="inputDisabled">
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
              :disabled="inputDisabled || !userMessage.trim()"
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
        <div class="preview-content" v-if="previewUrl">
          <iframe :key="previewUrl" :src="previewUrl" frameborder="0" class="preview-iframe"></iframe>
        </div>
        <div v-else class="preview-placeholder">
          <a-empty description="网站生成完成后将在此展示" />
        </div>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <a-modal
      v-model:open="detailModalOpen"
      title="应用详情"
      :footer="null"
      :maskClosable="true"
      destroyOnClose
    >
      <div class="app-detail">
        <div class="app-detail-user">
          <img
            :src="appInfo?.user?.userAvatar || hamburgerImg"
            alt="创建者头像"
            class="app-detail-avatar"
            @error="handleAvatarError"
          />
          <div class="app-detail-user-meta">
            <div class="app-detail-user-name">{{ appInfo?.user?.userName || '匿名' }}</div>
            <div class="app-detail-user-sub">创建者</div>
          </div>
        </div>

        <div class="app-detail-row">
          <span class="app-detail-label">应用名称：</span>
          <span class="app-detail-value">{{ appInfo?.appName || '未命名应用' }}</span>
        </div>
        <div class="app-detail-row">
          <span class="app-detail-label">创建时间：</span>
          <span class="app-detail-value">{{ formatDate(appInfo?.createTime) }}</span>
        </div>

        <div class="app-detail-actions">
          <a-button :disabled="!canManage" @click="handleEditApp">修改</a-button>
          <a-button danger :disabled="!canManage" :loading="deleting" @click="handleDeleteApp">
            删除
          </a-button>
        </div>
        <div v-if="!canManage" class="app-detail-tip">仅创建者或管理员可修改 / 删除</div>
      </div>
    </a-modal>
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
  ArrowLeftOutlined,
  EditOutlined,
  CloudUploadOutlined,
  RobotOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import { deleteApp, deleteAppByAdmin, deployApp, getAppVoById } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import hamburgerImg from '@/assets/hamburger.png'
import request from '@/request'
import ACCESS_ENUM from '@/access/accessEnum'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref<string>('')
const appName = ref('')
const appLoading = ref(false)
const appInfo = ref<API.AppVO | null>(null)
const isViewMode = computed(() => route.query.view === '1')
const isAdmin = computed(() => loginUserStore.loginUser.userRole === ACCESS_ENUM.ADMIN)
const isOwner = computed(() => {
  const appUserId = appInfo.value?.userId
  const loginUserId = loginUserStore.loginUser.id
  return Boolean(appUserId && loginUserId && appUserId === loginUserId)
})
const permissionDisabled = computed(() => appInfo.value?.userId != null && !isOwner.value)
const canManage = computed(() => Boolean(isOwner.value || isAdmin.value))

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
const inputDisabled = computed(
  () => streaming.value || appLoading.value || permissionDisabled.value
)
const inputTooltip = computed(() =>
  permissionDisabled.value ? '无法在别人的作品下对话哦~' : null
)

// 预览
const previewUrl = ref('')
const codeGenType = ref('')

// 部署
const deploying = ref(false)
const deleting = ref(false)

// 详情弹窗
const detailModalOpen = ref(false)
const openDetailModal = () => {
  detailModalOpen.value = true
}

// 用户头像
const userAvatar = computed(() => {
  return loginUserStore.loginUser.userAvatar || hamburgerImg
})

// EventSource 实例
let eventSource: EventSource | null = null

// Markdown 解析器配置
const md: MarkdownIt = new MarkdownIt({
  html: true, // 启用 HTML 标签
  linkify: true, // 自动识别链接
  typographer: true, // 启用一些语言中性的替换 + 引号美化
  highlight: function (str: string, lang?: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch (__) {
        // 忽略错误
      }
    }
    // 如果没有指定语言，尝试自动检测
    try {
      return (
        '<pre class="hljs"><code>' +
        hljs.highlightAuto(str).value +
        '</code></pre>'
      )
    } catch (__) {
      // 忽略错误
    }
    // 如果高亮失败，返回转义的代码块
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

/**
 * 渲染 Markdown 内容为 HTML
 */
const renderMarkdown = (content: string): string => {
  if (!content) return ''
  return md.render(content)
}

const handleGoHome = () => {
  router.push('/')
}

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

      // 如果是预览模式（从主页卡片点击进入），只加载预览，不自动发送对话
      if (isViewMode.value) {
        // 如果有生成的代码，加载预览
        if (codeGenType.value && appId.value) {
          // 等待一小段时间确保文件已生成
          setTimeout(() => {
            updatePreview()
          }, 500)
        }
      } else {
        // 非预览模式：如果有初始提示词且非权限受限，自动发送
        if (res.data.data.initPrompt && !permissionDisabled.value) {
          // 添加用户消息
          messages.value.push({
            type: 'user',
            content: res.data.data.initPrompt,
            timestamp: Date.now(),
          })

          // 自动发送给AI
          await sendMessageToAI(res.data.data.initPrompt)
        }
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
 * 发送消息给AI（SSE流式）- EventSource
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
    const baseURL = request.defaults.baseURL
    const url = `${baseURL}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(messageText)}`

    // 关闭旧连接
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }

    let version = 1
    let finished = false

    const finalizeSuccess = async () => {
      if (finished) return
      finished = true

      streaming.value = false
      showOptimizeBtn.value = true

      if (eventSource) {
        eventSource.close()
        eventSource = null
      }

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

    const finalizeError = (errMsg: string) => {
      if (finished) return
      finished = true

      console.error(errMsg)
      streaming.value = false

      if (eventSource) {
        eventSource.close()
        eventSource = null
      }

      message.error('发送消息失败')
      // 移除失败的AI消息
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'ai') {
        messages.value.pop()
      }

      throw new Error(errMsg)
    }

    await new Promise<void>((resolve, reject) => {
      try {
        // 创建 EventSource 连接
        eventSource = new EventSource(url, { withCredentials: true })

        // 默认消息事件
        eventSource.onmessage = (e: MessageEvent) => {
          // 兼容后端直接发 [DONE] 作为结束标记
          if (e.data === '[DONE]') {
            finalizeSuccess()
              .then(() => resolve())
              .catch((err) => reject(err))
            return
          }

          if (e.data) {
            try {
              // 尝试解析JSON（后端返回格式：{"d": "chunk内容"}）
              const parsed = JSON.parse(e.data)
              if (parsed.d) {
                currentAiMessage.value += parsed.d
              } else if (parsed.content) {
                currentAiMessage.value += parsed.content
              } else if (typeof parsed === 'string') {
                currentAiMessage.value += parsed
              }
            } catch {
              // 如果不是JSON，直接作为文本内容
              currentAiMessage.value += e.data
            }

            // 更新消息内容
            messages.value[aiMessageIndex].content = currentAiMessage.value
            messages.value[aiMessageIndex].version = version

            // 滚动到底部
            nextTick(() => {
              scrollToBottom()
            })
          }
        }

        // 监听自定义 done 事件
        eventSource.addEventListener('done', () => {
          finalizeSuccess()
            .then(() => resolve())
            .catch((err) => reject(err))
        })

        // 错误事件
        eventSource.onerror = () => {
          // 有些服务端正常关闭也会触发 onerror，这里把 CLOSED 当作正常结束处理
          if (eventSource && eventSource.readyState === EventSource.CLOSED) {
            if (currentAiMessage.value) {
              finalizeSuccess()
                .then(() => resolve())
                .catch((err) => reject(err))
            } else {
              reject(new Error('SSE连接关闭且无内容'))
            }
            return
          }
          reject(new Error('EventSource 连接错误'))
        }
      } catch (e) {
        reject(e)
      }
    }).catch((e) => {
      if (e && (e as any).message === 'SSE连接关闭且无内容') {
        message.error('发送消息失败：连接已关闭')
        streaming.value = false
        if (eventSource) {
          eventSource.close()
          eventSource = null
        }
        if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'ai') {
          messages.value.pop()
        }
        throw e
      }
      finalizeError(e?.message || '创建SSE连接失败')
    })
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
    // 先清空previewUrl，强制iframe卸载
    previewUrl.value = ''

    // 使用nextTick确保DOM更新后再设置新URL
    nextTick(() => {
      // 构建预览URL: http://localhost:8123/api/static/{codeGenType}_{appId}/
      // 添加时间戳参数强制刷新iframe，避免显示缓存内容
      const timestamp = Date.now()
      previewUrl.value = `http://localhost:8123/api/static/${codeGenType.value}_${appId.value}/?t=${timestamp}`
    })
  }
}

/**
 * 处理发送消息
 */
const handleSendMessage = async () => {
  if (permissionDisabled.value) {
    message.warning('无法在别人的作品下对话哦~')
    return
  }

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
  if (permissionDisabled.value) {
    message.warning('无法在别人的作品下对话哦~')
    return
  }

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

const handleEditApp = () => {
  if (!appId.value) return
  if (!canManage.value) {
    message.warning('无权限操作')
    return
  }
  detailModalOpen.value = false
  router.push(`/app/edit/${appId.value}`)
}

const handleDeleteApp = async () => {
  if (!appId.value) return
  if (!canManage.value) {
    message.warning('无权限操作')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用“${appInfo.value?.appName || '未命名应用'}”吗？此操作不可恢复。`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    onOk: async () => {
      try {
        deleting.value = true
        const id = appId.value as any
        const res = isAdmin.value ? await deleteAppByAdmin({ id }) : await deleteApp({ id })
        if (res.data.code === 0) {
          message.success('删除成功')
          detailModalOpen.value = false
          router.push('/')
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (e) {
        console.error('删除失败:', e)
        message.error('删除失败')
      } finally {
        deleting.value = false
      }
    },
  })
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

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
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
  gap: 8px;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  padding: 0;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-area {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.chat-panel {
  width: 40%;
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
  font-size: 14px;
  margin: 0;
  padding: 0;
}

.message-text-plain {
  white-space: pre-wrap;
  font-family: 'Courier New', 'Consolas', 'Monaco', monospace;
}

/* Markdown 样式 */
.markdown-body {
  color: #333;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

.markdown-body :deep(h2) {
  font-size: 1.25em;
  border-bottom: 1px solid #eaecef;
  padding-bottom: 0.3em;
}

.markdown-body :deep(h3) {
  font-size: 1.1em;
}

.markdown-body :deep(p) {
  margin-top: 0;
  margin-bottom: 10px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin-top: 0;
  margin-bottom: 10px;
  padding-left: 24px;
}

.markdown-body :deep(li) {
  margin-top: 4px;
}

.markdown-body :deep(blockquote) {
  margin: 0;
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  margin-top: 0;
  margin-bottom: 16px;
  width: 100%;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 6px 13px;
  border: 1px solid #dfe2e5;
}

.markdown-body :deep(table th) {
  font-weight: 600;
  background-color: #f6f8fa;
}

.markdown-body :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(img) {
  max-width: 100%;
  box-sizing: content-box;
  background-color: #fff;
}

/* 代码块样式 */
.markdown-body :deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 6px;
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  display: inline;
  max-width: auto;
  padding: 0;
  margin: 0;
  overflow: visible;
  line-height: inherit;
  word-wrap: normal;
  background-color: transparent;
  border: 0;
  font-size: 100%;
  word-break: normal;
  white-space: pre;
}

.markdown-body :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
}

.markdown-body :deep(pre code) {
  padding: 0;
  background-color: transparent;
  border-radius: 0;
}

/* Highlight.js 代码高亮样式覆盖 */
.markdown-body :deep(.hljs) {
  display: block;
  overflow-x: auto;
  padding: 16px;
  background: #0d1117;
  color: #c9d1d9;
  border-radius: 6px;
  margin: 8px 0;
}

.markdown-body :deep(.hljs code) {
  background: transparent;
  padding: 0;
  font-size: 13px;
  line-height: 1.5;
}

/* 确保代码块在消息框中正确显示 */
.message-ai .markdown-body :deep(pre) {
  background-color: #0d1117;
  border: 1px solid #30363d;
}

.message-ai .markdown-body :deep(pre code) {
  color: #c9d1d9;
}

.message-user .message-text {
  color: inherit;
}

.message-ai .message-text {
  color: #333;
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

.message-input-wrapper {
  width: 100%;
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
  width: 60%;
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

.app-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-detail-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.app-detail-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.app-detail-user-name {
  font-size: 14px;
  font-weight: 600;
}

.app-detail-user-sub {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.app-detail-row {
  display: flex;
  gap: 8px;
  line-height: 22px;
}

.app-detail-label {
  color: #666;
  width: 80px;
  flex-shrink: 0;
}

.app-detail-value {
  color: rgba(0, 0, 0, 0.85);
  flex: 1;
  word-break: break-all;
}

.app-detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.app-detail-tip {
  font-size: 12px;
  color: #999;
  text-align: right;
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
