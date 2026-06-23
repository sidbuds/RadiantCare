import { get, post } from './request'
import type { ExamReport } from '@/types/api'

export function getReport(reportNo: string) {
  return get<ExamReport>(`/reports/${reportNo}`)
}

export function getMyReports() {
  return get<ExamReport[]>('/reports/mine')
}

export function generateReport(data: { taskNo: string; templateCode: string }) {
  return post<ExamReport>('/admin/reports/generate', data)
}

export function reviewReport(reportNo: string, data: { reviewStage: string; reviewStatus: string; reviewComment: string }) {
  return post<void>(`/admin/reports/${reportNo}/review`, data)
}

export function publishReport(reportNo: string) {
  return post<void>(`/admin/reports/${reportNo}/publish`)
}

