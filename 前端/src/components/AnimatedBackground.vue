<template>
  <div class="animated-bg" :class="variant" :style="bgStyle">
    <div class="overlay"></div>
    <div class="glow glow-one"></div>
    <div class="glow glow-two"></div>
    <div class="grid"></div>
    <div class="float-card card-one">订单 +18%</div>
    <div class="float-card card-two">库存预警</div>
    <div class="float-card card-three">热销商品</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { adminLoginBg, userLoginBg, registerBg } from '../utils/images'

const props = defineProps({
  variant: {
    type: String,
    default: 'user'
  }
})

const bgStyle = computed(() => {
  const map = {
    admin: adminLoginBg,
    user: userLoginBg,
    register: registerBg
  }
  return { backgroundImage: `url(${map[props.variant] || userLoginBg})` }
})
</script>

<style scoped>
.animated-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  animation: slowPan 18s ease-in-out infinite alternate;
}

.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(15, 23, 42, 0.72), rgba(37, 99, 235, 0.18));
}

.admin .overlay {
  background:
    radial-gradient(circle at 20% 15%, rgba(79, 70, 229, 0.22), transparent 30%),
    linear-gradient(120deg, rgba(2, 6, 23, 0.9), rgba(30, 41, 59, 0.82));
}

.register .overlay,
.user .overlay {
  background: linear-gradient(120deg, rgba(37, 99, 235, 0.62), rgba(124, 58, 237, 0.26));
}

.grid {
  position: absolute;
  inset: 0;
  opacity: 0.22;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.12) 1px, transparent 1px);
  background-size: 46px 46px;
  animation: gridMove 22s linear infinite;
}

.glow {
  position: absolute;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  filter: blur(18px);
  opacity: 0.34;
}

.glow-one {
  left: 8%;
  top: 14%;
  background: #60a5fa;
  animation: glowMove 12s ease-in-out infinite alternate;
}

.glow-two {
  right: 16%;
  bottom: 18%;
  background: #f59e0b;
  animation: glowMove 15s ease-in-out infinite alternate-reverse;
}

.float-card {
  position: absolute;
  min-width: 112px;
  padding: 12px 14px;
  border-radius: 12px;
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.22);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.24);
  backdrop-filter: blur(12px);
  font-weight: 700;
  animation: floatY 5s ease-in-out infinite;
}

.card-one { left: 13%; top: 22%; }
.card-two { left: 24%; bottom: 18%; animation-delay: 0.6s; }
.card-three { right: 14%; top: 18%; animation-delay: 1.1s; }

@keyframes slowPan {
  from { transform: scale(1); }
  to { transform: scale(1.04) translate3d(-10px, -8px, 0); }
}

@keyframes gridMove {
  from { background-position: 0 0; }
  to { background-position: 92px 46px; }
}

@keyframes glowMove {
  from { transform: translate3d(0, 0, 0); }
  to { transform: translate3d(30px, 18px, 0); }
}

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

@media (prefers-reduced-motion: reduce) {
  .animated-bg,
  .grid,
  .glow,
  .float-card {
    animation: none;
  }
}
</style>
