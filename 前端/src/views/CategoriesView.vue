<template>
  <div>
    <PageBanner title="商品分类管理" subtitle="管理商品分类、封面图片、排序与启用状态" :image="categoryBanner" />
    <div class="category-cards">
      <div v-for="item in records.slice(0, 6)" :key="item.id" class="category-card">
        <img :src="item.imageUrl || defaultProductImage" @error="imageError" />
        <div>
          <strong>{{ item.name }}</strong>
          <p>{{ item.description }}</p>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-form class="search-form" :model="query" inline>
          <el-form-item label="分类名称"><el-input v-model="query.name" placeholder="请输入分类名称" clearable /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form>
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="records" empty-text="暂无分类数据">
        <el-table-column label="分类信息" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <img class="table-image" :src="row.imageUrl || defaultProductImage" @error="imageError" />
              <div>
                <div>{{ row.name }}</div>
                <div class="muted text-ellipsis">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="分类编号" width="100" />
        <el-table-column prop="sortNo" label="排序号" width="100" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="changeStatus(row)" />
            <el-tag class="status-tag" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="load" @current-change="load" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="封面图片"><el-input v-model="form.imageUrl" placeholder="填写分类封面 URL" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortNo" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="分类描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageBanner from '../components/PageBanner.vue'
import { pageCategoriesApi, createCategoryApi, updateCategoryApi, deleteCategoryApi, updateCategoryStatusApi } from '../api/categories'
import { categoryBanner, defaultProductImage, imageError } from '../utils/images'

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ current: 1, size: 10, name: '', status: null })
const form = reactive({ id: null, name: '', description: '', sortNo: 0, status: 1, imageUrl: '' })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try {
    const data = await pageCategoriesApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, { current: 1, size: 10, name: '', status: null })
  load()
}

function openCreate() {
  Object.assign(form, { id: null, name: '', description: '', sortNo: 0, status: 1, imageUrl: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    form.id ? await updateCategoryApi(form.id, form) : await createCategoryApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row) {
  await updateCategoryStatusApi(row.id, row.status)
  ElMessage.success('状态已更新')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '删除确认', { type: 'warning' })
  await deleteCategoryApi(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.category-cards {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.category-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.category-card img {
  width: 100%;
  height: 92px;
  object-fit: cover;
}

.category-card div {
  padding: 10px;
}

.category-card p {
  margin: 6px 0 0;
  height: 34px;
  color: #64748b;
  font-size: 12px;
  overflow: hidden;
}

.status-tag {
  margin-left: 8px;
}
</style>
