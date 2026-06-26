<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  getOperatorSchedules,
  createOperatorSchedule,
  updateOperatorSchedule,
  getCenterOptions,
  getDepartmentOptions,
  getTimeSlotOptions,
} from '@/api/operator'
import type { CenterOption, DepartmentOption, TimeSlotOption } from '@/api/operator'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoading, usePagination } from '@/composables'
import type { Schedule } from '@/types/api'

type ScheduleForm = {
  id: number
  centerCode: string
  appointDate: string
  timeSlotCode: string
  resourceType: string
  resourceCode: string
  capacityTotal: number
  status: number
  departmentCode: string
  departmentName: string
}

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const schedules = ref<Schedule[]>([])
const centers = ref<CenterOption[]>([])
const departments = ref<DepartmentOption[]>([])
const timeSlots = ref<TimeSlotOption[]>([])
const filterDept = ref('')
const dialogVisible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const createEmptyForm = (): ScheduleForm => ({
  id: 0,
  centerCode: '',
  appointDate: '',
  timeSlotCode: '',
  resourceType: 'CENTER_SLOT',
  resourceCode: '',
  capacityTotal: 20,
  status: 1,
  departmentCode: '',
  departmentName: '',
})

const form = ref<ScheduleForm>(createEmptyForm())

const rules: FormRules = {
  centerCode: [{ required: true, message: '请输入中心编码', trigger: 'blur' }],
  appointDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlotCode: [{ required: true, message: '请输入时段编码', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请输入资源类型', trigger: 'blur' }],
  resourceCode: [{ required: true, message: '请输入资源编码', trigger: 'blur' }],
  capacityTotal: [{ required: true, message: '请输入总容量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function loadSchedules() {
  execute(async () => {
    const res = await getOperatorSchedules({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      departmentCode: filterDept.value || undefined,
    })
    schedules.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

async function loadOptions() {
  const [centerRes, departmentRes, timeSlotRes] = await Promise.all([
    getCenterOptions(),
    getDepartmentOptions(),
    getTimeSlotOptions(),
  ])
  centers.value = centerRes.data || []
  departments.value = departmentRes.data || []
  timeSlots.value = timeSlotRes.data || []
}

watch([pageNum, pageSize], loadSchedules)

function openCreate() {
  editing.value = false
  form.value = createEmptyForm()
  dialogVisible.value = true
  loadOptions()
  formRef.value?.clearValidate()
}

function openEdit(row: Schedule) {
  editing.value = true
  form.value = {
    id: row.id,
    centerCode: row.centerCode,
    appointDate: row.appointDate,
    timeSlotCode: row.timeSlotCode,
    resourceType: (row as any).resourceType || 'CENTER_SLOT',
    resourceCode: (row as any).resourceCode || '',
    capacityTotal: row.capacityTotal,
    status: row.status,
    departmentCode: (row as any).departmentCode || '',
    departmentName: (row as any).departmentName || '',
  }
  dialogVisible.value = true
  loadOptions()
  formRef.value?.clearValidate()
}

function handleCenterChange(centerCode: string) {
  form.value.centerCode = centerCode
  getDepartmentOptions({ centerCode }).then((res) => { departments.value = res.data || [] })
  getTimeSlotOptions({ centerCode }).then((res) => { timeSlots.value = res.data || [] })
}

function handleDepartmentChange(value: string) {
  const dept = departments.value.find((item) => `${item.centerCode}|${item.departmentCode}` === value)
  if (!dept) return
  form.value.centerCode = dept.centerCode
  form.value.departmentCode = dept.departmentCode
  form.value.departmentName = dept.departmentName
  getTimeSlotOptions({ centerCode: dept.centerCode, departmentCode: dept.departmentCode }).then((res) => { timeSlots.value = res.data || [] })
}

function handleTimeSlotChange(code: string) {
  const slot = timeSlots.value.find((item) => item.timeSlotCode === code)
  if (!slot) return
  form.value.resourceType = slot.resourceType || form.value.resourceType
  form.value.resourceCode = slot.resourceCode || form.value.resourceCode
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (editing.value) {
      await updateOperatorSchedule(form.value.id, form.value)
    } else {
      await createOperatorSchedule(form.value)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadSchedules()
  } finally {
    submitLoading.value = false
  }
}

loadSchedules()
loadOptions()
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

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="filterDept" placeholder="科室编码筛选" style="width: 180px;" clearable />
      <el-button type="primary" @click="loadSchedules">筛选</el-button>
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
        <el-table-column prop="departmentCode" label="科室" width="120">
          <template #default="{ row }">
            {{ row.departmentName || row.departmentCode || '-' }}
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑排班' : '新建排班'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="中心编码" prop="centerCode">
          <el-select v-model="form.centerCode" filterable style="width: 100%;" @change="handleCenterChange">
            <el-option
              v-for="center in centers"
              :key="center.centerCode"
              :label="`${center.centerCode} / ${center.centerName}`"
              :value="center.centerCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="appointDate">
          <el-date-picker v-model="form.appointDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="时段编码" prop="timeSlotCode">
          <el-select v-model="form.timeSlotCode" filterable allow-create style="width: 100%;" @change="handleTimeSlotChange">
            <el-option
              v-for="slot in timeSlots"
              :key="slot.timeSlotCode"
              :label="slot.timeSlotCode"
              :value="slot.timeSlotCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" style="width: 100%;">
            <el-option label="中心号源" value="CENTER_SLOT" />
            <el-option label="科室号源" value="DEPARTMENT_SLOT" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源编码" prop="resourceCode">
          <el-input v-model="form.resourceCode" />
        </el-form-item>
        <el-form-item label="科室编码" prop="departmentCode">
          <el-select
            :model-value="form.centerCode && form.departmentCode ? `${form.centerCode}|${form.departmentCode}` : ''"
            filterable
            clearable
            style="width: 100%;"
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departments"
              :key="`${dept.centerCode}|${dept.departmentCode}`"
              :label="`${dept.centerCode} / ${dept.departmentCode} / ${dept.departmentName}`"
              :value="`${dept.centerCode}|${dept.departmentCode}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室名称" prop="departmentName">
          <el-input v-model="form.departmentName" disabled />
        </el-form-item>
        <el-form-item label="总容量" prop="capacityTotal">
          <el-input-number v-model="form.capacityTotal" :min="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
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

:deep(.el-tag) {
  border-radius: 999px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-line);
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

@media (max-width: 640px) {
  .filter-bar {
    flex-direction: column;
  }
}
</style>
