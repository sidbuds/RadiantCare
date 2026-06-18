<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperatorSchedules, createOperatorSchedule, updateOperatorSchedule } from '@/api/operator'
import { ElMessage } from 'element-plus'

const schedules = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref<any>({
  centerCode: '', appointDate: '', timeSlotCode: '', resourceType: 'CENTER_SLOT', resourceCode: '', capacityTotal: 20, status: 1,
})

onMounted(() => {
  mounted.value = true
  loadSchedules()
})

async function loadSchedules() {
  loading.value = true
  try {
    const res: any = await getOperatorSchedules()
    schedules.value = res.data || []
  } catch {} finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  form.value = { centerCode: '', appointDate: '', timeSlotCode: '', resourceType: 'CENTER_SLOT', resourceCode: '', capacityTotal: 20, status: 1 }
  dialogVisible.value = true
}

function openEdit(row: any) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (editing.value) {
      await updateOperatorSchedule(form.value.id, form.value)
    } else {
      await createOperatorSchedule(form.value)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadSchedules()
  } catch {}
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">SCHEDULE</span>
        <h2>排班管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建排班
      </el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="schedules" v-loading="loading">
        <el-table-column prop="centerCode" label="中心" width="100">
          <template #default="{ row }">
            <span class="mono-text">{{ row.centerCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="appointDate" label="日期" width="120" />
        <el-table-column prop="timeSlotCode" label="时段" width="100" />
        <el-table-column prop="resourceType" label="资源类型" width="140" />
        <el-table-column prop="capacityTotal" label="总容量" width="100" />
        <el-table-column prop="capacityUsed" label="已用" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑排班' : '新建排班'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="中心编码"><el-input v-model="form.centerCode" /></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.appointDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="时段编码"><el-input v-model="form.timeSlotCode" placeholder="如 AM01" /></el-form-item>
        <el-form-item label="资源类型"><el-input v-model="form.resourceType" /></el-form-item>
        <el-form-item label="资源编码"><el-input v-model="form.resourceCode" /></el-form-item>
        <el-form-item label="总容量"><el-input-number v-model="form.capacityTotal" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
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
</style>
