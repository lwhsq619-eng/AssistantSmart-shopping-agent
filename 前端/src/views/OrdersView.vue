<template>
  <div>
    <PageBanner title="订单管理" subtitle="管理订单创建、商品数量、收货地址与订单流转状态" :image="orderBanner" />
    <div class="page-card">
      <div class="toolbar">
        <el-form class="search-form" :model="query" inline>
          <el-form-item label="订单编号"><el-input v-model="query.orderNo" placeholder="请输入订单编号" clearable /></el-form-item>
          <el-form-item label="用户名"><el-input v-model="query.userName" placeholder="请输入用户名" clearable /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form>
        <el-button type="primary" @click="openCreate">新增订单</el-button>
      </div>

      <el-table v-loading="loading" :data="records" empty-text="暂无订单数据">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div class="product-info">
              <img class="table-image" loading="lazy" :src="row.productImage || defaultProductImage" @error="imageError" />
              <div>
                <div>{{ row.productName }}</div>
                <div class="muted">￥{{ row.unitPrice }} × {{ row.quantity }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名称" width="110" />
        <el-table-column label="总金额" width="120"><template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template></el-table-column>
        <el-table-column prop="shippingAddress" label="收货地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="changeStatus(row)">
              <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="170" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑订单' : '新增订单'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择用户" filterable style="width: 100%" @change="fillUserAddress">
            <el-option v-for="item in users" :key="item.id" :label="`${item.username}（${item.phone}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option v-for="item in products" :key="item.id" :label="`${item.name} / 库存${item.stock}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="订单状态"><el-select v-model="form.status" style="width: 100%"><el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item label="收货地址"><el-input v-model="form.shippingAddress" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <div class="order-detail">
        <div class="order-head">
          <strong>{{ current.orderNo }}</strong>
          <el-tag :type="statusType(current.status)"><el-icon><CircleCheck /></el-icon>{{ current.status }}</el-tag>
        </div>
        <div class="order-product">
          <img class="dialog-image" :src="current.productImage || defaultProductImage" @error="imageError" />
          <div>
            <h3>{{ current.productName }}</h3>
            <p>单价：￥{{ current.unitPrice }} ｜ 数量：{{ current.quantity }}</p>
            <p class="price">小计：￥{{ current.subtotal }}</p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名称">{{ current.userName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">￥{{ current.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ current.shippingAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ current.orderTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageBanner from '../components/PageBanner.vue'
import { pageOrdersApi, createOrderApi, updateOrderApi, deleteOrderApi, updateOrderStatusApi } from '../api/orders'
import { listUsersApi } from '../api/users'
import { listProductsApi } from '../api/products'
import { defaultProductImage, imageError, orderBanner } from '../utils/images'

const statusOptions = ['待付款', '待发货', '待收货', '已完成', '已取消']
const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const users = ref([])
const products = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref()
const current = ref({})
const query = reactive({ current: 1, size: 10, orderNo: '', userName: '', status: '' })
const form = reactive({ id: null, userId: null, productId: null, quantity: 1, shippingAddress: '', status: '待付款' })
const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageOrdersApi(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadSelects() {
  users.value = await listUsersApi({ status: 1 })
  products.value = await listProductsApi({ status: 1 })
}

function reset() {
  Object.assign(query, { current: 1, size: 10, orderNo: '', userName: '', status: '' })
  load()
}

function openCreate() {
  Object.assign(form, { id: null, userId: null, productId: null, quantity: 1, shippingAddress: '', status: '待付款' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, userId: row.userId, productId: row.productId, quantity: row.quantity, shippingAddress: row.shippingAddress, status: row.status })
  dialogVisible.value = true
}

function view(row) {
  current.value = row
  detailVisible.value = true
}

function fillUserAddress(userId) {
  const user = users.value.find((item) => item.id === userId)
  if (user && !form.shippingAddress) form.shippingAddress = user.address
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    form.id ? await updateOrderApi(form.id, form) : await createOrderApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadSelects()
    load()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row) {
  await updateOrderStatusApi(row.id, row.status)
  ElMessage.success('状态已更新')
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除订单「${row.orderNo}」？`, '删除确认', { type: 'warning' })
  await deleteOrderApi(row.id)
  ElMessage.success('删除成功')
  load()
}

function statusType(status) {
  if (status === '已完成') return 'success'
  if (status === '已取消') return 'danger'
  if (status === '待付款') return 'warning'
  return 'primary'
}

onMounted(() => {
  loadSelects()
  load()
})
</script>

<style scoped>
.order-detail {
  display: grid;
  gap: 18px;
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-product {
  display: flex;
  gap: 16px;
  padding: 14px;
  border-radius: 8px;
  background: #f8fafc;
}
</style>
