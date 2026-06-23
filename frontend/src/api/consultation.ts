import { get, post } from './request'
import type { Consultation } from '@/types/api'

export interface CreateConsultationData {
  reportNo: string
  sourceType: string
  consultationType: string
  consultationTitle: string
  consultationContent: string
  priorityLevel?: number
}

export function createConsultation(data: CreateConsultationData) {
  return post<Consultation>('/doctor-consultations', data)
}

export function getMyConsultations() {
  return get<Consultation[]>('/doctor-consultations/mine')
}

export function getDoctorConsultationTodo() {
  return get<Consultation[]>('/doctor/consultations/todo')
}

export function getDoctorConsultation(consultationNo: string) {
  return get<Consultation>(`/doctor/consultations/${consultationNo}`)
}

export function replyConsultation(consultationNo: string, data: { replyContent: string; attachmentUrl?: string }) {
  return post<void>(`/doctor/consultations/${consultationNo}/reply`, data)
}

export function assignConsultation(consultationNo: string, data: { doctorId: number }) {
  return post<void>(`/admin/doctor-consultations/${consultationNo}/assign`, data)
}

