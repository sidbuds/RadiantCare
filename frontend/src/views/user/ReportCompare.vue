<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { createCompareTask, getCompareTask, getCompareResults, getHealthAdvice } from '@/api/compare'
import { ElMessage } from 'element-plus'

const form = ref({ baselineReportNo: '', compareReportNo: '' })
const loading = ref(false)
const compareResult = ref<any>(null)
const results = ref<any[]>([])
const advice = ref<any[]>([])
const mounted = ref(false)

onMounted(() => {
  mounted.value = true
})

async function handleCompare() {
  if (!form.value.baselineReportNo || !form.value.compareReportNo) {
    ElMessage.warning('请输入两份报告编号')
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
    results.value = resultsRes.data || []
    advice.value = adviceRes.data || []
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT COMPARE</span>
        <h2>历年报告对比</h2>
      </div>
      <p>对比两份报告的指标变化，了解健康趋势。</p>
    </div>

    <div class="compare-form-card glass-panel" :class="{ 'is-mounted': mounted }">
      <el-form :inline="true" :model="form">
        <el-form-item label="基线报告">
          <el-input v-model="form.baselineReportNo" placeholder="报告编号" />
        </el-form-item>
        <el-form-item label="对比报告">
          <el-input v-model="form.compareReportNo" placeholder="报告编号" />
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

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  .el-button:hover .btn-arrow {
    transform: translateX(3px);
  }
}

.results-card,
.advice-card {
  padding: 32px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

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
  padding-left: 16px;
  position: relative;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 4px;
    bottom: 4px;
    width: 4px;
    border-radius: 2px;
    background: linear-gradient(180deg, var(--color-brand), var(--color-accent));
  }
}

.advice-item {
  display: flex;
  gap: 16px;
  padding: 18px 0;
  border-bottom: 1px solid var(--color-line);

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-child {
    padding-top: 0;
  }
}

.advice-marker {
  flex-shrink: 0;
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

.advice-content {
  h4 {
    font-family: var(--font-display);
    font-size: 16px;
    font-weight: 700;
    color: var(--color-ink);
    margin: 0 0 6px;
  }

  p {
    font-size: 14px;
    color: var(--color-ink-muted);
    line-height: 1.75;
    margin: 0;
  }
}

@media (max-width: 768px) {
  .compare-form-card,
  .results-card,
  .advice-card {
    padding: 24px;
  }
}
</style>
