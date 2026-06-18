<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getExamTaskGuide } from '@/api/exam'

const route = useRoute()
const router = useRouter()
const guide = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getExamTaskGuide(route.params.taskNo as string)
    guide.value = res.data || []
  } catch {} finally { loading.value = false }
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

    <div class="guide-card glass-panel" :class="{ 'is-mounted': mounted }" v-loading="loading">
      <div class="card-header">
        <span class="section-eyebrow">EXAM ROUTE</span>
        <h2>导检路线</h2>
        <p>按照以下顺序完成各科室检查。</p>
      </div>

      <div class="timeline">
        <div
          v-for="(item, index) in guide"
          :key="index"
          class="timeline-step"
          :class="{ 'is-mounted': mounted }"
          :style="{ '--delay': `${0.2 + index * 0.1}s` }"
        >
          <div class="step-marker">
            <span class="step-number">{{ index + 1 }}</span>
            <div v-if="index < guide.length - 1" class="step-line"></div>
          </div>
          <div class="step-card">
            <div class="step-header">
              <h4>{{ item.departmentName }}</h4>
              <el-tag v-if="item.needEmptyStomach" type="warning" size="small">需空腹</el-tag>
            </div>
            <p class="step-item">{{ item.itemName }}</p>
            <p class="step-location">
              <el-icon :size="14"><Location /></el-icon>
              {{ item.buildingNo }} {{ item.floorNo }} {{ item.roomNo }}
            </p>
            <p class="step-desc">{{ item.guideText }}</p>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && guide.length === 0" description="暂无导检路线" />
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

.guide-card {
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
  margin-bottom: 32px;

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

  p {
    margin-top: 8px;
    color: var(--color-ink-muted);
    font-size: 14px;
  }
}

.timeline {
  display: flex;
  flex-direction: column;
}

.timeline-step {
  display: flex;
  gap: 20px;
  opacity: 0;
  transform: translateX(-16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateX(0);
    transition-delay: var(--delay);
  }
}

.step-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.step-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-deep));
  color: var(--color-on-brand);
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(201, 164, 78, 0.3);
  position: relative;
  z-index: 1;
}

.step-line {
  width: 2px;
  flex: 1;
  min-height: 16px;
  background: linear-gradient(180deg, var(--color-brand-muted), var(--color-line));
  margin: 4px 0;
}

.step-card {
  flex: 1;
  padding: 20px 24px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-panel);

  margin-bottom: 12px;
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--color-line-strong);
    background: rgba(255,255,255,0.04);
  }
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  h4 {
    font-family: var(--font-display);
    font-size: 17px;
    font-weight: 700;
    color: var(--color-ink);
    margin: 0;
  }
}

.step-item {
  font-size: 14px;
  color: var(--color-ink-soft);
  margin: 0 0 8px;
}

.step-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-ink-muted);
  margin: 0 0 8px;
}

.step-desc {
  font-size: 13px;
  color: var(--color-ink-muted);
  line-height: 1.7;
  margin: 0;
}

:deep(.el-tag--warning) {
  background: var(--color-warning-light);
  color: var(--color-warning);
  border: none;
}

@media (max-width: 768px) {
  .guide-card {
    padding: 24px;
  }
}
</style>
