<template>
  <div class="forbidden">
    <el-result icon="warning" title="403 权限不足" sub-title="当前账号无权访问该页面">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.forbidden {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f4f7fb;
}
</style>
