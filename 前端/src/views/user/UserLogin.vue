<template>
  <div class="user-login">
    <AnimatedBackground variant="user" />
    <section class="shop-copy">
      <div class="floating-product">精选好物</div>
      <h1>发现好物，轻松购物</h1>
      <p>精选商品 · 便捷下单 · 实时订单追踪</p>
    </section>

    <el-card class="user-card" shadow="never">
      <div class="card-title">
        <div class="logo"><el-icon><ShoppingBag /></el-icon></div>
        <h2>欢迎回来</h2>
        <p>登录您的购物账户</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名 / 手机号" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名或手机号" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password type="password" size="large">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="图形验证码" prop="captchaCode">
          <CaptchaInput ref="captchaRef" v-model="form.captchaCode" v-model:captcha-id="form.captchaId" @enter="submit" />
        </el-form-item>
        <div class="form-row">
          <el-checkbox v-model="form.remember">记住密码</el-checkbox>
          <router-link to="/login/admin">进入管理员登录</router-link>
        </div>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
        <div class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AnimatedBackground from '../../components/AnimatedBackground.vue'
import CaptchaInput from '../../components/CaptchaInput.vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const captchaRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: '',
  remember: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function submit() {
  if (loading.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    await auth.userLogin({ ...form })
    ElMessage.success('用户登录成功')
    router.replace('/user/home')
  } catch (error) {
    captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.user-login {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(420px, 1fr) 440px;
  align-items: center;
  gap: 60px;
  padding: 64px 9vw;
}

.shop-copy,
.user-card {
  position: relative;
  z-index: 1;
}

.shop-copy {
  color: #fff;
}

.floating-product {
  display: inline-block;
  padding: 10px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.26);
  animation: floatY 5s ease-in-out infinite;
}

.shop-copy h1 {
  margin: 28px 0 14px;
  font-size: 46px;
}

.shop-copy p {
  margin: 0;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.88);
}

.user-card {
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px);
  box-shadow: 0 28px 70px rgba(37, 99, 235, 0.22);
  animation: cardIn 0.55s ease both;
}

.user-card :deep(.el-card__body) {
  padding: 34px;
}

.user-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 4px rgba(59, 130, 246, 0.12);
}

.card-title {
  text-align: center;
  margin-bottom: 24px;
}

.logo {
  width: 54px;
  height: 54px;
  margin: 0 auto 12px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  color: #fff;
  font-size: 24px;
  background: linear-gradient(135deg, #2563eb, #f59e0b);
}

.card-title h2 {
  margin: 0;
  font-size: 26px;
}

.card-title p {
  margin: 8px 0 0;
  color: #64748b;
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 18px;
}

.form-row a,
.register-link a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 700;
}

.submit-btn {
  width: 100%;
  height: 44px;
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.24);
}

.submit-btn:hover {
  transform: translateY(-1px);
}

.register-link {
  margin-top: 16px;
  text-align: center;
  color: #64748b;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (max-width: 980px) {
  .user-login {
    grid-template-columns: 1fr;
    padding: 36px 18px;
  }

  .shop-copy {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .user-card,
  .floating-product,
  .submit-btn:hover {
    animation: none;
    transform: none;
  }
}
</style>
