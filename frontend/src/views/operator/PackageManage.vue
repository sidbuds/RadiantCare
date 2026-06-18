<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperatorPackages, createOperatorPackage, updateOperatorPackage } from '@/api/operator'
import { ElMessage } from 'element-plus'

const packages = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref<any>({
  packageCode: '', packageName: '', category: '', price: 0, status: 1, remark: '', templateCode: '',
  items: [{ itemCode: '', itemName: '', sortNo: 1 }],
})

onMounted(async () => {
  mounted.value = true
  await loadPackages()
})

async function loadPackages() {
  loading.value = true
  try {
    const res: any = await getOperatorPackages()
    packages.value = res.data || []
  } catch {} finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  form.value = { packageCode: '', packageName: '', category: '', price: 0, status: 1, remark: '', templateCode: '', items: [{ itemCode: '', itemName: '', sortNo: 1 }] }
  dialogVisible.value = true
}

function openEdit(row: any) {
  editing.value = true
  form.value = { ...row, items: row.items || [{ itemCode: '', itemName: '', sortNo: 1 }] }
  dialogVisible.value = true
}

function addItem() {
  form.value.items.push({ itemCode: '', itemName: '', sortNo: form.value.items.length + 1 })
}

function removeItem(index: number | string) {
  form.value.items.splice(Number(index), 1)
}

async function handleSubmit() {
  try {
    if (editing.value) {
      await updateOperatorPackage(form.value.id, form.value)
    } else {
      await createOperatorPackage(form.value)
    }
    ElMessage.success(editing.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await loadPackages()
  } catch {}
}
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
    </section>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑套餐' : '新建套餐'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="套餐编码"><el-input v-model="form.packageCode" /></el-form-item>
        <el-form-item label="套餐名称"><el-input v-model="form.packageName" /></el-form-item>
        <el-form-item label="类别"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="模板编码"><el-input v-model="form.templateCode" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
