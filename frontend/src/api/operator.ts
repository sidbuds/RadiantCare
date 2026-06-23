import { get, post, put } from './request'
import type {
  Appointment,
  ConversionItem,
  DashboardData,
  Order,
  PackageAnalysisItem,
  PackageItem,
  PageResult,
  RefundApply,
  Schedule,
  TrendItem,
} from '@/types/api'

export interface PaginationParams {
  pageNum?: number
  pageSize?: number
}

export interface PackageFormData {
  packageCode?: string
  packageName?: string
  category?: string
  price?: number
  status?: number
  remark?: string
  templateCode?: string
  items?: Array<{ itemCode: string; itemName: string; unit?: string; refRange?: string; sortNo: number }>
}

export interface ScheduleFormData {
  centerCode?: string
  appointDate?: string
  timeSlotCode?: string
  resourceType?: string
  resourceCode?: string
  capacityTotal?: number
  status?: number
}

export function getOperatorPackages(params?: { packageName?: string; status?: number } & PaginationParams) {
  return get<PageResult<PackageItem>>('/operator/packages', { params })
}

export function createOperatorPackage(data: PackageFormData) {
  return post<void>('/operator/packages', data)
}

export function updateOperatorPackage(id: number, data: PackageFormData) {
  return put<void>(`/operator/packages/${id}`, data)
}

// 修复：排班列表使用 startDate/endDate 替代 date
export function getOperatorSchedules(params?: { centerCode?: string; startDate?: string; endDate?: string; status?: number } & PaginationParams) {
  return get<PageResult<Schedule>>('/operator/schedules', { params })
}

export function createOperatorSchedule(data: ScheduleFormData) {
  return post<void>('/operator/schedules', data)
}

export function updateOperatorSchedule(id: number, data: ScheduleFormData) {
  return put<void>(`/operator/schedules/${id}`, data)
}

// 修复：预约列表使用 appointDate 替代 date
export function getOperatorAppointments(params?: { centerCode?: string; appointDate?: string; status?: number } & PaginationParams) {
  return get<PageResult<Appointment>>('/operator/appointments', { params })
}

// 修复：订单列表添加 userId 和 payChannel 过滤
export function getOperatorOrders(params?: { orderNo?: string; userId?: number; status?: number; payChannel?: string } & PaginationParams) {
  return get<PageResult<Order>>('/operator/orders', { params })
}

export function getOperatorRefunds(params?: { applyStatus?: number; orderNo?: string } & PaginationParams) {
  return get<PageResult<RefundApply>>('/operator/refunds', { params })
}

export function getOperatorRefundDetail(applyNo: string) {
  return get<RefundApply>(`/operator/refunds/${applyNo}`)
}

export function approveRefund(applyNo: string, data: { auditRemark: string }) {
  return post<void>(`/operator/refunds/${applyNo}/approve`, data)
}

export function rejectRefund(applyNo: string, data: { auditRemark: string }) {
  return post<void>(`/operator/refunds/${applyNo}/reject`, data)
}

export function getDashboard(params?: { startDate?: string; endDate?: string }) {
  return get<DashboardData>('/operator/analytics/dashboard', { params })
}

export function getAppointmentTrend(params?: { startDate?: string; endDate?: string }) {
  return get<TrendItem[]>('/operator/analytics/appointment-trend', { params })
}

export function getOrderConversion(params?: { startDate?: string; endDate?: string }) {
  return get<ConversionItem[]>('/operator/analytics/order-conversion', { params })
}

export function getPackageAnalysis(params?: { startDate?: string; endDate?: string }) {
  return get<PackageAnalysisItem[]>('/operator/analytics/package-analysis', { params })
}
