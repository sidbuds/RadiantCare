<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDoctorExamItem, startExamItem, submitExamResults, completeExamItem } from '@/api/exam'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const taskNo = route.params.taskNo as string
const taskItemNo = route.params.itemNo as string

const item = ref<any>(null)
const loading = ref(true)
const submitting = ref(false)
const mounted = ref(false)

const resultEntries = ref<any[]>([])
const conclusion = ref('')

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getDoctorExamItem(taskNo, taskItemNo)
    const data = res.data || {}
    item.value = data.item || data
    const presets = data.presetMetrics?.length
      ? data.presetMetrics
      : [{ metricCode: item.value.itemCode, metricName: item.value.itemName, unit: '', refRange: '' }]
    resultEntries.value = presets.map((preset: any) => ({
      metricCode: preset.metricCode,
      metricName: preset.metricName,
      unit: preset.unit || '',
      refRange: preset.refRange || '',
      resultNumber: null,
      abnormal: false,
      abnormalLevel: 0,
    }))
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

async function handleStart() {
  try {
    await startExamItem(taskNo, taskItemNo)
    ElMessage.success('已开始')
    item.value.itemStatus = 1
  } catch {
    // handled by interceptor
  }
}

async function handleSubmitResults() {
  if (!resultEntries.value.length || resultEntries.value.some(e => e.resultNumber === null || e.resultNumber === undefined || e.resultNumber === '')) {
    ElMessage.warning('请填写检查数值')
    return
  }
  submitting.value = true
  try {
    await submitExamResults(taskNo, taskItemNo, {
      itemCode: item.value.itemCode,
      resultEntries: resultEntries.value.map(entry => ({
        metricCode: entry.metricCode,
        metricName: entry.metricName,
        resultValue: String(entry.resultNumber),
        resultNumber: entry.resultNumber,
        unit: entry.unit,
        refRange: entry.refRange,
        abnormal: entry.abnormal,
        abnormalLevel: entry.abnormal ? entry.abnormalLevel : 0,
      })),
      conclusion: conclusion.value,
    })
    ElMessage.success('结果已提交')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleComplete() {
  try {
    await completeExamItem(taskNo, taskItemNo)
    ElMessage.success('已完成')
    router.push('/doctor/exam-tasks')
  } catch {
    // handled by interceptor
  }
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">RESULT ENTRY</span>
        <h2>结果录入</h2>
      </div>
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <template v-if="item">
      <section class="info-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">任务编号</span>
            <span class="info-value mono-text">{{ taskNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">项目</span>
            <span class="info-value">{{ item.itemName }} ({{ item.itemCode }})</span>
          </div>
          <div class="info-item">
            <span class="info-label">科室</span>
            <span class="info-value">{{ item.departmentName || '-' }}</span>
          </div>
        </div>
        <div class="status-action">
          <el-button v-if="item.itemStatus === 0" type="primary" @click="handleStart">开始检查</el-button>
          <el-tag v-if="item.itemStatus === 1" type="warning">进行中</el-tag>
          <el-tag v-if="item.itemStatus === 2" type="success">已完成</el-tag>
        </div>
      </section>

      <section v-if="item.itemStatus === 1" class="entry-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="entry-header">
          <h3>检查指标</h3>
        </div>

        <el-table :data="resultEntries" class="metric-table">
          <el-table-column prop="metricCode" label="编码" min-width="120" />
          <el-table-column prop="metricName" label="名称" min-width="160" />
          <el-table-column prop="unit" label="单位" width="100">
            <template #default="{ row }">{{ row.unit || '-' }}</template>
          </el-table-column>
          <el-table-column prop="refRange" label="参考范围" min-width="140">
            <template #default="{ row }">{{ row.refRange || '-' }}</template>
          </el-table-column>
          <el-table-column label="数值" min-width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.resultNumber" :controls="false" placeholder="填写数值" style="width: 140px;" />
            </template>
          </el-table-column>
          <el-table-column label="异常" width="90">
            <template #default="{ row }">
              <el-checkbox v-model="row.abnormal" />
            </template>
          </el-table-column>
          <el-table-column label="异常等级" width="130">
            <template #default="{ row }">
              <el-select v-model="row.abnormalLevel" :disabled="!row.abnormal">
                <el-option :value="0" label="正常" />
                <el-option :value="1" label="轻度" />
                <el-option :value="2" label="中度" />
                <el-option :value="3" label="重度" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>

        <el-input v-model="conclusion" type="textarea" :rows="3" placeholder="结论备注" class="conclusion-input" />

        <div class="submit-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmitResults">提交结果</el-button>
          <el-button type="success" @click="handleComplete">完成检查</el-button>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.back-btn {
  background: var(--color-panel) !important;
  border: 1px solid var(--color-line) !important;
  color: var(--color-ink-soft) !important;

  &:hover {
    border-color: var(--color-line-strong) !important;
    color: var(--color-ink) !important;
  }
}

.info-card {
  padding: 24px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .info-label {
    font-size: 11px;
    font-weight: 600;
    color: var(--color-ink-faint);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .info-value {
    font-size: 14px;
    color: var(--color-ink);
    font-weight: 500;
  }
}

.status-action {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid var(--color-line);
}

.entry-card {
  padding: 28px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 700;
    color: var(--color-ink);
  }
}

.metric-table {
  margin-bottom: 18px;
}

.conclusion-input {
  margin-top: 20px;
}

.submit-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

:deep(.el-tag) {
  border-radius: 999px;
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
  border: none;
}

:deep(.el-tag--warning) {
  background: var(--color-warning-light);
  color: var(--color-warning);
  border: none;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
