<script setup lang="ts">
import { ref, watch } from 'vue'
import { getAdminUsers, updateUserStatus } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { useLoading, usePagination } from '@/composables'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const users = ref<any[]>([])
const keyword = ref('')

function loadUsers() {
  execute(async () => {
    const res = await getAdminUsers({ keyword: keyword.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value })
    users.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadUsers)

function handleSearch() { pageNum.value = 1; loadUsers() }

async function handleToggleStatus(user: any) {
  const newStatus = user.status === 1 ? 0 : 1
  await updateUserStatus(user.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已停用')
  loadUsers()
}

loadUsers()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">USER MANAGE</span>
        <h2>用户管理</h2>
      </div>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="keyword" placeholder="搜索用户" style="width: 200px;" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="userNo" label="用户编号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}</template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
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
