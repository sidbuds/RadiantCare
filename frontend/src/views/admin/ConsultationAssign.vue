<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { assignConsultation } from '@/api/consultation'
import { ElMessage } from 'element-plus'

const consultations = ref<any[]>([])
const loading = ref(false)
const mounted = ref(false)
const dialogVisible = ref(false)
const currentNo = ref('')
const doctorId = ref<number | null>(null)

onMounted(() => {
  mounted.value = true
})

async function handleAssign() {
  if (!doctorId.value) {
    ElMessage.warning('请输入医生ID')
    return
  }
  try {
    await assignConsultation(currentNo.value, { doctorId: doctorId.value })
    ElMessage.success('分配成功')
    dialogVisible.value = false
  } catch {}
}

function openAssign(no: string) {
  currentNo.value = no
  doctorId.value = null
  dialogVisible.value = true
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CONSULTATION ASSIGN</span>
        <h2>咨询分配</h2>
      </div>
      <p>将待分配的咨询单指派给医生。</p>
    </div>

    <div class="assign-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="card-title">
        <span class="title-icon"><el-icon :size="18"><User /></el-icon></span>
        快速分配
      </div>
      <div class="assign-row">
        <el-input
          v-model="currentNo"
          placeholder="请输入咨询编号"
          size="large"
          class="assign-input"
        />
        <el-button type="primary" size="large" @click="openAssign(currentNo)">
          分配
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="分配咨询" width="440px">
      <el-form label-width="80px">
        <el-form-item label="咨询编号">
          <el-input :model-value="currentNo" disabled />
        </el-form-item>
        <el-form-item label="医生ID">
          <el-input-number v-model="doctorId" :min="1" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.assign-card {
  max-width: 600px;
  padding: 32px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  .el-button:hover .btn-arrow {
    transform: translateX(3px);
  }
}

.card-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-ink);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--color-brand-light);
  color: var(--color-brand);
}

.assign-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.assign-input {
  flex: 1;
}
</style>
