import { get, post } from './request'
import type { Order } from '@/types/api'

export function createOrder(data: { appointmentNo: string; couponCode?: string; extraItemCodes?: string[] }) {
  return post<Order>('/orders', data)
}

export function getOrder(orderNo: string) {
  return get<Order>(`/orders/${orderNo}`)
}

export function payOrder(orderNo: string) {
  return post<Order>(`/orders/${orderNo}/pay`)
}

export function cancelOrder(orderNo: string) {
  return post<Order>(`/orders/${orderNo}/cancel`)
}

export function applyRefund(orderNo: string, data: { reason: string }) {
  return post<void>(`/orders/${orderNo}/refund`, data)
}


