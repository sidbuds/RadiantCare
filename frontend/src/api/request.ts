import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import type { ApiResult } from '@/types/api'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

const publicAuthPaths = new Set(['/auth/login', '/auth/register'])

function normalizeRequestPath(url?: string) {
  if (!url) {
    return ''
  }
  const path = url.split('?')[0]
  return path.startsWith('/api/') ? path.slice(4) : path
}

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  const isPublicAuthApi = publicAuthPaths.has(normalizeRequestPath(config.url))
  if (userStore.token && !isPublicAuthApi) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 2001) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res as unknown as typeof response
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    if (error.response?.status === 403) {
      ElMessage.error('无操作权限')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

export function get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return request.get(url, config) as unknown as Promise<ApiResult<T>>
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return request.post(url, data, config) as unknown as Promise<ApiResult<T>>
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return request.put(url, data, config) as unknown as Promise<ApiResult<T>>
}

export function patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return request.patch(url, data, config) as unknown as Promise<ApiResult<T>>
}




export function del<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return request.delete(url, config) as unknown as Promise<ApiResult<T>>
}
