<template>
  <div class="login-redirect">
    <el-empty description="正在跳转到登录页" />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace('/login/user')
})
</script>

<style scoped>
.login-redirect {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f4f7fb;
}
</style>
