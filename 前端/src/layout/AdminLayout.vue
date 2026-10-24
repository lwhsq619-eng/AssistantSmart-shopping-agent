<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        <el-icon><ShoppingBag /></el-icon>
        <span>商城后台</span>
      </div>
      <el-menu router :default-active="$route.path" background-color="#111827" text-color="#cbd5e1" active-text-color="#fff">
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine /></el-icon><span>首页仪表盘</span></el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Grid /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-sub-menu index="/admin/ai">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 运营中心</span>
          </template>
          <el-menu-item index="/admin/ai/workbench">AI 工作台</el-menu-item>
          <el-menu-item index="/admin/ai/copywriting">商品文案</el-menu-item>
          <el-menu-item index="/admin/ai/comment">评论分析</el-menu-item>
          <el-menu-item index="/admin/ai/marketing">营销素材</el-menu-item>
          <el-menu-item index="/admin/ai/order-analysis">订单分析</el-menu-item>
          <el-menu-item index="/admin/ai/inventory">库存分析</el-menu-item>
          <el-menu-item index="/admin/ai/briefing">运营简报</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div>
          <strong>网上商城购物系统</strong>
          <span class="topbar-title">{{ $route.meta.title }}</span>
        </div>
        <div class="topbar-actions">
          <el-icon><Bell /></el-icon>
          <span>当前管理员：{{ auth.user?.username || 'admin' }}</span>
          <el-button type="primary" plain @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login/admin')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  background: #111827;
  box-shadow: 8px 0 24px rgba(15, 23, 42, 0.12);
  overflow-y: auto;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  font-weight: 800;
  font-size: 18px;
  background: #0f172a;
}

.brand .el-icon {
  color: #f59e0b;
  font-size: 24px;
}

.el-menu {
  border-right: none;
}

.topbar {
  position: fixed;
  left: 220px;
  right: 0;
  top: 0;
  z-index: 5;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.topbar-title {
  margin-left: 14px;
  color: #64748b;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #475569;
}

.main {
  margin-left: 220px;
  padding: 84px 24px 24px;
  min-height: 100vh;
  background: #f4f7fb;
}
</style>
