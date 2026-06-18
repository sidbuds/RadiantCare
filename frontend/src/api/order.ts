import request from './request'

export function createOrder(data: { appointmentNo: string; couponCode?: string; extraItemCodes?: string[] }) {
  return request.post('/orders', data)
}

export function getOrder(orderNo: string) {
  return request.get(`/orders/${orderNo}`)
}

export function payOrder(orderNo: string) {
  return request.post(`/orders/${orderNo}/pay`)
}

export function applyRefund(orderNo: string, data: { reason: string }) {
  return request.post(`/orders/${orderNo}/refund`, data)
}
