<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getReport } from '@/api/report'

const route = useRoute()
const router = useRouter()
const report = ref<any>(null)
const loading = ref(true)
const mounted = ref(false)

const statusMap: Record<string, { label: string; type: string }> = {
  DRAFT: { label: '草稿', type: 'info' },
  REVIEWING: { label: '审核中', type: 'warning' },
  REVIEWED: { label: '已审核', type: 'success' },
  PUBLISHED: { label: '已发布', type: '' },
}

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getReport(route.params.reportNo as string)
    report.value = res.data
  } catch {} finally { loading.value = false }
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="detail-header" :class="{ 'is-mounted': mounted }">
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <template v-if="report">
      <div class="info-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="card-header">
          <span class="section-eyebrow">REPORT DETAIL</span>
          <h2>报告详情</h2>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">报告编号</span>
            <span class="info-value mono-text">{{ report.reportNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态</span>
            <el-tag :type="statusMap[report.status]?.type as any">{{ statusMap[report.status]?.label }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">用户</span>
            <span class="info-value">{{ report.userName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">套餐</span>
            <span class="info-value">{{ report.packageName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">体检日期</span>
            <span class="info-value">{{ report.taskDate }}</span>
          </div>
          <div class="info-item full-width" v-if="report.conclusion">
            <span class="info-label">总检结论</span>
            <span class="info-value">{{ report.conclusion }}</span>
          </div>
        </div>
      </div>

      <div v-if="report.items && report.items.length" class="items-card glass-panel" :class="{ 'is-mounted': mounted }">
        <h3 class="items-heading">检查指标</h3>
        <el-table :data="report.items">
          <el-table-column prop="metricName" label="指标名称" />
          <el-table-column prop="resultValue" label="结果" />
          <el-table-column prop="unit" label="单位" width="100" />
          <el-table-column prop="refRange" label="参考范围" width="140" />
          <el-table-column label="异常" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.abnormal" type="danger" size="small">异常</el-tag>
              <span v-else class="normal-text">正常</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="action-bar" :class="{ 'is-mounted': mounted }">
        <el-button @click="router.push('/user/reports/compare')">
          <el-icon><TrendCharts /></el-icon>
          历年对比
        </el-button>
        <el-button @click="router.push('/user/consultations')">
          <el-icon><ChatDotRound /></el-icon>
          咨询医生
        </el-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.detail-header {
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

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
  padding: 36px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.1s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  margin-bottom: 28px;

  h2 {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-ink);
    margin-top: 12px;
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &.full-width {
    grid-column: 1 / -1;
  }

  .info-label {
    font-size: 11px;
    font-weight: 600;
    color: var(--color-ink-faint);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .info-value {
    font-size: 15px;
    color: var(--color-ink);
    font-weight: 500;
  }
}

.items-card {
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

.items-heading {
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

.normal-text {
  color: var(--color-success);
  font-size: 13px;
  font-weight: 500;
}

.action-bar {
  display: flex;
  gap: 12px;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.3s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
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

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
  border: none;
}

:deep(.el-tag--danger) {
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: none;
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-card,
  .items-card {
    padding: 24px;
  }
}
</style>
