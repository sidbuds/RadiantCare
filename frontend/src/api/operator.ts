import { get, post, put, patch as requestPatch } from './request'
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
  departmentCode?: string
  departmentName?: string
}

export function getOperatorPackages(params?: { packageName?: string; status?: number } & PaginationParams) {
  return get<PageResult<PackageItem>>('/operator/packages', { params })
}

export function getOperatorPackageDetail(id: number) {
  return get<PackageItem>(`/operator/packages/${id}`)
}

export function createOperatorPackage(data: PackageFormData) {
  return post<void>('/operator/packages', data)
}

export function updateOperatorPackage(id: number, data: PackageFormData) {
  return put<void>(`/operator/packages/${id}`, data)
}

export function updateOperatorPackageStatus(id: number, status: number) {
  return requestPatch<void>(`/operator/packages/${id}/status`, { status })
}

export interface CenterOption {
  id: number
  centerCode: string
  centerName: string
  status: number
}

export interface DepartmentOption {
  centerCode: string
  departmentCode: string
  departmentName: string
}

export interface ExamItemOption {
  itemCode: string
  itemName: string
  unit?: string
  refRange?: string
}

export interface TimeSlotOption {
  timeSlotCode: string
  resourceType?: string
  resourceCode?: string
}

export function getCenterOptions() {
  return get<CenterOption[]>('/operator/options/centers')
}

export function getDepartmentOptions(params?: { centerCode?: string }) {
  return get<DepartmentOption[]>('/operator/options/departments', { params })
}

export function getExamItemOptions() {
  return get<ExamItemOption[]>('/operator/options/exam-items')
}

export function getTimeSlotOptions(params?: { centerCode?: string; departmentCode?: string }) {
  return get<TimeSlotOption[]>('/operator/options/time-slots', { params })
}

export function getOperatorCenters(params?: { keyword?: string; status?: number } & PaginationParams) {
  return get<PageResult<any>>('/operator/centers', { params })
}

export function createOperatorCenter(data: Record<string, any>) {
  return post<void>('/operator/centers', data)
}

export function updateOperatorCenter(id: number, data: Record<string, any>) {
  return put<void>(`/operator/centers/${id}`, data)
}

export function updateOperatorCenterStatus(id: number, status: number) {
  return requestPatch<void>(`/operator/centers/${id}/status`, { status })
}

// 修复：排班列表使用 startDate/endDate 替代 date
export function getOperatorSchedules(params?: { centerCode?: string; departmentCode?: string; startDate?: string; endDate?: string; status?: number } & PaginationParams) {
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

export function getDashboard(params?: { departmentCode?: string; startDate?: string; endDate?: string }) {
  return get<DashboardData>('/operator/analytics/dashboard', { params })
}

export function getAppointmentTrend(params?: { departmentCode?: string; startDate?: string; endDate?: string }) {
  return get<TrendItem[]>('/operator/analytics/appointment-trend', { params })
}

export function getOrderConversion(params?: { departmentCode?: string; startDate?: string; endDate?: string }) {
  return get<ConversionItem[]>('/operator/analytics/order-conversion', { params })
}

export function getPackageAnalysis(params?: { departmentCode?: string; startDate?: string; endDate?: string }) {
  return get<PackageAnalysisItem[]>('/operator/analytics/package-analysis', { params })
}
