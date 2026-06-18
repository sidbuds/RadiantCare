<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { generateReport, reviewReport, publishReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const generateForm = ref({ taskNo: '', templateCode: '' })
const reviewForm = ref({ reportNo: '', reviewStage: 'FINAL', reviewStatus: 'PASS', reviewComment: '' })
const publishReportNo = ref('')
const loading = ref(false)
const generatedReport = ref<any>(null)
const mounted = ref(false)

onMounted(() => {
  mounted.value = true
})

async function handleGenerate() {
  if (!generateForm.value.taskNo || !generateForm.value.templateCode) {
    ElMessage.warning('请填写任务编号和模板编码')
    return
  }
  loading.value = true
  try {
    const res: any = await generateReport(generateForm.value)
    generatedReport.value = res.data
    ElMessage.success('报告生成成功')
  } catch {} finally { loading.value = false }
}

async function handleReview() {
  if (!reviewForm.value.reportNo) {
    ElMessage.warning('请输入报告编号')
    return
  }
  loading.value = true
  try {
    await reviewReport(reviewForm.value.reportNo, reviewForm.value)
    ElMessage.success('审核完成')
  } catch {} finally { loading.value = false }
}

async function handlePublish() {
  if (!publishReportNo.value) {
    ElMessage.warning('请输入报告编号')
    return
  }
  loading.value = true
  try {
    await publishReport(publishReportNo.value)
    ElMessage.success('报告已发布')
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT PUBLISH</span>
        <h2>报告发布管理</h2>
      </div>
      <p>按流程完成报告的生成、审核与发布。</p>
    </div>

    <div class="pipeline-grid">
      <div class="pipeline-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="card-title">
          <span class="step-badge">1</span>
          生成报告
        </div>
        <el-form :model="generateForm" label-width="80px" class="compact-form">
          <el-form-item label="任务编号">
            <el-input v-model="generateForm.taskNo" placeholder="请输入任务编号" />
          </el-form-item>
          <el-form-item label="模板编码">
            <el-input v-model="generateForm.templateCode" placeholder="如 PKG1001_V1" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleGenerate">
              生成
              <el-icon class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>
        <transition name="fade-slide">
          <div v-if="generatedReport" class="result-banner">
            <span class="result-label">报告编号</span>
            <span class="mono-text">{{ generatedReport.reportNo }}</span>
          </div>
        </transition>
      </div>

      <div class="pipeline-connector">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

      <div class="pipeline-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="card-title">
          <span class="step-badge">2</span>
          审核报告
        </div>
        <el-form :model="reviewForm" label-width="80px" class="compact-form">
          <el-form-item label="报告编号">
            <el-input v-model="reviewForm.reportNo" placeholder="请输入报告编号" />
          </el-form-item>
          <el-form-item label="审核阶段">
            <el-select v-model="reviewForm.reviewStage" style="width: 100%;">
              <el-option label="初审" value="INITIAL" />
              <el-option label="终审" value="FINAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核结果">
            <el-select v-model="reviewForm.reviewStatus" style="width: 100%;">
              <el-option label="通过" value="PASS" />
              <el-option label="驳回" value="REJECT" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核意见">
            <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="3" placeholder="可选填写审核意见" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleReview">
              审核
              <el-icon class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="pipeline-connector">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>

      <div class="pipeline-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="card-title">
          <span class="step-badge">3</span>
          发布报告
        </div>
        <el-form label-width="80px" class="compact-form">
          <el-form-item label="报告编号">
            <el-input v-model="publishReportNo" placeholder="请输入报告编号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handlePublish">
              发布
              <el-icon class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.pipeline-grid {
  display: flex;
  align-items: flex-start;
  gap: 0;
}

.pipeline-card {
  flex: 1;
  min-width: 0;
  padding: 28px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  &:nth-child(1) { transition-delay: 0.1s; }
  &:nth-child(3) { transition-delay: 0.2s; }
  &:nth-child(5) { transition-delay: 0.3s; }

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  .el-button:hover .btn-arrow {
    transform: translateX(3px);
  }
}

.pipeline-connector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  color: var(--color-ink-faint);
  margin-top: 60px;
}

.card-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-ink);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-deep));
  color: var(--color-on-brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}

.result-banner {
  margin-top: 20px;
  padding: 14px 18px;
  background: var(--color-brand-light);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-label {
  font-size: 12px;
  color: var(--color-brand);
  font-weight: 600;
  white-space: nowrap;
}

.fade-slide-enter-active {
  transition: all 0.3s var(--ease-out-expo);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

@media (max-width: 900px) {
  .pipeline-grid {
    flex-direction: column;
  }

  .pipeline-connector {
    transform: rotate(90deg);
    margin: 8px 0;
    padding: 0;
  }
}
</style>
