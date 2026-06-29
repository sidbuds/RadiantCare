import { get, post } from './request'
import type { PackageItem, ExamCenter, TimeSlot, CheckupGuideItem, FaqItem } from '@/types/api'

export function listPackages() {
  return get<PackageItem[]>('/public/packages')
}

export function listPackagesByCenter(centerCode: string) {
  return get<PackageItem[]>('/public/packages', { params: { centerCode } })
}

export function getPackageDetail(packageCode: string) {
  return get<PackageItem>(`/public/packages/${packageCode}`)
}

export function listCenters() {
  return get<ExamCenter[]>('/public/centers')
}

export function getCenterDetail(centerCode: string) {
  return get<ExamCenter>(`/public/centers/${centerCode}`)
}

export function getCenterSlots(centerCode: string, date?: string) {
  return get<TimeSlot[]>(`/public/centers/${centerCode}/slots`, { params: { date } })
}

export function getCheckupGuide() {
  return get<CheckupGuideItem[]>('/public/content/checkup-guide')
}

export function getFaq() {
  return get<FaqItem[]>('/public/content/faq')
}

export function getAppointmentConfig() {
  return get<{ advanceDays: number; allowToday: boolean; defaultCapacity: number }>('/public/content/appointment-config')
}


