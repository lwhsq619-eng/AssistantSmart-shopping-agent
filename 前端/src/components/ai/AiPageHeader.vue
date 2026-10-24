<template>
  <section class="ai-page-header">
    <div>
      <el-button v-if="back" class="back-btn" text @click="$router.back()">返回</el-button>
      <div v-if="tag" class="ai-status-tag">{{ tag }}</div>
      <h1 class="ai-page-title">{{ title }}</h1>
      <p class="ai-page-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  tag: { type: String, default: '' },
  back: { type: Boolean, default: false }
})
</script>

<style scoped>
.back-btn {
  margin: 0 0 12px -10px;
  color: #fff;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
