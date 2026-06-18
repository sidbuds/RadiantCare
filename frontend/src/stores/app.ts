import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'dark' | 'light'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const themeMode = ref<ThemeMode>('dark')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /** Initialize theme from localStorage or system preference */
  function initTheme() {
    const saved = localStorage.getItem('xixin-theme') as ThemeMode | null
    if (saved === 'light' || saved === 'dark') {
      themeMode.value = saved
    } else {
      // Respect system preference, default to dark
      const prefersLight = window.matchMedia('(prefers-color-scheme: light)').matches
      themeMode.value = prefersLight ? 'light' : 'dark'
    }
    applyTheme()
  }

  /** Apply current theme to document */
  function applyTheme() {
    const root = document.documentElement
    if (themeMode.value === 'light') {
      root.setAttribute('data-theme', 'light')
    } else {
      root.removeAttribute('data-theme')
    }
  }

  /** Toggle between light and dark */
  function toggleTheme() {
    themeMode.value = themeMode.value === 'dark' ? 'light' : 'dark'
  }

  // Persist and apply whenever theme changes
  watch(themeMode, () => {
    localStorage.setItem('xixin-theme', themeMode.value)
    applyTheme()
  })

  return { sidebarCollapsed, toggleSidebar, themeMode, toggleTheme, initTheme }
})
