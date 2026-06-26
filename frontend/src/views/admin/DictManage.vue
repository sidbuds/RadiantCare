<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post, del } from '@/api/request'
import { ElMessage } from 'element-plus'

const types = ref<any[]>([])
const items = ref<any[]>([])
const selectedType = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const editForm = ref({ id: null as number | null, dictType: '', dictCode: '', dictName: '', sortNo: 0, status: 1, remark: '' })

async function loadTypes() {
  const res: any = await get('/admin/dicts/types')
  types.value = res.data?.list || []
}

async function loadItems(type: string) {
  selectedType.value = type
  loading.value = true
  try {
    const res: any = await get('/admin/dicts/items', { params: { dictType: type } })
    items.value = res.data || []
  } finally { loading.value = false }
}

function openCreate() {
  editForm.value = { id: null, dictType: selectedType.value, dictCode: '', dictName: '', sortNo: 0, status: 1, remark: '' }
  dialogVisible.value = true
}

function openEdit(row: any) {
  editForm.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  await post('/admin/dicts/items', editForm.value)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  if (selectedType.value) loadItems(selectedType.value)
}

async function handleDelete(id: number) {
  await del(`/admin/dicts/items/${id}`)
  ElMessage.success('已删除')
  if (selectedType.value) loadItems(selectedType.value)
}

onMounted(loadTypes)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><span class="section-eyebrow">DICTIONARY</span><h2>字典管理</h2></div>
    </div>
    <div style="display: flex; gap: 16px;">
      <section class="data-card" style="width: 280px; padding: 16px;">
        <h4 style="margin-bottom: 12px;">字典类型</h4>
        <div v-for="t in types" :key="t.dict_type" class="dict-type-item" :class="{ active: t.dict_type === selectedType }" @click="loadItems(t.dict_type)">
          <span>{{ t.dict_type }}</span>
          <el-tag size="small">{{ t.dict_count }}</el-tag>
        </div>
        <el-empty v-if="types.length === 0" description="暂无字典" :image-size="60" />
      </section>
      <section class="data-card" style="flex: 1; padding: 16px;" v-loading="loading">
        <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
          <h4>{{ selectedType || '请选择字典类型' }}</h4>
          <el-button v-if="selectedType" type="primary" size="small" @click="openCreate">新增</el-button>
        </div>
        <el-table :data="items" v-if="selectedType">
          <el-table-column prop="dictCode" label="编码" width="160" />
          <el-table-column prop="dictName" label="名称" />
          <el-table-column prop="sortNo" label="排序" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
    <el-dialog v-model="dialogVisible" title="字典项" width="480px">
      <el-form label-width="80px">
        <el-form-item label="类型"><el-input v-model="editForm.dictType" :disabled="!!editForm.id" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="editForm.dictCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="editForm.dictName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sortNo" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="editForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="editForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.dict-type-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border-radius: var(--radius-sm); cursor: pointer; margin-bottom: 4px; &:hover, &.active { background: var(--color-brand-light); } }
</style>
