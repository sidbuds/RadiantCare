import request from './request'

export function listPackages() {
  return request.get('/public/packages')
}

export function getPackageDetail(packageCode: string) {
  return request.get(`/public/packages/${packageCode}`)
}

export function listCenters() {
  return request.get('/public/centers')
}

export function getCenterDetail(centerCode: string) {
  return request.get(`/public/centers/${centerCode}`)
}

export function getCenterSlots(centerCode: string, date?: string) {
  return request.get(`/public/centers/${centerCode}/slots`, { params: { date } })
}

export function getCheckupGuide() {
  return request.get('/public/content/checkup-guide')
}

export function getFaq() {
  return request.get('/public/content/faq')
}
