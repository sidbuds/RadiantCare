<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  getOperatorCenters,
  createOperatorCenter,
  updateOperatorCenter,
  updateOperatorCenterStatus,
} from '@/api/operator'
import { ElMessage } from 'element-plus'
import { useLoading, usePagination } from '@/composables'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const centers = ref<any[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const submitLoading = ref(false)
const form = ref({
  id: 0,
  centerCode: '',
  centerName: '',
  address: '',
  phone: '',
  businessHours: '',
  description: '',
  status: 1,
})

function loadCenters() {
  execute(async () => {
    const res = await getOperatorCenters({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    centers.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadCenters)

function openCreate() {
  editing.value = false
  form.value = { id: 0, centerCode: '', centerName: '', address: '', phone: '', businessHours: '', description: '', status: 1 }
  dialogVisible.value = true
}

function openEdit(row: any) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.centerCode || !form.value.centerName) {
    ElMessage.warning('请填写中心编码和名称')
    return
  }
  submitLoading.value = true
  try {
    if (editing.value) {
      await updateOperatorCenter(form.value.id, form.value)
    } else {
      await createOperatorCenter(form.value)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadCenters()
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: any) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateOperatorCenterStatus(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '已启用' : '已停用')
  loadCenters()
}

function handleSearch() {
  pageNum.value = 1
  loadCenters()
}

loadCenters()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CENTER</span>
        <h2>体检中心管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新增中心
      </el-button>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="keyword" placeholder="中心编码或名称" style="width: 220px;" clearable />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="centers" v-loading="loading">
        <el-table-column prop="centerCode" label="编码" width="120" />
        <el-table-column prop="centerName" label="名称" min-width="180" />
        <el-table-column prop="address" label="地址" min-width="220" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="businessHours" label="营业时间" width="180" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑体检中心' : '新增体检中心'" width="560px">
      <el-form label-width="100px">
        <el-form-item label="中心编码"><el-input v-model="form.centerCode" :disabled="editing" /></el-form-item>
        <el-form-item label="中心名称"><el-input v-model="form.centerName" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="form.businessHours" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.table-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

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
</style>
