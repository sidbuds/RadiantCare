import request from './request'

// 用户端
export function createConsultation(data: {
  reportNo: string
  sourceType: string
  consultationType: string
  consultationTitle: string
  consultationContent: string
  priorityLevel?: number
}) {
  return request.post('/doctor-consultations', data)
}

export function getMyConsultations() {
  return request.get('/doctor-consultations/mine')
}

// 医生端
export function getDoctorConsultationTodo() {
  return request.get('/doctor/consultations/todo')
}

export function getDoctorConsultation(consultationNo: string) {
  return request.get(`/doctor/consultations/${consultationNo}`)
}

export function replyConsultation(consultationNo: string, data: {
  replyContent: string
  attachmentUrl?: string
}) {
  return request.post(`/doctor/consultations/${consultationNo}/reply`, data)
}

// 管理端
export function assignConsultation(consultationNo: string, data: { doctorId: number }) {
  return request.post(`/admin/doctor-consultations/${consultationNo}/assign`, data)
}
