<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { generateExamTask } from '@/api/exam'
import { ElMessage } from 'element-plus'

const orderNo = ref('')
const loading = ref(false)
const result = ref<any>(null)
const mounted = ref(false)

onMounted(() => {
  mounted.value = true
})

async function handleGenerate() {
  if (!orderNo.value) {
    ElMessage.warning('请输入订单编号')
    return
  }
  loading.value = true
  try {
    const res: any = await generateExamTask({ orderNo: orderNo.value })
    result.value = res.data
    ElMessage.success('体检任务生成成功')
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">TASK GENERATE</span>
        <h2>体检任务生成</h2>
      </div>
      <p>输入已支付的订单编号，系统将自动为用户生成体检任务。</p>
    </div>

    <div class="form-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="card-title">
        <span class="title-icon"><el-icon :size="18"><Tickets /></el-icon></span>
        订单信息
      </div>
      <el-form label-width="100px" class="compact-form">
        <el-form-item label="订单编号">
          <el-input v-model="orderNo" placeholder="输入已支付的订单编号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleGenerate" class="submit-btn">
            生成任务
            <el-icon class="btn-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <transition name="fade-slide">
      <div v-if="result" class="result-card glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="card-title">
          <span class="result-dot"></span>
          生成结果
        </div>
        <div class="result-grid">
          <div class="result-item">
            <span class="result-label">任务编号</span>
            <span class="result-value mono-text">{{ result.taskNo }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">状态</span>
            <el-tag type="success" size="small">{{ result.status }}</el-tag>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.form-card {
  max-width: 580px;
  padding: 32px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
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

.submit-btn {
  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  &:hover .btn-arrow {
    transform: translateX(3px);
  }
}

.result-card {
  margin-top: 20px;
  max-width: 580px;
  padding: 28px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-success);
  box-shadow: 0 0 8px var(--color-success-glow);
  animation: pulse 2s ease-in-out infinite;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .result-label {
    font-size: 11px;
    font-weight: 600;
    color: var(--color-ink-faint);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .result-value {
    font-size: 15px;
    color: var(--color-ink);
    font-weight: 500;
  }
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
  border: none;
  border-radius: 999px;
}

.fade-slide-enter-active {
  transition: all 0.3s var(--ease-out-expo);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
</style>
