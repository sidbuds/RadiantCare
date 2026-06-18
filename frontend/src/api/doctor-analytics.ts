import request from './request'

export function getAbnormalOverview(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/abnormal-overview', { params })
}

export function getAbnormalDistribution(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/abnormal-distribution', { params })
}

export function getHighRiskUsers(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/high-risk-users', { params })
}

export function getAbnormalTrend(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/abnormal-trend', { params })
}

export function getWorkloadOverview(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/workload-overview', { params })
}

export function getWorkloadTrend(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/workload-trend', { params })
}

export function getWorkloadBreakdown(params?: { startDate?: string; endDate?: string }) {
  return request.get('/doctor/analytics/workload-breakdown', { params })
}
