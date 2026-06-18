<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFaq } from '@/api/public'

const faqList = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getFaq()
    faqList.value = res.data || []
  } catch {} finally { loading.value = false }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">FAQ</span>
        <h2>常见问题</h2>
      </div>
      <p>找到您关心的问题解答。</p>
    </div>

    <div v-loading="loading">
      <div v-if="faqList.length > 0" class="faq-list" :class="{ 'is-mounted': mounted }">
        <div
          v-for="(faq, index) in faqList"
          :key="index"
          class="faq-item glass-panel"
          :class="{ 'is-mounted': mounted }"
          :style="{ '--delay': `${0.1 + index * 0.08}s` }"
        >
          <el-collapse accordion>
            <el-collapse-item :name="index">
              <template #title>
                <div class="faq-question">
                  <span class="question-icon">
                    <span class="q-mark">Q</span>
                  </span>
                  <span>{{ faq.question }}</span>
                </div>
              </template>
              <div class="faq-answer">
                <span class="a-mark">A</span>
                <span>{{ faq.answer }}</span>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
      <el-empty v-if="!loading && faqList.length === 0" description="暂无常见问题" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.faq-item {
  padding: 0;
  overflow: hidden;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.35s var(--ease-out-expo), transform 0.35s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  :deep(.el-collapse) {
    border: none;
    background: transparent;
  }

  :deep(.el-collapse-item) {
    border: none;
  }

  :deep(.el-collapse-item__header) {
    padding: 0 24px;
    font-size: 14px;
    background: transparent;
    border-bottom: none;
    min-height: 60px;
    color: var(--color-ink);
    font-weight: 500;
  }

  :deep(.el-collapse-item__wrap) {
    border-bottom: none;
    background: transparent;
  }

  :deep(.el-collapse-item__content) {
    padding: 0 24px 20px;
  }

  :deep(.el-collapse-item__arrow) {
    color: var(--color-ink-muted);
  }
}

.faq-question {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
}

.question-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-xs);
  background: var(--color-brand-light);
  flex-shrink: 0;
}

.q-mark {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 700;
  color: var(--color-brand);
}

.faq-answer {
  display: flex;
  gap: 14px;
  color: var(--color-ink-soft);
  line-height: 1.8;
  font-size: 13px;
  padding: 4px 0;
}

.a-mark {
  display: inline-flex;
  align-items: flex-start;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-xs);
  background: var(--color-accent-light);
  color: var(--color-accent);
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
</style>
