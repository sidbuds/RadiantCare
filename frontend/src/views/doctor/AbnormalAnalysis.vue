<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAbnormalOverview, getAbnormalDistribution, getHighRiskUsers, getAbnormalTrend } from '@/api/doctor-analytics'
import * as echarts from 'echarts'
import { nextTick } from 'vue'

const dateRange = ref<string[]>([])
const overview = ref<any>({})
const distribution = ref<any[]>([])
const highRiskUsers = ref<any[]>([])
const trend = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

const distributionChart = ref<HTMLElement>()
const trendChart = ref<HTMLElement>()

const chartTheme = {
  backgroundColor: 'transparent',
  textStyle: { color: '#9d9aaf', fontFamily: 'Outfit, sans-serif' },
  title: { textStyle: { color: '#f0ece4', fontFamily: 'Playfair Display, serif', fontSize: 16, fontWeight: 700 } },
  legend: { textStyle: { color: '#9d9aaf' } },
  tooltip: {
    backgroundColor: 'rgba(22, 22, 31, 0.95)',
    borderColor: 'rgba(255, 255, 255, 0.06)',
    textStyle: { color: '#f0ece4' },
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
}

async function fetchData() {
  loading.value = true
  const params: any = {}
  if (dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  try {
    const [ov, dist, hr, tr]: any = await Promise.all([
      getAbnormalOverview(params),
      getAbnormalDistribution(params),
      getHighRiskUsers(params),
      getAbnormalTrend(params),
    ])
    overview.value = ov.data || {}
    distribution.value = dist.data || []
    highRiskUsers.value = hr.data || []
    trend.value = tr.data || []
    await nextTick()
    renderCharts()
  } catch {} finally { loading.value = false }
}

function renderCharts() {
  if (distributionChart.value && distribution.value.length) {
    const chart = echarts.init(distributionChart.value)
    chart.setOption({
      ...chartTheme,
      title: { text: '异常项目分布', ...chartTheme.title },
      tooltip: { ...chartTheme.tooltip },
      xAxis: {
        data: distribution.value.map(d => d.itemName || d.itemCode),
        axisLabel: { rotate: 30, color: '#5c5a6e' },
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
      },
      yAxis: {
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
        axisLabel: { color: '#5c5a6e' },
      },
      series: [
        { name: '异常数', type: 'bar', data: distribution.value.map(d => d.abnormalCount), itemStyle: { color: '#6a9a92', borderRadius: [4, 4, 0, 0] } },
        { name: '高风险', type: 'bar', data: distribution.value.map(d => d.highRiskCount), itemStyle: { color: '#ef4444', borderRadius: [4, 4, 0, 0] } },
      ],
    })
  }
  if (trendChart.value && trend.value.length) {
    const chart = echarts.init(trendChart.value)
    chart.setOption({
      ...chartTheme,
      title: { text: '异常趋势', ...chartTheme.title },
      tooltip: { ...chartTheme.tooltip, trigger: 'axis' },
      legend: { ...chartTheme.legend, data: ['异常数', '高风险'] },
      xAxis: {
        data: trend.value.map(t => t.date),
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        axisLabel: { color: '#5c5a6e' },
      },
      yAxis: {
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
        axisLabel: { color: '#5c5a6e' },
      },
      series: [
        { name: '异常数', type: 'line', smooth: true, data: trend.value.map(t => t.abnormalCount), lineStyle: { color: '#6a9a92', width: 2 }, itemStyle: { color: '#6a9a92' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(201,164,78,0.15)' }, { offset: 1, color: 'rgba(201,164,78,0)' }]) } },
        { name: '高风险', type: 'line', smooth: true, data: trend.value.map(t => t.highRiskCount), lineStyle: { color: '#ef4444', width: 2 }, itemStyle: { color: '#ef4444' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(239,68,68,0.15)' }, { offset: 1, color: 'rgba(239,68,68,0)' }]) } },
      ],
    })
  }
}

onMounted(() => {
  mounted.value = true
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">ABNORMAL ANALYSIS</span>
        <h2>异常分析</h2>
      </div>
      <p>追踪异常指标分布与趋势，及时发现高风险用户。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <section class="summary-strip" v-loading="loading">
      <article
        v-for="(item, idx) in [
          { label: '总检查数', value: overview.totalResultCount || 0, color: 'var(--color-brand)' },
          { label: '异常数', value: overview.abnormalCount || 0, color: 'var(--color-danger)' },
          { label: '异常率', value: `${overview.abnormalRate || 0}%`, color: 'var(--color-warning)' },
          { label: '高风险数', value: overview.highRiskCount || 0, color: 'var(--color-danger)' },
        ]"
        :key="item.label"
        class="summary-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.1}s`, '--accent': item.color }"
      >
        <div class="card-accent"></div>
        <span class="card-label">{{ item.label }}</span>
        <strong class="card-number">{{ item.value }}</strong>
      </article>
    </section>

    <section class="chart-grid">
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }"><div ref="distributionChart" class="chart-box" /></div>
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }"><div ref="trendChart" class="chart-box" /></div>
    </section>

    <section v-if="highRiskUsers.length" class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <h3 class="table-title">高风险用户</h3>
      <el-table :data="highRiskUsers">
        <el-table-column prop="userName" label="用户" />
        <el-table-column prop="mobile" label="手机号" />
        <el-table-column prop="highRiskItemCount" label="高风险项数" />
        <el-table-column prop="maxAbnormalLevel" label="最高异常等级" />
      </el-table>
    </section>
  </div>
</template>

<style scoped lang="scss">
.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 24px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  .card-accent {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--accent);
    opacity: 0.5;
  }

  .card-label {
    color: var(--color-ink-muted);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .card-number {
    display: block;
    margin-top: 12px;
    font-family: var(--font-display);
    font-size: 38px;
    line-height: 1;
    color: var(--color-ink);
    letter-spacing: -0.02em;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 20px;
}

.chart-card {
  padding: 24px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.3s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-box {
  height: 360px;
}

.table-shell {
  padding: 24px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.4s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.table-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-ink);
  margin: 0 0 16px;
}

@media (max-width: 1080px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>
