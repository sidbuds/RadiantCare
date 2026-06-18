<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getReport } from '@/api/report'
import { ElMessage } from 'element-plus'

const router = useRouter()
const reportNo = ref('')
const loading = ref(false)
const mounted = ref(false)

onMounted(() => {
  mounted.value = true
})

async function handleView() {
  if (!reportNo.value) {
    ElMessage.warning('请输入报告编号')
    return
  }

  loading.value = true
  try {
    await getReport(reportNo.value)
    router.push(`/user/reports/${reportNo.value}`)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REPORT CENTER</span>
        <h2>体检报告</h2>
      </div>
      <p>输入报告编号后直接查看详情，后续可以继续对比历年结果。</p>
    </div>

    <section class="report-entry glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="report-copy">
        <strong>查看体检报告</strong>
        <span>输入报告编号，即可查看详细的体检结果与指标分析。</span>
      </div>

      <div class="report-form">
        <el-input v-model="reportNo" placeholder="请输入报告编号" size="large" />
        <el-button type="primary" size="large" :loading="loading" @click="handleView">
          查看报告
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.report-entry {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 28px;
  padding: 36px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.report-copy {
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;

  strong {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-ink);
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  span {
    color: var(--color-ink-muted);
    line-height: 1.8;
    font-size: 14px;
  }
}

.report-form {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 420px;
  position: relative;

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  .el-button:hover .btn-arrow {
    transform: translateX(3px);
  }
}

@media (max-width: 960px) {
  .report-entry,
  .report-form {
    grid-template-columns: 1fr;
  }

  .report-form {
    min-width: 0;
  }
}
</style>
