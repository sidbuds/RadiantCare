import { describe, it, expect } from 'vitest'
import { usePagination } from '@/composables/usePagination'

describe('usePagination', () => {
  it('should initialize with default values', () => {
    const { pageNum, pageSize, total } = usePagination()

    expect(pageNum.value).toBe(1)
    expect(pageSize.value).toBe(10)
    expect(total.value).toBe(0)
  })

  it('should accept custom default values', () => {
    const { pageNum, pageSize } = usePagination({ defaultPageSize: 20, defaultPageNum: 2 })

    expect(pageNum.value).toBe(2)
    expect(pageSize.value).toBe(20)
  })

  it('should calculate total pages correctly', () => {
    const { total, totalPages, setTotal } = usePagination()

    setTotal(25)
    expect(totalPages.value).toBe(3) // ceil(25/10) = 3

    setTotal(30)
    expect(totalPages.value).toBe(3) // ceil(30/10) = 3

    setTotal(0)
    expect(totalPages.value).toBe(0)
  })

  it('should handle page change', () => {
    const { pageNum, onPageChange } = usePagination()

    onPageChange(3)
    expect(pageNum.value).toBe(3)
  })

  it('should handle size change and reset to page 1', () => {
    const { pageNum, pageSize, onSizeChange } = usePagination()

    pageNum.value = 5
    onSizeChange(20)

    expect(pageSize.value).toBe(20)
    expect(pageNum.value).toBe(1)
  })

  it('should reset to default values', () => {
    const { pageNum, total, setTotal, reset } = usePagination()

    pageNum.value = 5
    setTotal(100)
    reset()

    expect(pageNum.value).toBe(1)
    expect(total.value).toBe(0)
  })
})
