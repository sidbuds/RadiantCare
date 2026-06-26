import { get } from './request'

export interface Department {
  departmentCode: string
  departmentName: string
  centerCode?: string
}

export function listDepartments() {
  return get<Department[]>('/public/departments')
}
