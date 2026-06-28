<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminDoctors, updateAdminDoctor, bindDoctorDepartment, unbindDoctorDepartment } from '@/api/admin'
import { getCenterOptions, getDepartmentOptions } from '@/api/operator'
import type { CenterOption, DepartmentOption } from '@/api/operator'
import { useLoading, usePagination } from '@/composables'

interface DoctorRelation {
  id: number
  centerCode: string
  departmentCode: string
  departmentName: string
  isPrimary?: number
}

interface DoctorRow {
  id: number
  username: string
  displayName: string
  centerCode?: string
  departmentCode?: string
  departmentName?: string
  specialty?: string
  status: number
  departments?: DoctorRelation[]
}

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const doctors = ref<DoctorRow[]>([])
const centers = ref<CenterOption[]>([])
const editDepartments = ref<DepartmentOption[]>([])
const bindDepartments = ref<DepartmentOption[]>([])
const keyword = ref('')
const editDialog = ref(false)
const deptDialog = ref(false)
const deptSaving = ref(false)
const currentDoctor = ref<DoctorRow | null>(null)
const editForm = ref({ displayName: '', departmentCode: '', departmentName: '', specialty: '', centerCode: '', status: 1 })
const deptForm = ref({ departmentCode: '', departmentName: '', centerCode: '', isPrimary: false })

const existingCenterCode = computed(() => {
  const departments = currentDoctor.value?.departments || []
  return departments.length > 0 ? departments[0].centerCode : ''
})

const bindCenterLocked = computed(() => !!existingCenterCode.value)

function centerLabel(centerCode?: string | null) {
  if (!centerCode) return '-'
  const center = centers.value.find((item) => item.centerCode === centerCode)
  return center ? `${center.centerCode} / ${center.centerName}` : centerCode
}

function departmentLabel(dept: DepartmentOption) {
  return `${dept.departmentCode} / ${dept.departmentName}`
}

function relationLabel(dept: DoctorRelation) {
  return `${centerLabel(dept.centerCode)} / ${dept.departmentCode || '-'} / ${dept.departmentName || '-'}`
}

async function loadDoctors() {
  await execute(async () => {
    const res = await getAdminDoctors({ keyword: keyword.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value })
    doctors.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

async function loadCenters() {
  const res = await getCenterOptions()
  centers.value = res.data || []
}

async function loadDepartmentsFor(centerCode: string, target: typeof editDepartments | typeof bindDepartments) {
  if (!centerCode) {
    target.value = []
    return
  }
  const res = await getDepartmentOptions({ centerCode })
  target.value = res.data || []
}

watch([pageNum, pageSize], loadDoctors)

function handleSearch() {
  pageNum.value = 1
  loadDoctors()
}

async function openEdit(doc: DoctorRow) {
  currentDoctor.value = doc
  editForm.value = {
    displayName: doc.displayName || '',
    departmentCode: doc.departmentCode || '',
    departmentName: doc.departmentName || '',
    specialty: doc.specialty || '',
    centerCode: doc.centerCode || '',
    status: doc.status ?? 1,
  }
  editDialog.value = true
  await loadCenters()
  await loadDepartmentsFor(editForm.value.centerCode, editDepartments)
}

async function handleEditCenterChange() {
  editForm.value.departmentCode = ''
  editForm.value.departmentName = ''
  await loadDepartmentsFor(editForm.value.centerCode, editDepartments)
}

function handleEditDeptChange(value: string) {
  const dept = editDepartments.value.find((item) => item.departmentCode === value)
  if (!dept) return
  editForm.value.departmentCode = dept.departmentCode
  editForm.value.departmentName = dept.departmentName
}

async function handleEditSave() {
  if (!editForm.value.centerCode || !editForm.value.departmentCode) {
    ElMessage.warning('请选择体检中心和科室')
    return
  }
  if (!currentDoctor.value) return
  await updateAdminDoctor(currentDoctor.value.id, editForm.value)
  ElMessage.success('更新成功')
  editDialog.value = false
  await loadDoctors()
}

async function openBindDept(doc: DoctorRow) {
  currentDoctor.value = doc
  const centerCode = (doc.departments && doc.departments.length > 0 ? doc.departments[0].centerCode : doc.centerCode) || ''
  deptForm.value = { departmentCode: '', departmentName: '', centerCode, isPrimary: false }
  deptDialog.value = true
  await loadCenters()
  await loadDepartmentsFor(centerCode, bindDepartments)
}

async function handleBindCenterChange() {
  deptForm.value.departmentCode = ''
  deptForm.value.departmentName = ''
  await loadDepartmentsFor(deptForm.value.centerCode, bindDepartments)
}

function handleDeptChange(value: string) {
  const dept = bindDepartments.value.find((item) => item.departmentCode === value)
  if (!dept) return
  deptForm.value.departmentCode = dept.departmentCode
  deptForm.value.departmentName = dept.departmentName
}

async function handleBindDept() {
  if (!currentDoctor.value) return
  if (!deptForm.value.centerCode) {
    ElMessage.warning('请选择体检中心')
    return
  }
  if (!deptForm.value.departmentCode) {
    ElMessage.warning('请选择科室')
    return
  }
  if (deptSaving.value) return
  deptSaving.value = true
  try {
    await bindDoctorDepartment(currentDoctor.value.id, deptForm.value)
    ElMessage.success('绑定成功')
    deptDialog.value = false
    await loadDoctors()
  } finally {
    deptSaving.value = false
  }
}

async function handleUnbind(relId: number) {
  await ElMessageBox.confirm('确认解绑该中心/科室吗？', '解绑确认', { type: 'warning' })
  await unbindDoctorDepartment(relId)
  ElMessage.success('解绑成功')
  await loadDoctors()
}

loadDoctors()
loadCenters()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">DOCTOR MANAGE</span>
        <h2>医生管理</h2>
      </div>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-input v-model="keyword" placeholder="搜索医生" style="width: 200px;" clearable />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="doctors" v-loading="loading">
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="displayName" label="姓名" width="120" />
        <el-table-column label="主中心/科室" min-width="240">
          <template #default="{ row }">
            <span>{{ centerLabel(row.centerCode) }} / {{ row.departmentCode || '-' }} / {{ row.departmentName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specialty" label="专长" width="140" />
        <el-table-column label="有效绑定中心/科室" min-width="320">
          <template #default="{ row }">
            <template v-if="row.departments?.length">
              <el-tag
                v-for="dept in row.departments"
                :key="dept.id"
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
                closable
                @close="handleUnbind(dept.id)"
              >
                {{ relationLabel(dept) }}
              </el-tag>
            </template>
            <span v-else class="muted">未绑定</span>
            <el-button type="primary" link size="small" @click="openBindDept(row)">+ 绑定</el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
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

    <el-dialog v-model="editDialog" title="编辑医生" width="500px">
      <el-form label-width="100px">
        <el-form-item label="姓名"><el-input v-model="editForm.displayName" /></el-form-item>
        <el-form-item label="体检中心">
          <el-select v-model="editForm.centerCode" filterable placeholder="请选择体检中心" style="width: 100%;" @change="handleEditCenterChange">
            <el-option v-for="center in centers" :key="center.centerCode" :label="centerLabel(center.centerCode)" :value="center.centerCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="editForm.departmentCode" :disabled="!editForm.centerCode" filterable placeholder="请选择科室" style="width: 100%;" @change="handleEditDeptChange">
            <el-option v-for="dept in editDepartments" :key="dept.departmentCode" :label="departmentLabel(dept)" :value="dept.departmentCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="中心编码"><el-input v-model="editForm.centerCode" disabled /></el-form-item>
        <el-form-item label="科室编码"><el-input v-model="editForm.departmentCode" disabled /></el-form-item>
        <el-form-item label="科室名称"><el-input v-model="editForm.departmentName" disabled /></el-form-item>
        <el-form-item label="专长"><el-input v-model="editForm.specialty" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deptDialog" title="绑定中心/科室" width="500px">
      <el-form label-width="100px">
        <el-form-item label="体检中心">
          <el-select
            v-model="deptForm.centerCode"
            :disabled="bindCenterLocked"
            filterable
            placeholder="请选择体检中心"
            style="width: 100%;"
            @change="handleBindCenterChange"
          >
            <el-option v-for="center in centers" :key="center.centerCode" :label="centerLabel(center.centerCode)" :value="center.centerCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="deptForm.departmentCode" :disabled="!deptForm.centerCode" filterable placeholder="请选择科室" style="width: 100%;" @change="handleDeptChange">
            <el-option v-for="dept in bindDepartments" :key="dept.departmentCode" :label="departmentLabel(dept)" :value="dept.departmentCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="中心编码"><el-input v-model="deptForm.centerCode" disabled /></el-form-item>
        <el-form-item label="科室编码"><el-input v-model="deptForm.departmentCode" disabled /></el-form-item>
        <el-form-item label="科室名称"><el-input v-model="deptForm.departmentName" disabled /></el-form-item>
        <el-form-item label="主科室"><el-switch v-model="deptForm.isPrimary" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialog = false">取消</el-button>
        <el-button type="primary" :loading="deptSaving" :disabled="deptSaving" @click="handleBindDept">确定</el-button>
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.muted {
  color: var(--text-secondary);
  margin-right: 8px;
}
</style>
