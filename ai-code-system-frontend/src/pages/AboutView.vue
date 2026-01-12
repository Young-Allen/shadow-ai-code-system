<template>
  <div class="about">
    <h1>This is an about page</h1>
    <p>{{ healthCheckResult }}</p>
  </div>
</template>

<script setup lang="ts">
import { healthCheck } from '@/api/healthController.ts'
import { ref, onMounted } from 'vue'

const healthCheckResult = ref<string | undefined>(undefined)

onMounted(() => {
  healthCheck().then(res => {
    console.log(res)
    healthCheckResult.value = res.data.data
  })
})

</script>


<style scoped>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
  }
}
</style>
