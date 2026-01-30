<template>
  <div class="app-edit-page">
    <h2 class="page-title">{{ isAdmin ? '编辑应用（管理员）' : '编辑应用' }}</h2>
    <a-spin :spinning="loading">
      <a-form
        :model="formData"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
        @finish="handleSubmit"
      >
        <a-form-item label="初始提示词" name="initPrompt">
          <a-textarea
            v-model:value="formData.initPrompt"
            :rows="4"
            placeholder="初始提示词"
            disabled
          />
        </a-form-item>

        <a-form-item label="生成类型" name="codeGenType">
          <a-input v-model:value="formData.codeGenType" placeholder="生成类型" disabled />
        </a-form-item>

        <a-form-item label="部署密钥" name="deployKey">
          <a-input v-model:value="formData.deployKey" placeholder="部署密钥" disabled />
        </a-form-item>

        <a-form-item label="应用名称" name="appName" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input v-model:value="formData.appName" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item v-if="isAdmin" label="应用封面" name="cover">
          <a-input v-model:value="formData.cover" placeholder="请输入封面URL" />
          <div v-if="formData.cover" class="cover-preview">
            <img :src="formData.cover" alt="封面预览" @error="handleImageError" />
          </div>
        </a-form-item>

        <a-form-item v-if="isAdmin" label="优先级" name="priority">
          <a-input-number
            v-model:value="formData.priority"
            :min="0"
            :max="100"
            placeholder="请输入优先级（0-100，99为精选）"
            style="width: 100%"
          />
          <div class="form-tip">优先级范围：0-100，设置为99表示精选应用</div>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
          <a-button type="primary" html-type="submit" :loading="submitting">
            保存
          </a-button>
          <a-button style="margin-left: 8px" @click="handleCancel">取消</a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, getAppById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import ACCESS_ENUM from '@/access/accessEnum'
import { handleImageError } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const submitting = ref(false)
const appId = ref<string>('')

// 判断是否为管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === ACCESS_ENUM.ADMIN
})

// 表单数据
const formData = reactive<{
  appName: string
  initPrompt?: string
  codeGenType?: string
  deployKey?: string
  cover?: string
  priority?: number
}>({
  appName: '',
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
  cover: '',
  priority: 0,
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
    loading.value = true
    let res
    if (isAdmin.value) {
      // 管理员获取完整信息
      res = await getAppById({ id: id as any })
      if (res.data.code === 0 && res.data.data) {
        const app = res.data.data
        formData.initPrompt = app.initPrompt || ''
        formData.codeGenType = app.codeGenType || ''
        formData.deployKey = app.deployKey || ''
        formData.appName = app.appName || ''
        formData.cover = app.cover || ''
        formData.priority = app.priority || 0
      }
    } else {
      // 普通用户获取VO信息
      res = await getAppVoById({ id: id as any })
      if (res.data.code === 0 && res.data.data) {
        const app = res.data.data
        // 检查是否为当前用户的应用
        if (app.userId !== loginUserStore.loginUser.id) {
          message.error('无权编辑此应用')
          router.push('/')
          return
        }
        formData.initPrompt = app.initPrompt || ''
        formData.codeGenType = app.codeGenType || ''
        formData.deployKey = app.deployKey || ''
        formData.appName = app.appName || ''
      }
    }

    if (res.data.code !== 0) {
      message.error('获取应用信息失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取应用信息失败:', error)
    message.error('获取应用信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  try {
    submitting.value = true
    let res
    if (isAdmin.value) {
      // 管理员更新
      res = await updateAppByAdmin({
        id: appId.value as any,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户更新
      res = await updateApp({
        id: appId.value as any,
        appName: formData.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('保存成功')
      router.back()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 取消编辑
 */
const handleCancel = () => {
  router.back()
}


// 组件挂载时初始化
onMounted(() => {
  initApp()
})
</script>

<style scoped>
.app-edit-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  background: #fff;
}

.page-title {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
}

.cover-preview {
  margin-top: 12px;
}

.cover-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .app-edit-page {
    padding: 16px;
  }

  .page-title {
    font-size: 18px;
    margin-bottom: 16px;
  }
}
</style>
