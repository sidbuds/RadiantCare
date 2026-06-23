import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'dark' | 'light' | 'auto'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const themeMode = ref<ThemeMode>('auto')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /** 根据当前时间判断应该使用日间还是夜间模式 */
  function getTimeBasedTheme(): 'dark' | 'light' {
    const hour = new Date().getHours()
    // 6:00-18:00 日间模式，其余夜间模式
    return hour >= 6 && hour < 18 ? 'light' : 'dark'
  }

  /** 获取实际应用的主题（解析 auto） */
  function getResolvedTheme(): 'dark' | 'light' {
    if (themeMode.value === 'auto') {
      return getTimeBasedTheme()
    }
    return themeMode.value
  }

  /** Initialize theme from localStorage or system preference */
  function initTheme() {
    const saved = localStorage.getItem('xixin-theme') as ThemeMode | null
    if (saved === 'light' || saved === 'dark' || saved === 'auto') {
      themeMode.value = saved
    } else {
      // 默认 auto，根据时间自动切换
      themeMode.value = 'auto'
    }
    applyTheme()
  }

  /** Apply current theme to document */
  function applyTheme() {
    const root = document.documentElement
    const resolved = getResolvedTheme()
    if (resolved === 'light') {
      root.setAttribute('data-theme', 'light')
    } else {
      root.removeAttribute('data-theme')
    }
  }

  /** Toggle between light, dark and auto */
  function toggleTheme() {
    const cycle: ThemeMode[] = ['auto', 'light', 'dark']
    const currentIndex = cycle.indexOf(themeMode.value)
    themeMode.value = cycle[(currentIndex + 1) % cycle.length]
  }

  /** Set specific theme mode */
  function setTheme(mode: ThemeMode) {
    themeMode.value = mode
  }

  // Persist and apply whenever theme changes
  watch(themeMode, () => {
    localStorage.setItem('xixin-theme', themeMode.value)
    applyTheme()
  })

  // 每分钟检查一次时间，自动切换主题
  let timeCheckInterval: ReturnType<typeof setInterval> | null = null

  function startTimeCheck() {
    if (timeCheckInterval) return
    timeCheckInterval = setInterval(() => {
      if (themeMode.value === 'auto') {
        applyTheme()
      }
    }, 60000) // 每分钟检查一次
  }

  function stopTimeCheck() {
    if (timeCheckInterval) {
      clearInterval(timeCheckInterval)
      timeCheckInterval = null
    }
  }

  return {
    sidebarCollapsed,
    toggleSidebar,
    themeMode,
    toggleTheme,
    setTheme,
    initTheme,
    getResolvedTheme,
    startTimeCheck,
    stopTimeCheck,
  }
})
