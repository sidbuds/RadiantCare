<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createOrder, getOrder, payOrder } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(false)
const paying = ref(false)
const mounted = ref(false)
const appointmentNo = route.params.no as string

async function handleCreateOrder() {
  loading.value = true
  try {
    const res: any = await createOrder({ appointmentNo })
    order.value = res.data
    ElMessage.success('订单创建成功')
  } catch {} finally { loading.value = false }
}

async function handlePay() {
  paying.value = true
  try {
    await payOrder(order.value.orderNo)
    ElMessage.success('支付成功')
    order.value.orderStatus = 'PAID'
  } catch {} finally { paying.value = false }
}

function goToGuide() {
  router.push(`/user/appointments/${appointmentNo}`)
}

onMounted(() => {
  mounted.value = true
  handleCreateOrder()
})
</script>

<template>
  <div class="page-container">
    <div class="detail-header" :class="{ 'is-mounted': mounted }">
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-if="order" class="order-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="card-header">
        <span class="section-eyebrow">ORDER CONFIRM</span>
        <h2>订单确认</h2>
      </div>

      <div class="order-status" :class="{ paid: order.orderStatus === 'PAID' }">
        <div class="status-icon">
          <el-icon :size="24"><CircleCheck v-if="order.orderStatus === 'PAID'" /><Wallet v-else /></el-icon>
        </div>
        <div class="status-text">
          <strong>{{ order.orderStatus === 'PAID' ? '支付完成' : '待支付' }}</strong>
          <span>{{ order.orderStatus === 'PAID' ? '您的订单已支付成功' : '请完成支付以确认订单' }}</span>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">订单编号</span>
          <span class="info-value mono-text">{{ order.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">订单状态</span>
          <el-tag :type="order.orderStatus === 'PAID' ? 'success' : 'warning'">{{ order.orderStatus }}</el-tag>
        </div>
        <div class="info-item">
          <span class="info-label">订单金额</span>
          <span class="info-value price-text">¥{{ order.totalAmount }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">预约编号</span>
          <span class="info-value mono-text">{{ appointmentNo }}</span>
        </div>
      </div>

      <div class="action-bar">
        <el-button v-if="order.orderStatus === 'PENDING'" type="primary" :loading="paying" @click="handlePay" class="pay-btn">
          模拟支付
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
        <el-button v-if="order.orderStatus === 'PAID'" type="primary" @click="goToGuide">
          查看预约
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
    <div v-else-if="loading" class="loading-state" v-loading="true" />
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

.order-card {
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

.order-status {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--radius-md);
  background: var(--color-warning-light);
  border: 1px solid rgba(245, 158, 11, 0.15);
  margin-bottom: 28px;

  &.paid {
    background: var(--color-success-light);
    border-color: rgba(34, 197, 94, 0.15);

    .status-icon {
      color: var(--color-success);
      background: rgba(34, 197, 94, 0.15);
    }
  }

  .status-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(245, 158, 11, 0.15);
    color: var(--color-warning);
    flex-shrink: 0;
  }

  .status-text {
    display: flex;
    flex-direction: column;
    gap: 4px;

    strong {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-ink);
    }

    span {
      font-size: 13px;
      color: var(--color-ink-muted);
    }
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

.price-text {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 900;
  color: var(--color-brand);
  text-shadow: 0 0 20px rgba(201, 164, 78, 0.2);
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

.loading-state {
  height: 300px;
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

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .order-card {
    padding: 24px;
  }
}
</style>
