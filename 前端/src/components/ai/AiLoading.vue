<template>
  <div class="ai-loading">
    <el-icon class="is-loading"><Loading /></el-icon>
    <span>{{ text }}</span>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: 'AI 正在分析，请稍候...' }
})
</script>

<style scoped>
.ai-loading {
  min-height: 120px;
  display: grid;
  place-items: center;
  gap: 10px;
  color: #2563eb;
  border-radius: 12px;
  background: #eff6ff;
}

.el-icon {
  font-size: 28px;
}
</style>
