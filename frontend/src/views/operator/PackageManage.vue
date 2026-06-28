<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  getOperatorPackages,
  getOperatorPackageDetail,
  createOperatorPackage,
  updateOperatorPackage,
  updateOperatorPackageStatus,
  getExamItemOptions,
  getCenterOptions,
  getDepartmentOptions,
  getOperatorPackageRoutes,
  saveOperatorPackageRoutes,
} from '@/api/operator'
import type { CenterOption, DepartmentOption, ExamItemOption, PackageFormData, PackageRouteItem } from '@/api/operator'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoading, usePagination } from '@/composables'
import type { PackageItem } from '@/types/api'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const packages = ref<PackageItem[]>([])
const itemOptions = ref<ExamItemOption[]>([])
const centers = ref<CenterOption[]>([])
const departments = ref<DepartmentOption[]>([])
const dialogVisible = ref(false)
const routeDialogVisible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const routeSaving = ref(false)
const currentPackage = ref<PackageItem | null>(null)
const routeCenterCode = ref('')
const routes = ref<PackageRouteItem[]>([])
const routeDepartments = computed(() => departments.value.filter((dept) => !dept.centerCode || dept.centerCode === routeCenterCode.value))

const form = ref({
  id: 0,
  packageCode: '',
  packageName: '',
  category: '',
  price: 0,
  status: 1,
  remark: '',
  centerCodes: [] as string[],
  items: [{ itemCode: '', itemName: '', sortNo: 1 }] as Array<{ itemCode: string; itemName: string; unit?: string; refRange?: string; sortNo: number }>,
})

const rules: FormRules = {
  packageCode: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  centerCodes: [{ required: true, type: 'array', min: 1, message: '请选择适用体检中心', trigger: 'change' }],
}

function loadPackages() {
  execute(async () => {
    const res = await getOperatorPackages({ pageNum: pageNum.value, pageSize: pageSize.value })
    packages.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

async function loadOptions() {
  const [itemRes, centerRes, deptRes] = await Promise.all([
    getExamItemOptions(),
    getCenterOptions(),
    getDepartmentOptions(),
  ])
  itemOptions.value = itemRes.data || []
  centers.value = centerRes.data || []
  departments.value = deptRes.data || []
}

watch([pageNum, pageSize], loadPackages)

function openCreate() {
  editing.value = false
  form.value = { id: 0, packageCode: '', packageName: '', category: '', price: 0, status: 1, remark: '', centerCodes: [], items: [{ itemCode: '', itemName: '', sortNo: 1 }] }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function openEdit(row: PackageItem) {
  editing.value = true
  const detail = await getOperatorPackageDetail(row.id)
  const pkg = detail.data || row
  const packageWithItems = pkg as PackageItem & Pick<PackageFormData, 'items' | 'centerCodes'>
  form.value = {
    id: pkg.id,
    packageCode: pkg.packageCode,
    packageName: pkg.packageName,
    category: pkg.category,
    price: pkg.price,
    status: pkg.status,
    remark: pkg.remark,
    centerCodes: packageWithItems.centerCodes || [],
    items: packageWithItems.items?.length ? packageWithItems.items as any : [{ itemCode: '', itemName: '', sortNo: 1 }],
  }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function addItem() {
  form.value.items.push({ itemCode: '', itemName: '', sortNo: form.value.items.length + 1 })
}

function removeItem(index: number) {
  form.value.items.splice(index, 1)
}

function handleItemChange(index: number, itemCode: string) {
  const option = itemOptions.value.find((item) => item.itemCode === itemCode)
  if (!option) return
  form.value.items[index] = {
    ...form.value.items[index],
    itemCode: option.itemCode,
    itemName: option.itemName,
    unit: option.unit,
    refRange: option.refRange,
  }
}

async function handleToggleStatus(row: PackageItem) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateOperatorPackageStatus(row.id, nextStatus)
  ElMessage.success(nextStatus === 1 ? '已启用' : '已停用')
  loadPackages()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (editing.value) {
      await updateOperatorPackage(form.value.id, form.value)
    } else {
      await createOperatorPackage(form.value)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadPackages()
  } finally {
    submitLoading.value = false
  }
}

async function openRoutes(row: PackageItem) {
  const detail = await getOperatorPackageDetail(row.id)
  currentPackage.value = detail.data || row
  const centerCodes = currentPackage.value.centerCodes || []
  routeCenterCode.value = centerCodes[0] || ''
  await loadRoutes()
  routeDialogVisible.value = true
}

async function loadRoutes() {
  if (!currentPackage.value || !routeCenterCode.value) {
    routes.value = []
    return
  }
  const deptRes = await getDepartmentOptions({ centerCode: routeCenterCode.value })
  departments.value = deptRes.data || []
  const res = await getOperatorPackageRoutes(currentPackage.value.id, routeCenterCode.value)
  const existing = res.data || []
  const byItemCode = new Map(existing.map((item) => [item.itemCode, item]))
  routes.value = (currentPackage.value.items || []).map((item, index) => ({
    itemCode: item.itemCode,
    itemName: item.itemName,
    departmentCode: byItemCode.get(item.itemCode)?.departmentCode || '',
    departmentName: byItemCode.get(item.itemCode)?.departmentName || '',
    roomNo: byItemCode.get(item.itemCode)?.roomNo || '',
    floorNo: byItemCode.get(item.itemCode)?.floorNo || '',
    buildingNo: byItemCode.get(item.itemCode)?.buildingNo || '',
    routeSort: byItemCode.get(item.itemCode)?.routeSort || index + 1,
    guideText: byItemCode.get(item.itemCode)?.guideText || '',
    needEmptyStomach: byItemCode.get(item.itemCode)?.needEmptyStomach || 0,
    status: 1,
  }))
}

function handleRouteDeptChange(route: PackageRouteItem, value: string) {
  const dept = routeDepartments.value.find((item) => item.departmentCode === value)
  route.departmentCode = value
  route.departmentName = dept?.departmentName || ''
}

async function handleSaveRoutes() {
  if (!currentPackage.value || !routeCenterCode.value) return
  routeSaving.value = true
  try {
    await saveOperatorPackageRoutes(currentPackage.value.id, { centerCode: routeCenterCode.value, routes: routes.value })
    ElMessage.success('导引路线已保存')
  } finally {
    routeSaving.value = false
  }
}

loadPackages()
loadOptions()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">PACKAGE</span>
        <h2>套餐管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>
        新建套餐
      </el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="packages" v-loading="loading">
        <el-table-column prop="packageCode" label="编码" width="120" />
        <el-table-column prop="packageName" label="名称" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column label="适用中心" min-width="180">
          <template #default="{ row }">{{ (row.centerCodes || []).join('、') || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="openRoutes(row)">路线</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑套餐' : '新建套餐'" width="min(820px, 92vw)">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="套餐编码" prop="packageCode"><el-input v-model="form.packageCode" :disabled="editing" /></el-form-item>
        <el-form-item label="套餐名称" prop="packageName"><el-input v-model="form.packageName" /></el-form-item>
        <el-form-item label="适用中心" prop="centerCodes">
          <el-select v-model="form.centerCodes" multiple filterable style="width: 100%;">
            <el-option v-for="center in centers" :key="center.centerCode" :label="`${center.centerCode} / ${center.centerName}`" :value="center.centerCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-form-item label="检查项" class="package-items-form">
          <div v-for="(item, index) in form.items" :key="index" class="item-row">
            <el-select v-model="item.itemCode" filterable placeholder="检查项" class="item-code-select" @change="(value: string) => handleItemChange(index, value)">
              <el-option v-for="option in itemOptions" :key="option.itemCode" :label="`${option.itemCode} / ${option.itemName}`" :value="option.itemCode" />
            </el-select>
            <el-input v-model="item.itemName" placeholder="名称" class="item-name-input" disabled />
            <el-input-number v-model="item.sortNo" placeholder="排序" class="item-sort-input" :controls="false" />
            <el-button type="danger" :icon="'Delete'" circle size="small" class="item-delete-button" @click="removeItem(index)" />
          </div>
          <el-button size="small" @click="addItem">添加项目</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="routeDialogVisible" title="导引路线配置" width="min(1080px, 96vw)">
      <div class="route-toolbar">
        <el-select v-model="routeCenterCode" placeholder="选择体检中心" style="width: 260px;" @change="loadRoutes">
          <el-option v-for="code in currentPackage?.centerCodes || []" :key="code" :label="code" :value="code" />
        </el-select>
      </div>
      <el-table :data="routes">
        <el-table-column prop="itemName" label="检查项" min-width="140" />
        <el-table-column label="科室" min-width="170">
          <template #default="{ row }">
            <el-select v-model="row.departmentCode" filterable @change="(value: string) => handleRouteDeptChange(row, value)">
              <el-option v-for="dept in routeDepartments" :key="`${dept.centerCode || routeCenterCode}-${dept.departmentCode}`" :label="`${dept.departmentCode} / ${dept.departmentName}`" :value="dept.departmentCode" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="楼栋" width="110"><template #default="{ row }"><el-input v-model="row.buildingNo" /></template></el-table-column>
        <el-table-column label="楼层" width="110"><template #default="{ row }"><el-input v-model="row.floorNo" /></template></el-table-column>
        <el-table-column label="房间" width="110"><template #default="{ row }"><el-input v-model="row.roomNo" /></template></el-table-column>
        <el-table-column label="排序" width="90"><template #default="{ row }"><el-input-number v-model="row.routeSort" :controls="false" /></template></el-table-column>
        <el-table-column label="空腹" width="80"><template #default="{ row }"><el-switch v-model="row.needEmptyStomach" :active-value="1" :inactive-value="0" /></template></el-table-column>
        <el-table-column label="说明" min-width="180"><template #default="{ row }"><el-input v-model="row.guideText" /></template></el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="routeDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="routeSaving" @click="handleSaveRoutes">保存路线</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.table-shell { padding: 20px; opacity: 0; transform: translateY(16px); transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo); &.is-mounted { opacity: 1; transform: translateY(0); } }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-line); }
.package-items-form :deep(.el-form-item__content) { min-width: 0; }
.item-row { display: grid; grid-template-columns: minmax(220px, 1fr) minmax(140px, 180px) 88px 32px; gap: 8px; width: 100%; margin-bottom: 8px; align-items: center; }
.item-code-select, .item-name-input, .item-sort-input { width: 100%; min-width: 0; }
.item-delete-button { justify-self: center; }
.route-toolbar { margin-bottom: 12px; display: flex; justify-content: flex-start; }
@media (max-width: 720px) { .item-row { grid-template-columns: minmax(0, 1fr) 88px 32px; } .item-name-input { grid-column: 1 / -1; } }
</style>
