<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createOrder, getOrder, payOrder, cancelOrder, applyRefund } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const loading = ref(false)
const paying = ref(false)
const canceling = ref(false)
const mounted = ref(false)
const appointmentNo = route.params.no as string
const remainingSeconds = ref(0)
let countdownTimer: number | undefined

const refundReasons = ['行程有变', '身体原因', '套餐不合适', '时间冲突', '其他']
const selectedReason = ref('')
const customReason = ref('')
const showRefundDialog = ref(false)

async function handleCreateOrder() {
  loading.value = true
  try {
    const res: any = await createOrder({ appointmentNo })
    order.value = normalizeOrder(res.data)
    startCountdown(order.value)
    if (!res.data.isExisting) {
      ElMessage.success('订单创建成功')
    }
  } catch {} finally { loading.value = false }
}

async function handlePay() {
  if (remainingSeconds.value <= 0 || order.value.status === 'CANCELED') {
    ElMessage.warning('订单已超时，请重新预约')
    return
  }
  paying.value = true
  try {
    await payOrder(order.value.orderNo)
    ElMessage.success('支付成功')
    order.value.status = 'PAID'
  } catch {} finally { paying.value = false }
}

async function handleCancelOrder() {
  if (!order.value?.orderNo || order.value.status !== 'PENDING') return
  try {
    await ElMessageBox.confirm('取消后该预约也会同步取消，是否继续？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  canceling.value = true
  try {
    await cancelOrder(order.value.orderNo)
    stopCountdown()
    remainingSeconds.value = 0
    order.value.status = 'CANCELED'
    ElMessage.success('订单已取消')
  } catch {} finally {
    canceling.value = false
  }
}

function openRefundDialog() {
  selectedReason.value = ''
  customReason.value = ''
  showRefundDialog.value = true
}

async function submitRefund() {
  const reason = selectedReason.value === '其他' ? customReason.value : selectedReason.value
  if (!reason || !reason.trim()) {
    ElMessage.warning('请选择或输入退款原因')
    return
  }
  try {
    await applyRefund(order.value.orderNo, { reason: reason.trim() })
    ElMessage.success('退款申请已提交')
    order.value.status = 'REFUNDING'
    showRefundDialog.value = false
  } catch {}
}

function goToGuide() {
  router.push(`/user/appointments/${appointmentNo}`)
}

function normalizeOrder(data: any) {
  if (!data) return data
  return {
    ...data,
    status: typeof data.status === 'number' ? resolveStatusText(data.status) : (data.status || resolveStatusText(data.orderStatus)),
  }
}

function resolveStatusText(status: number | undefined) {
  if (status === 0) return 'PENDING'
  if (status === 1) return 'PAID'
  if (status === 2) return 'REFUNDED'
  if (status === 3) return 'REFUNDING'
  if (status === 4) return 'COMPLETED'
  if (status === 5) return 'CANCELED'
  return 'PENDING'
}

function calcRemainingSeconds(data: any) {
  if (typeof data?.remainingSeconds === 'number') {
    return Math.max(Math.floor(data.remainingSeconds), 0)
  }
  if (data?.expireTime) {
    const expire = new Date(data.expireTime).getTime()
    if (!Number.isNaN(expire)) {
      return Math.max(Math.floor((expire - Date.now()) / 1000), 0)
    }
  }
  return 0
}

function startCountdown(data: any) {
  stopCountdown()
  remainingSeconds.value = calcRemainingSeconds(data)
  if (order.value?.status !== 'PENDING') return
  countdownTimer = window.setInterval(async () => {
    remainingSeconds.value = Math.max(remainingSeconds.value - 1, 0)
    if (remainingSeconds.value <= 0) {
      stopCountdown()
      await refreshOrderAfterTimeout()
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = undefined
  }
}

async function refreshOrderAfterTimeout() {
  if (!order.value?.orderNo) return
  try {
    const res: any = await getOrder(order.value.orderNo)
    order.value = normalizeOrder(res.data)
    if (order.value.status === 'PENDING') {
      order.value.status = 'CANCELED'
    }
    ElMessage.warning('订单已超时自动取消')
  } catch {
    order.value.status = 'CANCELED'
  }
}

function formatCountdown(seconds: number) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

onMounted(() => {
  mounted.value = true
  handleCreateOrder()
})

onBeforeUnmount(stopCountdown)
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

      <div class="order-status" :class="{ paid: order.status === 'PAID', refunding: order.status === 'REFUNDING', canceled: order.status === 'CANCELED' }">
        <div class="status-icon">
          <el-icon :size="24">
            <CircleCheck v-if="order.status === 'PAID'" />
            <Clock v-else-if="order.status === 'REFUNDING'" />
            <CircleClose v-else-if="order.status === 'CANCELED'" />
            <Wallet v-else />
          </el-icon>
        </div>
        <div class="status-text">
          <strong>{{ order.status === 'PAID' ? '支付完成' : order.status === 'REFUNDING' ? '退款申请中' : '待支付' }}</strong>
          <span>{{ order.status === 'PAID' ? '您的订单已支付成功' : order.status === 'REFUNDING' ? '退款申请正在审核中，请等待运营处理' : '请完成支付以确认订单' }}</span>
        </div>
      </div>

      <div v-if="order.status === 'PENDING'" class="countdown-box">
        <span>支付剩余时间</span>
        <strong>{{ formatCountdown(remainingSeconds) }}</strong>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">订单编号</span>
          <span class="info-value mono-text">{{ order.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">订单状态</span>
          <el-tag :type="order.status === 'PAID' ? 'success' : order.status === 'REFUNDING' ? 'warning' : 'info'">
            {{ order.status === 'PAID' ? '已支付' : order.status === 'REFUNDING' ? '退款中' : '待支付' }}
          </el-tag>
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
        <el-button v-if="order.status === 'PENDING'" type="primary" :loading="paying" :disabled="remainingSeconds <= 0" @click="handlePay" class="pay-btn">
          模拟支付
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
        <el-button v-if="order.status === 'PENDING'" :loading="canceling" @click="handleCancelOrder">
          取消订单
        </el-button>
        <el-button v-if="order.status === 'PAID'" type="primary" @click="goToGuide">
          查看预约
          <el-icon class="btn-arrow"><ArrowRight /></el-icon>
        </el-button>
        <el-button v-if="order.status === 'PAID'" type="danger" @click="openRefundDialog">
          申请退款
        </el-button>
      </div>
    </div>
    <div v-else-if="loading" class="loading-state" v-loading="true" />

    <el-dialog v-model="showRefundDialog" title="申请退款" width="400px">
      <div class="refund-reason-list">
        <p class="refund-hint">请选择退款原因：</p>
        <el-radio-group v-model="selectedReason" class="reason-group">
          <el-radio v-for="r in refundReasons" :key="r" :value="r" border class="reason-radio">{{ r }}</el-radio>
        </el-radio-group>
        <el-input
          v-if="selectedReason === '其他'"
          v-model="customReason"
          type="textarea"
          :rows="3"
          placeholder="请输入退款原因"
          style="margin-top: 12px;"
        />
      </div>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>
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

  &.refunding {
    background: var(--color-warning-light);
    border-color: rgba(245, 158, 11, 0.2);

    .status-icon {
      color: var(--color-warning);
      background: rgba(245, 158, 11, 0.15);
    }
  }

  &.canceled {
    background: var(--color-danger-light);
    border-color: rgba(239, 68, 68, 0.18);

    .status-icon {
      color: var(--color-danger);
      background: rgba(239, 68, 68, 0.12);
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

.countdown-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 24px;
  border: 1px solid rgba(245, 158, 11, 0.18);
  border-radius: var(--radius-sm);
  background: rgba(245, 158, 11, 0.08);

  span {
    font-size: 13px;
    color: var(--color-ink-muted);
  }

  strong {
    font-family: var(--font-display);
    font-size: 24px;
    color: var(--color-warning);
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

:deep(.el-tag--info) {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-ink-muted);
  border: none;
}

.refund-reason-list {
  .refund-hint {
    margin-bottom: 12px;
    font-size: 14px;
    color: var(--color-ink-soft);
  }

  .reason-group {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .reason-radio {
      margin: 0;
      width: 100%;
    }
  }
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
