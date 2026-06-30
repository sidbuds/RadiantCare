import { del, get, post } from './request'

export interface AgentSession {
  sessionNo: string
  status: number
  reports: Array<{ reportNo: string; reportId?: number }>
}

export interface AgentPackageRecommendation {
  packageId: number
  packageCode: string
  packageName: string
  price?: number
  reason?: string
  actionUrl?: string
}

export interface AgentChatResult {
  answer: string
  orderPitch?: string
  recommendedPackages?: AgentPackageRecommendation[]
  safetyNotice?: string
}

export function createAgentSession() {
  return post<AgentSession>('/ai/report-agent/sessions')
}

export function getCurrentAgentSession() {
  return get<AgentSession>('/ai/report-agent/sessions/current')
}

export function sendReportToAgent(sessionNo: string, reportNo: string) {
  return post<{ sessionNo: string; reportNo: string; status: string }>(`/ai/report-agent/sessions/${sessionNo}/reports`, { reportNo })
}

export function removeReportFromAgent(sessionNo: string, reportNo: string) {
  return del(`/ai/report-agent/sessions/${sessionNo}/reports/${reportNo}`)
}

export function sendAgentMessage(sessionNo: string, question: string) {
  return post<AgentChatResult>(`/ai/report-agent/sessions/${sessionNo}/messages`, { question }, { timeout: 60000 })
}
