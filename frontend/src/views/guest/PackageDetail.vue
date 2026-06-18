<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPackageDetail } from '@/api/public'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pkg = ref<any>(null)
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await getPackageDetail(route.params.code as string)
    pkg.value = res.data
  } catch {} finally { loading.value = false }
})

function handleAppointment() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push({ path: '/user/appointments/create', query: { packageId: pkg.value.id, packageCode: pkg.value.packageCode } })
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <template v-if="pkg">
      <div class="detail-header" :class="{ 'is-mounted': mounted }">
        <el-button class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <div class="detail-hero glass-panel" :class="{ 'is-mounted': mounted }">
        <div class="hero-content">
          <span class="section-eyebrow">PACKAGE DETAIL</span>
          <h1>{{ pkg.packageName }}</h1>
          <div class="hero-meta">
            <span class="meta-item">
              <span class="meta-label">类别</span>
              <span class="meta-value">{{ pkg.category }}</span>
            </span>
            <span class="meta-divider"></span>
            <span class="meta-item">
              <span class="meta-label">价格</span>
              <span class="meta-price">¥{{ pkg.price }}</span>
            </span>
          </div>
          <p v-if="pkg.remark" class="hero-desc">{{ pkg.remark }}</p>
        </div>
        <div class="hero-action">
          <el-button type="primary" size="large" class="book-btn" @click="handleAppointment">
            立即预约
            <el-icon class="btn-arrow"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="items-section glass-panel" :class="{ 'is-mounted': mounted }">
        <h3 class="items-heading">检查项目</h3>
        <el-table :data="pkg.items" class="detail-table">
          <el-table-column prop="itemCode" label="编码" width="140">
            <template #default="{ row }">
              <span class="mono-text">{{ row.itemCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目名称" />
          <el-table-column prop="unit" label="单位" width="120" />
          <el-table-column prop="refRange" label="参考范围" width="180" />
        </el-table>
      </div>
    </template>
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

.detail-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding: 40px;
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.1s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }

  h1 {
    margin-top: 14px;
    font-family: var(--font-display);
    font-size: clamp(28px, 3vw, 40px);
    font-weight: 900;
    line-height: 1.15;
    letter-spacing: -0.02em;
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-ink-faint);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.meta-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}

.meta-price {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 900;
  color: var(--color-brand);
}

.meta-divider {
  width: 1px;
  height: 40px;
  background: var(--color-line);
}

.hero-desc {
  margin-top: 14px;
  color: var(--color-ink-muted);
  font-size: 14px;
  line-height: 1.7;
  max-width: 500px;
}

.hero-action {
  flex-shrink: 0;
}

.book-btn {
  padding: 14px 36px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  display: flex;
  align-items: center;
  gap: 8px;

  .btn-arrow {
    transition: transform 0.3s var(--ease-out-expo);
  }

  &:hover .btn-arrow {
    transform: translateX(4px);
  }
}

.items-section {
  padding: 32px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.items-heading {
  margin-bottom: 20px;
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-ink);
  position: relative;
  padding-left: 16px;

  &::before {
    content: "";
    position: absolute;
    left: 0;
    top: 4px;
    bottom: 4px;
    width: 4px;
    border-radius: 2px;
    background: linear-gradient(180deg, var(--color-brand), var(--color-accent));
  }
}

@media (max-width: 768px) {
  .detail-hero {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
  }

  .items-section {
    padding: 20px;
  }
}
</style>
