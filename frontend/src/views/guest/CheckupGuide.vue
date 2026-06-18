<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCheckupGuide } from '@/api/public'

const guide = ref<any>(null)
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getCheckupGuide()
    guide.value = res.data
  } catch {} finally { loading.value = false }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CHECKUP GUIDE</span>
        <h2>体检流程</h2>
      </div>
      <p>了解体检的完整流程，做好充分准备。</p>
    </div>

    <div v-loading="loading">
      <template v-if="guide">
        <div class="guide-timeline glass-panel" :class="{ 'is-mounted': mounted }">
          <div
            v-for="(step, index) in (guide.steps as any[])"
            :key="index"
            class="timeline-step"
            :class="{ 'is-mounted': mounted }"
            :style="{ '--delay': `${0.2 + index * 0.12}s` }"
          >
            <div class="step-marker">
              <span class="step-number">{{ index + 1 }}</span>
              <div v-if="index < guide.steps.length - 1" class="step-line"></div>
            </div>
            <div class="step-content">
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>

        <div class="tips-card glass-panel" :class="{ 'is-mounted': mounted }">
          <div class="tips-header">
            <span class="tips-icon"><el-icon :size="18"><InfoFilled /></el-icon></span>
            <span class="tips-title">温馨提示</span>
          </div>
          <div class="tips-body">
            <p>{{ guide.tips }}</p>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped lang="scss">
.guide-timeline {
  padding: 40px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
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

  &:not(:last-child) {
    margin-bottom: 0;
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
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.step-line {
  width: 2px;
  flex: 1;
  min-height: 24px;
  background: linear-gradient(180deg, var(--color-brand-muted), var(--color-line));
  margin: 4px 0;
}

.step-content {
  padding-bottom: 28px;

  h3 {
    font-family: var(--font-display);
    font-size: 17px;
    font-weight: 700;
    color: var(--color-ink);
    margin-bottom: 8px;
    line-height: 1;
    padding-top: 6px;
  }

  p {
    color: var(--color-ink-muted);
    line-height: 1.75;
    font-size: 13px;
  }
}

.tips-card {
  padding: 28px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.5s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.tips-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.tips-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 700;
  color: var(--color-ink);
}

.tips-body {
  p {
    color: var(--color-ink-soft);
    line-height: 1.85;
    font-size: 14px;
    padding: 18px 22px;
    background: var(--color-panel);
    border-radius: var(--radius-md);
    border: 1px solid var(--color-line);
  }
}

@media (max-width: 768px) {
  .guide-timeline,
  .tips-card {
    padding: 24px;
  }
}
</style>
