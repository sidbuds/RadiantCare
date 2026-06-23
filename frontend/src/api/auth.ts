import { get, post } from './request'
import type { LoginData, UserInfo } from '@/types/api'

export function login(data: { username: string; password: string }) {
  return post<LoginData>('/auth/login', data)
}

export function register(data: { username: string; password: string; name: string; mobile: string }) {
  return post<LoginData>('/auth/register', data)
}

export function getMe() {
  return get<UserInfo>('/auth/me')
}
