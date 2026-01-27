<template>
  <div class="app-manager-page">
    <h2 class="page-title">应用管理</h2>
    <!-- 搜索表单 -->
    <a-form :model="searchForm" layout="inline" class="search-form">
      <a-form-item label="应用名称">
        <a-input
          v-model:value="searchForm.appName"
          placeholder="请输入应用名称"
          allow-clear
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="代码类型">
        <a-select
          v-model:value="searchForm.codeGenType"
          :options="CODE_GEN_TYPE_OPTIONS as any"
          placeholder="请选择代码类型"
          allow-clear
          style="width: 260px"
        />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input-number
          v-model:value="searchForm.userId"
          placeholder="请输入用户ID"
          allow-clear
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="用户姓名">
        <a-input
          v-model:value="searchForm.userName"
          placeholder="请输入用户姓名"
          allow-clear
          style="width: 200px"
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
    <a-table
      :columns="columns"
      :data-source="appList"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'cover'">
          <img
            v-if="record.cover"
            :src="record.cover"
            alt="应用封面"
            class="app-cover-img"
            @error="handleImageError"
          />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag :color="record.priority === 99 ? 'red' : 'default'">
            {{ record.priority === 99 ? '精选' : record.priority || '-' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'user'">
          <div v-if="record.user" class="user-info">
            <img
              :src="record.user.userAvatar || defaultAvatar"
              :alt="record.user.userName"
              class="user-avatar-img"
              @error="handleAvatarError"
            />
            <span>{{ record.user.userName || '-' }}</span>
          </div>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'createTime'">
          {{ formatDate(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'updateTime'">
          {{ formatDate(record.updateTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button
            type="link"
            @click="handleEdit(record)"
            :loading="editingIds.includes(record.id!)"
          >
            编辑
          </a-button>
          <a-button
            type="link"
            danger
            @click="handleDelete(record)"
            :loading="deletingIds.includes(record.id!)"
          >
            删除
          </a-button>
          <a-button
            type="link"
            :danger="record.priority === 99"
            @click="handleSetFeatured(record)"
            :loading="featuredIds.includes(record.id!)"
          >
            {{ record.priority === 99 ? '取消精选' : '精选' }}
          </a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import hamburgerImg from '@/assets/hamburger.png'
import { CODE_GEN_TYPE_OPTIONS } from '@/constants/codeGenType'

const router = useRouter()

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    width: 150,
  },
  {
    title: '封面',
    key: 'cover',
    width: 100,
  },
  {
    title: '代码类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120,
  },
  {
    title: '优先级',
    key: 'priority',
    width: 100,
  },
  {
    title: '用户',
    key: 'user',
    width: 150,
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const,
  },
]

// 数据
const appList = ref<API.AppVO[]>([])
const loading = ref(false)
const deletingIds = ref<number[]>([])
const editingIds = ref<number[]>([])
const featuredIds = ref<number[]>([])
const defaultAvatar = hamburgerImg

// 搜索表单
const searchForm = reactive<{
  appName?: string
  codeGenType?: string
  userId?: number
  userName?: string
}>({
  appName: '',
  codeGenType: undefined,
  userId: undefined,
  userName: '',
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
 * 获取应用列表
 */
const fetchAppList = async () => {
  try {
    loading.value = true
    const queryParams: API.AppQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    }
    // 添加搜索条件
    if (searchForm.appName) {
      queryParams.appName = searchForm.appName
    }
    if (searchForm.codeGenType) {
      queryParams.codeGenType = searchForm.codeGenType
    }
    if (searchForm.userId) {
      queryParams.userId = searchForm.userId
    }
    if (searchForm.userName) {
      ;(queryParams as any).userName = searchForm.userName
    }

    const res = await listAppVoByPageByAdmin(queryParams)
    if (res.data.code === 0 && res.data.data) {
      appList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error('获取应用列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.current = 1
  fetchAppList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.appName = ''
  searchForm.codeGenType = undefined
  searchForm.userId = undefined
  searchForm.userName = ''
  pagination.current = 1
  fetchAppList()
}

/**
 * 处理表格变化（分页、排序等）
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchAppList()
}

/**
 * 处理编辑
 */
const handleEdit = (record: API.AppVO) => {
  if (!record.id) {
    message.error('应用ID不存在')
    return
  }
  router.push(`/app/edit/${String(record.id)}`)
}

/**
 * 处理删除
 */
const handleDelete = (record: API.AppVO) => {
  if (!record.id) {
    message.error('应用ID不存在')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用 "${record.appName || '未命名应用'}" 吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        deletingIds.value.push(record.id!)
        const res = await deleteAppByAdmin({ id: record.id as any })
        if (res.data.code === 0) {
          message.success('删除成功')
          await fetchAppList()
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        console.error('删除应用失败:', error)
        message.error('删除应用失败')
      } finally {
        deletingIds.value = deletingIds.value.filter((id) => id !== record.id)
      }
    },
  })
}

/**
 * 处理设置精选
 */
const handleSetFeatured = (record: API.AppVO) => {
  if (!record.id) {
    message.error('应用ID不存在')
    return
  }

  const isFeatured = record.priority === 99
  const newPriority = isFeatured ? 0 : 99

  Modal.confirm({
    title: isFeatured ? '取消精选' : '设置精选',
    content: `确定要${isFeatured ? '取消' : '设置'}应用 "${record.appName || '未命名应用'}" 为精选吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        featuredIds.value.push(record.id!)
        const res = await updateAppByAdmin({
          id: record.id as any,
          priority: newPriority,
        })
        if (res.data.code === 0) {
          message.success(`${isFeatured ? '取消' : '设置'}精选成功`)
          await fetchAppList()
        } else {
          message.error(`${isFeatured ? '取消' : '设置'}精选失败：` + res.data.message)
        }
      } catch (error) {
        console.error('设置精选失败:', error)
        message.error('设置精选失败')
      } finally {
        featuredIds.value = featuredIds.value.filter((id) => id !== record.id)
      }
    },
  })
}

/**
 * 格式化日期
 */
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
 * 处理图片加载错误
 */
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

/**
 * 处理头像加载错误
 */
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = defaultAvatar
}

// 组件挂载时获取应用列表
onMounted(() => {
  fetchAppList()
})
</script>

<style scoped>
.app-manager-page {
  padding: 24px;
  background: #fff;
}

.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.app-cover-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar-img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-manager-page {
    padding: 16px;
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
