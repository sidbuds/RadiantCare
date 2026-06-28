<script setup lang="ts">
import { ref, watch } from 'vue'
import { get } from '@/api/request'
import { useLoading, usePagination } from '@/composables'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()
const logs = ref<any[]>([])
const filters = ref({ module: '', action: '', operatorId: '', operatorName: '', targetType: '', targetId: '', range: [] as string[] })

function loadLogs() {
  execute(async () => {
    const [startTime, endTime] = filters.value.range || []
    const res: any = await get('/admin/audit-logs', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        module: filters.value.module || undefined,
        action: filters.value.action || undefined,
        operatorId: filters.value.operatorId || undefined,
        operatorName: filters.value.operatorName || undefined,
        targetType: filters.value.targetType || undefined,
        targetId: filters.value.targetId || undefined,
        startTime,
        endTime,
      },
    })
    logs.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadLogs)

function handleSearch() {
  pageNum.value = 1
  loadLogs()
}

loadLogs()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><span class="section-eyebrow">AUDIT LOG</span><h2>审计日志</h2></div>
    </div>
    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="filters.module" placeholder="模块" clearable />
      <el-input v-model="filters.action" placeholder="操作" clearable />
      <el-input v-model="filters.operatorId" placeholder="操作人ID" clearable />
      <el-input v-model="filters.operatorName" placeholder="操作人" clearable />
      <el-input v-model="filters.targetType" placeholder="目标类型" clearable />
      <el-input v-model="filters.targetId" placeholder="目标ID" clearable />
      <el-date-picker v-model="filters.range" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>
    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="logs" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorId" label="操作人ID" width="100" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作" width="120" />
        <el-table-column prop="targetType" label="目标类型" width="130" />
        <el-table-column prop="targetId" label="目标ID" width="140" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next, jumper" background @current-change="onPageChange" @size-change="onSizeChange" />
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.filter-bar { display: grid; grid-template-columns: repeat(6, minmax(120px, 1fr)) minmax(260px, 1.4fr) auto; gap: 10px; margin-bottom: 20px; align-items: center; }
.table-shell { padding: 20px; opacity: 0; transform: translateY(16px); transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo); &.is-mounted { opacity: 1; transform: translateY(0); } }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-line); }
@media (max-width: 1100px) { .filter-bar { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
