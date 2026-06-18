<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDoctorExamTodo } from '@/api/exam'

const router = useRouter()
const tasks = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待开始', type: 'info' },
  1: { label: '进行中', type: 'warning' },
  2: { label: '已完成', type: 'success' },
}

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getDoctorExamTodo()
    tasks.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

function goEntry(taskNo: string, itemNo: string) {
  router.push(`/doctor/exam-tasks/${taskNo}/items/${itemNo}/entry`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">TASK INBOX</span>
        <h2>待检任务</h2>
      </div>
      <p>查看待处理的检查任务，及时录入检查结果。</p>
    </div>

    <section class="summary-strip">
      <article
        v-for="(item, idx) in [
          { label: '总任务数', value: tasks.length, color: 'var(--color-brand)' },
          { label: '进行中', value: tasks.filter(t => t.itemStatus === 1).length, color: 'var(--color-warning)' },
          { label: '待开始', value: tasks.filter(t => t.itemStatus === 0).length, color: 'var(--color-accent)' },
        ]"
        :key="item.label"
        class="summary-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.1}s`, '--accent': item.color }"
      >
        <div class="card-accent"></div>
        <span class="card-label">{{ item.label }}</span>
        <strong class="card-number">{{ item.value }}</strong>
      </article>
    </section>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="taskNo" label="任务编号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.taskNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="taskItemNo" label="任务项编号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.taskItemNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="检查项目" min-width="180" />
        <el-table-column prop="userName" label="用户" min-width="120" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.itemStatus]?.type as any">{{ statusMap[row.itemStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.itemStatus !== 2" type="primary" link @click="goEntry(row.taskNo, row.taskItemNo)">录入结果</el-button>
            <span v-else class="done-text">已完成</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped lang="scss">
.summary-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  padding: 24px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  .card-accent {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: var(--accent);
    opacity: 0.5;
  }

  .card-label {
    color: var(--color-ink-muted);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .card-number {
    display: block;
    margin-top: 12px;
    font-family: var(--font-display);
    font-size: 40px;
    line-height: 1;
    color: var(--color-ink);
    letter-spacing: -0.02em;
  }
}

.table-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.4s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.done-text {
  color: var(--color-ink-muted);
  font-size: 13px;
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

@media (max-width: 960px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>
