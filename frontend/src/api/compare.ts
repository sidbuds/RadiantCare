import request from './request'

export function createCompareTask(data: { baselineReportNo: string; compareReportNo: string }) {
  return request.post('/report-compare/tasks', data)
}

export function getCompareTask(taskNo: string) {
  return request.get(`/report-compare/tasks/${taskNo}`)
}

export function getCompareResults(taskNo: string) {
  return request.get(`/report-compare/tasks/${taskNo}/results`)
}

export function getHealthAdvice(compareTaskNo: string) {
  return request.get(`/health-advices/compare-tasks/${compareTaskNo}`)
}
