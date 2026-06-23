import { describe, it, expect, vi } from 'vitest'
import { useLoading } from '@/composables/useLoading'

describe('useLoading', () => {
  it('should initialize with loading true', () => {
    const { loading } = useLoading()
    expect(loading.value).toBe(true)
  })

  it('should execute function and manage loading state', async () => {
    const { loading, execute } = useLoading()
    const mockFn = vi.fn().mockResolvedValue('result')

    const result = await execute(mockFn)

    expect(mockFn).toHaveBeenCalled()
    expect(result).toBe('result')
    expect(loading.value).toBe(false)
  })

  it('should handle errors gracefully', async () => {
    const { loading, execute } = useLoading()
    const mockFn = vi.fn().mockRejectedValue(new Error('test error'))

    const result = await execute(mockFn)

    expect(result).toBeUndefined()
    expect(loading.value).toBe(false)
  })

  it('should set loading to false after execute completes', async () => {
    const { loading, execute } = useLoading()
    const mockFn = vi.fn().mockResolvedValue(null)

    await execute(mockFn)

    expect(loading.value).toBe(false)
  })

  it('should keep loading true during async operation', async () => {
    const { loading, execute } = useLoading()
    let resolvePromise: (value: unknown) => void
    const promise = new Promise((resolve) => {
      resolvePromise = resolve
    })
    const mockFn = vi.fn().mockReturnValue(promise)

    // Start the async operation
    const executePromise = execute(mockFn)

    // Loading should still be true
    expect(loading.value).toBe(true)

    // Resolve the promise
    resolvePromise!(null)
    await executePromise

    // Now loading should be false
    expect(loading.value).toBe(false)
  })
})
