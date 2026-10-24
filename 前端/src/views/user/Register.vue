<template>
  <div class="register-page">
    <AnimatedBackground variant="register" />
    <el-card class="register-card" shadow="never">
      <div class="title">
        <div class="logo"><el-icon><ShoppingCart /></el-icon></div>
        <h1>创建您的购物账户</h1>
        <p>注册后即可浏览商品、下单并查看订单</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-20位字母、数字或下划线" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="6-20位密码" show-password type="password" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" placeholder="请再次输入密码" show-password type="password" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入默认收货地址" />
        </el-form-item>
        <el-form-item class="full" label="图形验证码" prop="captchaCode">
          <CaptchaInput ref="captchaRef" v-model="form.captchaCode" v-model:captcha-id="form.captchaId" @enter="submit" />
        </el-form-item>
        <el-form-item class="full" prop="agreement">
          <el-checkbox v-model="form.agreement">我已阅读并同意用户协议</el-checkbox>
        </el-form-item>
        <el-button class="submit-btn full" type="primary" size="large" :loading="loading" @click="submit">注册</el-button>
        <div class="login-link full">已有账号？<router-link to="/login/user">立即登录</router-link></div>
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
  confirmPassword: '',
  phone: '',
  email: '',
  address: '',
  captchaId: '',
  captchaCode: '',
  agreement: false
})

const validateConfirm = (_, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^\w{3,20}$/, message: '用户名必须为3-20位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式错误', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  agreement: [
    {
      validator: (_, value, callback) => {
        if (!value) callback(new Error('请先同意用户协议'))
        else callback()
      },
      trigger: 'change'
    }
  ]
}

async function submit() {
  if (loading.value) return
  loading.value = true
  try {
    await formRef.value.validate()
    await auth.register({ ...form })
    ElMessage.success('注册成功，请登录')
    router.replace('/login/user')
  } catch (error) {
    captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 38px 18px;
}

.register-card {
  position: relative;
  z-index: 1;
  width: min(820px, 100%);
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(18px);
  box-shadow: 0 30px 80px rgba(37, 99, 235, 0.22);
  animation: cardIn 0.55s ease both;
}

.register-card :deep(.el-card__body) {
  padding: 34px;
}

.title {
  text-align: center;
  margin-bottom: 22px;
}

.logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  color: #fff;
  font-size: 24px;
  background: linear-gradient(135deg, #2563eb, #f59e0b);
}

.title h1 {
  margin: 0;
  font-size: 28px;
}

.title p {
  margin: 8px 0 0;
  color: #64748b;
}

.register-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 16px;
}

.full {
  grid-column: 1 / -1;
}

.submit-btn {
  height: 44px;
}

.login-link {
  text-align: center;
  color: #64748b;
}

.login-link a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 700;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 760px) {
  .register-form {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .register-card {
    animation: none;
  }
}
</style>
