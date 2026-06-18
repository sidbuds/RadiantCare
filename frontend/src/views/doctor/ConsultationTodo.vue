<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDoctorConsultationTodo, replyConsultation } from '@/api/consultation'
import { ElMessage } from 'element-plus'

const consultations = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const dialogVisible = ref(false)
const currentNo = ref('')
const replyForm = ref({ replyContent: '', attachmentUrl: '' })
const replying = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getDoctorConsultationTodo()
    consultations.value = res.data || []
  } catch {} finally { loading.value = false }
})

function openReply(no: string) {
  currentNo.value = no
  replyForm.value = { replyContent: '', attachmentUrl: '' }
  dialogVisible.value = true
}

async function handleReply() {
  if (!replyForm.value.replyContent) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    await replyConsultation(currentNo.value, replyForm.value)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    const res: any = await getDoctorConsultationTodo()
    consultations.value = res.data || []
  } catch {} finally { replying.value = false }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CONSULTATION</span>
        <h2>待回复咨询</h2>
      </div>
      <p>及时回复用户咨询，提升服务质量和用户满意度。</p>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="consultations" v-loading="loading">
        <el-table-column prop="consultationNo" label="咨询编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.consultationNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="consultationTitle" label="标题" />
        <el-table-column prop="consultationContent" label="内容" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'REPLIED' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'REPLIED'" type="primary" link @click="openReply(row.consultationNo)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="回复咨询" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="5" placeholder="请输入回复" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">提交回复</el-button>
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

:deep(.el-tag) {
  border-radius: 999px;
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
  border: none;
}

:deep(.el-tag--warning) {
  background: var(--color-warning-light);
  color: var(--color-warning);
  border: none;
}
</style>
