import request from './request'

// 用户端
export function getExamTaskGuide(taskNo: string) {
  return request.get(`/exam-tasks/${taskNo}/guide`)
}

export function getExamTaskItems(taskNo: string) {
  return request.get(`/exam-tasks/${taskNo}/items`)
}

// 医生端
export function getDoctorExamTodo() {
  return request.get('/doctor/exam-tasks/todo')
}

export function getDoctorExamItem(taskNo: string, taskItemNo: string) {
  return request.get(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}`)
}

export function startExamItem(taskNo: string, taskItemNo: string) {
  return request.post(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/start`)
}

export function submitExamResults(taskNo: string, taskItemNo: string, data: {
  itemCode: string
  resultEntries: Array<{
    metricCode: string
    metricName: string
    resultValue: string
    resultNumber?: number | null
    unit?: string
    refRange?: string
    abnormal?: boolean
    abnormalLevel?: number
  }>
  conclusion: string
}) {
  return request.post(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/results`, data)
}

export function completeExamItem(taskNo: string, taskItemNo: string) {
  return request.post(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/complete`)
}

// 管理端
export function generateExamTask(data: { orderNo: string }) {
  return request.post('/admin/exam-tasks/generate', data)
}
