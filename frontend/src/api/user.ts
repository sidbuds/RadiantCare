import { get, put, post } from './request'

export interface UserProfile {
  userNo: string
  name: string
  gender: number | null
  birthDate: string | null
  idType: number | null
  idNo: string
  mobile: string
  email: string | null
  address: string | null
  emergencyContact: string | null
  emergencyMobile: string | null
}

export interface UpdateProfileData {
  name: string
  gender?: number | null
  birthDate?: string | null
  email?: string | null
  address?: string | null
  emergencyContact?: string | null
  emergencyMobile?: string | null
}

export interface ChangePasswordData {
  oldPassword: string
  newPassword: string
}

export function getProfile() {
  return get<UserProfile>('/user/profile')
}

export function updateProfile(data: UpdateProfileData) {
  return put<void>('/user/profile', data)
}

export function changePassword(data: ChangePasswordData) {
  return post<void>('/user/profile/password', data)
}


export interface HealthProfile {
  allergyHistory: string | null
  medicalHistory: string | null
  familyHistory: string | null
  medicationHistory: string | null
  smokingStatus: number | null
  drinkingStatus: number | null
  remark: string | null
}

export function getHealthProfile() {
  return get<HealthProfile>('/user/health-profile')
}

export function saveHealthProfile(data: Partial<HealthProfile>) {
  return put<void>('/user/health-profile', data)
}
