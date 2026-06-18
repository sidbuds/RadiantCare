import request from './request'

// 用户端
export function getReport(reportNo: string) {
  return request.get(`/reports/${reportNo}`)
}

// 管理端
export function generateReport(data: { taskNo: string; templateCode: string }) {
  return request.post('/admin/reports/generate', data)
}

export function reviewReport(reportNo: string, data: {
  reviewStage: string
  reviewStatus: string
  reviewComment: string
}) {
  return request.post(`/admin/reports/${reportNo}/review`, data)
}

export function publishReport(reportNo: string) {
  return request.post(`/admin/reports/${reportNo}/publish`)
}
