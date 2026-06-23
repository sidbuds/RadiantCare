import { ref, computed } from 'vue'

export interface PaginationState {
  pageNum: number
  pageSize: number
  total: number
}

export interface UsePaginationOptions {
  defaultPageSize?: number
  defaultPageNum?: number
}

/**
 * 分页状态 composable
 */
export function usePagination(options: UsePaginationOptions = {}) {
  const { defaultPageSize = 10, defaultPageNum = 1 } = options

  const pageNum = ref(defaultPageNum)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  function onPageChange(page: number) {
    pageNum.value = page
  }

  function onSizeChange(size: number) {
    pageSize.value = size
    pageNum.value = 1
  }

  function setTotal(t: number) {
    total.value = t
  }

  function reset() {
    pageNum.value = defaultPageNum
    total.value = 0
  }

  return {
    pageNum,
    pageSize,
    total,
    totalPages,
    onPageChange,
    onSizeChange,
    setTotal,
    reset,
  }
}
