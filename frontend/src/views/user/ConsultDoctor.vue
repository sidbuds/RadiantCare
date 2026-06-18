<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { createConsultation, getMyConsultations } from '@/api/consultation'
import { ElMessage } from 'element-plus'

const consultations = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const dialogVisible = ref(false)
const form = ref({
  reportNo: '',
  sourceType: 'REPORT_DETAIL',
  consultationType: 'ABNORMAL_INDEX',
  consultationTitle: '',
  consultationContent: '',
  priorityLevel: 1,
})

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getMyConsultations()
    consultations.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (!form.value.consultationTitle || !form.value.consultationContent) {
    ElMessage.warning('请填写完整咨询信息')
    return
  }

  try {
    await createConsultation(form.value)
    ElMessage.success('咨询已提交')
    dialogVisible.value = false
    const res: any = await getMyConsultations()
    consultations.value = res.data || []
  } catch {
    // handled by interceptor
  }
}

function openDialog() {
  form.value = {
    reportNo: '',
    sourceType: 'REPORT_DETAIL',
    consultationType: 'ABNORMAL_INDEX',
    consultationTitle: '',
    consultationContent: '',
    priorityLevel: 1,
  }
  dialogVisible.value = true
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">DOCTOR CONSULT</span>
        <h2>咨询医生</h2>
      </div>
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>
        发起咨询
      </el-button>
    </div>

    <section class="intro-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="intro-content">
        <strong>体检报告有疑问？在线咨询医生。</strong>
        <p>针对报告中的异常指标，提交咨询后由专业医生为您解答。</p>
      </div>
    </section>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="consultations" v-loading="loading">
        <el-table-column prop="consultationNo" label="咨询编号" min-width="180">
          <template #default="{ row }">
            <span class="mono-text">{{ row.consultationNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="consultationTitle" label="标题" min-width="220" />
        <el-table-column prop="consultationType" label="类型" min-width="140" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'REPLIED' ? 'success' : 'warning'">
              {{ row.status === 'REPLIED' ? '已回复' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="发起咨询" width="560px" class="consult-dialog">
      <el-form :model="form" label-width="96px">
        <el-form-item label="关联报告">
          <el-input v-model="form.reportNo" placeholder="报告编号，可选" />
        </el-form-item>
        <el-form-item label="咨询标题">
          <el-input v-model="form.consultationTitle" placeholder="比如：血脂异常后续需要注意什么" />
        </el-form-item>
        <el-form-item label="咨询内容">
          <el-input v-model="form.consultationContent" type="textarea" :rows="5" placeholder="把异常项、困惑点和想确认的问题写清楚" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交咨询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.intro-card {
  padding: 28px 32px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  strong {
    display: block;
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-ink);
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    margin-top: 10px;
    color: var(--color-ink-muted);
    line-height: 1.8;
    font-size: 14px;
  }
}

.table-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
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
