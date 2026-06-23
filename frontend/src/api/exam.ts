import { get, post } from './request'
import type { ExamGuideRoute, ExamTask, ExamTaskItem } from '@/types/api'

export interface SubmitExamResultsData {
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
}

export function getExamTaskGuide(taskNo: string) {
  return get<ExamGuideRoute[]>(`/exam-tasks/${taskNo}/guide`)
}

export function getExamTaskItems(taskNo: string) {
  return get<ExamTaskItem[]>(`/exam-tasks/${taskNo}/items`)
}

export function getDoctorExamTodo() {
  return get<ExamTask[]>('/doctor/exam-tasks/todo')
}

export function getDoctorExamItem(taskNo: string, taskItemNo: string) {
  return get<ExamTaskItem>(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}`)
}

export function startExamItem(taskNo: string, taskItemNo: string) {
  return post<void>(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/start`)
}

export function submitExamResults(taskNo: string, taskItemNo: string, data: SubmitExamResultsData) {
  return post<void>(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/results`, data)
}

export function completeExamItem(taskNo: string, taskItemNo: string) {
  return post<void>(`/doctor/exam-tasks/${taskNo}/items/${taskItemNo}/complete`)
}

export function generateExamTask(data: { orderNo: string }) {
  return post<ExamTask>('/admin/exam-tasks/generate', data)
}

