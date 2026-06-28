import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  // 游客端
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/guest/Home.vue') },
      { path: 'packages', name: 'PackageList', component: () => import('@/views/guest/PackageList.vue') },
      { path: 'packages/:code', name: 'PackageDetail', component: () => import('@/views/guest/PackageDetail.vue') },
      { path: 'centers', name: 'CenterList', component: () => import('@/views/guest/CenterList.vue') },
      { path: 'guide', name: 'CheckupGuide', component: () => import('@/views/guest/CheckupGuide.vue') },
      { path: 'faq', name: 'Faq', component: () => import('@/views/guest/Faq.vue') },
    ],
  },
  // 登录/注册
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginPage.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/login/RegisterPage.vue') },
  // 用户端
  {
    path: '/user',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true, role: 'USER' },
    children: [
      { path: 'profile', name: 'UserProfile', component: () => import('@/views/user/UserProfile.vue') },
      { path: 'appointments', name: 'MyAppointments', component: () => import('@/views/user/MyAppointments.vue') },
      { path: 'appointments/create', name: 'AppointmentCreate', component: () => import('@/views/user/AppointmentCreate.vue') },
      { path: 'appointments/:no', name: 'AppointmentDetail', component: () => import('@/views/user/AppointmentDetail.vue') },
      { path: 'orders/:no/confirm', name: 'OrderConfirm', component: () => import('@/views/user/OrderConfirm.vue') },
      { path: 'exam-tasks/:taskNo/guide', name: 'ExamTaskGuide', component: () => import('@/views/user/ExamTaskGuide.vue') },
      { path: 'reports', name: 'ReportList', component: () => import('@/views/user/ReportList.vue') },
      { path: 'reports/compare', name: 'ReportCompare', component: () => import('@/views/user/ReportCompare.vue') },
      { path: 'reports/:reportNo', name: 'ReportDetail', component: () => import('@/views/user/ReportDetail.vue') },
      { path: 'health-profile', name: 'HealthProfile', component: () => import('@/views/user/HealthProfile.vue') },
      { path: 'consultations', name: 'ConsultDoctor', component: () => import('@/views/user/ConsultDoctor.vue') },
    ],
  },
  // 医生端
  {
    path: '/doctor',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'DOCTOR' },
    children: [
      { path: 'exam-tasks', name: 'DoctorExamTaskTodo', component: () => import('@/views/doctor/ExamTaskTodo.vue') },
      { path: 'exam-tasks/:taskNo/items/:itemNo/entry', name: 'ResultEntry', component: () => import('@/views/doctor/ResultEntry.vue') },
      { path: 'reports/review', name: 'DoctorReportReview', component: () => import('@/views/doctor/ReportReviewTodo.vue') },
      { path: 'consultations', name: 'DoctorConsultationTodo', component: () => import('@/views/doctor/ConsultationTodo.vue') },
      { path: 'analytics/abnormal', name: 'AbnormalAnalysis', component: () => import('@/views/doctor/AbnormalAnalysis.vue') },
      { path: 'analytics/workload', name: 'WorkloadStats', component: () => import('@/views/doctor/WorkloadStats.vue') },
    ],
  },
  // 运营端
  {
    path: '/operator',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'OPERATOR' },
    children: [
      { path: 'packages', name: 'PackageManage', component: () => import('@/views/operator/PackageManage.vue') },
      { path: 'centers', name: 'OperatorCenterManage', component: () => import('@/views/operator/CenterManage.vue') },
      { path: 'schedules', name: 'ScheduleManage', component: () => import('@/views/operator/ScheduleManage.vue') },
      { path: 'appointments', name: 'AppointmentManage', component: () => import('@/views/operator/AppointmentManage.vue') },
      { path: 'orders', name: 'OrderManage', component: () => import('@/views/operator/OrderManage.vue') },
      { path: 'refunds', name: 'RefundManage', component: () => import('@/views/operator/RefundManage.vue') },
      { path: 'analytics', name: 'AnalyticsDashboard', component: () => import('@/views/operator/AnalyticsDashboard.vue') },
    ],
  },
  // 管理端
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: 'doctors', name: 'DoctorManage', component: () => import('@/views/admin/DoctorManage.vue') },
      { path: 'roles', name: 'RoleManage', component: () => import('@/views/admin/RoleManage.vue') },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'dicts', name: 'DictManage', component: () => import('@/views/admin/DictManage.vue') },
      { path: 'configs', name: 'SystemConfig', component: () => import('@/views/admin/SystemConfig.vue') },
      { path: 'audit-logs', name: 'AuditLog', component: () => import('@/views/admin/AuditLog.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const roleHomeMap: Record<string, string> = {
  USER: '/',
  DOCTOR: '/doctor/exam-tasks',
  OPERATOR: '/operator/packages',
  ADMIN: '/admin/doctors',
}

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.role && userStore.role && to.meta.role !== userStore.role) {
    next(roleHomeMap[userStore.role] || '/')
    return
  }
  next()
})

export default router
export { roleHomeMap }
