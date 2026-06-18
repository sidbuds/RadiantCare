<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getAdminAbnormalOverview, getAdminAbnormalDistribution, getAdminHighRiskUsers, getWorkloadRanking, getDepartmentWorkload } from '@/api/admin'
import * as echarts from 'echarts'

const filters = ref({ startDate: '', endDate: '', doctorId: undefined as number | undefined, departmentCode: '' })
const overview = ref<any>({})
const distribution = ref<any[]>([])
const highRiskUsers = ref<any[]>([])
const ranking = ref<any[]>([])
const deptWorkload = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const distChart = ref<HTMLElement>()
const rankChart = ref<HTMLElement>()

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
  if (filters.value.startDate) params.startDate = filters.value.startDate
  if (filters.value.endDate) params.endDate = filters.value.endDate
  if (filters.value.doctorId) params.doctorId = filters.value.doctorId
  if (filters.value.departmentCode) params.departmentCode = filters.value.departmentCode

  try {
    const [ov, dist, hr, rk, dept]: any = await Promise.all([
      getAdminAbnormalOverview(params),
      getAdminAbnormalDistribution(params),
      getAdminHighRiskUsers(params),
      getWorkloadRanking(params),
      getDepartmentWorkload(params),
    ])
    overview.value = ov.data || {}
    distribution.value = dist.data || []
    highRiskUsers.value = hr.data || []
    ranking.value = rk.data || []
    deptWorkload.value = dept.data || []
    await nextTick()
    renderCharts()
  } catch {} finally { loading.value = false }
}

function renderCharts() {
  if (distChart.value && distribution.value.length) {
    const chart = echarts.init(distChart.value)
    chart.setOption({
      ...chartTheme,
      title: { text: '异常分布', ...chartTheme.title },
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
  if (rankChart.value && ranking.value.length) {
    const chart = echarts.init(rankChart.value)
    chart.setOption({
      ...chartTheme,
      title: { text: '医生工作量排行', ...chartTheme.title },
      tooltip: { ...chartTheme.tooltip },
      xAxis: {
        data: ranking.value.map(r => r.doctorName),
        axisLabel: { rotate: 30, color: '#5c5a6e' },
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
      },
      yAxis: {
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
        axisLabel: { color: '#5c5a6e' },
      },
      series: [
        { name: '完成检查', type: 'bar', data: ranking.value.map(r => r.completedItemCount), itemStyle: { color: '#6a9a92', borderRadius: [4, 4, 0, 0] } },
        { name: '结果录入', type: 'bar', data: ranking.value.map(r => r.resultEntryCount), itemStyle: { color: '#b87070', borderRadius: [4, 4, 0, 0] } },
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
        <span class="section-eyebrow">DOCTOR ANALYTICS</span>
        <h2>医生分析总览</h2>
      </div>
      <p>查看医生工作量、异常分布与高风险用户数据。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
      <el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
      <el-input v-model.number="filters.doctorId" placeholder="医生ID" style="width: 130px;" clearable />
      <el-input v-model="filters.departmentCode" placeholder="科室编码" style="width: 130px;" clearable />
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
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }">
        <h3 class="chart-heading">异常分布</h3>
        <div ref="distChart" class="chart-box" />
      </div>
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }">
        <h3 class="chart-heading">医生工作量排行</h3>
        <div ref="rankChart" class="chart-box" />
      </div>
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

    <section v-if="deptWorkload.length" class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <h3 class="table-title">科室工作量</h3>
      <el-table :data="deptWorkload">
        <el-table-column prop="departmentName" label="科室" />
        <el-table-column prop="taskItemCount" label="任务数" />
        <el-table-column prop="completedItemCount" label="完成数" />
        <el-table-column prop="resultEntryCount" label="结果录入" />
        <el-table-column prop="abnormalCount" label="异常数" />
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

.chart-heading {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-ink);
  margin: 0 0 16px;
}

.chart-box {
  height: 360px;
}

.table-shell {
  padding: 24px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.4s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  &:last-child {
    margin-bottom: 0;
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
