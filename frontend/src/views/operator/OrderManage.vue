<script setup lang="ts">
import { ref, watch } from 'vue'
import { getOperatorOrders } from '@/api/operator'
import { useLoading, useStatusTag, usePagination } from '@/composables'
import type { Order } from '@/types/api'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const orders = ref<Order[]>([])
const filters = ref({ orderNo: '', status: '' })

const statusMap = {
  PENDING: { label: '待支付', type: 'warning' as const },
  PAID: { label: '已支付', type: 'success' as const },
  REFUNDED: { label: '已退款', type: 'danger' as const },
  REFUNDING: { label: '退款中', type: 'warning' as const },
  COMPLETED: { label: '已完成', type: '' as const },
}
const { getLabel, getType } = useStatusTag(statusMap)

function resolveStatus(status: number) {
  if (status === 0) return 'PENDING'
  if (status === 1) return 'PAID'
  if (status === 2) return 'REFUNDED'
  if (status === 3) return 'REFUNDING'
  if (status === 4) return 'COMPLETED'
  if (status === 5) return 'CANCELED'
  return ''
}

function loadData() {
  execute(async () => {
    const params: Record<string, string | number> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (filters.value.orderNo) params.orderNo = filters.value.orderNo
    if (filters.value.status !== '') params.status = filters.value.status
    const res = await getOperatorOrders(params)
    orders.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadData)

function handleSearch() {
  pageNum.value = 1
  loadData()
}

loadData()
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
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="orders" v-loading="loading">
        <el-table-column label="订单编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.order?.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            {{ row.userName || row.order?.userId }}
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.order?.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getType(resolveStatus(row.order?.status))">{{ getLabel(resolveStatus(row.order?.status)) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.order?.createdAt }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-line);
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
