<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
  } finally {
    loading.value = false
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
      <p>选择两份已发布报告，生成指标变化趋势和健康建议。</p>
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

    <div v-if="results.length" class="results-card glass-panel" :class="{ 'is-mounted': mounted }">
      <h3 class="card-heading">指标对比结果</h3>
      <el-table :data="results">
        <el-table-column prop="metricName" label="指标" />
        <el-table-column prop="baselineValue" label="基线值" />
        <el-table-column prop="compareValue" label="对比值" />
        <el-table-column prop="changeDirection" label="变化趋势" />
        <el-table-column prop="changePercent" label="变化幅度" />
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
.compare-form-card {
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

.results-card,
.advice-card {
  padding: 32px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
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
</style>
