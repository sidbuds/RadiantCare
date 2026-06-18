import request from './request'

// 套餐管理
export function getOperatorPackages() {
  return request.get('/operator/packages')
}

export function createOperatorPackage(data: any) {
  return request.post('/operator/packages', data)
}

export function updateOperatorPackage(id: number, data: any) {
  return request.put(`/operator/packages/${id}`, data)
}

// 排班管理
export function getOperatorSchedules(params?: any) {
  return request.get('/operator/schedules', { params })
}

export function createOperatorSchedule(data: any) {
  return request.post('/operator/schedules', data)
}

export function updateOperatorSchedule(id: number, data: any) {
  return request.put(`/operator/schedules/${id}`, data)
}

// 预约管理
export function getOperatorAppointments(params?: any) {
  return request.get('/operator/appointments', { params })
}

// 订单管理
export function getOperatorOrders(params?: any) {
  return request.get('/operator/orders', { params })
}

// 退款管理
export function getOperatorRefunds(params?: any) {
  return request.get('/operator/refunds', { params })
}

export function getOperatorRefundDetail(applyNo: string) {
  return request.get(`/operator/refunds/${applyNo}`)
}

export function approveRefund(applyNo: string, data: { auditRemark: string }) {
  return request.post(`/operator/refunds/${applyNo}/approve`, data)
}

export function rejectRefund(applyNo: string, data: { auditRemark: string }) {
  return request.post(`/operator/refunds/${applyNo}/reject`, data)
}

// 运营分析
export function getDashboard() {
  return request.get('/operator/analytics/dashboard')
}

export function getAppointmentTrend(params?: any) {
  return request.get('/operator/analytics/appointment-trend', { params })
}

export function getOrderConversion(params?: any) {
  return request.get('/operator/analytics/order-conversion', { params })
}

export function getPackageAnalysis(params?: any) {
  return request.get('/operator/analytics/package-analysis', { params })
}
