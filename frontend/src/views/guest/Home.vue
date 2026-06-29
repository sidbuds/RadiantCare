<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Box,
  Calendar,
  ChatDotRound,
  CircleCheck,
  Document,
  Goods,
  Location,
  MapLocation,
  Right,
  Tickets,
  UserFilled,
} from '@element-plus/icons-vue'
import { listPackages } from '@/api/public'
import { getMyAppointments } from '@/api/appointment'
import { getMyReports } from '@/api/report'
import { getMyConsultations } from '@/api/consultation'
import { useUserStore } from '@/stores/user'
import type { Appointment, Consultation, ExamReport, PackageItem } from '@/types/api'

interface StatusSummary {
  key: string
  title: string
  value: string
  detail: string
  actionText: string
  route: string
  icon: typeof Calendar
}

const router = useRouter()
const userStore = useUserStore()

const packages = ref<PackageItem[]>([])
const appointments = ref<Appointment[]>([])
const reports = ref<ExamReport[]>([])
const consultations = ref<Consultation[]>([])
const packageLoading = ref(true)
const summaryLoading = ref(false)
const mounted = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const displayName = computed(() => userStore.userInfo?.displayName || '用户')

const flowSteps = [
  { title: '浏览套餐', desc: '按年龄、预算和检查重点确定体检方案', icon: Box },
  { title: '选择中心', desc: '确认体检地点、服务时间和可预约资源', icon: MapLocation },
  { title: '选择时间', desc: '挑选合适日期与时段，避开行程冲突', icon: Calendar },
  { title: '确认预约', desc: '提交预约信息，后续可随时查看进度', icon: CircleCheck },
  { title: '查看报告', desc: '体检完成后跟进导检任务、报告和咨询', icon: Tickets },
]

const guestSummaries = computed<StatusSummary[]>(() => [
  {
    key: 'appointment',
    title: '我的预约',
    value: '登录后同步',
    detail: '预约成功后，这里会显示最近一次体检安排。',
    actionText: '去登录',
    route: '/login',
    icon: Calendar,
  },
  {
    key: 'report',
    title: '体检报告',
    value: '集中查看',
    detail: '报告发布后可查看结论、指标明细和历史对比。',
    actionText: '浏览套餐',
    route: '/packages',
    icon: Document,
  },
  {
    key: 'consultation',
    title: '医生咨询',
    value: '报告后跟进',
    detail: '发现异常指标时，可围绕报告发起在线咨询。',
    actionText: '查看流程',
    route: '/guide',
    icon: ChatDotRound,
  },
])

const statusSummaries = computed<StatusSummary[]>(() => {
  if (!isLoggedIn.value) {
    return guestSummaries.value
  }

  const latestAppointment = firstByTime(appointments.value, 'createdAt', 'appointDate')
  const latestReport = firstByTime(reports.value, 'publishedAt', 'createdAt')
  const latestConsultation = firstByTime(consultations.value, 'createdAt')

  return [
    {
      key: 'appointment',
      title: '最近预约',
      value: latestAppointment ? appointmentStatusText(latestAppointment) : '暂无预约',
      detail: latestAppointment
        ? `${latestAppointment.centerName || '体检中心'} · ${formatDate(latestAppointment.appointDate)} ${latestAppointment.timeSlotName || ''}`.trim()
        : '选择套餐后即可提交预约，这里会同步最新安排。',
      actionText: latestAppointment ? '查看预约' : '去预约',
      route: latestAppointment ? '/user/appointments' : '/packages',
      icon: Calendar,
    },
    {
      key: 'report',
      title: '最新报告',
      value: latestReport ? reportStatusText(latestReport) : '暂无报告',
      detail: latestReport
        ? `报告 ${latestReport.reportNo || ''} · ${formatDate(latestReport.publishedAt || latestReport.reportDate || latestReport.createdAt)}`
        : '报告发布后会在这里提示，方便继续查看详情。',
      actionText: latestReport ? '查看报告' : '报告列表',
      route: '/user/reports',
      icon: Document,
    },
    {
      key: 'consultation',
      title: '最近咨询',
      value: latestConsultation ? consultationStatusText(latestConsultation) : '暂无咨询',
      detail: latestConsultation
        ? `${latestConsultation.consultationTitle || '报告咨询'} · ${formatDate(latestConsultation.createdAt)}`
        : '有报告疑问时，可以从报告页发起医生咨询。',
      actionText: latestConsultation ? '查看咨询' : '去咨询',
      route: '/user/consultations',
      icon: ChatDotRound,
    },
  ]
})

function appointmentStatusText(appointment: Appointment) {
  if (appointment.statusText) {
    return appointment.statusText
  }
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '待体检',
    2: '已完成',
    3: '已取消',
    4: '爽约',
  }
  return statusMap[appointment.status] || '状态待确认'
}

function reportStatusText(report: ExamReport) {
  // 优先用后端返回的文本
  if (report.reportStatusText) {
    return report.reportStatusText
  }
  // 根据状态码映射
  const statusMap: Record<number, string> = {
    0: '待生成',
    1: '待审核',
    3: '已发布',
    4: '待处理',
  }
  return statusMap[report.reportStatus ?? report.status] || '暂无报告'
}

function consultationStatusText(consultation: Consultation) {
  if (consultation.consultationStatusText) {
    return consultation.consultationStatusText
  }
  const statusMap: Record<number, string> = {
    0: '待回复',
    1: '已回复',
    2: '已关闭',
  }
  return statusMap[consultation.consultationStatus] || '暂无咨询'
}

onMounted(async () => {
  await replayEntrance()
  await Promise.all([loadPackages(), loadStatusSummary()])
})

async function replayEntrance() {
  mounted.value = false
  await nextTick()
  await new Promise<void>((resolve) => {
    requestAnimationFrame(() => resolve())
  })
  mounted.value = true
}

async function loadPackages() {
  packageLoading.value = true
  try {
    const res = await listPackages()
    packages.value = (res.data || []).slice(0, 3)
  } catch {
    packages.value = []
  } finally {
    packageLoading.value = false
  }
}

async function loadStatusSummary() {
  if (!isLoggedIn.value) {
    return
  }

  summaryLoading.value = true
  try {
    const [appointmentRes, reportRes, consultationRes] = await Promise.allSettled([
      getMyAppointments(),
      getMyReports(),
      getMyConsultations(),
    ])

    appointments.value = appointmentRes.status === 'fulfilled' ? appointmentRes.value.data || [] : []
    reports.value = reportRes.status === 'fulfilled' ? reportRes.value.data || [] : []
    consultations.value = consultationRes.status === 'fulfilled' ? consultationRes.value.data || [] : []
  } finally {
    summaryLoading.value = false
  }
}

function firstByTime<T extends Record<string, any>>(items: T[], ...fields: string[]) {
  return [...items].sort((a, b) => {
    const timeA = pickTime(a, fields)
    const timeB = pickTime(b, fields)
    return timeB - timeA
  })[0]
}

function pickTime(item: Record<string, any>, fields: string[]) {
  for (const field of fields) {
    if (item[field]) {
      const time = new Date(item[field]).getTime()
      if (!Number.isNaN(time)) {
        return time
      }
    }
  }
  return 0
}

function formatDate(value?: string) {
  if (!value) {
    return '时间待确认'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function goPackage(code: string) {
  router.push(`/packages/${code}`)
}

function goRoute(route: string) {
  router.push(route)
}
</script>

<template>
  <div class="home-page">
    <section class="hero-board" :class="{ 'is-mounted': mounted }">
      <div class="hero-board__main">
        <div class="hero-copy">
          <h1>{{ isLoggedIn ? `${displayName}，欢迎回来` : '科学体检，从清楚下一步开始' }}</h1>
          <p>
            {{ isLoggedIn
              ? '您的预约、报告和咨询集中在首页同步，回到平台就能继续处理下一步。'
              : '从选套餐到查报告，按一条明确路径走。少一点跳转，多一点确定感。'
            }}
          </p>
        </div>

        <div class="flow-panel" aria-label="用户操作主链路">
          <div class="flow-panel__head">
            <h2>您的健康管理流程</h2>
          </div>
          <div class="flow-line" aria-hidden="true">
            <span v-for="(_, index) in flowSteps" :key="index">{{ index + 1 }}</span>
          </div>
          <ol class="flow-card-list">
            <li
              v-for="(step, index) in flowSteps"
              :key="step.title"
              class="flow-card"
              :style="{ '--delay': `${0.28 + index * 0.08}s` }"
            >
              <span class="flow-icon">
                <el-icon :size="26"><component :is="step.icon" /></el-icon>
              </span>
              <strong>{{ step.title }}</strong>
              <small>{{ step.desc }}</small>
            </li>
          </ol>
        </div>
      </div>

      <aside class="summary-panel" :class="{ 'is-mounted': mounted }" aria-label="首页状态摘要">
        <div class="summary-head">
          <div>
            <h2>{{ isLoggedIn ? '任务状态摘要' : '登录后查看个人进度' }}</h2>
          </div>
          <span class="summary-state">{{ summaryLoading ? '同步中' : '已就绪' }}</span>
        </div>

        <div class="summary-list">
          <button
            v-for="item in statusSummaries"
            :key="item.key"
            class="summary-card"
            type="button"
            @click="goRoute(item.route)"
          >
            <span class="summary-icon">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </span>
            <span class="summary-content">
              <span class="summary-title">{{ item.title }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.detail }}</small>
            </span>
            <span class="summary-action">
              {{ item.actionText }}
              <el-icon :size="13"><Right /></el-icon>
            </span>
          </button>
        </div>
      </aside>
    </section>

    <section class="packages-section" :class="{ 'is-mounted': mounted }">
      <div class="section-head">
        <div>
          <span class="section-eyebrow">PACKAGE</span>
          <h2>{{ isLoggedIn ? '继续选择体检套餐' : '热门体检套餐' }}</h2>
        </div>
        <button class="text-link" type="button" @click="goRoute('/packages')">
          查看全部
          <el-icon :size="14"><Right /></el-icon>
        </button>
      </div>

      <div v-if="packageLoading" class="package-grid">
        <article v-for="index in 3" :key="index" class="package-card package-card--loading">
          <span />
          <strong />
          <p />
        </article>
      </div>

      <div v-else class="package-grid">
        <article
          v-for="(pkg, idx) in packages"
          :key="pkg.id"
          class="package-card data-card"
          :class="{ 'is-mounted': mounted }"
          :style="{ '--delay': `${0.2 + idx * 0.08}s` }"
          @click="goPackage(pkg.packageCode)"
        >
          <div class="package-card__top">
            <span class="package-tag">
              <el-icon :size="13"><Goods /></el-icon>
              {{ pkg.category || '精选套餐' }}
            </span>
            <span class="package-price">￥{{ pkg.price }}</span>
          </div>
          <h3>{{ pkg.packageName }}</h3>
          <p class="remark">{{ pkg.remark || '覆盖常规检查项目，适合年度健康体检。' }}</p>
          <div class="package-card__foot">
            <span>
              <el-icon :size="13"><Location /></el-icon>
              可在线预约
            </span>
            <el-button type="primary" size="small">查看套餐</el-button>
          </div>
        </article>

        <div v-if="packages.length === 0" class="empty-state">
          <el-icon :size="24"><UserFilled /></el-icon>
          <strong>暂无可展示套餐</strong>
          <span>可以稍后再来，或先查看体检中心与检查流程。</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.home-page {
  width: min(var(--content-width), calc(100% - 48px));
  margin: 0 auto;
  padding: 32px 0 64px;
}

.hero-board,
.summary-panel,
.packages-section {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.58s var(--ease-out-expo), transform 0.58s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-copy,
.flow-panel,
.flow-card,
.summary-card {
  opacity: 0;
  transform: translateY(16px);
  transition:
    opacity 0.5s var(--ease-out-expo),
    transform 0.5s var(--ease-out-expo),
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.hero-board.is-mounted {
  .hero-copy {
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.08s;
  }

  .flow-panel {
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.18s;
  }

  .flow-card {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  .summary-card {
    opacity: 1;
    transform: translateY(0);
  }

  .summary-card:nth-child(1) {
    transition-delay: 0.18s;
  }

  .summary-card:nth-child(2) {
    transition-delay: 0.28s;
  }

  .summary-card:nth-child(3) {
    transition-delay: 0.38s;
  }

  .flow-card:hover,
  .flow-card:focus-within {
    transform: translateY(-6px);
  }

  .summary-card:hover,
  .summary-card:focus-visible {
    transform: translateY(-4px);
  }
}

.hero-board {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 390px;
  gap: 24px;
  min-height: 650px;
  margin-bottom: 24px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 32px;
  background:
    radial-gradient(circle at 10% 12%, rgba(218, 241, 235, 0.82), transparent 34%),
    radial-gradient(circle at 86% 16%, rgba(228, 237, 244, 0.72), transparent 32%),
    linear-gradient(135deg, rgba(248, 252, 250, 0.92), rgba(231, 241, 240, 0.9));
  box-shadow: 0 24px 70px rgba(34, 57, 57, 0.12);
  overflow: hidden;
}

.hero-board__main {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 30px;
  padding: 20px 0 0 22px;
}

.hero-copy {
  padding-top: 18px;

  h1 {
    max-width: 500px;
    margin: 0;
    font-family: var(--font-display);
    font-size: clamp(38px, 4.2vw, 58px);
    line-height: 1.12;
    font-weight: 900;
    letter-spacing: 0;
    color: #192322;
  }

  p {
    max-width: 520px;
    margin: 20px 0 0;
    color: rgba(25, 35, 34, 0.72);
    font-size: 17px;
    line-height: 1.85;
  }
}

.flow-panel {
  margin-top: auto;
  padding: 28px;
  border: 1px solid rgba(255, 255, 255, 0.92);
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.62), rgba(255, 255, 255, 0.28)),
    rgba(229, 243, 240, 0.32);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    0 18px 46px rgba(56, 82, 82, 0.08);
}

.flow-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;

  h2 {
    margin: 0;
    color: #192322;
    font-family: var(--font-display);
    font-size: 30px;
    line-height: 1.2;
  }

  span {
    color: rgba(23, 25, 29, 0.76);
    font-size: 16px;
    font-weight: 700;
  }
}

.flow-line {
  position: relative;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  align-items: center;
  margin: 18px 0 24px;

  &::before {
    content: "";
    position: absolute;
    left: 8px;
    right: 8px;
    top: 50%;
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, rgba(79, 137, 130, 0.16), rgba(79, 137, 130, 0.7), rgba(79, 137, 130, 0.16));
    transform: translateY(-50%);
  }

  span {
    position: relative;
    z-index: 1;
    justify-self: center;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 999px;
    background: #4f8982;
    color: #fff;
    font-family: var(--font-display);
    font-size: 16px;
    font-weight: 800;
  }
}

.flow-card-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(118px, 1fr));
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.flow-card {
  position: relative;
  min-height: 204px;
  padding: 28px 14px 18px;
  border: 1px solid rgba(79, 137, 130, 0.18);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 28px rgba(49, 78, 78, 0.1);
  text-align: center;

  &::before {
    content: "";
    position: absolute;
    top: -14px;
    left: 50%;
    width: 24px;
    height: 24px;
    background: inherit;
    border-left: 1px solid rgba(79, 137, 130, 0.18);
    border-top: 1px solid rgba(79, 137, 130, 0.18);
    transform: translateX(-50%) rotate(45deg);
    transition: border-color 0.2s ease, background 0.2s ease;
  }

  &:hover,
  &:focus-within {
    transform: translateY(-6px);
    border-color: rgba(79, 137, 130, 0.34);
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 22px 40px rgba(49, 78, 78, 0.14);
  }

  &:hover::before,
  &:focus-within::before {
    border-color: rgba(79, 137, 130, 0.34);
    background: rgba(255, 255, 255, 0.96);
  }

  &:hover .flow-icon,
  &:focus-within .flow-icon {
    color: #fff;
    background: #4f8982;
    transform: translateY(-3px) scale(1.04);
    box-shadow: 0 12px 24px rgba(79, 137, 130, 0.22);
  }

  strong,
  small {
    display: block;
  }

  strong {
    margin-top: 16px;
    color: #16201f;
    font-size: clamp(16px, 1.05vw, 18px);
    line-height: 1.2;
    white-space: nowrap;
  }

  small {
    margin-top: 8px;
    color: rgba(22, 32, 31, 0.66);
    font-size: 14px;
    line-height: 1.62;
  }
}

.flow-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin: 0 auto;
  border-radius: 999px;
  background: linear-gradient(145deg, rgba(79, 137, 130, 0.16), rgba(79, 137, 130, 0.07));
  color: #4f8982;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.summary-panel {
  display: flex;
  flex-direction: column;
  align-self: end;
  gap: 20px;
  padding: 28px;
  min-height: 0;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 26px;
  background:
    radial-gradient(circle at 100% 100%, rgba(218, 241, 235, 0.55), transparent 22%),
    linear-gradient(145deg, rgba(250, 253, 252, 0.82), rgba(233, 242, 240, 0.78));
  color: #192322;
  transition-delay: 0.1s;
  transform: none;
  box-shadow: 0 18px 48px rgba(45, 68, 68, 0.1);
}

.summary-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;

  h2 {
    max-width: 280px;
    margin: 0;
    font-family: var(--font-display);
    font-size: 30px;
    line-height: 1.25;
    color: #192322;
  }
}

.summary-state {
  color: rgba(25, 35, 34, 0.52);
  font-size: 13px;
  font-weight: 700;
}

.summary-state {
  height: fit-content;
  padding: 7px 12px;
  border: 1px solid rgba(79, 137, 130, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.44);
  white-space: nowrap;
}

.summary-list {
  display: grid;
  gap: 14px;
}

.summary-card {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 16px;
  width: 100%;
  min-height: 124px;
  padding: 20px;
  border: 1px solid rgba(79, 137, 130, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  color: inherit;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(52, 73, 73, 0.08);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo), background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;

  &:hover,
  &:focus-visible {
    transform: translateY(-4px);
    border-color: rgba(79, 137, 130, 0.28);
    background: rgba(255, 255, 255, 0.92);
    box-shadow: 0 18px 32px rgba(52, 73, 73, 0.12);
    outline: none;
  }

  &:hover .summary-icon,
  &:focus-visible .summary-icon {
    color: #fff;
    background: #4f8982;
    transform: scale(1.05);
  }

  &:hover .summary-action,
  &:focus-visible .summary-action {
    gap: 8px;
  }
}

.summary-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(79, 137, 130, 0.14);
  color: #4f8982;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.summary-content {
  min-width: 0;

  .summary-title,
  strong,
  small {
    display: block;
  }

  .summary-title {
    color: rgba(25, 35, 34, 0.48);
    font-size: 14px;
  }

  strong {
    margin-top: 4px;
    color: #16201f;
    font-size: 20px;
    line-height: 1.25;
  }

  small {
    margin-top: 6px;
    color: rgba(22, 32, 31, 0.7);
    font-size: 13px;
    line-height: 1.55;
  }
}

.summary-action {
  grid-column: 2;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  color: #4f8982;
  font-size: 13px;
  font-weight: 700;
  transition: gap 0.18s ease;
}

.packages-section {
  padding: 28px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-xl);
  background: var(--color-panel);
  box-shadow: var(--shadow-sm);
  transition-delay: 0.18s;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;

  h2 {
    margin: 6px 0 0;
    font-family: var(--font-display);
    font-size: clamp(22px, 2.2vw, 30px);
    line-height: 1.2;
    color: var(--color-ink);
  }
}

.text-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 0;
  border: 0;
  background: transparent;
  color: var(--color-brand);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.18s ease, gap 0.18s ease, transform 0.18s ease;

  &:hover,
  &:focus-visible {
    gap: 9px;
    color: var(--color-brand-deep);
    transform: translateX(2px);
    outline: none;
  }
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.package-card {
  display: flex;
  flex-direction: column;
  min-height: 210px;
  padding: 22px;
  cursor: pointer;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.35s var(--ease-out-expo), transform 0.35s var(--ease-out-expo), border-color 0.18s ease, box-shadow 0.18s ease;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
    transition-delay: var(--delay);
  }

  &:hover {
    transform: translateY(-3px);
    border-color: var(--color-brand-muted);
    box-shadow: var(--shadow-xl);
  }

  h3 {
    margin: 16px 0 8px;
    color: var(--color-ink);
    font-family: var(--font-display);
    font-size: 18px;
    line-height: 1.3;
  }
}

.package-card--loading {
  border: 1px solid var(--color-line);
  border-radius: var(--radius-lg);
  background: var(--color-panel);
  opacity: 1;
  transform: none;
  cursor: default;

  span,
  strong,
  p {
    display: block;
    border-radius: var(--radius-sm);
    background: rgba(255, 255, 255, 0.08);
  }

  span {
    width: 42%;
    height: 24px;
  }

  strong {
    width: 70%;
    height: 26px;
    margin-top: 22px;
  }

  p {
    width: 100%;
    height: 54px;
    margin-top: 14px;
  }
}

.package-card__top,
.package-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.package-tag,
.package-card__foot span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.package-tag {
  min-width: 0;
  padding: 5px 10px;
  border: 1px solid var(--color-line);
  border-radius: 999px;
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 11px;
  font-weight: 700;
}

.package-price {
  flex-shrink: 0;
  color: var(--color-brand);
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 900;
}

.remark {
  flex: 1;
  margin: 0;
  color: var(--color-ink-muted);
  font-size: 12px;
  line-height: 1.7;
}

.package-card__foot {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--color-line);

  span {
    color: var(--color-ink-muted);
    font-size: 12px;
    font-weight: 600;
  }
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px;
  border: 1px dashed var(--color-line);
  border-radius: var(--radius-lg);
  color: var(--color-ink-muted);

  strong {
    color: var(--color-ink);
  }
}

@media (max-width: 1180px) {
  .hero-board {
    grid-template-columns: 1fr;
  }

  .hero-board__main {
    padding: 36px;
  }

  .summary-panel {
    align-self: stretch;
    border-left: 0;
  }

  .flow-card-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flow-line {
    display: none;
  }

  .flow-card::before {
    display: none;
  }

  .package-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .home-page {
    width: min(calc(100% - 24px), var(--content-width));
    padding: 22px 0 44px;
  }

  .hero-board__main,
  .summary-panel,
  .packages-section {
    padding: 22px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .flow-card-list,
  .package-grid {
    grid-template-columns: 1fr;
  }

  .section-head,
  .summary-head {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-board,
  .summary-panel,
  .packages-section,
  .hero-copy,
  .flow-panel,
  .flow-card,
  .summary-card,
  .package-card {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
</style>
