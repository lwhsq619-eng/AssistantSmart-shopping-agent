<template>
  <div class="ai-empty">
    <el-empty :description="description">
      <el-button v-if="actionText" type="primary" plain @click="$emit('action')">{{ actionText }}</el-button>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>
