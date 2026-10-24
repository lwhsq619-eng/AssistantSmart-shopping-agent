<template>
  <div class="banner" :style="{ backgroundImage: `url(${image})` }">
    <div class="banner-content">
      <h1 class="banner-title">{{ title }}</h1>
      <p class="banner-subtitle">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, required: true },
  image: { type: String, required: true }
})
</script>
