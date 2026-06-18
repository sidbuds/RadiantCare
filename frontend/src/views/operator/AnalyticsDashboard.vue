<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { getDashboard, getAppointmentTrend, getOrderConversion, getPackageAnalysis } from '@/api/operator'
import * as echarts from 'echarts'

const dashboard = ref<any>({})
const loading = ref(true)
const mounted = ref(false)
const trendChart = ref<HTMLElement>()
const conversionChart = ref<HTMLElement>()
const packageChart = ref<HTMLElement>()

// Dark theme chart defaults
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

onMounted(async () => {
  mounted.value = true
  try {
    const [dash, trend, conv, pkg]: any = await Promise.all([
      getDashboard(),
      getAppointmentTrend(),
      getOrderConversion(),
      getPackageAnalysis(),
    ])

    dashboard.value = dash.data || {}
    await nextTick()

    if (trendChart.value && trend.data?.length) {
      const chart = echarts.init(trendChart.value)
      chart.setOption({
        ...chartTheme,
        title: { text: '预约趋势', ...chartTheme.title },
        tooltip: { ...chartTheme.tooltip, trigger: 'axis' },
        xAxis: {
          data: trend.data.map((t: any) => t.date),
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          axisLabel: { color: '#5c5a6e' },
        },
        yAxis: {
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
          axisLabel: { color: '#5c5a6e' },
        },
        series: [{
          name: '预约数',
          type: 'line',
          smooth: true,
          data: trend.data.map((t: any) => t.count),
          lineStyle: { color: '#6a9a92', width: 2 },
          itemStyle: { color: '#6a9a92' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(201, 164, 78, 0.2)' },
              { offset: 1, color: 'rgba(201, 164, 78, 0)' },
            ]),
          },
        }],
      })
    }

    if (conversionChart.value && conv.data?.length) {
      const chart = echarts.init(conversionChart.value)
      chart.setOption({
        ...chartTheme,
        title: { text: '预约到订单转化', ...chartTheme.title },
        tooltip: { ...chartTheme.tooltip, trigger: 'axis' },
        legend: { ...chartTheme.legend, data: ['预约数', '订单数'] },
        xAxis: {
          data: conv.data.map((c: any) => c.date),
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          axisLabel: { color: '#5c5a6e' },
        },
        yAxis: {
          axisLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
          axisLabel: { color: '#5c5a6e' },
        },
        series: [
          {
            name: '预约数',
            type: 'bar',
            data: conv.data.map((c: any) => c.appointmentCount),
            itemStyle: { color: '#6a9a92', borderRadius: [4, 4, 0, 0] },
            barWidth: '35%',
          },
          {
            name: '订单数',
            type: 'bar',
            data: conv.data.map((c: any) => c.orderCount),
            itemStyle: { color: '#b87070', borderRadius: [4, 4, 0, 0] },
            barWidth: '35%',
          },
        ],
      })
    }

    if (packageChart.value && pkg.data?.length) {
      const chart = echarts.init(packageChart.value)
      chart.setOption({
        ...chartTheme,
        title: { text: '套餐订单分布', ...chartTheme.title },
        tooltip: { ...chartTheme.tooltip, trigger: 'item' },
        series: [{
          type: 'pie',
          radius: ['42%', '70%'],
          center: ['50%', '55%'],
          data: pkg.data.map((p: any, i: number) => ({
            name: p.packageName,
            value: p.orderCount,
            itemStyle: {
              color: ['#6a9a92', '#b87070', '#22c55e', '#f59e0b', '#8b5cf6', '#ef4444'][i % 6],
            },
          })),
          label: { color: '#9d9aaf', fontSize: 12 },
          emphasis: {
            itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.3)' },
          },
        }],
      })
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">OPERATIONS OVERVIEW</span>
        <h2>运营看板</h2>
      </div>
      <p>实时查看预约、订单与收入数据，掌握运营动态。</p>
    </div>

    <section class="metrics-grid" v-loading="loading">
      <article
        v-for="(item, idx) in [
          { label: '今日预约', value: dashboard.todayAppointments || 0, icon: 'Calendar', color: '#6a9a92' },
          { label: '今日订单', value: dashboard.todayOrders || 0, icon: 'List', color: '#b87070' },
          { label: '今日收入', value: `¥${dashboard.todayRevenue || 0}`, icon: 'Money', color: '#22c55e' },
          { label: '待处理退款', value: dashboard.pendingRefunds || 0, icon: 'RefreshLeft', color: '#f59e0b' },
        ]"
        :key="item.label"
        class="metric-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.1}s`, '--accent': item.color }"
      >
        <div class="card-accent"></div>
        <div class="card-header">
          <span class="card-icon"><el-icon :size="16"><component :is="item.icon" /></el-icon></span>
          <span class="card-label">{{ item.label }}</span>
        </div>
        <strong class="card-number">{{ item.value }}</strong>
      </article>
    </section>

    <section class="chart-grid">
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }">
        <div ref="trendChart" class="chart-box" />
      </div>
      <div class="chart-card data-card" :class="{ 'is-mounted': mounted }">
        <div ref="conversionChart" class="chart-box" />
      </div>
    </section>

    <section class="chart-card data-card single-chart" :class="{ 'is-mounted': mounted }">
      <div ref="packageChart" class="chart-box" />
    </section>
  </div>
</template>

<style scoped lang="scss">
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
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

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .card-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: var(--radius-sm);
    background: color-mix(in srgb, var(--accent) 12%, transparent);
    color: var(--accent);
  }

  .card-label {
    color: var(--color-ink-muted);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }

  .card-number {
    display: block;
    margin-top: 14px;
    font-family: var(--font-display);
    font-size: 38px;
    line-height: 1.05;
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
  transition-delay: 0.4s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.single-chart {
  transition-delay: 0.5s;
}

.chart-box {
  height: 360px;
}

@media (max-width: 1080px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
