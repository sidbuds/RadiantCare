<script setup lang="ts">
import { ref, watch } from 'vue'
import { getOperatorPackages, createOperatorPackage, updateOperatorPackage } from '@/api/operator'
import type { PackageFormData } from '@/api/operator'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoading, usePagination } from '@/composables'
import type { PackageItem } from '@/types/api'

const { loading, mounted, execute } = useLoading()
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const packages = ref<PackageItem[]>([])
const dialogVisible = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const form = ref({
  id: 0,
  packageCode: '',
  packageName: '',
  category: '',
  price: 0,
  status: 1,
  remark: '',
  templateCode: '',
  items: [{ itemCode: '', itemName: '', sortNo: 1 }] as Array<{ itemCode: string; itemName: string; sortNo: number }>,
})

const rules: FormRules = {
  packageCode: [
    { required: true, message: '请输入套餐编码', trigger: 'blur' },
    { max: 64, message: '编码最长 64 个字符', trigger: 'blur' },
  ],
  packageName: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { max: 128, message: '名称最长 128 个字符', trigger: 'blur' },
  ],
  category: [{ required: true, message: '请输入类别', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function loadPackages() {
  execute(async () => {
    const res = await getOperatorPackages({ pageNum: pageNum.value, pageSize: pageSize.value })
    packages.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadPackages)

function openCreate() {
  editing.value = false
  form.value = { id: 0, packageCode: '', packageName: '', category: '', price: 0, status: 1, remark: '', templateCode: '', items: [{ itemCode: '', itemName: '', sortNo: 1 }] }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEdit(row: PackageItem) {
  editing.value = true
  const packageWithItems = row as PackageItem & Pick<PackageFormData, 'items'>
  form.value = { ...row, items: packageWithItems.items || [{ itemCode: '', itemName: '', sortNo: 1 }] }
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function addItem() {
  form.value.items.push({ itemCode: '', itemName: '', sortNo: form.value.items.length + 1 })
}

function removeItem(index: number) {
  form.value.items.splice(index, 1)
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
  } catch (error) {
    console.error('保存套餐失败', error)
  } finally { submitLoading.value = false }
}

loadPackages()
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
        <el-table-column prop="packageCode" label="编码" width="120">
          <template #default="{ row }">
            <span class="mono-text">{{ row.packageCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="packageName" label="名称" />
        <el-table-column prop="category" label="类别" width="140" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑套餐' : '新建套餐'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐编码" prop="packageCode">
          <el-input v-model="form.packageCode" :disabled="editing" />
        </el-form-item>
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模板编码">
          <el-input v-model="form.templateCode" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <el-form-item label="检查项">
          <div v-for="(item, index) in form.items" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px;">
            <el-input v-model="item.itemCode" placeholder="编码" style="width: 120px;" />
            <el-input v-model="item.itemName" placeholder="名称" style="width: 160px;" />
            <el-input-number v-model="item.sortNo" placeholder="排序" style="width: 100px;" :controls="false" />
            <el-button type="danger" :icon="'Delete'" circle size="small" @click="removeItem(index)" />
          </div>
          <el-button size="small" @click="addItem">添加项目</el-button>
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

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
  border: none;
}
</style>
