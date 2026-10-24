<template>
  <div class="user-layout">
    <header class="user-header">
      <router-link class="brand" to="/user/home">
        <el-icon><ShoppingBag /></el-icon>
        <span>网上商城</span>
      </router-link>

      <nav class="nav-links" aria-label="商城导航">
        <router-link to="/user/home">商城首页</router-link>
        <router-link to="/user/category">商品分类</router-link>
        <router-link to="/user/search">今日特价</router-link>
        <router-link to="/user/gift-card">礼品卡</router-link>
        <router-link to="/user/cart">购物车</router-link>
        <router-link to="/user/orders">我的订单</router-link>
        <router-link to="/ai/guide">AI导购</router-link>
        <router-link to="/ai/search">AI搜索</router-link>
        <router-link to="/ai/customer-service">AI客服</router-link>
      </nav>

      <el-input v-model="keyword" class="search" placeholder="搜索商品" clearable @keyup.enter="doSearch">
        <template #append><el-button @click="doSearch">搜索</el-button></template>
      </el-input>

      <div class="user-box">
        <el-avatar :src="auth.user?.avatar || defaultAvatar" :size="34" />
        <span>{{ auth.user?.username }}</span>
        <el-button text type="primary" @click="logout">退出</el-button>
      </div>
    </header>

    <main class="user-main">
      <router-view />
    </main>

    <AiFloatingAssistant />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AiFloatingAssistant from '../components/ai/AiFloatingAssistant.vue'
import { useAuthStore } from '../stores/auth'
import { defaultAvatar } from '../utils/images'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')

function doSearch() {
  router.push({ path: '/user/search', query: { keyword: keyword.value || undefined } })
}

function logout() {
  auth.logout()
  router.push('/login/user')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: #f4f7fb;
}

.user-header {
  position: sticky;
  top: 0;
  z-index: 20;
  min-height: 66px;
  padding: 10px 24px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) minmax(220px, 300px) auto;
  gap: 18px;
  align-items: center;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #111827;
  font-weight: 900;
  text-decoration: none;
  white-space: nowrap;
  font-size: 18px;
}

.brand .el-icon {
  color: #f59e0b;
  font-size: 24px;
}

.nav-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  min-width: 0;
}

.nav-links a {
  color: #475569;
  text-decoration: none;
  font-weight: 700;
  white-space: nowrap;
}

.nav-links a.router-link-active {
  color: #2563eb;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  white-space: nowrap;
}

.user-main {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 1180px) {
  .user-header {
    grid-template-columns: auto 1fr auto;
  }

  .search {
    grid-column: 1 / -1;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .user-header {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .nav-links {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 4px;
  }

  .user-box {
    justify-content: space-between;
  }

  .user-main {
    padding: 16px;
  }
}
</style>
