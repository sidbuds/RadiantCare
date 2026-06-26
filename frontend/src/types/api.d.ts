/** 统一 API 响应结构 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页请求参数 */
export interface PageParams {
  pageNum?: number
  pageSize?: number
}

/** 分页响应数据 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// ==================== 用户与认证 ====================

export interface UserInfo {
  accountId: number
  userId: number
  username: string
  displayName: string
  role: UserRole
}

export type UserRole = 'USER' | 'DOCTOR' | 'OPERATOR' | 'ADMIN'

export interface LoginData {
  accessToken: string
  accountId: number
  userId: number
  username: string
  displayName: string
  role: UserRole
}

// ==================== 公开接口 ====================

export interface PackageItem {
  id: number
  packageCode: string
  packageName: string
  category: string
  price: number
  status: number
  remark: string
  templateCode: string
  items?: ExamPackageItem[]
}

export interface ExamPackageItem {
  id: number
  packageId: number
  itemCode: string
  itemName: string
  sortNo: number
}

export interface ExamCenter {
  id: number
  centerCode: string
  centerName: string
  address: string
  phone: string
  status: number
}

export interface TimeSlot {
  timeSlotCode: string
  timeSlotName: string
  startTime: string
  endTime: string
  capacityTotal: number
  capacityUsed: number
  capacityLocked: number
  available: boolean
}

export interface CheckupGuideItem {
  title: string
  content: string
}

export interface FaqItem {
  question: string
  answer: string
}

// ==================== 预约 ====================

export interface Appointment {
  id: number
  appointmentNo: string
  taskNo?: string
  userId: number
  packageId: number
  packageName: string
  centerCode: string
  centerName: string
  appointDate: string
  timeSlotCode: string
  timeSlotName: string
  status: number
  statusText: string
  remark: string
  cancelReason: string
  createdAt: string
}

// ==================== 订单 ====================

export interface Order {
  id: number
  orderNo: string
  userId: number
  appointmentNo: string
  packageId: number
  packageName: string
  totalAmount: number
  payAmount: number
  payChannel: string
  orderStatus: number
  orderStatusText: string
  payStatus: number
  payTime: string
  createdAt: string
  items?: OrderItem[]
}

export interface OrderItem {
  id: number
  orderId: number
  itemCode: string
  itemName: string
  price: number
  quantity: number
}

export interface RefundApply {
  id: number
  applyNo: string
  orderNo: string
  userId: number
  userName: string
  refundAmount: number
  reason: string
  applyStatus: number
  applyStatusText: string
  auditRemark: string
  createdAt: string
}

export interface Schedule {
  id: number
  centerCode: string
  appointDate: string
  timeSlotCode: string
  resourceType: string
  resourceCode: string
  capacityTotal: number
  capacityUsed: number
  capacityLocked: number
  status: number
}

// ==================== 体检任务 ====================

export interface ExamTask {
  id: number
  taskNo: string
  orderNo: string
  userId: number
  packageName: string
  taskStatus: number
  taskStatusText: string
  totalItemCount: number
  completedItemCount: number
  progress: number
  createdAt: string
}

export interface ExamTaskItem {
  id: number
  taskItemId: string
  taskNo: string
  taskItemNo: string
  itemCode: string
  itemName: string
  departmentCode: string
  departmentName: string
  doctorId: number
  doctorName: string
  itemStatus: number
  itemStatusText: string
  startTime: string
  completeTime: string
}

export interface ExamGuideRoute {
  id: number
  routeNo: number
  departmentCode: string
  departmentName: string
  location: string
  remark: string
  status: number
}

export interface ExamResult {
  id: number
  taskItemNo: string
  metricCode: string
  metricName: string
  resultValue: string
  resultNumber: number | null
  unit: string
  refRange: string
  abnormal: boolean
  abnormalLevel: number
  conclusion: string
  entryDoctorName: string
  entryTime: string
}

// ==================== 报告 ====================

export interface ExamReport {
  id: number
  reportNo: string
  taskNo: string
  userId: number
  userName: string
  packageId: number
  packageName: string
  templateCode: string
  reportDate?: string
  reportStatus: number
  reportStatusText: string
  conclusion: string
  publishedAt: string
  createdAt: string
  items?: ExamReportItem[]
}

export interface ExamReportItem {
  id: number
  reportId: number
  metricCode: string
  metricName: string
  resultValue: string
  resultNumber: number | null
  unit: string
  refRange: string
  abnormal: boolean
  abnormalLevel: number
  conclusion: string
}

// ==================== 对比与健康建议 ====================

export interface CompareTask {
  id: number
  taskNo: string
  userId: number
  baselineReportNo: string
  compareReportNo: string
  taskStatus: number
  createdAt: string
}

export interface CompareResult {
  id: number
  taskNo: string
  metricCode: string
  metricName: string
  baselineValue: number
  compareValue: number
  changeValue: number
  changeRate: number
  trend: string
  trendTag: string
  riskLevel: number
  unit: string
}

export interface HealthAdvice {
  id: number
  taskNo: string
  metricCode: string
  adviceType: string
  adviceContent: string
  actionSuggestion: string
  riskLevel: number
}

export interface HealthRiskScore {
  taskNo: number
  scoreAbnormal: number
  scoreTrend: number
  totalScore: number
  riskLevel: string
}

// ==================== 咨询 ====================

export interface Consultation {
  id: number
  consultationNo: string
  userId: number
  userName: string
  doctorId: number
  doctorName: string
  reportNo: string
  sourceType: string
  consultationType: string
  consultationTitle: string
  consultationContent: string
  consultationStatus: number
  consultationStatusText: string
  priorityLevel: number
  createdAt: string
  replies?: ConsultationReply[]
}

export interface ConsultationReply {
  id: number
  consultationNo: string
  replyRole: string
  replyUserId: number
  replyUserName: string
  replyContent: string
  attachmentUrl: string
  createdAt: string
}

// ==================== 运营分析 ====================

export interface DashboardData {
  appointmentCount: number
  paidOrderCount: number
  refundCount: number
  publishedReportCount: number
  appointmentTrend: TrendItem[]
  orderConversion: ConversionItem[]
  packageAnalysis: PackageAnalysisItem[]
}

export interface TrendItem {
  date: string
  count?: number
  createdCount?: number
  cancelCount?: number
}

export interface ConversionItem {
  appointmentCount: number
  orderCount: number
  paidCount: number
  refundCount?: number
  conversionRate?: number
}

export interface PackageAnalysisItem {
  packageId: number
  packageName: string
  appointmentCount: number
  orderCount: number
  paidAmount: number
}

// ==================== 医生分析 ====================

export interface AbnormalOverview {
  doctorId: number
  startDate: string
  endDate: string
  totalResultCount: number
  abnormalCount: number
  abnormalRate: number
  highRiskCount: number
  highRiskRate: number
}

export interface AbnormalDistributionItem {
  itemCode: string
  itemName: string
  totalCount: number
  abnormalCount: number
  highRiskCount: number
  level1Count: number
  level2Count: number
  level3Count: number
}

export interface HighRiskUser {
  userId: number
  userName: string
  mobile: string
  highRiskItemCount: number
  maxAbnormalLevel: number
  items: HighRiskItem[]
}

export interface HighRiskItem {
  itemCode: string
  itemName: string
  resultValue: string
  abnormalLevel: number
}

export interface WorkloadOverview {
  doctorId: number
  startDate: string
  endDate: string
  completedItemCount: number
  resultEntryCount: number
  reviewCount: number
  consultationReplyCount: number
  avgCompletionMinutes: number
}

export interface WorkloadRankingItem {
  doctorId: number
  doctorName: string
  completedItemCount: number
  resultEntryCount: number
  reviewCount: number
  consultationReplyCount: number
  avgCompletionMinutes: number
  totalScore: number
}

export interface DepartmentWorkloadItem {
  departmentCode: string
  departmentName: string
  taskItemCount: number
  completedItemCount: number
  resultEntryCount: number
  abnormalCount: number
}
