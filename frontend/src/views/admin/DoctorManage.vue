<script setup lang="ts">
import { ref, watch } from 'vue'
import { getAdminDoctors, updateAdminDoctor, bindDoctorDepartment, unbindDoctorDepartment } from '@/api/admin'
import { getDepartmentOptions } from '@/api/operator'
import type { DepartmentOption } from '@/api/operator'
import { ElMessage } from 'element-plus'
import { useLoading, usePagination } from '@/composables'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const doctors = ref<any[]>([])
const departments = ref<DepartmentOption[]>([])
const keyword = ref('')
const editDialog = ref(false)
const deptDialog = ref(false)
const currentDoctor = ref<any>(null)
const editForm = ref({ displayName: '', departmentCode: '', departmentName: '', specialty: '', centerCode: '', status: 1 })
const deptForm = ref({ departmentCode: '', departmentName: '', centerCode: '', isPrimary: false })

function loadDoctors() {
  execute(async () => {
    const res = await getAdminDoctors({ keyword: keyword.value || undefined, pageNum: pageNum.value, pageSize: pageSize.value })
    doctors.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

async function loadDepartments() {
  const res = await getDepartmentOptions()
  departments.value = res.data || []
}

watch([pageNum, pageSize], loadDoctors)

function handleSearch() { pageNum.value = 1; loadDoctors() }

function openEdit(doc: any) {
  currentDoctor.value = doc
  editForm.value = { displayName: doc.displayName || '', departmentCode: doc.departmentCode || '', departmentName: doc.departmentName || '', specialty: doc.specialty || '', centerCode: doc.centerCode || '', status: doc.status ?? 1 }
  editDialog.value = true
}

async function handleEditSave() {
  await updateAdminDoctor(currentDoctor.value.id, editForm.value)
  ElMessage.success('更新成功')
  editDialog.value = false
  loadDoctors()
}

function openBindDept(doc: any) {
  currentDoctor.value = doc
  deptForm.value = { departmentCode: '', departmentName: '', centerCode: '', isPrimary: false }
  deptDialog.value = true
  loadDepartments()
}

function handleDeptChange(value: string) {
  const dept = departments.value.find((item) => `${item.centerCode}|${item.departmentCode}` === value)
  if (!dept) return
  deptForm.value.centerCode = dept.centerCode
  deptForm.value.departmentCode = dept.departmentCode
  deptForm.value.departmentName = dept.departmentName
}

async function handleBindDept() {
  if (!deptForm.value.departmentCode) { ElMessage.warning('请输入科室编码'); return }
  await bindDoctorDepartment(currentDoctor.value.id, deptForm.value)
  ElMessage.success('绑定成功')
  deptDialog.value = false
  loadDoctors()
}

async function handleUnbind(relId: number) {
  await unbindDoctorDepartment(relId)
  ElMessage.success('解绑成功')
  loadDoctors()
}

loadDoctors()
loadDepartments()
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
        <el-table-column prop="departmentName" label="科室" width="120">
          <template #default="{ row }">{{ row.departmentName || row.departmentCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="specialty" label="专长" width="120" />
        <el-table-column label="绑定科室" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="dept in row.departments" :key="dept.id" size="small" style="margin-right: 4px;" closable @close="handleUnbind(dept.id)">
              {{ dept.departmentName || dept.departmentCode }}
            </el-tag>
            <el-button type="primary" link size="small" @click="openBindDept(row)">+ 绑定</el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next, jumper" background @current-change="onPageChange" @size-change="onSizeChange" />
      </div>
    </section>

    <el-dialog v-model="editDialog" title="编辑医生" width="500px">
      <el-form label-width="100px">
        <el-form-item label="姓名"><el-input v-model="editForm.displayName" /></el-form-item>
        <el-form-item label="科室编码"><el-input v-model="editForm.departmentCode" /></el-form-item>
        <el-form-item label="科室名称"><el-input v-model="editForm.departmentName" /></el-form-item>
        <el-form-item label="专长"><el-input v-model="editForm.specialty" /></el-form-item>
        <el-form-item label="中心编码"><el-input v-model="editForm.centerCode" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialog = false">取消</el-button><el-button type="primary" @click="handleEditSave">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="deptDialog" title="绑定科室" width="440px">
      <el-form label-width="100px">
        <el-form-item label="科室">
          <el-select
            :model-value="deptForm.centerCode && deptForm.departmentCode ? `${deptForm.centerCode}|${deptForm.departmentCode}` : ''"
            filterable
            placeholder="请选择中心和科室"
            style="width: 100%;"
            @change="handleDeptChange"
          >
            <el-option
              v-for="dept in departments"
              :key="`${dept.centerCode}|${dept.departmentCode}`"
              :label="`${dept.centerCode} / ${dept.departmentCode} / ${dept.departmentName}`"
              :value="`${dept.centerCode}|${dept.departmentCode}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室编码"><el-input v-model="deptForm.departmentCode" disabled /></el-form-item>
        <el-form-item label="科室名称"><el-input v-model="deptForm.departmentName" disabled /></el-form-item>
        <el-form-item label="中心编码"><el-input v-model="deptForm.centerCode" disabled /></el-form-item>
        <el-form-item label="主科室"><el-switch v-model="deptForm.isPrimary" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="deptDialog = false">取消</el-button><el-button type="primary" @click="handleBindDept">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.table-shell { padding: 20px; opacity: 0; transform: translateY(16px); transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo); &.is-mounted { opacity: 1; transform: translateY(0); } }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-line); }
</style>
