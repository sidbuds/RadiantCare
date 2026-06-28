<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listPackages } from '@/api/public'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const packages = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const displayName = computed(() => userStore.userInfo?.displayName || '用户')

const flowSteps = [
  { title: '浏览套餐', desc: '先看适合自己的体检方案' },
  { title: '选择中心', desc: '按体检中心查看可用套餐' },
  { title: '选择时间', desc: '挑选合适的日期与时段' },
  { title: '确认预约', desc: '提交预约并完成后续安排' },
  { title: '查看任务与报告', desc: '完成体检后持续跟进结果' },
]

const quickActions = computed(() => {
  if (isLoggedIn.value) {
    return [
      { label: '我的预约', description: '查看预约进度与到检信息', icon: 'Calendar', action: () => router.push('/user/appointments') },
      { label: '导检路线', description: '查看体检引导与科室路线', icon: 'Guide', action: () => router.push('/user/exam-tasks') },
      { label: '体检报告', description: '查看结果与历年对比', icon: 'Document', action: () => router.push('/user/reports') },
      { label: '咨询医生', description: '针对异常指标在线咨询', icon: 'ChatDotRound', action: () => router.push('/user/consultations') },
    ]
  }
  return [
    { label: '浏览套餐', description: '按需求挑选适合的体检方案', icon: 'Goods', action: () => router.push('/packages') },
    { label: '查看中心', description: '了解体检地点与服务时间', icon: 'OfficeBuilding', action: () => router.push('/centers') },
    { label: '登录进入', description: '登录后管理预约与报告', icon: 'User', action: () => router.push('/login') },
  ]
})

const serviceNotes = computed(() => {
  if (isLoggedIn.value) {
    return [
      { title: '预约安排', value: '随时查看', detail: '日期、时段、到检信息集中展示' },
      { title: '报告结果', value: '统一管理', detail: '体检报告与历年对比同入口查看' },
      { title: '后续咨询', value: '继续跟进', detail: '发现异常后可直接发起在线咨询' },
    ]
  }
  return [
    { title: '服务流程', value: '清晰透明', detail: '从选套餐到查报告，每一步都可跟着走' },
    { title: '体检中心', value: '提前了解', detail: '确认地点、服务时间和到检须知' },
    { title: '报告管理', value: '随时查阅', detail: '登录后查看报告、对比历年结果' },
  ]
})

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await listPackages()
    packages.value = (res.data || []).slice(0, 3)
  } catch {
    packages.value = []
  } finally {
    loading.value = false
  }
})

function goPackage(code: string) {
  router.push(`/packages/${code}`)
}
</script>

<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-content glass-panel" :class="{ 'is-mounted': mounted }">
        <span class="section-eyebrow">{{ isLoggedIn ? 'WELCOME BACK' : 'HEALTH CHECK' }}</span>
        <h1 class="hero-title">{{ isLoggedIn ? `${displayName}，欢迎回来` : '科学体检，守护健康' }}</h1>
        <p class="hero-desc">
          {{ isLoggedIn
            ? '你的预约、报告和健康咨询都在这里统一管理。'
            : '专业体检套餐、便捷预约流程、详细报告解读，让健康管理更简单。'
          }}
        </p>

        <div class="flow-card">
          <div class="flow-card__head">
            <span class="flow-eyebrow">用户操作主链路</span>
            <strong>按这个顺序走，预约更顺手</strong>
          </div>
          <ol class="flow-list">
            <li v-for="step in flowSteps" :key="step.title">
              <span>{{ step.title }}</span>
              <small>{{ step.desc }}</small>
            </li>
          </ol>
        </div>

        <div class="hero-actions">
          <button
            v-for="(item, idx) in quickActions"
            :key="item.label"
            class="action-item"
            :class="{ 'is-mounted': mounted }"
            :style="{ '--delay': `${0.3 + idx * 0.1}s` }"
            type="button"
            @click="item.action"
          >
            <span class="action-icon">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </span>
            <div class="action-text">
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </div>
            <span class="action-arrow">
              <el-icon :size="14"><ArrowRight /></el-icon>
            </span>
          </button>
        </div>
      </div>

      <div class="hero-side" :class="{ 'is-mounted': mounted }">
        <div class="status-panel">
          <div class="status-header">
            <span class="status-label">{{ isLoggedIn ? '快捷入口' : '开始体检' }}</span>
            <div class="status-dot"></div>
          </div>
          <strong>{{ isLoggedIn ? '继续上次的健康管理' : '三步完成预约' }}</strong>
          <p>{{ isLoggedIn ? '直接进入预约、报告和咨询流程。' : '选套餐、选中心、确认预约，流程一目了然。' }}</p>
        </div>

        <div class="service-grid">
          <article
            v-for="(item, idx) in serviceNotes"
            :key="item.title"
            class="service-card"
            :class="{ 'is-mounted': mounted }"
            :style="{ '--delay': `${0.5 + idx * 0.12}s` }"
          >
            <span class="service-label">{{ item.title }}</span>
            <strong>{{ item.value }}</strong>
            <p>{{ item.detail }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="packages-section" :class="{ 'is-mounted': mounted }">
      <div class="section-head">
        <div>
          <h2>{{ isLoggedIn ? '精选体检套餐' : '热门体检套餐' }}</h2>
        </div>
        <p>{{ isLoggedIn ? '根据自身需求，选择或更换更适合的体检方案。' : '覆盖常规筛查与专项检查，适合不同人群的健康管理需求。' }}</p>
      </div>

      <div v-loading="loading" class="package-grid">
        <article
          v-for="(pkg, idx) in packages"
          :key="pkg.id"
          class="package-card data-card"
          :class="{ 'is-mounted': mounted }"
          :style="{ '--delay': `${0.6 + idx * 0.15}s` }"
          @click="goPackage(pkg.packageCode)"
        >
          <div class="package-card__top">
            <span class="package-tag">{{ pkg.category || '精选套餐' }}</span>
            <span class="package-price">¥{{ pkg.price }}</span>
          </div>
          <h3>{{ pkg.packageName }}</h3>
          <p class="remark">{{ pkg.remark || '覆盖常规检查项目，适合年度健康体检。' }}</p>
          <div class="package-card__foot">
            <span class="metric-chip">{{ isLoggedIn ? '查看详情' : '可在线预约' }}</span>
            <el-button type="primary" size="small">{{ isLoggedIn ? '查看方案' : '了解套餐' }}</el-button>
          </div>
        </article>
        <el-empty v-if="!loading && packages.length === 0" description="暂无可展示套餐" />
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  width: min(var(--content-width), calc(100% - 48px));
  margin: 0 auto;
  padding: 36px 0 72px;
}

.hero {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  margin-bottom: 32px;
  position: relative;
}

.hero-content {
  padding: 44px;
  display: flex;
  flex-direction: column;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  h1 {
    margin-top: 18px;
    font-family: var(--font-display);
    font-size: clamp(30px, 3.8vw, 48px);
    line-height: 1.1;
    font-weight: 900;
    letter-spacing: 0;
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-desc {
  max-width: 480px;
  margin-top: 16px;
  color: var(--color-ink-soft);
  font-size: 14px;
  line-height: 1.8;
}

.flow-card {
  margin-top: 22px;
  padding: 18px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
}

.flow-card__head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;

  strong {
    font-size: 15px;
    color: var(--color-ink);
    line-height: 1.3;
  }
}

.flow-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-ink-faint);
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    min-width: 0;
    padding: 12px 10px;
    border-radius: var(--radius-sm);
    border: 1px solid var(--color-line);
    background: var(--color-panel);
  }

  span {
    display: block;
    font-size: 13px;
    font-weight: 700;
    color: var(--color-ink);
    line-height: 1.3;
  }

  small {
    display: block;
    margin-top: 6px;
    color: var(--color-ink-muted);
    font-size: 11px;
    line-height: 1.45;
  }
}

.hero-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 24px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-panel);
  cursor: pointer;
  text-align: left;
  opacity: 0;
  transform: translateX(-16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo), border-color 0.15s ease, background 0.15s ease;

  &.is-mounted {
    opacity: 1;
    transform: translateX(0);
    transition-delay: var(--delay);
  }
}

.action-text {
  flex: 1;

  strong {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-ink);
    line-height: 1.3;
  }

  span {
    color: var(--color-ink-muted);
    font-size: 12px;
  }
}

.action-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  background: var(--color-brand-light);
  color: var(--color-brand);
  flex-shrink: 0;
}

.action-arrow {
  color: var(--color-brand);
}

.hero-side {
  display: grid;
  gap: 14px;
  align-content: start;
  opacity: 0;
  transform: translateX(20px);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo);
  transition-delay: 0.15s;

  &.is-mounted {
    opacity: 1;
    transform: translateX(0);
  }
}

.status-panel {
  padding: 28px;
  border-radius: var(--radius-xl);
  background: linear-gradient(145deg, #1a4a3e, #0d3b33);
  border: 1px solid rgba(58, 143, 133, 0.2);
  color: #f0ece4;
}

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(240, 236, 228, 0.5);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-success);
}

.status-panel strong {
  display: block;
  margin: 12px 0 8px;
  font-family: var(--font-display);
  font-size: 22px;
  line-height: 1.25;
  color: #ffffff;
}

.status-panel p {
  color: rgba(240, 236, 228, 0.7);
  font-size: 12px;
  line-height: 1.7;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.service-card {
  padding: 16px 14px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-panel);
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo), border-color 0.15s ease;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  .service-label {
    display: block;
    color: var(--color-ink-faint);
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  strong {
    display: block;
    margin: 8px 0 4px;
    font-family: var(--font-display);
    font-size: 16px;
    line-height: 1.2;
    color: var(--color-ink);
  }

  p {
    color: var(--color-ink-muted);
    font-size: 11px;
    line-height: 1.65;
  }
}

.packages-section {
  padding: 40px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-xl);
  background: var(--color-panel);
  box-shadow: var(--shadow-sm);
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo);
  transition-delay: 0.3s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 28px;
}

.section-head h2 {
  font-family: var(--font-display);
  font-size: clamp(24px, 2.5vw, 32px);
  line-height: 1.15;
  font-weight: 700;
  letter-spacing: 0;
}

.section-head p {
  max-width: 380px;
  color: var(--color-ink-muted);
  font-size: 13px;
  line-height: 1.75;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.package-card {
  display: flex;
  flex-direction: column;
  min-height: 260px;
  padding: 28px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.45s var(--ease-out-expo), transform 0.45s var(--ease-out-expo), border-color 0.2s ease, box-shadow 0.2s ease;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  h3 {
    margin: 16px 0 10px;
    font-family: var(--font-display);
    font-size: 19px;
    line-height: 1.3;
    font-weight: 700;
    color: var(--color-ink);
  }
}

.package-card__top,
.package-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.package-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 10px;
  font-weight: 700;
  border: 1px solid var(--color-line);
}

.package-price {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: 900;
  color: var(--color-brand);
}

.remark {
  color: var(--color-ink-muted);
  font-size: 12px;
  line-height: 1.75;
  flex: 1;
}

.package-card__foot {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-line);
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .flow-list {
    grid-template-columns: 1fr 1fr;
  }

  .package-grid {
    grid-template-columns: 1fr;
  }

  .service-grid {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .home-page {
    width: min(calc(100% - 24px), var(--content-width));
    padding: 24px 0 48px;
  }

  .hero-content {
    padding: 28px;
  }

  .packages-section {
    padding: 24px;
  }

  .flow-list {
    grid-template-columns: 1fr;
  }

  .hero-content h1 {
    font-size: 30px;
  }
}
</style>
