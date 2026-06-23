import { get, post } from './request'
import type {
  ApiResult, AbnormalOverview, AbnormalDistributionItem,
  HighRiskUser, WorkloadOverview
} from '@/types/api'

export interface DateParams {
  startDate?: string
  endDate?: string
}

export function getAbnormalOverview(params?: DateParams) {
  return get<AbnormalOverview>('/doctor/analytics/abnormal-overview', { params })
}

export function getAbnormalDistribution(params?: DateParams) {
  return get<AbnormalDistributionItem[]>('/doctor/analytics/abnormal-distribution', { params })
}

export function getHighRiskUsers(params?: DateParams) {
  return get<HighRiskUser[]>('/doctor/analytics/high-risk-users', { params })
}

export function getAbnormalTrend(params?: DateParams) {
  return get<any[]>('/doctor/analytics/abnormal-trend', { params })
}

export function getWorkloadOverview(params?: DateParams) {
  return get<WorkloadOverview>('/doctor/analytics/workload-overview', { params })
}

export function getWorkloadTrend(params?: DateParams) {
  return get<any[]>('/doctor/analytics/workload-trend', { params })
}

export function getWorkloadBreakdown(params?: DateParams) {
  return get<any[]>('/doctor/analytics/workload-breakdown', { params })
}


