import { ref, onMounted, nextTick } from 'vue'

/**
 * 通用加载状态 composable
 * 封装 loading / mounted 状态和异步执行逻辑
 */
export function useLoading() {
  const loading = ref(false)
  const mounted = ref(false)

  onMounted(async () => {
    await nextTick()
    mounted.value = true
  })

  /**
   * 执行异步操作，自动管理 loading 状态
   * @param fn 异步函数
   * @param options 选项
   */
  async function execute<T>(
    fn: () => Promise<T>,
    options: { silent?: boolean } = {}
  ): Promise<T | undefined> {
    loading.value = true
    try {
      return await fn()
    } catch (err) {
      if (!options.silent) {
        // 错误已由 axios 拦截器处理
      }
      return undefined
    } finally {
      loading.value = false
    }
  }

  return { loading, mounted, execute }
}
