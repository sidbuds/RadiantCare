import { download, get, post } from './request'
import type { ExamReport } from '@/types/api'

export function getReport(reportNo: string) {
  return get<ExamReport>(`/reports/${reportNo}`)
}

export function getMyReports() {
  return get<ExamReport[]>('/reports/mine')
}

export function getReportItems(reportNo: string) {
  return get<any[]>(`/reports/${reportNo}/items`)
}

export function generateReportPdf(reportNo: string) {
  return post<{ reportNo: string; pdfUrl: string; objectKey: string }>(`/reports/${reportNo}/pdf`)
}

export function precheckReportPdf(reportNo: string) {
  return get<{ reportNo: string; complete: boolean; missingFields: string[] }>(`/reports/${reportNo}/pdf/precheck`)
}

export function downloadReportPdf(reportNo: string) {
  return download(`/reports/${reportNo}/pdf/download`)
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

export function getDoctorReviewTodo() {
  return get<any[]>('/doctor/reports/review-todo')
}

export function getDoctorReviewHistory() {
  return get<any[]>('/doctor/reports/review-history')
}

export function getDoctorReviewReport(reportNo: string) {
  return get<any>(`/doctor/reports/${reportNo}`)
}

export function submitDoctorReportReview(reportNo: string, data: { reviewStage: string; reviewStatus: string; reviewComment: string }) {
  return post<void>(`/doctor/reports/${reportNo}/review`, data)
}
