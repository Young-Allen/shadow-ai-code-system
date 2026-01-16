<script setup lang="ts">
// 首页内容
</script>

<template>
  <div class="home-page">
    <div class="features">
      <a-card title="快速开发" :bordered="false" style="width: 300px">
        <p>通过可视化界面快速构建应用</p>
      </a-card>
      <a-card title="零代码" :bordered="false" style="width: 300px">
        <p>无需编写代码即可生成完整应用</p>
      </a-card>
      <a-card title="AI 驱动" :bordered="false" style="width: 300px">
        <p>智能生成应用代码和逻辑</p>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  text-align: center;
  padding: 40px 20px;
}

.home-page h1 {
  font-size: 32px;
  margin-bottom: 16px;
  color: #1890ff;
}

.description {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
}

.features {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .home-page h1 {
    font-size: 24px;
  }

  .description {
    font-size: 16px;
  }

  .features {
    flex-direction: column;
    align-items: center;
  }
}
</style>
