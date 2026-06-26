import { get, post, put, del } from './request'
import type {
  AbnormalDistributionItem,
  AbnormalOverview,
  DepartmentWorkloadItem,
  HighRiskUser,
  WorkloadRankingItem,
} from '@/types/api'

export interface AdminAnalyticsParams {
  startDate?: string
  endDate?: string
  doctorId?: number
  departmentCode?: string
}

export function getAdminAbnormalOverview(params?: AdminAnalyticsParams) {
  return get<AbnormalOverview>('/admin/doctor-analytics/abnormal-overview', { params })
}

export function getAdminAbnormalDistribution(params?: AdminAnalyticsParams) {
  return get<AbnormalDistributionItem[]>('/admin/doctor-analytics/abnormal-distribution', { params })
}

export function getAdminHighRiskUsers(params?: AdminAnalyticsParams) {
  return get<HighRiskUser[]>('/admin/doctor-analytics/high-risk-users', { params })
}

export function getWorkloadRanking(params?: AdminAnalyticsParams) {
  return get<WorkloadRankingItem[]>('/admin/doctor-analytics/workload-ranking', { params })
}

export function getDepartmentWorkload(params?: AdminAnalyticsParams) {
  return get<DepartmentWorkloadItem[]>('/admin/doctor-analytics/department-workload', { params })
}



// ==================== 医生管理 ====================
export function getAdminDoctors(params?: { keyword?: string; pageNum?: number; pageSize?: number }) {
  return get<{ list: any[]; total: number }>('/admin/doctors', { params })
}

export function updateAdminDoctor(id: number, data: Record<string, any>) {
  return put<void>(`/admin/doctors/${id}`, data)
}

export function bindDoctorDepartment(doctorId: number, data: { departmentCode: string; departmentName?: string; centerCode?: string; isPrimary?: boolean }) {
  return post<void>(`/admin/doctors/${doctorId}/departments`, data)
}

export function unbindDoctorDepartment(relId: number) {
  return post<void>(`/admin/doctors/departments/${relId}/unbind`)
}

// ==================== 用户管理 ====================
export function getAdminUsers(params?: { keyword?: string; pageNum?: number; pageSize?: number }) {
  return get<{ list: any[]; total: number }>('/admin/users', { params })
}

export function updateUserStatus(userId: number, status: number) {
  return post<void>(`/admin/users/${userId}/status?status=${status}`)
}


// ==================== 字典管理 ====================
export function getDictTypes(params?: { pageNum?: number; pageSize?: number }) {
  return get<{ list: any[]; total: number }>('/admin/dicts/types', { params })
}

export function getDictItems(dictType: string) {
  return get<any[]>('/admin/dicts/items', { params: { dictType } })
}

export function saveDictItem(data: any) {
  return post<void>('/admin/dicts/items', data)
}

export function deleteDictItem(id: number) {
  return del<void>(`/admin/dicts/items/${id}`)
}

// ==================== 审计日志 ====================
export function getAuditLogs(params?: { module?: string; action?: string; pageNum?: number; pageSize?: number }) {
  return get<{ list: any[]; total: number }>('/admin/audit-logs', { params })
}

export function getAuditLogDetail(id: number) {
  return get<any>(`/admin/audit-logs/${id}`)
}
