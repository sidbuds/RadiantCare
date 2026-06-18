<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getWorkloadOverview, getWorkloadTrend, getWorkloadBreakdown } from '@/api/doctor-analytics'
import * as echarts from 'echarts'

const dateRange = ref<string[]>([])
const overview = ref<any>({})
const trend = ref<any[]>([])
const breakdown = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
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
    const [ov, tr, bd]: any = await Promise.all([
      getWorkloadOverview(params),
      getWorkloadTrend(params),
      getWorkloadBreakdown(params),
    ])
    overview.value = ov.data || {}
    trend.value = tr.data || []
    breakdown.value = bd.data || []
    await nextTick()
    renderTrendChart()
  } catch {} finally { loading.value = false }
}

function renderTrendChart() {
  if (trendChart.value && trend.value.length) {
    const chart = echarts.init(trendChart.value)
    const colors = ['#6a9a92', '#b87070', '#22c55e', '#8b5cf6']
    chart.setOption({
      ...chartTheme,
      title: { text: '工作量趋势', ...chartTheme.title },
      tooltip: { ...chartTheme.tooltip, trigger: 'axis' },
      legend: { ...chartTheme.legend, data: ['完成检查', '结果录入', '报告审核', '咨询回复'] },
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
        { name: '完成检查', type: 'line', smooth: true, data: trend.value.map(t => t.completedItemCount), lineStyle: { color: colors[0], width: 2 }, itemStyle: { color: colors[0] } },
        { name: '结果录入', type: 'line', smooth: true, data: trend.value.map(t => t.resultEntryCount), lineStyle: { color: colors[1], width: 2 }, itemStyle: { color: colors[1] } },
        { name: '报告审核', type: 'line', smooth: true, data: trend.value.map(t => t.reviewCount), lineStyle: { color: colors[2], width: 2 }, itemStyle: { color: colors[2] } },
        { name: '咨询回复', type: 'line', smooth: true, data: trend.value.map(t => t.consultationReplyCount), lineStyle: { color: colors[3], width: 2 }, itemStyle: { color: colors[3] } },
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
        <span class="section-eyebrow">WORKLOAD</span>
        <h2>工作量统计</h2>
      </div>
      <p>查看各维度工作量数据，掌握个人产出与效率。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <section class="summary-strip" v-loading="loading">
      <article
        v-for="(item, idx) in [
          { label: '完成检查', value: overview.completedItemCount || 0, color: 'var(--color-brand)' },
          { label: '结果录入', value: overview.resultEntryCount || 0, color: 'var(--color-accent)' },
          { label: '报告审核', value: overview.reviewCount || 0, color: 'var(--color-success)' },
          { label: '咨询回复', value: overview.consultationReplyCount || 0, color: '#8b5cf6' },
          { label: '平均时长(分)', value: overview.avgCompletionMinutes || 0, color: 'var(--color-warning)' },
        ]"
        :key="item.label"
        class="summary-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.08}s`, '--accent': item.color }"
      >
        <div class="card-accent"></div>
        <span class="card-label">{{ item.label }}</span>
        <strong class="card-number">{{ item.value }}</strong>
      </article>
    </section>

    <section class="chart-card data-card" :class="{ 'is-mounted': mounted }">
      <div ref="trendChart" class="chart-box" />
    </section>

    <section v-if="breakdown.length" class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <h3 class="table-title">按科室/项目拆分</h3>
      <el-table :data="breakdown">
        <el-table-column prop="departmentName" label="科室" />
        <el-table-column prop="itemName" label="项目" />
        <el-table-column prop="taskItemCount" label="任务数" />
        <el-table-column prop="completedItemCount" label="完成数" />
        <el-table-column prop="avgCompletionMinutes" label="平均时长(分)" />
      </el-table>
    </section>
  </div>
</template>

<style scoped lang="scss">
.summary-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
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

.chart-card {
  padding: 24px;
  margin-bottom: 20px;
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
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
