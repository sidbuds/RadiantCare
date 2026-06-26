<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorReviewTodo, getDoctorReviewHistory, getDoctorReviewReport, submitDoctorReportReview } from '@/api/report'

const activeTab = ref<'todo' | 'history'>('todo')
const todoReviews = ref<any[]>([])
const historyReviews = ref<any[]>([])
const detail = ref<any>(null)
const selectedReportNo = ref('')
const loading = ref(true)
const historyLoading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const mounted = ref(false)
const reviewComment = ref('')

onMounted(async () => {
  mounted.value = true
  await Promise.all([loadTodos(), loadHistory()])
})

async function loadTodos() {
  loading.value = true
  try {
    const res: any = await getDoctorReviewTodo()
    todoReviews.value = res.data || []
    if (!todoReviews.value.some(row => row.reportNo === selectedReportNo.value)) {
      detail.value = null
      selectedReportNo.value = ''
    }
    if (activeTab.value === 'todo' && todoReviews.value.length && !selectedReportNo.value) {
      await loadDetail(todoReviews.value[0].reportNo)
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res: any = await getDoctorReviewHistory()
    historyReviews.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    historyLoading.value = false
  }
}

async function loadDetail(reportNo: string) {
  selectedReportNo.value = reportNo
  detailLoading.value = true
  reviewComment.value = ''
  try {
    const res: any = await getDoctorReviewReport(reportNo)
    detail.value = res.data
  } catch {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

async function submitReview(status: 'PASS' | 'REJECT') {
  if (!selectedReportNo.value) return
  if (status === 'REJECT' && !reviewComment.value.trim()) {
    ElMessage.warning('驳回时请填写审核意见')
    return
  }
  if (status === 'REJECT') {
    await ElMessageBox.confirm('确认驳回该报告并转管理员处理吗？', '审核确认', { type: 'warning' })
  }
  submitting.value = true
  try {
    await submitDoctorReportReview(selectedReportNo.value, {
      reviewStage: 'DOCTOR_REVIEW',
      reviewStatus: status,
      reviewComment: reviewComment.value.trim() || (status === 'PASS' ? '审核通过' : '审核驳回'),
    })
    ElMessage.success(status === 'PASS' ? '审核通过' : '已驳回')
    detail.value = null
    selectedReportNo.value = ''
    await Promise.all([loadTodos(), loadHistory()])
    activeTab.value = 'history'
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleTabChange(name: string | number) {
  activeTab.value = name as 'todo' | 'history'
  if (activeTab.value === 'todo' && todoReviews.value.length && !selectedReportNo.value) {
    loadDetail(todoReviews.value[0].reportNo)
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT REVIEW</span>
        <h2>报告审核</h2>
      </div>
      <p>处理系统分配的体检报告复核任务。两名医生都通过后，报告才会自动发布给用户。</p>
    </div>

    <div class="review-layout">
      <section class="todo-panel data-card" :class="{ 'is-mounted': mounted }">
        <div class="panel-title">审核任务</div>
        <el-tabs :model-value="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="待审核" name="todo">
            <el-table :data="todoReviews" v-loading="loading" height="520" @row-click="(row: any) => loadDetail(row.reportNo)">
              <el-table-column prop="reportNo" label="报告编号" min-width="160">
                <template #default="{ row }">
                  <span class="mono-text" :class="{ active: row.reportNo === selectedReportNo }">{{ row.reportNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reportDate" label="报告日期" min-width="120" />
              <el-table-column label="状态" width="90">
                <template #default>
                  <el-tag type="warning">待审核</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="已审核" name="history">
            <el-table :data="historyReviews" v-loading="historyLoading" height="520">
              <el-table-column prop="reportNo" label="报告编号" min-width="160">
                <template #default="{ row }">
                  <span class="mono-text">{{ row.reportNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="reviewStatus" label="审核结果" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.reviewStatus === 'PASS' ? 'success' : 'danger'">
                    {{ row.reviewStatus === 'PASS' ? '通过' : '驳回' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reviewedAt" label="审核时间" min-width="170" />
              <el-table-column prop="overallConclusion" label="结论摘要" min-width="220" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </section>

      <section class="detail-panel data-card" :class="{ 'is-mounted': mounted }" v-loading="detailLoading">
        <template v-if="activeTab === 'todo' && detail">
          <div class="panel-head">
            <div>
              <span class="section-eyebrow">REPORT</span>
              <h3 class="report-no mono-text">{{ detail.report.reportNo }}</h3>
            </div>
            <el-tag type="info">待双审</el-tag>
          </div>

          <div class="conclusion-box">
            <span>总体结论</span>
            <p>{{ detail.report.overallConclusion }}</p>
          </div>

          <el-table :data="detail.items || []" class="items-table">
            <el-table-column prop="itemName" label="项目" min-width="150" />
            <el-table-column prop="resultValue" label="结果" min-width="140" />
            <el-table-column prop="unit" label="单位" width="90" />
            <el-table-column prop="refRange" label="参考范围" min-width="120" />
            <el-table-column label="异常" width="90">
              <template #default="{ row }">
                <el-tag :type="row.isAbnormal ? 'danger' : 'success'" size="small">
                  {{ row.isAbnormal ? '异常' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-input
            v-model="reviewComment"
            type="textarea"
            :rows="3"
            placeholder="填写审核意见，驳回时必填"
            class="review-input"
          />

          <div class="action-bar">
            <el-button type="danger" :loading="submitting" @click="submitReview('REJECT')">驳回</el-button>
            <el-button type="primary" :loading="submitting" @click="submitReview('PASS')">通过</el-button>
          </div>
        </template>
        <div v-else class="empty-state">
          <el-empty :description="activeTab === 'history' ? '暂无已审核记录' : '暂无待审核报告'" />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.review-layout {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.6fr);
  gap: 20px;
}

.todo-panel,
.detail-panel {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-title {
  font-family: var(--font-display);
  font-size: 18px;
  color: var(--color-ink);
  margin-bottom: 12px;
}

.active {
  color: var(--color-brand);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.report-no {
  margin: 8px 0 0;
  color: var(--color-ink);
  font-size: 24px;
}

.conclusion-box {
  padding: 16px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-panel);
  margin-bottom: 18px;

  span {
    display: block;
    font-size: 12px;
    color: var(--color-ink-muted);
    margin-bottom: 8px;
  }

  p {
    margin: 0;
    color: var(--color-ink);
    line-height: 1.7;
  }
}

.items-table {
  margin-bottom: 18px;
}

.review-input {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.empty-state {
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-tag) {
  border-radius: 999px;
}

:deep(.el-tabs__nav-wrap::after) {
  background: var(--color-line);
}

@media (max-width: 1080px) {
  .review-layout {
    grid-template-columns: 1fr;
  }
}
</style>
