<template>
  <a-modal
    v-model:open="open"
    title="应用详情"
    :footer="null"
    :maskClosable="true"
    destroyOnClose
    @cancel="handleCancel"
  >
    <div class="app-detail">
      <div class="app-detail-user">
        <img
          :src="appInfo?.user?.userAvatar || defaultAvatar"
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
        <span class="app-detail-label">生成类型：</span>
        <span class="app-detail-value">
          <a-tag v-if="codeGenTypeLabel" color="blue">
            {{ codeGenTypeLabel }}
          </a-tag>
          <span v-else>-</span>
        </span>
      </div>
      <div class="app-detail-row">
        <span class="app-detail-label">创建时间：</span>
        <span class="app-detail-value">{{ formatDate(appInfo?.createTime) }}</span>
      </div>

      <div class="app-detail-actions">
        <slot name="actions" :appInfo="appInfo" :canManage="canManage">
          <a-button :disabled="!canManage" @click="handleEdit">修改</a-button>
          <a-button danger :disabled="!canManage" :loading="deleting" @click="handleDelete">
            删除
          </a-button>
        </slot>
      </div>
      <div v-if="!canManage" class="app-detail-tip">仅创建者或管理员可修改 / 删除</div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDate } from '@/utils/format'
import { handleAvatarError, DEFAULT_AVATAR } from '@/utils/image'
import { CODE_GEN_TYPE_OPTIONS } from '@/constants/codeGenType'

interface Props {
  appInfo: API.AppVO | null
  canManage: boolean
  deleting?: boolean
  open?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  deleting: false,
  open: undefined,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  cancel: []
  edit: [appInfo: API.AppVO]
  delete: [appInfo: API.AppVO]
}>()

const open = computed({
  get: () => props.open !== undefined ? props.open : props.appInfo !== null,
  set: (value) => {
    emit('update:open', value)
    if (!value) {
      emit('cancel')
    }
  },
})

const defaultAvatar = DEFAULT_AVATAR

const codeGenTypeLabel = computed(() => {
  const type = props.appInfo?.codeGenType
  if (!type) return ''
  const found = CODE_GEN_TYPE_OPTIONS.find((item) => item.value === type)
  return found?.label || type
})

const handleCancel = () => {
  emit('cancel')
}

const handleEdit = () => {
  if (props.appInfo) {
    emit('edit', props.appInfo)
  }
}

const handleDelete = () => {
  if (props.appInfo) {
    emit('delete', props.appInfo)
  }
}
</script>

<style scoped>
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
</style>
