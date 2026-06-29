<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { createCompareTask, getCompareTask, getCompareResults, getHealthAdvice } from '@/api/compare'
import { getMyReports } from '@/api/report'
import { ElMessage } from 'element-plus'
import type { ExamReport } from '@/types/api'

const form = ref({ baselineReportNo: '', compareReportNo: '' })
const loading = ref(false)
const compareResult = ref<any>(null)
const results = ref<any[]>([])
const advice = ref<any[]>([])
const reports = ref<ExamReport[]>([])
const mounted = ref(false)
const changeChart = ref<HTMLElement>()
const valueChart = ref<HTMLElement>()
let changeChartInstance: echarts.ECharts | undefined
let valueChartInstance: echarts.ECharts | undefined

onMounted(async () => {
  mounted.value = true
  const res = await getMyReports()
  reports.value = res.data || []
})

function reportLabel(report: ExamReport) {
  const date = report.reportDate || report.publishedAt || report.createdAt || ''
  return `${report.reportNo} / ${report.packageName || '体检报告'} / ${date}`
}

async function handleCompare() {
  if (!form.value.baselineReportNo || !form.value.compareReportNo) {
    ElMessage.warning('请选择两份报告')
    return
  }
  if (form.value.baselineReportNo === form.value.compareReportNo) {
    ElMessage.warning('不能选择同一份报告进行对比')
    return
  }
  loading.value = true
  try {
    const res: any = await createCompareTask(form.value)
    const taskNo = res.data.taskNo || res.data.compareTaskNo
    const [taskRes, resultsRes, adviceRes]: any = await Promise.all([
      getCompareTask(taskNo),
      getCompareResults(taskNo),
      getHealthAdvice(taskNo),
    ])
    compareResult.value = taskRes.data
    results.value = resultsRes.data?.compareResults || resultsRes.data || []
    advice.value = adviceRes.data || []
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function metricName(row: any) {
  return row.itemName || row.metricName || row.itemCode || '-'
}

function baseValue(row: any) {
  return row.baseValue ?? row.baselineValue ?? '-'
}

function changeText(row: any) {
  const value = row.changeRate ?? row.changePercent
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  if (Number.isNaN(number)) return String(value)
  return `${(number * 100).toFixed(2)}%`
}

function trendText(row: any) {
  const trend = row.trend ?? row.changeDirection
  if (trend === 1 || trend === 'UP') return '升高'
  if (trend === 2 || trend === 'DOWN') return '下降'
  if (trend === 0 || trend === 'FLAT') return '持平'
  return trend || '-'
}

function numeric(value: any) {
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

function chartRows() {
  return results.value
    .map((row) => ({
      name: metricName(row),
      base: numeric(row.baseValue ?? row.baselineValue),
      compare: numeric(row.compareValue),
      changeRate: numeric(row.changeRate ?? row.changePercent),
    }))
    .filter((row) => row.base !== null || row.compare !== null || row.changeRate !== null)
}

function renderCharts() {
  const rows = chartRows()
  if (!rows.length) {
    changeChartInstance?.clear()
    valueChartInstance?.clear()
    return
  }
  if (changeChart.value) {
    changeChartInstance = changeChartInstance || echarts.init(changeChart.value)
    changeChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 42, right: 20, top: 36, bottom: 72 },
      xAxis: { type: 'category', data: rows.map((row) => row.name), axisLabel: { rotate: 28 } },
      yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
      series: [{
        name: '变化幅度',
        type: 'bar',
        data: rows.map((row) => row.changeRate === null ? 0 : Number((row.changeRate * 100).toFixed(2))),
        itemStyle: { color: '#6a9a92', borderRadius: [4, 4, 0, 0] },
      }],
    })
  }
  if (valueChart.value) {
    valueChartInstance = valueChartInstance || echarts.init(valueChart.value)
    valueChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['基线值', '对比值'] },
      grid: { left: 42, right: 20, top: 48, bottom: 72 },
      xAxis: { type: 'category', data: rows.map((row) => row.name), axisLabel: { rotate: 28 } },
      yAxis: { type: 'value' },
      series: [
        { name: '基线值', type: 'bar', data: rows.map((row) => row.base), itemStyle: { color: '#9ca3af' } },
        { name: '对比值', type: 'bar', data: rows.map((row) => row.compare), itemStyle: { color: '#c9a44e' } },
      ],
    })
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT COMPARE</span>
        <h2>历年报告对比</h2>
      </div>
      <p>选择两份已发布报告，生成指标变化趋势、风险概览和健康建议。</p>
    </div>

    <div class="compare-form-card glass-panel" :class="{ 'is-mounted': mounted }">
      <el-form :inline="true" :model="form">
        <el-form-item label="基线报告">
          <el-select v-model="form.baselineReportNo" filterable placeholder="请选择报告" style="width: 300px;">
            <el-option
              v-for="report in reports"
              :key="report.reportNo"
              :label="reportLabel(report)"
              :value="report.reportNo"
              :disabled="report.reportNo === form.compareReportNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="对比报告">
          <el-select v-model="form.compareReportNo" filterable placeholder="请选择报告" style="width: 300px;">
            <el-option
              v-for="report in reports"
              :key="report.reportNo"
              :label="reportLabel(report)"
              :value="report.reportNo"
              :disabled="report.reportNo === form.baselineReportNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleCompare">
            开始对比
            <el-icon class="btn-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="compareResult" class="risk-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="risk-item">
        <span>可对比指标</span>
        <strong>{{ results.length }}</strong>
      </div>
      <div class="risk-item">
        <span>风险等级</span>
        <strong>{{ compareResult.riskScore?.riskLevel ?? compareResult.riskLevel ?? '-' }}</strong>
      </div>
      <div class="risk-item">
        <span>健康建议</span>
        <strong>{{ advice.length }}</strong>
      </div>
    </div>

    <div v-if="results.length" class="visual-card glass-panel" :class="{ 'is-mounted': mounted }">
      <h3 class="card-heading">可视化对比</h3>
      <div class="chart-grid">
        <div class="chart-panel"><div ref="changeChart" class="chart-box" /></div>
        <div class="chart-panel"><div ref="valueChart" class="chart-box" /></div>
      </div>
    </div>

    <div v-if="results.length" class="results-card glass-panel" :class="{ 'is-mounted': mounted }">
      <h3 class="card-heading">指标对比结果</h3>
      <el-table :data="results">
        <el-table-column label="指标">
          <template #default="{ row }">{{ metricName(row) }}</template>
        </el-table-column>
        <el-table-column label="基线值">
          <template #default="{ row }">{{ baseValue(row) }}</template>
        </el-table-column>
        <el-table-column prop="compareValue" label="对比值" />
        <el-table-column label="变化趋势">
          <template #default="{ row }">{{ trendText(row) }}</template>
        </el-table-column>
        <el-table-column label="变化幅度">
          <template #default="{ row }">{{ changeText(row) }}</template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="advice.length" class="advice-card glass-panel" :class="{ 'is-mounted': mounted }">
      <h3 class="card-heading">健康建议</h3>
      <div v-for="(item, index) in advice" :key="index" class="advice-item">
        <div class="advice-marker">
          <span class="marker-icon"><el-icon :size="14"><FirstAidKit /></el-icon></span>
        </div>
        <div class="advice-content">
          <h4>{{ item.adviceTitle || '建议' }}</h4>
          <p>{{ item.adviceContent }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.compare-form-card,
.risk-card,
.visual-card,
.results-card,
.advice-card {
  padding: 28px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.risk-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.risk-item {
  padding: 18px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);

  span {
    display: block;
    font-size: 12px;
    color: var(--color-ink-muted);
  }

  strong {
    display: block;
    margin-top: 8px;
    font-family: var(--font-display);
    font-size: 28px;
    color: var(--color-ink);
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-panel {
  min-width: 0;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);
}

.chart-box {
  height: 340px;
}

.card-heading {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-ink);
  margin: 0 0 20px;
}

.advice-item {
  display: flex;
  gap: 16px;
  padding: 18px 0;
  border-bottom: 1px solid var(--color-line);
}

.marker-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--color-accent-light);
  color: var(--color-accent);
}

@media (max-width: 900px) {
  .risk-card,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
