<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" />
    <a-layout-content class="layout-content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

interface MenuItem {
  key: string
  label: string
  path?: string
}

interface Props {
  menuItems?: MenuItem[]
}

withDefaults(defineProps<Props>(), {
  menuItems: () => [
    { key: 'home', label: '首页', path: '/' },
    { key: 'about', label: '关于', path: '/about' },
  ],
})
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  .layout-content {
    flex: 1;
    padding: 24px;
    background: #fff;
    min-height: 0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .basic-layout .layout-content {
    padding: 16px;
  }
}
</style>