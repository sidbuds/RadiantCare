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
    item.value = res.data
    resultEntries.value = [
      { metricCode: '', metricName: '', resultValue: '', resultNumber: null, unit: '', refRange: '', abnormal: false, abnormalLevel: 0 },
    ]
  } catch {} finally { loading.value = false }
})

async function handleStart() {
  try {
    await startExamItem(taskNo, taskItemNo)
    ElMessage.success('已开始')
    item.value.itemStatus = 1
  } catch {}
}

function addRow() {
  resultEntries.value.push({ metricCode: '', metricName: '', resultValue: '', resultNumber: null, unit: '', refRange: '', abnormal: false, abnormalLevel: 0 })
}

function removeRow(index: number) {
  resultEntries.value.splice(index, 1)
}

async function handleSubmitResults() {
  if (!resultEntries.value.length || resultEntries.value.some(e => !e.metricCode || !e.metricName || !e.resultValue)) {
    ElMessage.warning('请填写完整的指标信息')
    return
  }
  submitting.value = true
  try {
    await submitExamResults(taskNo, taskItemNo, {
      itemCode: item.value.itemCode,
      resultEntries: resultEntries.value,
      conclusion: conclusion.value,
    })
    ElMessage.success('结果已提交')
  } catch {} finally { submitting.value = false }
}

async function handleComplete() {
  try {
    await completeExamItem(taskNo, taskItemNo)
    ElMessage.success('已完成')
    router.push('/doctor/exam-tasks')
  } catch {}
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
            <span class="info-label">用户</span>
            <span class="info-value">{{ item.userName }}</span>
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
          <el-button size="small" type="primary" @click="addRow">
            <el-icon><Plus /></el-icon>
            添加指标
          </el-button>
        </div>

        <div v-for="(entry, index) in resultEntries" :key="index" class="entry-row">
          <el-input v-model="entry.metricCode" placeholder="编码" style="width: 120px;" />
          <el-input v-model="entry.metricName" placeholder="名称" style="width: 140px;" />
          <el-input v-model="entry.resultValue" placeholder="结果值" style="width: 120px;" />
          <el-input-number v-model="entry.resultNumber" placeholder="数值" style="width: 120px;" :controls="false" />
          <el-input v-model="entry.unit" placeholder="单位" style="width: 100px;" />
          <el-input v-model="entry.refRange" placeholder="参考范围" style="width: 120px;" />
          <el-checkbox v-model="entry.abnormal">异常</el-checkbox>
          <el-select v-model="entry.abnormalLevel" style="width: 100px;" :disabled="!entry.abnormal">
            <el-option :value="0" label="正常" />
            <el-option :value="1" label="轻度" />
            <el-option :value="2" label="中度" />
            <el-option :value="3" label="重度" />
          </el-select>
          <el-button type="danger" :icon="'Delete'" circle size="small" @click="removeRow(index)" />
        </div>

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

.entry-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: var(--color-panel);
  border: 1px solid var(--color-line);
  flex-wrap: wrap;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: var(--color-line-strong);
  }
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

  .entry-row {
    flex-direction: column;
    align-items: stretch;

    > * {
      width: 100% !important;
    }
  }
}
</style>
