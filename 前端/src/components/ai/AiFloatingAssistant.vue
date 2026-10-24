<template>
  <div class="floating-ai">
    <el-button class="float-btn" type="primary" circle aria-label="打开 AI 助手" @click="open = !open">
      <el-icon><ChatDotRound /></el-icon>
    </el-button>
    <transition name="fade">
      <section v-if="open" class="float-panel">
        <div class="head">
          <strong>AI 助手</strong>
          <el-button text size="small" @click="open = false">关闭</el-button>
        </div>
        <p>快速进入智能导购、客服和语义搜索。</p>
        <div class="links">
          <el-button type="primary" plain @click="go('/ai/guide')">AI 导购</el-button>
          <el-button type="primary" plain @click="go('/ai/customer-service')">智能客服</el-button>
          <el-button type="primary" plain @click="go('/ai/search')">语义搜索</el-button>
        </div>
      </section>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const open = ref(false)

function go(path) {
  open.value = false
  router.push(path)
}
</script>

<style scoped>
.floating-ai {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 30;
}

.float-btn {
  width: 52px;
  height: 52px;
  box-shadow: 0 12px 30px rgba(37, 99, 235, .32);
}

.float-panel {
  position: absolute;
  right: 0;
  bottom: 64px;
  width: min(320px, calc(100vw - 32px));
  padding: 16px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 18px 42px rgba(15, 23, 42, .18);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.float-panel p {
  margin: 8px 0 14px;
  color: #64748b;
  line-height: 1.6;
}

.links {
  display: grid;
  gap: 8px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity .18s ease, transform .18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 768px) {
  .floating-ai {
    right: 14px;
    bottom: 14px;
  }

  .float-btn {
    width: 46px;
    height: 46px;
  }
}
</style>
