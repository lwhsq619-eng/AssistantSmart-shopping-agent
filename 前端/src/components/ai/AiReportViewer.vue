<template>
  <section class="ai-report-panel">
    <div class="report-head">
      <h3>{{ title }}</h3>
      <div class="actions">
        <el-button size="small" plain @click="copyReport">复制报告</el-button>
        <el-button size="small" type="primary" plain @click="downloadReport">导出文本</el-button>
      </div>
    </div>
    <div v-if="metrics.length" class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
    <div v-if="sections.length" class="section-list">
      <article v-for="section in sections" :key="section.sectionType || section.title">
        <h4>{{ section.title || section.sectionType }}</h4>
        <p>{{ section.content }}</p>
      </article>
    </div>
    <div class="ai-markdown" v-html="html"></div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  title: { type: String, default: 'AI 报告' },
  markdown: { type: String, default: '' },
  metricSummary: { type: Object, default: () => ({}) },
  sections: { type: Array, default: () => [] }
})

const metrics = computed(() => Object.entries(props.metricSummary || {}).map(([label, value]) => ({ label, value })))
const html = computed(() => renderMarkdown(props.markdown || '暂无报告内容。'))

async function copyReport() {
  await navigator.clipboard.writeText(props.markdown || '')
  ElMessage.success('报告已复制')
}

function downloadReport() {
  const blob = new Blob([props.markdown || ''], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'ai-report.txt'
  a.click()
  URL.revokeObjectURL(url)
}

function renderMarkdown(text) {
  return escapeHtml(text)
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/\n/g, '<br>')
}

function escapeHtml(text) {
  return String(text).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}
</script>

<style scoped>
.report-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.report-head h3 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 8px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.metric-card {
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.metric-card span {
  display: block;
  color: #64748b;
  margin-bottom: 6px;
}

.metric-card strong {
  color: #2563eb;
  font-size: 20px;
}

.section-list {
  display: grid;
  gap: 10px;
  margin-bottom: 18px;
}

.section-list article {
  padding: 12px;
  border-radius: 10px;
  background: #f9fafb;
}

.section-list h4,
.section-list p {
  margin: 0;
}

.section-list p {
  margin-top: 6px;
  color: #475569;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .report-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
