<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperatorAppointments } from '@/api/operator'

const appointments = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const filters = ref({ centerCode: '', status: '' })

onMounted(() => {
  mounted.value = true
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.centerCode) params.centerCode = filters.value.centerCode
    if (filters.value.status !== '') params.status = filters.value.status
    const res: any = await getOperatorAppointments(params)
    appointments.value = res.data?.list || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待支付', type: 'info' },
  1: { label: '待体检', type: 'success' },
  2: { label: '已体检', type: '' },
  3: { label: '已关闭', type: 'danger' },
  4: { label: '爽约', type: 'danger' },
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">APPOINTMENT</span>
        <h2>预约管理</h2>
      </div>
      <p>查看与管理所有预约记录，按体检中心和状态筛选。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="filters.centerCode" placeholder="中心编码" style="width: 150px;" clearable />
      <el-select v-model="filters.status" placeholder="状态" style="width: 120px;" clearable>
        <el-option label="待支付" :value="0" />
        <el-option label="待体检" :value="1" />
        <el-option label="已体检" :value="2" />
        <el-option label="已关闭" :value="3" />
        <el-option label="爽约" :value="4" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="appointments" v-loading="loading">
        <el-table-column label="编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.appointment?.appointmentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            {{ row.userName || row.appointment?.userId }}
          </template>
        </el-table-column>
        <el-table-column label="套餐 ID" width="100">
          <template #default="{ row }">
            {{ row.appointment?.packageId }}
          </template>
        </el-table-column>
        <el-table-column label="中心" width="100">
          <template #default="{ row }">
            {{ row.appointment?.centerCode }}
          </template>
        </el-table-column>
        <el-table-column label="日期" width="120">
          <template #default="{ row }">
            {{ row.appointment?.appointDate }}
          </template>
        </el-table-column>
        <el-table-column label="时段" width="100">
          <template #default="{ row }">
            {{ row.appointment?.timeSlotCode }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.appointment?.status]?.type as any">{{ statusMap[row.appointment?.status]?.label }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<style scoped lang="scss">
.table-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

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
</style>
