<template>
  <div class="captcha-input">
    <el-input
      :model-value="modelValue"
      placeholder="请输入验证码"
      maxlength="4"
      clearable
      @update:model-value="$emit('update:modelValue', $event)"
      @keyup.enter="$emit('enter')"
    >
      <template #prefix>
        <el-icon><Key /></el-icon>
      </template>
    </el-input>
    <button class="captcha-image" type="button" :class="{ loading }" title="点击刷新验证码" @click="loadCaptcha">
      <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
      <span v-else>刷新</span>
    </button>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCaptchaApi } from '../api/captcha'

defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'update:captchaId', 'enter'])
const captchaImage = ref('')
const loading = ref(false)

async function loadCaptcha() {
  loading.value = true
  try {
    const data = await getCaptchaApi()
    captchaImage.value = data.captchaImage
    emit('update:captchaId', data.captchaId)
    emit('update:modelValue', '')
  } catch (error) {
    ElMessage.error(error.message || '验证码加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
defineExpose({ refresh: loadCaptcha })
</script>

<style scoped>
.captcha-input {
  display: grid;
  grid-template-columns: 1fr 132px;
  gap: 10px;
  align-items: center;
}

.captcha-image {
  height: 38px;
  border: 1px solid rgba(59, 130, 246, 0.24);
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
  background: #f8fafc;
  cursor: pointer;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.captcha-image.loading {
  animation: captchaSpin 0.45s ease;
}

@keyframes captchaSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .captcha-image.loading {
    animation: none;
  }
}
</style>
