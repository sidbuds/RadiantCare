<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listPackages } from '@/api/public'

const router = useRouter()
const packages = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await listPackages()
    packages.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})

function goDetail(code: string) {
  router.push(`/packages/${code}`)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">PACKAGE INDEX</span>
        <h2>体检套餐总览</h2>
      </div>
      <p>从基础筛查到专项深度检查，为您和家人选择合适的体检方案。</p>
    </div>

    <div v-loading="loading" class="package-grid">
      <article
        v-for="(pkg, idx) in packages"
        :key="pkg.id"
        class="package-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.08}s` }"
        @click="goDetail(pkg.packageCode)"
      >
        <div class="package-head">
          <span class="package-category">{{ pkg.category || '健康筛查' }}</span>
          <span class="package-price">¥{{ pkg.price }}</span>
        </div>
        <h3>{{ pkg.packageName }}</h3>
        <p class="remark">{{ pkg.remark || '涵盖常规检查项目，适合年度健康体检。' }}</p>
        <div class="package-foot">
          <span class="metric-chip">查看详情</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </article>
      <el-empty v-if="!loading && packages.length === 0" description="暂无套餐" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.package-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
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
  transform: translateY(20px);
  transition: opacity 0.35s var(--ease-out-expo), transform 0.35s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--color-brand), var(--color-accent));
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-xl);
    border-color: var(--color-brand-muted);

    &::before {
      opacity: 1;
    }

    .package-foot {
      color: var(--color-brand);

      .el-icon {
        transform: translateX(4px);
      }
    }
  }

  h3 {
    margin: 18px 0 12px;
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    line-height: 1.3;
    color: var(--color-ink);
  }
}

.package-head,
.package-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  position: relative;
}

.package-category {
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  border: 1px solid var(--color-line);
}

.package-price {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 900;
  color: var(--color-brand);
  letter-spacing: -0.02em;
}

.remark {
  color: var(--color-ink-muted);
  line-height: 1.8;
  font-size: 13px;
  flex: 1;
}

.package-foot {
  margin-top: auto;
  padding-top: 18px;
  border-top: 1px solid var(--color-line);
  color: var(--color-ink-muted);
  font-weight: 600;
  font-size: 13px;
  transition: color 0.3s ease;

  .el-icon {
    transition: transform 0.3s var(--ease-out-expo);
  }
}

@media (max-width: 1080px) {
  .package-grid {
    grid-template-columns: 1fr;
  }
}
</style>
