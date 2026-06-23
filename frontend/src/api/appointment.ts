import { get, post } from './request'
import type { Appointment, TimeSlot } from '@/types/api'

export function getAvailableSlots(params: { centerCode: string; packageId: number; date: string }) {
  return get<TimeSlot[]>('/appointments/available-slots', { params })
}

export function createAppointment(data: {
  packageId: number
  centerCode: string
  appointDate: string
  timeSlotCode: string
  remark?: string
}) {
  return post<Appointment>('/appointments', data)
}

export function getAppointment(appointmentNo: string) {
  return get<Appointment>(`/appointments/${appointmentNo}`)
}

export function cancelAppointment(appointmentNo: string, reason?: string) {
  return post<void>(`/appointments/${appointmentNo}/cancel${reason ? `?reason=${encodeURIComponent(reason)}` : ''}`)
}

export function getMyAppointments() {
  return get<Appointment[]>('/appointments/mine')
}


