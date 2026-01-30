<template>
  <div class="user-manager-page">
    <h2 class="page-title">用户管理</h2>
    <!-- 搜索表单 -->
    <a-form :model="searchForm" layout="inline" class="search-form">
      <a-form-item label="账号">
        <a-input
          v-model:value="searchForm.userAccount"
          placeholder="请输入账号"
          allow-clear
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input
          v-model:value="searchForm.userName"
          placeholder="请输入用户名"
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
    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ y: 'calc(100vh - 320px)', x: 'max-content' }"
        row-key="id"
        @change="handleTableChange"
      >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'userAvatar'">
          <img
            :src="record.userAvatar || defaultAvatar"
            alt="用户头像"
            class="user-avatar-img"
            @error="handleAvatarError"
          />
        </template>
        <template v-else-if="column.key === 'userRole'">
          <a-tag :color="getRoleColor(record.userRole)">
            {{ record.userRole || '普通用户' }}
          </a-tag>
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
            danger
            @click="handleDelete(record)"
            :loading="deletingIds.includes(record.id!)"
          >
            删除
          </a-button>
        </template>
      </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listUserVoByPage, deleteUser } from '@/api/userController'
import { message, Modal } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { formatDate } from '@/utils/format'
import { handleAvatarError, DEFAULT_AVATAR } from '@/utils/image'

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 200,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    width: 120,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '头像',
    key: 'userAvatar',
    width: 80,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
  },
  {
    title: '用户角色',
    key: 'userRole',
    width: 120,
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
    width: 100,
    fixed: 'right' as const,
  },
]

// 数据
const userList = ref<API.UserVO[]>([])
const loading = ref(false)
const deletingIds = ref<number[]>([])
const defaultAvatar = DEFAULT_AVATAR

// 搜索表单
const searchForm = reactive<{
  userAccount?: string
  userName?: string
}>({
  userAccount: '',
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
 * 获取用户列表
 */
const fetchUserList = async () => {
  try {
    loading.value = true
    const queryParams: API.UserQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    }
    // 添加搜索条件
    if (searchForm.userAccount) {
      queryParams.userAccount = searchForm.userAccount
    }
    if (searchForm.userName) {
      queryParams.userName = searchForm.userName
    }
    
    const res = await listUserVoByPage(queryParams)
    if (res.data.code === 0 && res.data.data) {
      userList.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error('获取用户列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  // 重置到第一页
  pagination.current = 1
  fetchUserList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.userAccount = ''
  searchForm.userName = ''
  // 重置到第一页并重新获取数据
  pagination.current = 1
  fetchUserList()
}

/**
 * 处理表格变化（分页、排序等）
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUserList()
}

/**
 * 处理删除用户
 */
const handleDelete = (record: API.UserVO) => {
  if (!record.id) {
    message.error('用户ID不存在')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.userName || record.userAccount}" 吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        deletingIds.value.push(record.id!)
        const res = await deleteUser({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          // 重新获取列表
          await fetchUserList()
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除用户失败')
      } finally {
        deletingIds.value = deletingIds.value.filter((id) => id !== record.id)
      }
    },
  })
}

/**
 * 获取角色颜色
 */
const getRoleColor = (role?: string) => {
  const roleColors: Record<string, string> = {
    admin: 'red',
    user: 'blue',
    管理员: 'red',
    用户: 'blue',
  }
  return roleColors[role || ''] || 'default'
}


// 组件挂载时获取用户列表
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manager-page {
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

.user-avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-manager-page {
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
