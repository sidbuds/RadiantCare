<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listCenters } from '@/api/public'

const centers = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)

onMounted(async () => {
  mounted.value = true
  try {
    const res: any = await listCenters()
    centers.value = res.data || []
  } catch {} finally { loading.value = false }
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">EXAM CENTERS</span>
        <h2>体检中心</h2>
      </div>
      <p>了解各体检中心的地址、联系方式和服务时间。</p>
    </div>

    <div v-loading="loading" class="center-grid">
      <article
        v-for="(center, idx) in centers"
        :key="center.id"
        class="center-card data-card"
        :class="{ 'is-mounted': mounted }"
        :style="{ '--delay': `${0.1 + idx * 0.1}s` }"
      >
        <h3>{{ center.centerName }}</h3>
        <div class="center-info">
          <p>
            <span class="icon-wrap"><el-icon><Location /></el-icon></span>
            <span>{{ center.address }}</span>
          </p>
          <p>
            <span class="icon-wrap"><el-icon><Phone /></el-icon></span>
            <span>{{ center.phone }}</span>
          </p>
          <p>
            <span class="icon-wrap"><el-icon><Clock /></el-icon></span>
            <span>{{ center.businessHours }}</span>
          </p>
        </div>
        <p v-if="center.description" class="desc">{{ center.description }}</p>
      </article>
      <el-empty v-if="!loading && centers.length === 0" description="暂无体检中心" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.center-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

.center-card {
  padding: 28px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  h3 {
    font-family: var(--font-display);
    font-size: 19px;
    font-weight: 700;
    margin-bottom: 18px;
    color: var(--color-ink);
    position: relative;
  }
}

.center-info {
  display: flex;
  flex-direction: column;
  gap: 12px;

  p {
    display: flex;
    align-items: center;
    gap: 12px;
    color: var(--color-ink-soft);
    font-size: 13px;
    line-height: 1.6;
    position: relative;
  }
}

.icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  background: var(--color-brand-light);
  color: var(--color-brand);
  flex-shrink: 0;
  font-size: 15px;
  border: 1px solid var(--color-line);
}

.desc {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--color-line);
  color: var(--color-ink-muted);
  font-size: 13px;
  line-height: 1.7;
  position: relative;
}
</style>
