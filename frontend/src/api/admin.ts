import { get } from './request'
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

