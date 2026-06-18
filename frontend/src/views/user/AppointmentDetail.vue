<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAppointment, cancelAppointment } from '@/api/appointment'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appointment = ref<any>(null)
const loading = ref(true)
const mounted = ref(false)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待确认', type: 'info' },
  1: { label: '已确认', type: 'success' },
  2: { label: '已完成', type: '' },
  3: { label: '已取消', type: 'danger' },
}

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getAppointment(route.params.no as string)
    appointment.value = res.data
  } catch {} finally { loading.value = false }
})

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消此预约吗？', '取消预约')
    await cancelAppointment(route.params.no as string)
    ElMessage.success('预约已取消')
    appointment.value.status = 3
  } catch {}
}

function goToOrder() {
  router.push(`/user/orders/${appointment.value.appointmentNo}/confirm`)
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="detail-header" :class="{ 'is-mounted': mounted }">
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-if="appointment" class="detail-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="card-header">
        <span class="section-eyebrow">APPOINTMENT DETAIL</span>
        <h2>预约详情</h2>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">预约编号</span>
          <span class="info-value mono-text">{{ appointment.appointmentNo }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">状态</span>
          <el-tag :type="statusMap[appointment.status]?.type as any">{{ statusMap[appointment.status]?.label }}</el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">套餐ID</span>
          <span class="info-value">{{ appointment.packageId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">体检中心</span>
          <span class="info-value">{{ appointment.centerCode }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">预约日期</span>
          <span class="info-value">{{ appointment.appointDate }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">时段</span>
          <span class="info-value">{{ appointment.timeSlotCode }}</span>
        </div>
        <div class="info-item full-width" v-if="appointment.remark">
          <span class="info-label">备注</span>
          <span class="info-value">{{ appointment.remark }}</span>
        </div>
      </div>

      <div class="action-bar">
        <el-button v-if="appointment.status === 0 || appointment.status === 1" type="primary" @click="goToOrder">
          去下单
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
        <el-button v-if="appointment.status === 0 || appointment.status === 1" type="danger" @click="handleCancel">取消预约</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-header {
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.back-btn {
  background: var(--color-panel) !important;
  border: 1px solid var(--color-line) !important;
  color: var(--color-ink-soft) !important;

  &:hover {
    border-color: var(--color-line-strong) !important;
    color: var(--color-ink) !important;
  }
}

.detail-card {
  padding: 36px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.1s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  margin-bottom: 28px;

  h2 {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-ink);
    margin-top: 12px;
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &.full-width {
    grid-column: 1 / -1;
  }

  .info-label {
    font-size: 11px;
    font-weight: 600;
    color: var(--color-ink-faint);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .info-value {
    font-size: 15px;
    color: var(--color-ink);
    font-weight: 500;
  }
}

.action-bar {
  display: flex;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid var(--color-line);

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  .el-button:hover .btn-arrow {
    transform: translateX(3px);
  }
}

:deep(.el-tag) {
  background: var(--color-brand-light);
  color: var(--color-brand);
  border: none;
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
}

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
}

:deep(.el-tag--danger) {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .detail-card {
    padding: 24px;
  }
}
</style>
