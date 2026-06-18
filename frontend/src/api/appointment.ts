import request from './request'

export function getAvailableSlots(params: { centerCode: string; packageId: number; date: string }) {
  return request.get('/appointments/available-slots', { params })
}

export function createAppointment(data: {
  packageId: number
  centerCode: string
  appointDate: string
  timeSlotCode: string
  remark?: string
}) {
  return request.post('/appointments', data)
}

export function getAppointment(appointmentNo: string) {
  return request.get(`/appointments/${appointmentNo}`)
}

export function cancelAppointment(appointmentNo: string, reason?: string) {
  return request.post(`/appointments/${appointmentNo}/cancel`, { reason })
}

export function getMyAppointments() {
  return request.get('/appointments/mine')
}
