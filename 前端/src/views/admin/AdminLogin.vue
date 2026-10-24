<template>
  <div class="admin-login">
    <AnimatedBackground variant="admin" />
    <section class="admin-copy">
      <div class="badge"><el-icon><DataLine /></el-icon> 电商运营数据中心</div>
      <h1>网上商城购物系统</h1>
      <p>统一管理商品、订单、用户与库存状态，实时掌握商城经营数据。</p>
    </section>

    <el-card class="login-card" shadow="never">
      <div class="card-head">
        <div class="logo"><el-icon><Monitor /></el-icon></div>
        <div>
          <h2>网上商城购物系统</h2>
          <p>运营管理后台</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" size="large">
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
          <el-checkbox v-model="form.remember">记住登录状态</el-checkbox>
          <router-link to="/login/user">返回用户登录入口</router-link>
        </div>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="submit">登录后台</el-button>
        <p class="hint">默认管理员：admin / 123456</p>
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
  username: 'admin',
  password: '123456',
  captchaId: '',
  captchaCode: '',
  remember: true
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function submit() {
  if (loading.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    await auth.adminLogin({ ...form })
    ElMessage.success('管理员登录成功')
    router.replace('/admin/dashboard')
  } catch (error) {
    captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(420px, 1fr) 460px;
  align-items: center;
  gap: 56px;
  padding: 64px 9vw;
}

.admin-copy,
.login-card {
  position: relative;
  z-index: 1;
}

.admin-copy {
  color: #fff;
  max-width: 620px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.13);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
}

.admin-copy h1 {
  margin: 26px 0 14px;
  font-size: 48px;
  letter-spacing: 0;
}

.admin-copy p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 18px;
  line-height: 1.8;
}

.login-card {
  border: 1px solid rgba(147, 197, 253, 0.26);
  border-radius: 16px;
  padding: 14px;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(18px);
  box-shadow: 0 28px 80px rgba(2, 6, 23, 0.42);
  animation: cardIn 0.55s ease both;
}

.login-card :deep(.el-card__body) {
  padding: 22px;
}

.login-card :deep(.el-form-item__label),
.login-card :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.82);
}

.login-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.93);
}

.card-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
  color: #fff;
}

.logo {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #f59e0b;
  background: rgba(37, 99, 235, 0.2);
  border: 1px solid rgba(96, 165, 250, 0.35);
}

.card-head h2 {
  margin: 0;
  font-size: 22px;
}

.card-head p {
  margin: 6px 0 0;
  color: rgba(255, 255, 255, 0.62);
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 18px;
}

.form-row a {
  color: #93c5fd;
  text-decoration: none;
}

.submit-btn {
  width: 100%;
  height: 44px;
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.32);
}

.submit-btn:hover {
  transform: translateY(-1px);
}

.hint {
  margin: 14px 0 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.46);
  font-size: 13px;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 980px) {
  .admin-login {
    grid-template-columns: 1fr;
    padding: 36px 18px;
  }

  .admin-copy {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-card,
  .submit-btn:hover {
    animation: none;
    transform: none;
  }
}
</style>
