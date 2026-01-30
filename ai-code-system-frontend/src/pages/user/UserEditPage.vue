<template>
  <div class="user-edit-page">
    <h2 class="page-title">个人中心</h2>
    <a-card>
      <a-form
        :model="formState"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <a-form-item label="用户ID">
          <a-input :value="loginUser.id" disabled />
        </a-form-item>
        <a-form-item label="账号">
          <a-input :value="loginUser.userAccount" disabled />
        </a-form-item>
        <a-form-item
          label="用户名"
          name="userName"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像URL" name="userAvatar">
          <a-input
            v-model:value="formState.userAvatar"
            placeholder="请输入头像URL"
            allow-clear
          />
          <div class="avatar-preview" v-if="formState.userAvatar">
            <img
              :src="formState.userAvatar"
              alt="头像预览"
              class="preview-img"
              @error="handleAvatarError"
            />
          </div>
        </a-form-item>
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="formState.userProfile"
            placeholder="请输入个人简介"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-input :value="loginUser.userRole || '普通用户'" disabled />
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
          <a-button type="primary" html-type="submit" :loading="loading">
            保存修改
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          <a-button style="margin-left: 8px" @click="handleCancel">取消</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateUser } from '@/api/userController'
import { handleAvatarError, DEFAULT_AVATAR } from '@/utils/image'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)

// 获取当前登录用户信息（使用 computed 确保响应式）
const loginUser = computed(() => loginUserStore.loginUser)

// 表单状态
const formState = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

/**
 * 初始化表单数据
 */
const initForm = () => {
  const user = loginUser.value
  formState.id = user.id
  formState.userName = user.userName || ''
  formState.userAvatar = user.userAvatar || ''
  formState.userProfile = user.userProfile || ''
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formState.id) {
    message.error('用户ID不存在')
    return
  }

  try {
    loading.value = true
    const res = await updateUser(formState)
    if (res.data.code === 0) {
      message.success('修改成功')
      // 更新 store 中的用户信息
      loginUserStore.setLoginUser({
        ...loginUser.value,
        userName: formState.userName,
        userAvatar: formState.userAvatar,
        userProfile: formState.userProfile,
      })
      // 返回上一页或首页
      router.back()
    } else {
      message.error('修改失败：' + res.data.message)
    }
  } catch (error) {
    console.error('修改用户信息失败:', error)
    message.error('修改用户信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  initForm()
  message.info('已重置为原始数据')
}

/**
 * 取消编辑
 */
const handleCancel = () => {
  router.back()
}


// 组件挂载时初始化表单
onMounted(() => {
  initForm()
})
</script>

<style scoped>
.user-edit-page {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
}

.avatar-preview {
  margin-top: 8px;
}

.preview-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #d9d9d9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-edit-page {
    padding: 16px;
  }

  .page-title {
    font-size: 18px;
    margin-bottom: 16px;
  }
}
</style>
