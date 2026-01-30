<template>
  <div class="chat-history-manager-page">
    <h2 class="page-title">对话管理</h2>
    <!-- 搜索表单 -->
    <a-form :model="searchForm" layout="inline" class="search-form">
      <a-form-item label="应用ID">
        <a-input-number
          v-model:value="searchForm.appId"
          placeholder="请输入应用ID"
          :min="1"
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input-number
          v-model:value="searchForm.userId"
          placeholder="请输入用户ID"
          :min="1"
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-select
          v-model:value="searchForm.messageType"
          :options="messageTypeOptions"
          placeholder="请选择消息类型"
          allow-clear
          style="width: 160px"
        />
      </a-form-item>
      <a-form-item label="消息内容">
        <a-input
          v-model:value="searchForm.message"
          placeholder="请输入消息关键词"
          allow-clear
          style="width: 220px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch" :loading="loading">
          <template #icon>
            <SearchOutlined />
          </template>
          搜索
        </a-button>
        <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>

    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="chatHistoryList"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ y: 'calc(100vh - 360px)', x: 'max-content' }"
        row-key="id"
        @change="handleTableChange"
      >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'messageType'">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'green'">
            {{ record.messageType === 'user' ? '用户' : 'AI' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'message'">
          <span class="message-text" :title="record.message">
            {{ record.message }}
          </span>
        </template>
        <template v-else-if="column.key === 'createTime'">
          {{ formatDate(record.createTime) }}
        </template>
      </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listByAdmin } from '@/api/chatHistoryController'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { formatDate } from '@/utils/format'

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    key: 'appId',
    width: 120,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 120,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 120,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
]

// 消息类型选项
const messageTypeOptions = [
  { label: '用户', value: 'user' },
  { label: 'AI', value: 'ai' },
]

// 数据
const chatHistoryList = ref<API.ChatHistoryVO[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive<{
  appId?: number
  userId?: number
  messageType?: string
  message?: string
}>({
  appId: undefined,
  userId: undefined,
  messageType: undefined,
  message: '',
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100'],
})

/**
 * 获取对话记录列表
 */
const fetchChatHistoryList = async () => {
  try {
    loading.value = true
    const queryParams: API.ChatHistoryQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    }

    if (searchForm.appId) {
      queryParams.appId = searchForm.appId
    }
    if (searchForm.userId) {
      queryParams.userId = searchForm.userId
    }
    if (searchForm.messageType) {
      queryParams.messageType = searchForm.messageType
    }
    if (searchForm.message) {
      queryParams.message = searchForm.message
    }

    const res = await listByAdmin(queryParams)
    if (res.data.code === 0 && res.data.data) {
      chatHistoryList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error('获取对话记录失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取对话记录失败:', error)
    message.error('获取对话记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.current = 1
  fetchChatHistoryList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.appId = undefined
  searchForm.userId = undefined
  searchForm.messageType = undefined
  searchForm.message = ''
  pagination.current = 1
  fetchChatHistoryList()
}

/**
 * 处理表格变化（分页等）
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchChatHistoryList()
}


// 组件挂载时获取对话记录列表
onMounted(() => {
  fetchChatHistoryList()
})
</script>

<style scoped>
.chat-history-manager-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  padding: 24px;
  background: #fff;
  overflow: hidden;
}

.page-title {
  flex-shrink: 0;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
}

.search-form {
  flex-shrink: 0;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.table-container {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.message-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-history-manager-page {
    padding: 16px;
    height: calc(100vh - 64px);
  }

  .page-title {
    font-size: 18px;
    margin-bottom: 16px;
  }

  .search-form {
    padding: 12px;
    margin-bottom: 16px;
  }
}
</style>

