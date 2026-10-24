<template>
  <div class="checkout">
    <section class="page-card">
      <h2>选择收货地址</h2>
      <el-radio-group v-model="addressId" class="address-grid">
        <el-radio v-for="a in addresses" :key="a.id" :label="a.id" border>
          {{ a.receiverName }} {{ a.receiverPhone }} - {{ a.province }}{{ a.city }}{{ a.detailAddress }}
        </el-radio>
      </el-radio-group>
      <el-button type="primary" plain @click="dialogVisible = true">新增地址</el-button>
    </section>

    <section class="page-card">
      <h2>结算商品</h2>
      <el-table :data="items">
        <el-table-column label="商品" prop="productName" />
        <el-table-column label="规格" prop="specInfo" />
        <el-table-column label="数量" prop="quantity" />
        <el-table-column label="金额"><template #default="{ row }">￥{{ money(row.price * row.quantity) }}</template></el-table-column>
      </el-table>
      <div class="summary">
        <span>商品总价 ￥{{ money(total) }}</span>
        <span>优惠 ￥{{ money(discount) }}</span>
        <span>运费 ￥{{ money(freight) }}</span>
        <b>应付 ￥{{ money(payAmount) }}</b>
        <el-select v-model="paymentMethod"><el-option v-for="m in methods" :key="m" :label="m" :value="m" /></el-select>
        <el-button type="primary" :disabled="!addressId || items.length === 0" @click="submit">提交订单</el-button>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="新增收货地址" width="520px">
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.receiverPhone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="addressForm.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="addressForm.city" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detailAddress" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="saveAddress">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createAddressApi, createOrderApi, getAddressesApi, getCartApi, getProductDetailApi } from '../../api/mall'

const route = useRoute()
const router = useRouter()
const addresses = ref([])
const addressId = ref()
const items = ref([])
const dialogVisible = ref(false)
const paymentMethod = ref('余额模拟支付')
const methods = ['支付宝模拟支付', '微信模拟支付', '银行卡模拟支付', '余额模拟支付']
const addressForm = reactive({ receiverName: '', receiverPhone: '', province: '广东省', city: '广州市', detailAddress: '', isDefault: 0 })

const total = computed(() => items.value.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0))
const discount = computed(() => total.value >= 300 ? 10 : 0)
const freight = computed(() => total.value >= 199 || total.value === 0 ? 0 : 8)
const payAmount = computed(() => total.value - discount.value + freight.value)
function money(v) { return Number(v || 0).toFixed(2) }

async function load() {
  addresses.value = await getAddressesApi()
  addressId.value = addresses.value.find(a => a.isDefault === 1)?.id || addresses.value[0]?.id
  if (route.query.productId) {
    const detail = await getProductDetailApi(route.query.productId)
    const p = detail.product
    items.value = [{ productId: p.id, productName: p.name, specInfo: route.query.specInfo || '默认规格', quantity: Number(route.query.quantity || 1), price: p.discountPrice || p.price }]
  } else {
    const ids = String(route.query.cartItemIds || '').split(',').filter(Boolean).map(Number)
    items.value = (await getCartApi()).filter(item => ids.includes(item.id))
  }
}

async function saveAddress() {
  await createAddressApi(addressForm)
  dialogVisible.value = false
  await load()
}

async function submit() {
  const payload = { addressId: addressId.value, paymentMethod: paymentMethod.value }
  if (route.query.productId) Object.assign(payload, { productId: Number(route.query.productId), specInfo: route.query.specInfo, quantity: Number(route.query.quantity || 1) })
  else payload.cartItemIds = String(route.query.cartItemIds || '').split(',').filter(Boolean).map(Number)
  const data = await createOrderApi(payload)
  router.replace(`/user/payment/${data.order.orderNo}`)
}

onMounted(load)
</script>

<style scoped>
.checkout { display: flex; flex-direction: column; gap: 18px; }
.address-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 12px; }
.summary { margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; gap: 16px; }
.summary b { color: #f59e0b; font-size: 20px; }
</style>
