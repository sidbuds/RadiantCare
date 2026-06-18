<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperatorOrders } from '@/api/operator'

const orders = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const filters = ref({ orderNo: '', status: '' })

onMounted(() => {
  mounted.value = true
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.orderNo) params.orderNo = filters.value.orderNo
    if (filters.value.status) params.status = filters.value.status
    const res: any = await getOperatorOrders(params)
    orders.value = res.data || []
  } catch {} finally { loading.value = false }
}

const statusMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付', type: 'success' },
  REFUNDED: { label: '已退款', type: 'danger' },
  COMPLETED: { label: '已完成', type: '' },
  CLOSED: { label: '已关闭', type: 'info' },
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">ORDER</span>
        <h2>订单管理</h2>
      </div>
      <p>查询和跟踪所有订单状态与金额信息。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="filters.orderNo" placeholder="订单编号" style="width: 200px;" clearable />
      <el-select v-model="filters.status" placeholder="状态" style="width: 120px;" clearable>
        <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="k" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="orders" v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="appointmentNo" label="预约编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.appointmentNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.orderStatus]?.type as any">{{ statusMap[row.orderStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
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

.price-text {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--color-brand);
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

:deep(.el-tag--danger) {
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: none;
}

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
  border: none;
}
</style>
