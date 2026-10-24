<template>
  <div>
    <PageBanner title="商品管理" subtitle="维护商品资料、图片、库存与上下架状态" :image="productBanner" />
    <div class="page-card">
      <div class="toolbar">
        <el-form class="search-form" :model="query" inline>
          <el-form-item label="商品名称"><el-input v-model="query.name" placeholder="请输入商品名称" clearable /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form>
        <el-button type="primary" @click="openCreate">新增商品</el-button>
      </div>

      <el-table v-loading="loading" :data="records" empty-text="暂无商品数据">
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div class="product-info">
              <img class="table-image" loading="lazy" :src="row.imageUrl || defaultProductImage" @error="imageError" />
              <div>
                <div>{{ row.name }}</div>
                <div class="muted text-ellipsis">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="productNo" label="商品编号" width="160" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="价格" width="110"><template #default="{ row }"><span class="price">￥{{ row.price }}</span></template></el-table-column>
        <el-table-column label="库存" width="110">
          <template #default="{ row }">
            <el-tag :type="row.stock < 10 ? 'danger' : 'success'">{{ row.stock }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="changeStatus(row)" />
            <el-tag class="status-tag" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品编号"><el-input v-model="form.productNo" placeholder="不填自动生成" /></el-form-item>
        <el-form-item label="商品名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片地址"><el-input v-model="form.imageUrl" placeholder="可填写商品图片 URL" /></el-form-item>
        <el-form-item label="商品价格" prop="price"><el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="库存数量" prop="stock"><el-input-number v-model="form.stock" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="上架状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="商品描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="商品详情" width="520px">
      <div class="detail">
        <img class="dialog-image" :src="current.imageUrl || defaultProductImage" @error="imageError" />
        <div>
          <h3>{{ current.name }}</h3>
          <p class="price">￥{{ current.price }}</p>
          <p>库存：{{ current.stock }} ｜ 分类：{{ current.categoryName }}</p>
          <p class="muted">{{ current.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageBanner from '../components/PageBanner.vue'
import { pageProductsApi, createProductApi, updateProductApi, deleteProductApi, updateProductStatusApi } from '../api/products'
import { listCategoriesApi } from '../api/categories'
import { defaultProductImage, imageError, productBanner } from '../utils/images'

const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const categories = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const current = ref({})
const query = reactive({ current: 1, size: 10, name: '', categoryId: null, status: null })
const form = reactive({ id: null, productNo: '', name: '', categoryId: null, imageUrl: '', price: 0.01, stock: 0, description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageProductsApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await listCategoriesApi({ status: 1 })
}

function reset() {
  Object.assign(query, { current: 1, size: 10, name: '', categoryId: null, status: null })
  load()
}

function openCreate() {
  Object.assign(form, { id: null, productNo: '', name: '', categoryId: null, imageUrl: '', price: 0.01, stock: 0, description: '', status: 1 })
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
    form.id ? await updateProductApi(form.id, form) : await createProductApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row) {
  await updateProductStatusApi(row.id, row.status)
  ElMessage.success('状态已更新')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '删除确认', { type: 'warning' })
  await deleteProductApi(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.status-tag {
  margin-left: 8px;
}

.detail {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}
</style>
