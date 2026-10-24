<template>
  <div class="page-card profile">
    <el-avatar :src="auth.user?.avatar" :size="82" />
    <h2>{{ auth.user?.username }}</h2>
    <p>角色：{{ auth.role }}</p>
    <p>手机号：{{ auth.user?.phone || '-' }}</p>
    <p>邮箱：{{ auth.user?.email || '-' }}</p>
    <p>地址：{{ auth.user?.address || '-' }}</p>
  </div>
</template>

<script setup>
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.profile { text-align: center; max-width: 520px; margin: 0 auto; }
.profile p { color: #64748b; }
</style>
