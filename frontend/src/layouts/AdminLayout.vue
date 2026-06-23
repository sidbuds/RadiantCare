<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const role = computed(() => userStore.role)
const displayName = computed(() => userStore.userInfo?.displayName || '')
const collapsed = computed(() => appStore.sidebarCollapsed)

const menuItems = computed(() => {
  switch (role.value) {
    case 'DOCTOR':
      return [
        { path: '/doctor/exam-tasks', icon: 'Document', title: '待检任务' },
        { path: '/doctor/consultations', icon: 'ChatDotRound', title: '咨询回复' },
        { path: '/doctor/analytics/abnormal', icon: 'Warning', title: '异常分析' },
        { path: '/doctor/analytics/workload', icon: 'DataAnalysis', title: '工作量统计' },
      ]
    case 'OPERATOR':
      return [
        { path: '/operator/packages', icon: 'Goods', title: '套餐管理' },
        { path: '/operator/schedules', icon: 'Calendar', title: '排班管理' },
        { path: '/operator/appointments', icon: 'Ticket', title: '预约管理' },
        { path: '/operator/orders', icon: 'List', title: '订单管理' },
        { path: '/operator/refunds', icon: 'RefreshLeft', title: '退款审核' },
        { path: '/operator/analytics', icon: 'DataLine', title: '运营看板' },
      ]
    case 'ADMIN':
      return [
        { path: '/admin/exam-tasks', icon: 'Tickets', title: '任务生成' },
        { path: '/admin/reports', icon: 'Notebook', title: '报告发布' },
        { path: '/admin/consultations', icon: 'User', title: '咨询分配' },
        { path: '/admin/doctor-analytics', icon: 'TrendCharts', title: '医生分析' },
      ]
    default:
      return []
  }
})

const roleLabel = computed(() => {
  const map: Record<string, string> = { DOCTOR: '医生端', OPERATOR: '运营端', ADMIN: '管理端' }
  return map[role.value] || role.value
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside :width="collapsed ? '68px' : '250px'" class="admin-aside">
      <div class="aside-header">
        <span class="aside-mark">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
          </svg>
        </span>
        <transition name="brand-text">
          <div v-if="!collapsed" class="aside-brand">
            <span class="logo-text">熙心健康</span>
            <span class="logo-subtext">Medical Console</span>
          </div>
        </transition>
      </div>

      <transition name="brand-text">
        <div v-if="!collapsed" class="role-badge">{{ roleLabel }}</div>
      </transition>

      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        router
        class="aside-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <div class="aside-footer">
        <div class="collapse-trigger" @click="appStore.toggleSidebar">
          <el-icon :size="15"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          <transition name="brand-text">
            <span v-if="!collapsed" class="collapse-text">收起</span>
          </transition>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <span class="section-eyebrow">{{ roleLabel }}</span>
          <span class="header-title">业务工作台</span>
        </div>
        <div class="header-right">
          <ThemeToggle />
          <el-dropdown>
            <span class="user-info">
              <span class="user-avatar">{{ displayName.slice(0, 1) || 'U' }}</span>
              {{ displayName }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.admin-layout { min-height: 100vh; }

.admin-aside {
  overflow: hidden;
  background: var(--color-sidebar-bg);
  border-right: 1px solid var(--color-line);
  transition: width 0.25s var(--ease-out);
  display: flex;
  flex-direction: column;
}

.aside-header {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 68px;
  padding: 18px 16px;
  border-bottom: 1px solid var(--color-line);
}

.aside-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--color-brand);
  color: var(--color-on-brand);
  flex-shrink: 0;
}

.aside-brand { display: flex; flex-direction: column; overflow: hidden; }

.logo-text {
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 16px;
  font-weight: 400;
  line-height: 1.2;
  white-space: nowrap;
}

.logo-subtext {
  color: var(--color-ink-faint);
  font-size: 9px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.role-badge {
  display: flex;
  align-items: center;
  margin: 10px 16px 2px;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  white-space: nowrap;
  overflow: hidden;
}

.aside-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 8px 10px;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.06);
    border-radius: 2px;

    :global([data-theme="light"]) & {
      background: rgba(0,0,0,0.1);
    }
  }

  :deep(.el-menu-item) {
    height: 40px;
    margin-bottom: 2px;
    border-radius: var(--radius-sm);
    color: var(--color-sidebar-text);
    font-weight: 500;
    font-size: 13px;
    transition: all 0.15s ease;

    .el-icon { font-size: 15px; }

    &:hover {
      background: var(--color-sidebar-hover);
      color: var(--color-ink-soft);
    }

    &.is-active {
      background: var(--color-sidebar-active);
      color: var(--color-brand);

      .el-icon { color: var(--color-brand); }

      &::before {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 18px;
        background: var(--color-brand);
        border-radius: 0 2px 2px 0;
      }
    }
  }
}

.aside-footer {
  padding: 10px 10px 14px;
  border-top: 1px solid var(--color-line);
}

.collapse-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  color: var(--color-ink-faint);
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 12px;

  &:hover { background: rgba(255,255,255,0.03); color: var(--color-ink-muted); }
}

.collapse-text { white-space: nowrap; }

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding: 0 26px;
  border-bottom: 1px solid var(--color-line);
  background: rgba(17,17,19,0.85);

  :global([data-theme="light"]) & {
    background: rgba(245, 242, 237, 0.9);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }
}

.header-left { display: flex; align-items: center; gap: 12px; }

.header-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 400;
  color: var(--color-brand);
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 9px;
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

.admin-main {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg);
}

.brand-text-enter-active { transition: all 0.2s var(--ease-out); transition-delay: 0.08s; }
.brand-text-leave-active { transition: all 0.12s ease; }
.brand-text-enter-from, .brand-text-leave-to { opacity: 0; transform: translateX(-6px); }

.page-fade-enter-active { transition: opacity 0.2s ease; }
.page-fade-leave-active { transition: opacity 0.12s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }

@media (max-width: 900px) {
  .admin-header { padding: 0 16px; }
  .header-title { display: none; }
}
</style>
