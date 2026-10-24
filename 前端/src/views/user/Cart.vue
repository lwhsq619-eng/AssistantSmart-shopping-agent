<template>
  <div class="page-card cart-page">
    <h2>购物车</h2>
    <el-empty v-if="items.length === 0" description="购物车为空" />
    <el-table v-else :data="items">
      <el-table-column width="54">
        <template #default="{ row }"><el-checkbox :model-value="row.selected === 1" @change="toggle(row, $event)" /></template>
      </el-table-column>
      <el-table-column label="商品">
        <template #default="{ row }">
          <div class="product-line"><img :src="row.productImage" /><div><b>{{ row.productName }}</b><p>{{ row.specInfo }}</p></div></div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120"><template #default="{ row }"><span class="price">￥{{ money(row.price) }}</span></template></el-table-column>
      <el-table-column label="数量" width="160"><template #default="{ row }"><el-input-number :model-value="row.quantity" :min="1" @change="qty(row, $event)" /></template></el-table-column>
      <el-table-column label="小计" width="120"><template #default="{ row }">￥{{ money(row.price * row.quantity) }}</template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button type="danger" link @click="remove(row.id)">删除</el-button></template></el-table-column>
    </el-table>
    <div class="summary">
      <span>已选 {{ selected.length }} 件</span>
      <span>优惠 ￥{{ money(discount) }}</span>
      <span>运费 ￥{{ money(freight) }}</span>
      <b>应付 ￥{{ money(payAmount) }}</b>
      <el-button type="primary" :disabled="selected.length === 0" @click="checkout">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deleteCartApi, getCartApi, updateCartApi } from '../../api/mall'

const router = useRouter()
const items = ref([])
const selected = computed(() => items.value.filter(i => i.selected === 1))
const total = computed(() => selected.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0))
const discount = computed(() => total.value >= 300 ? 10 : 0)
const freight = computed(() => total.value >= 199 || total.value === 0 ? 0 : 8)
const payAmount = computed(() => total.value - discount.value + freight.value)

function money(v) { return Number(v || 0).toFixed(2) }
async function load() { items.value = await getCartApi() }
async function toggle(row, value) { await updateCartApi(row.id, { selected: value ? 1 : 0 }); await load() }
async function qty(row, value) { await updateCartApi(row.id, { quantity: value }); await load() }
async function remove(id) { await deleteCartApi(id); ElMessage.success('已删除'); await load() }
function checkout() { router.push({ path: '/user/checkout', query: { cartItemIds: selected.value.map(i => i.id).join(',') } }) }
onMounted(load)
</script>

<style scoped>
.cart-page { display: flex; flex-direction: column; gap: 16px; }
.product-line { display: flex; gap: 12px; align-items: center; }
.product-line img { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; }
.product-line p { margin: 6px 0 0; color: #64748b; }
.price, .summary b { color: #f59e0b; font-weight: 900; }
.summary { display: flex; justify-content: flex-end; align-items: center; gap: 20px; }
</style>
