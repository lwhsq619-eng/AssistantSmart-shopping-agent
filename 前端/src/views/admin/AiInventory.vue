<template>
  <div class="ai-page">
    <AiPageHeader
      title="仓储 AI 工作台"
      subtitle="用于库存销售分析、补货建议和售后工单智能分拣。AI 只生成建议，不自动修改订单或库存。"
      tag="Warehouse AI"
    />

    <el-tabs v-model="active" class="ai-panel">
      <el-tab-pane label="统一工作台" name="chat">
        <AiChatBox
          :messages="messages"
          :loading="chatLoading"
          :quick-questions="['分析最近库存不足的商品', '商品1001需要补货吗', '帮我判断售后工单类型']"
          placeholder="例如：分析一下最近库存不足的商品"
          @send="sendWorkbench"
          @clear="messages = []"
        />
      </el-tab-pane>

      <el-tab-pane label="库存分析" name="inventory">
        <el-form :model="inventoryForm" label-width="110px" class="ai-form">
          <el-form-item label="商品ID"><el-input v-model="inventoryForm.productId" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="analyzeInventory">生成库存报告</el-button></el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="售后分拣" name="afterSale">
        <el-form :model="afterSaleForm" label-width="110px" class="ai-form">
          <el-form-item label="订单ID"><el-input v-model="afterSaleForm.orderId" /></el-form-item>
          <el-form-item label="问题描述"><el-input v-model="afterSaleForm.message" type="textarea" :rows="3" /></el-form-item>
          <el-form-item><el-button type="primary" :loading="loading" @click="classifyAfterSale">分拣工单</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <AiReportViewer
      v-if="lastResult"
      title="仓储 AI 输出"
      :markdown="reportMarkdown"
      :metric-summary="metricSummary"
      :sections="sections"
    />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AiPageHeader from '../../components/ai/AiPageHeader.vue'
import AiChatBox from '../../components/ai/AiChatBox.vue'
import AiReportViewer from '../../components/ai/AiReportViewer.vue'
import { afterSaleClassify, aiWorkbenchChat, inventoryAnalysis } from '../../api/ai'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const active = ref('chat')
const loading = ref(false)
const chatLoading = ref(false)
const messages = ref([])
const lastResult = ref(null)
const inventoryForm = reactive({ productId: '1001' })
const afterSaleForm = reactive({ orderId: '10001', message: '收到商品后发现包装破损，想换一个' })

const reportMarkdown = computed(() => lastResult.value?.reportMarkdown || lastResult.value?.data?.reportMarkdown || JSON.stringify(lastResult.value || '', null, 2))
const metricSummary = computed(() => lastResult.value?.metricSummary || lastResult.value?.data?.metricSummary || null)
const sections = computed(() => lastResult.value?.sections || lastResult.value?.data?.sections || [])

async function sendWorkbench(message) {
  messages.value.push({ id: `u-${Date.now()}`, role: 'user', content: message })
  chatLoading.value = true
  try {
    const data = await aiWorkbenchChat({ userId: auth.user?.id || 1, role: 'WAREHOUSE', sessionId: 'warehouse-chat', message })
    lastResult.value = data
    messages.value.push({ id: `a-${Date.now()}`, role: 'assistant', content: data.reply || data.message || 'AI 已返回结果。' })
  } catch (error) {
    messages.value.push({ id: `e-${Date.now()}`, role: 'assistant', content: error.message || 'AI 任务失败', error: true })
  } finally {
    chatLoading.value = false
  }
}

async function analyzeInventory() {
  await run(async () => {
    const end = new Date().toISOString().slice(0, 10)
    const start = new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 10)
    lastResult.value = await inventoryAnalysis({
      startDate: start,
      endDate: end,
      productIds: [Number(inventoryForm.productId)],
      categoryId: null,
      analysisTypes: ['FULL_REPORT']
    })
  })
}

async function classifyAfterSale() {
  await run(async () => {
    lastResult.value = await afterSaleClassify({
      userId: auth.user?.id || 1,
      orderId: Number(afterSaleForm.orderId),
      message: afterSaleForm.message,
      images: []
    })
  })
}

async function run(task) {
  loading.value = true
  try {
    await task()
    ElMessage.success('AI 任务完成')
  } catch (error) {
    ElMessage.error(error.message || 'AI 任务失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-form {
  max-width: 620px;
}
</style>
