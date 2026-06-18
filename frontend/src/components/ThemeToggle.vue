<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { computed } from 'vue'

const appStore = useAppStore()
const isDark = computed(() => appStore.themeMode === 'dark')
</script>

<template>
  <button
    class="theme-toggle"
    :aria-label="isDark ? '切换至日间模式' : '切换至夜间模式'"
    :title="isDark ? '日间模式' : '夜间模式'"
    @click="appStore.toggleTheme"
  >
    <span class="toggle-track" :class="{ 'is-light': !isDark }">
      <span class="toggle-thumb">
        <!-- Sun icon -->
        <svg v-if="!isDark" class="icon icon-sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="5" />
          <line x1="12" y1="1" x2="12" y2="3" />
          <line x1="12" y1="21" x2="12" y2="23" />
          <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
          <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
          <line x1="1" y1="12" x2="3" y2="12" />
          <line x1="21" y1="12" x2="23" y2="12" />
          <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
          <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
        </svg>
        <!-- Moon icon -->
        <svg v-else class="icon icon-moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      </span>
    </span>
  </button>
</template>

<style scoped lang="scss">
.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.toggle-track {
  position: relative;
  width: 48px;
  height: 26px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: background 0.35s var(--ease-out), border-color 0.35s var(--ease-out);

  &.is-light {
    background: rgba(0, 0, 0, 0.06);
    border-color: rgba(0, 0, 0, 0.1);
  }
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.4s var(--ease-out-expo), background 0.35s ease;

  .toggle-track:not(.is-light) & {
    transform: translateX(22px);
    background: rgba(255, 255, 255, 0.12);
  }

  .toggle-track.is-light & {
    transform: translateX(0);
    background: var(--color-brand);
  }
}

.icon {
  width: 12px;
  height: 12px;
  stroke-width: 2.2;
}

.icon-sun {
  color: #FFFFFF;
}

.icon-moon {
  color: rgba(255, 255, 255, 0.7);
}

.theme-toggle:hover .toggle-track {
  border-color: rgba(255, 255, 255, 0.12);
}

.theme-toggle:hover .toggle-track.is-light {
  border-color: rgba(0, 0, 0, 0.15);
}

.theme-toggle:focus-visible .toggle-track {
  outline: 2px solid var(--color-brand);
  outline-offset: 2px;
}
</style>
