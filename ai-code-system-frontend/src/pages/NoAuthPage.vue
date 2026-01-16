<template>
    <div style="max-width: 720px; margin: 48px auto; padding: 24px; background: #fff">
      <h2>无权限</h2>
      <p>你没有权限访问该页面。</p>
      <a-button type="primary" @click="goHome">返回首页</a-button>
    </div>
  </template>
  
  <script setup lang="ts">
  import { useRouter } from 'vue-router'
  const router = useRouter()
  const goHome = () => router.replace('/')
  </script>
  