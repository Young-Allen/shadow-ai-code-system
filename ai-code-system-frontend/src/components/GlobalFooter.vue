<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      {{ footerText }}
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
interface Props {
  footerText?: string
}

withDefaults(defineProps<Props>(), {
  footerText: 'ShadowW-AI 零代码应用生成平台',
})
</script>

<style scoped>
.global-footer {
  text-align: center;
  padding: 16px 24px;
  background: #f0f2f5;
  border-top: 1px solid #e8e8e8;
  position: relative;
  z-index: 1;

  .footer-content {
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
  }
}

/* 确保页脚固定在底部 */
:deep(.ant-layout) {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.ant-layout .ant-layout-content) {
  flex: 1;
}
</style>