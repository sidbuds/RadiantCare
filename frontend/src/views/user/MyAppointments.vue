<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyAppointments } from '@/api/appointment'
import { useLoading, useStatusTag, appointmentStatusMap } from '@/composables'
import type { Appointment } from '@/types/api'

const router = useRouter()
const { loading, mounted, execute } = useLoading()
const { getLabel, getType } = useStatusTag(appointmentStatusMap)

const appointments = ref<Appointment[]>([])

execute(async () => {
  const res = await getMyAppointments()
  appointments.value = res.data || []
})

function goDetail(no: string) {
  router.push(`/user/appointments/${no}`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">MY APPOINTMENTS</span>
        <h2>我的预约</h2>
      </div>
      <el-button type="primary" @click="router.push('/user/appointments/create')">
        <el-icon><Plus /></el-icon>
        新建预约
      </el-button>
    </div>

    <section class="summary-strip">
      <article
        v-for="(item, idx) in [
          { label: '预约总数', value: appointments.length, color: 'var(--color-brand)' },
          { label: '待支付', value: appointments.filter(a => a.status === 0).length, color: 'var(--color-accent)' },
          { label: '待体检', value: appointments.filter(a => a.status === 1).length, color: 'var(--color-success)' },
        ]"
        :key="item.label"
        class="summary-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.1}s`, '--accent': item.color }"
      >
        <div class="card-accent"></div>
        <span class="summary-label">{{ item.label }}</span>
        <strong class="summary-number">{{ item.value }}</strong>
      </article>
    </section>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="appointments" v-loading="loading">
        <el-table-column prop="appointmentNo" label="预约编号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.appointmentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="centerCode" label="体检中心" min-width="120" />
        <el-table-column prop="appointDate" label="预约日期" min-width="120" />
        <el-table-column prop="timeSlotCode" label="时段" min-width="100" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getType(row.status)">{{ getLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.appointmentNo)">查看详情</el-button>
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
  padding: 24px 28px;
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
    opacity: 0.6;
  }

  .summary-label {
    color: var(--color-ink-muted);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .summary-number {
    display: block;
    margin-top: 12px;
    font-family: var(--font-display);
    font-size: 40px;
    font-weight: 700;
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

:deep(.el-tag) {
  background: var(--color-brand-light);
  color: var(--color-brand);
  border: none;
}

:deep(.el-tag--danger) {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
}

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
}

@media (max-width: 960px) {
  .summary-strip {
    grid-template-columns: 1fr;
  }
}
</style>
