<template>
  <div class="gift-page">
    <section class="gift-hero"><h1>礼品卡专区</h1><p>模拟支付后生成兑换码，可在我的礼品卡中查看。</p></section>
    <div class="gift-grid">
      <article v-for="card in cards" :key="card.id" class="gift-card">
        <img :src="card.imageUrl" />
        <h3>{{ card.name }}</h3>
        <p>{{ card.description }}</p>
        <b>￥{{ Number(card.amount).toFixed(2) }}</b>
        <el-button type="warning" @click="buy(card)">购买</el-button>
      </article>
    </div>
    <section class="page-card">
      <h2>我的礼品卡</h2>
      <el-table :data="mine">
        <el-table-column prop="giftCardName" label="名称" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="redeemCode" label="兑换码" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </section>
    <el-dialog v-model="dialog" title="购买礼品卡" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="赠送对象"><el-input v-model="form.recipient" /></el-form-item>
        <el-form-item label="祝福语"><el-input v-model="form.blessing" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="submit">模拟支付购买</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGiftCardsApi, getMyGiftCardsApi, purchaseGiftCardApi } from '../../api/mall'

const cards = ref([])
const mine = ref([])
const dialog = ref(false)
const form = reactive({ giftCardId: null, recipient: '', blessing: '' })
async function load() { cards.value = await getGiftCardsApi(); mine.value = await getMyGiftCardsApi() }
function buy(card) { form.giftCardId = card.id; form.recipient = ''; form.blessing = '愿你每天都有好心情'; dialog.value = true }
async function submit() { const data = await purchaseGiftCardApi(form); ElMessage.success(`购买成功，兑换码：${data.redeemCode}`); dialog.value = false; await load() }
onMounted(load)
</script>

<style scoped>
.gift-page { display: flex; flex-direction: column; gap: 18px; }
.gift-hero { border-radius: 12px; padding: 32px; color: #fff; background: linear-gradient(90deg, rgba(15,23,42,.72), rgba(245,158,11,.25)), url('https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=1600&q=80') center/cover; }
.gift-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; }
.gift-card { padding: 14px; border-radius: 12px; background: #fff; box-shadow: 0 12px 28px rgba(15,23,42,.06); }
.gift-card img { width: 100%; height: 140px; object-fit: cover; border-radius: 10px; }
.gift-card b { display: block; color: #f59e0b; font-size: 22px; margin: 10px 0; }
</style>
