import request from './request'

// 医生分析（管理员视角）
export function getAdminAbnormalOverview(params?: { startDate?: string; endDate?: string; doctorId?: number; departmentCode?: string }) {
  return request.get('/admin/doctor-analytics/abnormal-overview', { params })
}

export function getAdminAbnormalDistribution(params?: { startDate?: string; endDate?: string; doctorId?: number; departmentCode?: string }) {
  return request.get('/admin/doctor-analytics/abnormal-distribution', { params })
}

export function getAdminHighRiskUsers(params?: { startDate?: string; endDate?: string; doctorId?: number; departmentCode?: string }) {
  return request.get('/admin/doctor-analytics/high-risk-users', { params })
}

export function getWorkloadRanking(params?: { startDate?: string; endDate?: string; doctorId?: number; departmentCode?: string }) {
  return request.get('/admin/doctor-analytics/workload-ranking', { params })
}

export function getDepartmentWorkload(params?: { startDate?: string; endDate?: string; doctorId?: number; departmentCode?: string }) {
  return request.get('/admin/doctor-analytics/department-workload', { params })
}
