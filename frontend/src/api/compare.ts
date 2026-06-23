import { get, post } from './request'
import type { CompareTask, CompareResult, HealthAdvice, HealthRiskScore } from '@/types/api'

export function createCompareTask(data: { baselineReportNo: string; compareReportNo: string }) {
  return post<CompareTask>('/report-compare/tasks', data)
}

export function getCompareTask(taskNo: string) {
  return get<CompareTask>(`/report-compare/tasks/${taskNo}`)
}

export function getCompareResults(taskNo: string) {
  return get<CompareResult[]>(`/report-compare/tasks/${taskNo}/results`)
}

export function getHealthAdvice(compareTaskNo: string) {
  return get<HealthAdvice[]>(`/health-advices/compare-tasks/${compareTaskNo}`)
}


