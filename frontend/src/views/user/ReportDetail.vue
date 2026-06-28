<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { downloadReportPdf, generateReportPdf, getReport, getReportItems, precheckReportPdf } from '@/api/report'
import { saveBlob } from '@/utils/download'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const report = ref<any>(null)
const items = ref<any[]>([])
const loading = ref(true)
const exporting = ref(false)
const mounted = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  1: { label: '待审核', type: 'warning' },
  3: { label: '已发布', type: 'success' },
  4: { label: '管理员处理', type: 'danger' },
}

onMounted(async () => {
  mounted.value = true
  try {
    const reportRes: any = await getReport(route.params.reportNo as string)
    report.value = reportRes.data
    const itemsRes: any = await getReportItems(route.params.reportNo as string)
    items.value = itemsRes.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

async function handleExportPdf() {
  if (!report.value?.reportNo) return
  exporting.value = true
  try {
    const precheckRes: any = await precheckReportPdf(report.value.reportNo)
    const missingFields = precheckRes.data?.missingFields || []
    if (missingFields.length > 0) {
      try {
        await ElMessageBox.confirm(
          `当前个人/体检基础信息缺少：${missingFields.join('、')}。是否仍然生成报告？`,
          '报告信息不完整',
          {
            confirmButtonText: '继续生成',
            cancelButtonText: '去补充资料',
            type: 'warning',
            distinguishCancelAndClose: true,
          }
        )
      } catch (action) {
        if (action === 'cancel') {
          router.push('/user/profile')
        }
        return
      }
    }

    await generateReportPdf(report.value.reportNo)
    const blob = await downloadReportPdf(report.value.reportNo)
    saveBlob(blob, `${report.value.reportNo}.pdf`)
    ElMessage.success('PDF已生成')
  } finally {
    exporting.value = false
  }
}
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
            <span class="info-label">报告日期</span>
            <span class="info-value">{{ report.reportDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">发布时间</span>
            <span class="info-value">{{ report.publishedAt || '-' }}</span>
          </div>
          <div class="info-item full-width">
            <span class="info-label">总体结论</span>
            <span class="info-value">{{ report.overallConclusion }}</span>
          </div>
        </div>
      </div>

      <div v-if="items.length" class="items-card glass-panel" :class="{ 'is-mounted': mounted }">
        <h3 class="items-heading">检查指标</h3>
        <el-table :data="items">
          <el-table-column prop="itemName" label="指标名称" min-width="180" />
          <el-table-column prop="resultValue" label="结果" min-width="160" />
          <el-table-column prop="unit" label="单位" width="100" />
          <el-table-column prop="refRange" label="参考范围" min-width="160" />
          <el-table-column label="异常" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isAbnormal ? 'danger' : 'success'" size="small">
                {{ row.isAbnormal ? '异常' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="action-bar" :class="{ 'is-mounted': mounted }">
        <el-button type="primary" :loading="exporting" @click="handleExportPdf">
          <el-icon><Download /></el-icon>
          导出 PDF
        </el-button>
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
}

.info-card,
.items-card {
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

.card-header {
  margin-bottom: 24px;

  h2 {
    font-family: var(--font-display);
    font-size: 28px;
    margin-top: 12px;
    color: var(--color-ink);
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
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-ink-faint);
  text-transform: uppercase;
}

.info-value {
  font-size: 15px;
  color: var(--color-ink);
  font-weight: 500;
}

.items-heading {
  font-family: var(--font-display);
  font-size: 20px;
  margin: 0 0 20px;
  color: var(--color-ink);
}

.action-bar {
  display: flex;
  gap: 12px;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
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
