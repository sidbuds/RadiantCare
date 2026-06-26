<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const configs = ref<any[]>([])
const loading = ref(false)
const editDialog = ref(false)
const editForm = ref({ id: 0, configKey: '', configValue: '', remark: '' })

async function loadConfigs() {
  loading.value = true
  try {
    const res: any = await get('/admin/configs')
    configs.value = res.data?.list || []
  } finally { loading.value = false }
}

function openEdit(row: any) {
  editForm.value = { id: row.id, configKey: row.configKey, configValue: row.configValue || '', remark: row.remark || '' }
  editDialog.value = true
}

async function handleSave() {
  await put(`/admin/configs/${editForm.value.id}?configValue=${encodeURIComponent(editForm.value.configValue)}`)
  ElMessage.success('保存成功')
  editDialog.value = false
  loadConfigs()
}

onMounted(loadConfigs)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div><span class="section-eyebrow">SYSTEM CONFIG</span><h2>系统配置</h2></div>
    </div>
    <section class="table-shell data-card" v-loading="loading">
      <el-table :data="configs">
        <el-table-column prop="configGroup" label="分组" width="120" />
        <el-table-column prop="configKey" label="配置键" width="240" />
        <el-table-column prop="configValue" label="配置值" />
        <el-table-column prop="dataType" label="类型" width="80" />
        <el-table-column prop="remark" label="备注" width="200" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }"><el-button type="primary" link @click="openEdit(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </section>
    <el-dialog v-model="editDialog" title="编辑配置" width="500px">
      <el-form label-width="80px">
        <el-form-item label="配置键"><el-input :model-value="editForm.configKey" disabled /></el-form-item>
        <el-form-item label="配置值"><el-input v-model="editForm.configValue" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialog = false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.table-shell { padding: 20px; }
</style>
