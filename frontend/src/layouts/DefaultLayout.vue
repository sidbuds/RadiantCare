<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { computed, ref, onMounted, onUnmounted } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const displayName = computed(() => userStore.userInfo?.displayName || '')
const scrolled = ref(false)

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  // 精确匹配首页
  if (path === '/') return '/'
  // 匹配其他菜单项（取第一段路径）
  const match = path.match(/^(\/[^/]+)/)
  return match ? match[1] : '/'
})

function goHome() { router.push('/') }
function goLogin() { router.push('/login') }
function handleLogout() { userStore.logout(); router.push('/login') }
function goMyAppointments() { router.push('/user/appointments') }
function goMyReports() { router.push('/user/reports') }

function onScroll() { scrolled.value = window.scrollY > 10 }
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <div class="default-layout">
    <header class="default-header" :class="{ scrolled }">
      <div class="page-shell header-shell">
        <div class="header-left" @click="goHome">
          <span class="brand-mark">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
            </svg>
          </span>
          <div class="brand-copy">
            <span class="brand-name">熙心健康</span>
            <span class="brand-subtitle">体检服务平台</span>
          </div>
        </div>

        <div class="header-nav">
          <el-menu mode="horizontal" :ellipsis="false" router :default-active="activeMenu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/packages">体检套餐</el-menu-item>
            <el-menu-item index="/centers">体检中心</el-menu-item>
            <el-menu-item index="/guide">检查流程</el-menu-item>
            <el-menu-item index="/faq">常见问题</el-menu-item>
          </el-menu>
        </div>

        <div class="header-right">
          <ThemeToggle />
          <template v-if="isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <span class="user-avatar">{{ displayName.slice(0, 1) || 'U' }}</span>
                <span class="user-name">{{ displayName }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goMyAppointments">我的预约</el-dropdown-item>
                  <el-dropdown-item @click="goMyReports">我的报告</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="goLogin" size="small">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="default-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped lang="scss">
.default-layout {
  min-height: 100vh;
}

.default-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--header-height);
  background: rgba(17, 17, 19, 0.85);
  border-bottom: 1px solid var(--color-line);
  transition: background 0.3s ease;

  &.scrolled {
    background: rgba(17, 17, 19, 0.95);
  }

  :global([data-theme="light"]) & {
    background: rgba(245, 242, 237, 0.85);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);

    &.scrolled {
      background: rgba(245, 242, 237, 0.95);
    }
  }
}

.header-shell {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--color-brand);
  color: var(--color-on-brand);
  flex-shrink: 0;
}

.brand-copy {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
  line-height: 1.2;
}

.brand-subtitle {
  color: var(--color-ink-faint);
  font-size: 10px;
  letter-spacing: 0.05em;
}

.header-nav {
  min-width: 0;

  :deep(.el-menu) {
    justify-content: center;
    border-bottom: none;
    background: transparent;
    gap: 2px;
  }

  :deep(.el-menu-item) {
    height: 36px;
    border-radius: var(--radius-sm);
    color: var(--color-ink-muted);
    font-weight: 500;
    font-size: 13px;
    padding: 0 14px;

    &:hover {
      color: var(--color-ink-soft);
      background: rgba(255,255,255,0.03);
    }
  }

  :deep(.el-menu-item.is-active) {
    color: var(--color-brand);
    background: var(--color-brand-light);
  }
}

.header-right {
  display: flex;
  justify-content: flex-end;

  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 5px 14px 5px 5px;
    border: 1px solid var(--color-line);
    border-radius: 999px;
    background: var(--color-panel);
    cursor: pointer;
    color: var(--color-ink);
    font-size: 13px;
    transition: border-color 0.15s ease;

    &:hover { border-color: var(--color-line-strong); }
  }
}

.user-name { font-weight: 500; }

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: var(--color-brand);
  color: var(--color-on-brand);
  font-size: 11px;
  font-weight: 700;
}

.default-main {
  min-height: calc(100vh - var(--header-height));
}

.page-fade-enter-active { transition: opacity 0.2s ease; }
.page-fade-leave-active { transition: opacity 0.12s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }

@media (max-width: 1024px) {
  .header-shell {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 8px 0;
    height: auto;
  }

  .default-header { position: static; height: auto; }

  .header-nav {
    order: 3;

    :deep(.el-menu) {
      justify-content: flex-start;
      overflow-x: auto;
      white-space: nowrap;
      &::-webkit-scrollbar { display: none; }
    }
  }
}

@media (max-width: 640px) {
  .brand-subtitle { display: none; }
}
</style>

<style>
/* 主题特定样式（非 scoped） */
[data-theme="light"] .default-header .brand-name {
  color: #3A8F85 !important;
}
</style>
