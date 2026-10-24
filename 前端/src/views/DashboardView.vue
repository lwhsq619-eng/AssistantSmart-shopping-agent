<template>
  <div v-loading="loading">
    <PageBanner title="欢迎进入网上商城购物系统" subtitle="实时掌握商品、订单与用户运营数据" :image="dashboardBanner" />

    <div class="stats-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :class="card.type"><el-icon><component :is="card.icon" /></el-icon></div>
        <div>
          <div class="muted">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="page-card chart-card">
        <div class="section-title">近 7 天订单趋势</div>
        <div ref="lineRef" class="chart"></div>
      </div>
      <div class="page-card chart-card">
        <div class="section-title">商品分类占比</div>
        <div ref="pieRef" class="chart"></div>
      </div>
    </div>

    <div class="lists-grid">
      <div class="page-card">
        <div class="section-title">最新订单</div>
        <el-table :data="dashboard.latestOrders || []" empty-text="暂无订单">
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="product-info">
                <img class="table-image" :src="row.productImage || defaultProductImage" @error="imageError" />
                <div>
                  <div>{{ row.productName }}</div>
                  <div class="muted">{{ row.orderNo }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="userName" label="用户" width="100" />
          <el-table-column label="金额" width="110">
            <template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
        </el-table>
      </div>
      <div class="page-card low-card">
        <div class="section-title">库存预警</div>
        <div v-if="!dashboard.lowStockProducts?.length" class="empty-tip">暂无低库存商品</div>
        <div v-for="item in dashboard.lowStockProducts" :key="item.id" class="low-item">
          <img class="table-image" :src="item.imageUrl || defaultProductImage" @error="imageError" />
          <div class="low-content">
            <div>{{ item.name }}</div>
            <el-tag type="danger">库存 {{ item.stock }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import PageBanner from '../components/PageBanner.vue'
import { getDashboardApi } from '../api/dashboard'
import { dashboardBanner, defaultProductImage, imageError } from '../utils/images'

const loading = ref(false)
const dashboard = ref({})
const lineRef = ref()
const pieRef = ref()
let lineChart
let pieChart

const statCards = computed(() => [
  { label: '商品总数', value: dashboard.value.productTotal || 0, icon: 'Goods', type: 'blue' },
  { label: '分类总数', value: dashboard.value.categoryTotal || 0, icon: 'Grid', type: 'green' },
  { label: '用户总数', value: dashboard.value.userTotal || 0, icon: 'User', type: 'orange' },
  { label: '订单总数', value: dashboard.value.orderTotal || 0, icon: 'Tickets', type: 'red' },
  { label: '今日订单数', value: dashboard.value.todayOrderTotal || 0, icon: 'TrendCharts', type: 'blue' },
  { label: '今日销售额', value: `￥${dashboard.value.todaySales || 0}`, icon: 'Money', type: 'orange' }
])

async function load() {
  loading.value = true
  try {
    dashboard.value = await getDashboardApi()
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  lineChart = echarts.init(lineRef.value)
  pieChart = echarts.init(pieRef.value)
  const trend = dashboard.value.lastSevenDaysOrders || []
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 36, right: 20, top: 28, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((i) => i.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ data: trend.map((i) => i.count), type: 'line', smooth: true, areaStyle: {}, itemStyle: { color: '#2563eb' } }]
  })
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['42%', '70%'], data: dashboard.value.categoryRatio || [] }]
  })
}

function resizeCharts() {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', resizeCharts)
})
onUnmounted(() => window.removeEventListener('resize', resizeCharts))
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  min-height: 100px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.stat-icon {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 24px;
}

.stat-icon.blue { background: #2563eb; }
.stat-icon.green { background: #16a34a; }
.stat-icon.orange { background: #f59e0b; }
.stat-icon.red { background: #ef4444; }

.stat-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 800;
}

.charts-grid,
.lists-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 18px;
  margin-bottom: 18px;
}

.chart {
  height: 320px;
}

.section-title {
  margin-bottom: 14px;
  font-weight: 800;
}

.low-card {
  background:
    linear-gradient(rgba(255,255,255,0.92), rgba(255,255,255,0.92)),
    url("https://images.unsplash.com/photo-1566576721346-d4a3b4eaeb55?auto=format&fit=crop&w=900&q=80") center/cover;
}

.low-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #edf2f7;
}

.low-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.empty-tip {
  color: #94a3b8;
  text-align: center;
  padding: 48px 0;
}
</style>
