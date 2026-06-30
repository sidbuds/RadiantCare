<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { get, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const configs = ref<any[]>([])
const loading = ref(false)
const editDialog = ref(false)
const activeGroup = ref('')
const editForm = ref({ id: 0, configKey: '', configValue: '', remark: '' })

const groups = [
  { label: '全部', value: '' },
  { label: 'AI API 配置', value: 'AI_API' },
  { label: '预约配置', value: 'APPOINTMENT' },
  { label: '排班配置', value: 'SCHEDULE' },
]

const isSecretConfig = computed(() => editForm.value.configKey === 'ai.api.key')

async function loadConfigs() {
  loading.value = true
  try {
    const res: any = await get('/admin/configs', {
      params: { configGroup: activeGroup.value || undefined, pageSize: 200 },
    })
    configs.value = res.data?.list || []
  } finally {
    loading.value = false
  }
}

function openEdit(row: any) {
  editForm.value = {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configKey === 'ai.api.key' ? '' : row.configValue || '',
    remark: row.remark || '',
  }
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
      <div>
        <span class="section-eyebrow">SYSTEM CONFIG</span>
        <h2>系统配置</h2>
      </div>
    </div>

    <div class="filter-bar">
      <el-segmented v-model="activeGroup" :options="groups" @change="loadConfigs" />
    </div>

    <section class="table-shell data-card" v-loading="loading">
      <el-table :data="configs">
        <el-table-column prop="configGroup" label="分组" width="140" />
        <el-table-column prop="configKey" label="配置键" min-width="240" />
        <el-table-column prop="configValue" label="配置值" min-width="220" />
        <el-table-column prop="dataType" label="类型" width="100" />
        <el-table-column prop="remark" label="备注" min-width="220" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="editDialog" title="编辑配置" width="520px">
      <el-form label-width="90px">
        <el-form-item label="配置键">
          <el-input :model-value="editForm.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input
            v-model="editForm.configValue"
            :type="isSecretConfig ? 'password' : 'textarea'"
            :rows="isSecretConfig ? undefined : 3"
            :placeholder="isSecretConfig ? '留空不修改当前 API Key' : ''"
            show-password
          />
        </el-form-item>
        <el-alert
          v-if="isSecretConfig"
          title="API Key 已脱敏显示；保存时留空不会覆盖旧值。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.filter-bar {
  margin-bottom: 16px;
}

.table-shell {
  padding: 20px;
}
</style>
