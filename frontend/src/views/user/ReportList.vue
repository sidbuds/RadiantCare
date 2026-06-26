<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyReports } from '@/api/report'

const router = useRouter()
const reports = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getMyReports()
    reports.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

function openReport(reportNo: string) {
  router.push(`/user/reports/${reportNo}`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT CENTER</span>
        <h2>我的报告</h2>
      </div>
      <p>已发布的体检报告会直接出现在这里，可随时查看详情、对比历史结果或发起咨询。</p>
    </div>

    <section class="report-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="reports" v-loading="loading">
        <el-table-column prop="reportNo" label="报告编号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.reportNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reportDate" label="报告日期" min-width="120" />
        <el-table-column prop="publishedAt" label="发布时间" min-width="180" />
        <el-table-column prop="overallConclusion" label="结论摘要" min-width="280" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReport(row.reportNo)">查看报告</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && !reports.length" class="empty-state">
        <el-empty description="暂无已发布报告" />
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.report-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-state {
  padding: 28px 0 8px;
}
</style>
