<template>
  <div>
    <PageBanner title="用户管理" subtitle="维护商城用户资料、联系方式、收货地址与账号状态" :image="userBanner" />
    <div class="page-card">
      <div class="toolbar">
        <el-form class="search-form" :model="query" inline>
          <el-form-item label="用户名"><el-input v-model="query.username" placeholder="请输入用户名" clearable /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="query.phone" placeholder="请输入手机号" clearable /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>

      <el-table v-loading="loading" :data="records" empty-text="暂无用户数据">
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="product-info">
              <el-avatar :size="44" :src="row.avatarUrl || defaultAvatar" @error="() => true" />
              <div>
                <div>{{ row.username }}</div>
                <div class="muted">编号：{{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="190" />
        <el-table-column prop="address" label="收货地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="changeStatus(row)" />
            <el-tag class="status-tag" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="view(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="load" @current-change="load" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="头像地址"><el-input v-model="form.avatarUrl" placeholder="填写头像 URL" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="form.address" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="用户状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="用户详情" width="460px">
      <div class="user-detail">
        <el-avatar :size="76" :src="current.avatarUrl || defaultAvatar" />
        <h3>{{ current.username }}</h3>
        <p>{{ current.phone }} ｜ {{ current.email }}</p>
        <p>{{ current.address }}</p>
        <p class="muted">注册时间：{{ current.registerTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageBanner from '../components/PageBanner.vue'
import { pageUsersApi, createUserApi, updateUserApi, deleteUserApi, updateUserStatusApi } from '../api/users'
import { defaultAvatar, userBanner } from '../utils/images'

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const current = ref({})
const query = reactive({ current: 1, size: 10, username: '', phone: '', status: null })
const form = reactive({ id: null, username: '', password: '', phone: '', email: '', address: '', status: 1, avatarUrl: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式错误', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageUsersApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { current: 1, size: 10, username: '', phone: '', status: null })
  load()
}

function openCreate() {
  Object.assign(form, { id: null, username: '', password: '', phone: '', email: '', address: '', status: 1, avatarUrl: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

function view(row) {
  current.value = row
  detailVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    form.id ? await updateUserApi(form.id, form) : await createUserApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row) {
  await updateUserStatusApi(row.id, row.status)
  ElMessage.success('状态已更新')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '删除确认', { type: 'warning' })
  await deleteUserApi(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.status-tag {
  margin-left: 8px;
}

.user-detail {
  text-align: center;
}

.user-detail p {
  color: #475569;
}
</style>
