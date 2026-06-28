<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, post, del } from '@/api/request'
import { ElMessage } from 'element-plus'

const types = ref<any[]>([])
const items = ref<any[]>([])
const selectedType = ref('')
const typeKeyword = ref('')
const itemKeyword = ref('')
const itemStatus = ref<number | undefined>()
const loading = ref(false)
const dialogVisible = ref(false)
const typeDialogVisible = ref(false)
const newType = ref('')
const editForm = ref({ id: null as number | null, dictType: '', dictCode: '', dictName: '', sortNo: 0, status: 1, remark: '' })

async function loadTypes() {
  const res: any = await get('/admin/dicts/types', { params: { keyword: typeKeyword.value || undefined } })
  types.value = res.data?.list || []
}

async function loadItems(type = selectedType.value) {
  if (!type) return
  selectedType.value = type
  loading.value = true
  try {
    const res: any = await get('/admin/dicts/items', { params: { dictType: type, status: itemStatus.value, keyword: itemKeyword.value || undefined } })
    items.value = res.data || []
  } finally { loading.value = false }
}

function openCreateType() {
  newType.value = ''
  typeDialogVisible.value = true
}

function handleCreateType() {
  if (!newType.value.trim()) {
    ElMessage.warning('请输入字典类型')
    return
  }
  selectedType.value = newType.value.trim().toUpperCase()
  typeDialogVisible.value = false
  openCreate()
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
  await loadTypes()
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
      <el-button type="primary" @click="openCreateType">新增字典类型</el-button>
    </div>
    <div class="dict-layout">
      <section class="data-card dict-types">
        <div class="dict-toolbar">
          <el-input v-model="typeKeyword" placeholder="搜索类型" clearable @keyup.enter="loadTypes" />
          <el-button @click="loadTypes">查询</el-button>
        </div>
        <div v-for="t in types" :key="t.dict_type" class="dict-type-item" :class="{ active: t.dict_type === selectedType }" @click="loadItems(t.dict_type)">
          <span>{{ t.dict_type }}</span>
          <el-tag size="small">{{ t.dict_count }}</el-tag>
        </div>
        <el-empty v-if="types.length === 0" description="暂无字典" :image-size="60" />
      </section>
      <section class="data-card dict-items" v-loading="loading">
        <div class="item-header">
          <h4>{{ selectedType || '请选择字典类型' }}</h4>
          <el-button v-if="selectedType" type="primary" size="small" @click="openCreate">新增字典项</el-button>
        </div>
        <div v-if="selectedType" class="dict-toolbar">
          <el-input v-model="itemKeyword" placeholder="编码/名称" clearable />
          <el-select v-model="itemStatus" placeholder="状态" clearable style="width: 120px;">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
          <el-button @click="loadItems()">筛选</el-button>
        </div>
        <el-table :data="items" v-if="selectedType">
          <el-table-column prop="dictCode" label="编码" width="180" />
          <el-table-column prop="dictName" label="名称" />
          <el-table-column prop="sortNo" label="排序" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
    <el-dialog v-model="typeDialogVisible" title="新增字典类型" width="420px">
      <el-input v-model="newType" placeholder="例如 EXAM_ITEM" />
      <template #footer><el-button @click="typeDialogVisible = false">取消</el-button><el-button type="primary" @click="handleCreateType">下一步</el-button></template>
    </el-dialog>
    <el-dialog v-model="dialogVisible" title="字典项" width="480px">
      <el-form label-width="80px">
        <el-form-item label="类型"><el-input v-model="editForm.dictType" :disabled="!!editForm.id" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="editForm.dictCode" :disabled="!!editForm.id" /></el-form-item>
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
.dict-layout { display: flex; gap: 16px; }
.dict-types { width: 300px; padding: 16px; }
.dict-items { flex: 1; padding: 16px; min-width: 0; }
.dict-toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.dict-type-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border-radius: var(--radius-sm); cursor: pointer; margin-bottom: 4px; &:hover, &.active { background: var(--color-brand-light); } }
@media (max-width: 760px) { .dict-layout { flex-direction: column; } .dict-types { width: auto; } }
</style>
