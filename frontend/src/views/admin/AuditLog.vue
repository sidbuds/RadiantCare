<script setup lang="ts">
import { ref, watch } from 'vue'
import { get } from '@/api/request'
import { useLoading, usePagination } from '@/composables'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()
const logs = ref<any[]>([])
const filters = ref({ module: '', action: '' })

function loadLogs() {
  execute(async () => {
    const res: any = await get('/admin/audit-logs', { params: { pageNum: pageNum.value, pageSize: pageSize.value, module: filters.value.module || undefined, action: filters.value.action || undefined } })
    logs.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadLogs)

function handleSearch() { pageNum.value = 1; loadLogs() }

loadLogs()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><span class="section-eyebrow">AUDIT LOG</span><h2>审计日志</h2></div>
    </div>
    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="filters.module" placeholder="模块" style="width: 140px;" clearable />
      <el-input v-model="filters.action" placeholder="操作" style="width: 140px;" clearable />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>
    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="logs" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operatorId" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作" width="160" />
        <el-table-column prop="targetType" label="目标类型" width="120" />
        <el-table-column prop="targetId" label="目标ID" width="120" />
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
.table-shell { padding: 20px; opacity: 0; transform: translateY(16px); transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo); &.is-mounted { opacity: 1; transform: translateY(0); } }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-line); }
</style>
