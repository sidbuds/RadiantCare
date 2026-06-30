<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentAgentSession, removeReportFromAgent, sendAgentMessage, sendReportToAgent, type AgentChatResult } from '@/api/ai-agent'
import { getMyReports } from '@/api/report'
import { ElMessage } from 'element-plus'

const router = useRouter()

const reports = ref<any[]>([])
const agentSessionNo = ref('')
const agentReports = ref<any[]>([])
const agentSelectedReportNo = ref('')
const agentQuestion = ref('')
const agentLoading = ref(false)
const agentSendingReport = ref(false)
const reportsLoading = ref(true)
const sidebarOpen = ref(true)
const messagesEndRef = ref<HTMLDivElement | null>(null)

const agentMessages = ref<Array<{
  role: 'user' | 'assistant'
  content: string
  result?: AgentChatResult
  timestamp: Date
}>>([])

const reportOptions = computed(() =>
  reports.value.map((item) => ({
    label: `${item.reportNo}${item.packageName ? ` - ${item.packageName}` : ''}`,
    value: item.reportNo,
  }))
)

const quickQuestions = [
  { text: '帮我解读这份报告', icon: '📋' },
  { text: '我需要重点关注什么？', icon: '🔍' },
  { text: '一句话帮我推荐体检套餐', icon: '💊' },
  { text: '哪些指标需要复查？', icon: '🔬' },
]

async function loadReports() {
  reportsLoading.value = true
  try {
    const res: any = await getMyReports()
    reports.value = res.data || []
  } finally {
    reportsLoading.value = false
  }
}

async function loadAgentSession() {
  const res: any = await getCurrentAgentSession()
  agentSessionNo.value = res.data?.sessionNo || ''
  agentReports.value = res.data?.reports || []
}

async function sendReportToAssistant() {
  if (!agentSessionNo.value || !agentSelectedReportNo.value) {
    ElMessage.warning('请选择要发送给智能助手的报告')
    return
  }
  agentSendingReport.value = true
  try {
    await sendReportToAgent(agentSessionNo.value, agentSelectedReportNo.value)
    ElMessage.success('报告已发送给智能助手')
    agentSelectedReportNo.value = ''
    await loadAgentSession()
  } finally {
    agentSendingReport.value = false
  }
}

async function revokeReport(reportNo: string) {
  if (!agentSessionNo.value) return
  try {
    await removeReportFromAgent(agentSessionNo.value, reportNo)
    ElMessage.success('已撤回报告授权')
    await loadAgentSession()
  } catch {}
}

async function askAgent(question?: string) {
  const text = (question || agentQuestion.value).trim()
  if (!agentSessionNo.value || !text) return
  agentLoading.value = true
  agentMessages.value.push({ role: 'user', content: text, timestamp: new Date() })
  agentQuestion.value = ''
  await scrollToBottom()
  try {
    const res: any = await sendAgentMessage(agentSessionNo.value, text)
    agentMessages.value.push({
      role: 'assistant',
      content: res.data?.answer || '',
      result: res.data,
      timestamp: new Date(),
    })
  } finally {
    agentLoading.value = false
    await scrollToBottom()
  }
}

function goAppointment(pkg: any) {
  router.push({
    path: '/user/appointments/create',
    query: { packageId: pkg.packageId, packageCode: pkg.packageCode },
  })
}

function goToConsultations() {
  router.push('/user/consultations')
}

async function scrollToBottom() {
  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    askAgent()
  }
}

onMounted(() => {
  loadReports()
  loadAgentSession()
})
</script>

<template>
  <div class="page-container ai-page">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">AI ASSISTANT</span>
        <h2>智能报告助手</h2>
      </div>
      <div class="header-actions">
        <el-button @click="goToConsultations">
          <el-icon><ChatDotRound /></el-icon>
          医生咨询
        </el-button>
      </div>
    </div>

    <div class="ai-shell" :class="{ 'sidebar-collapsed': !sidebarOpen }">
      <!-- 左侧边栏：报告管理 -->
      <aside class="ai-sidebar" v-show="sidebarOpen">
        <div class="sidebar-section">
          <div class="sidebar-title">
            <span>发送报告给助手</span>
          </div>
          <div class="report-send-row">
            <el-select
              v-model="agentSelectedReportNo"
              placeholder="选择报告"
              filterable
              clearable
              size="default"
            >
              <el-option
                v-for="item in reportOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button
              type="primary"
              :loading="agentSendingReport"
              :disabled="!agentSelectedReportNo"
              @click="sendReportToAssistant"
            >
              发送
            </el-button>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="sidebar-title">
            <span>已授权报告</span>
            <span class="count-badge">{{ agentReports.length }}</span>
          </div>
          <div class="authorized-list">
            <div
              v-for="item in agentReports"
              :key="item.reportNo"
              class="authorized-item"
            >
              <span class="auth-icon">✓</span>
              <span class="auth-no">{{ item.reportNo }}</span>
              <button
                class="auth-remove"
                type="button"
                title="撤回授权"
                @click="revokeReport(item.reportNo)"
              >×</button>
            </div>
            <div v-if="agentReports.length === 0" class="empty-hint">
              <p>尚未发送报告</p>
              <small>助手只能回答通用健康问题</small>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="sidebar-title">
            <span>快捷提问</span>
          </div>
          <div class="quick-grid">
            <button
              v-for="item in quickQuestions"
              :key="item.text"
              class="quick-chip"
              type="button"
              @click="askAgent(item.text)"
              :disabled="agentLoading"
            >
              <span class="chip-icon">{{ item.icon }}</span>
              <span>{{ item.text }}</span>
            </button>
          </div>
        </div>

        <div class="sidebar-footer">
          <p>助手基于你发送的报告提供分析，回答仅供参考，不构成医疗建议。</p>
        </div>
      </aside>

      <!-- 右侧聊天区域 -->
      <main class="ai-chat">
        <header class="chat-topbar">
          <button
            class="sidebar-toggle"
            type="button"
            @click="sidebarOpen = !sidebarOpen"
            :title="sidebarOpen ? '收起侧栏' : '展开侧栏'"
          >
            <el-icon :size="18"><Operation /></el-icon>
          </button>
          <div class="topbar-info">
            <strong>智能报告助手</strong>
            <span>基于你的体检报告，提供个性化健康分析</span>
          </div>
          <div class="topbar-status">
            <span class="status-dot" :class="{ active: !!agentSessionNo }"></span>
            <span>{{ agentSessionNo ? '已连接' : '连接中' }}</span>
          </div>
        </header>

        <div class="chat-messages">
          <!-- 欢迎消息 -->
          <div v-if="agentMessages.length === 0" class="welcome-state">
            <div class="welcome-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <rect width="48" height="48" rx="14" fill="var(--color-brand-light)" />
                <path
                  d="M16 20h16M16 26h10M14 14h20v20H14z"
                  stroke="var(--color-brand)"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <h3>你好，我是智能报告助手</h3>
            <p>我可以帮你解读体检报告、分析异常指标、推荐复查方案。请先在左侧发送一份报告给我。</p>
            <div class="welcome-suggestions">
              <button
                v-for="item in quickQuestions.slice(0, 3)"
                :key="item.text"
                class="suggestion-chip"
                type="button"
                @click="askAgent(item.text)"
                :disabled="!agentSessionNo || agentLoading"
              >
                {{ item.text }}
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(msg, index) in agentMessages"
            :key="index"
            class="message-row"
            :class="msg.role"
          >
            <div class="message-avatar" v-if="msg.role === 'assistant'">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <rect width="28" height="28" rx="8" fill="var(--color-brand-light)" />
                <path
                  d="M9 11h10M9 15h6M8 8h12v12H8z"
                  stroke="var(--color-brand)"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <div class="message-body">
              <div class="message-meta">
                <span>{{ msg.role === 'user' ? '我' : '智能助手' }}</span>
                <time>{{ msg.timestamp.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}</time>
              </div>
              <div class="message-bubble" :class="msg.role">
                <p>{{ msg.content }}</p>
                <template v-if="msg.result">
                  <p v-if="msg.result.orderPitch" class="order-pitch">{{ msg.result.orderPitch }}</p>
                  <div v-if="msg.result.recommendedPackages?.length" class="recommend-list">
                    <article
                      v-for="pkg in msg.result.recommendedPackages"
                      :key="pkg.packageCode"
                      class="recommend-card"
                    >
                      <div class="recommend-info">
                        <strong>{{ pkg.packageName }}</strong>
                        <span v-if="pkg.price" class="recommend-price">￥{{ pkg.price }}</span>
                        <p>{{ pkg.reason }}</p>
                      </div>
                      <el-button type="primary" size="small" @click="goAppointment(pkg)">
                        去预约
                      </el-button>
                    </article>
                  </div>
                  <div v-if="msg.result.safetyNotice" class="safety-notice">
                    <el-icon :size="16"><Warning /></el-icon>
                    <span>{{ msg.result.safetyNotice }}</span>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="agentLoading" class="message-row assistant">
            <div class="message-avatar">
              <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
                <rect width="28" height="28" rx="8" fill="var(--color-brand-light)" />
                <path
                  d="M9 11h10M9 15h6M8 8h12v12H8z"
                  stroke="var(--color-brand)"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
            <div class="message-body">
              <div class="message-bubble assistant loading-bubble">
                <span class="typing-dots">
                  <span></span><span></span><span></span>
                </span>
              </div>
            </div>
          </div>

          <div ref="messagesEndRef" />
        </div>

        <!-- 输入区域 -->
        <footer class="chat-input-area">
          <div class="input-wrapper">
            <textarea
              v-model="agentQuestion"
              placeholder="向智能助手提问，例如：我想复查血脂，帮我推荐套餐"
              rows="1"
              @keydown="handleKeydown"
              :disabled="agentLoading"
            />
            <button
              class="send-btn"
              type="button"
              :disabled="!agentQuestion.trim() || agentLoading"
              @click="askAgent()"
            >
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path
                  d="M15.5 2.5L7.5 10.5M15.5 2.5L10.5 15.5L7.5 10.5M15.5 2.5L2.5 7.5L7.5 10.5"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
          <p class="input-hint">按 Enter 发送，Shift + Enter 换行</p>
        </footer>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* ── 页面头部 ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ── 主布局：左右分栏 ── */
.ai-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
  height: calc(100vh - var(--header-height) - 180px);
  min-height: 560px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-lg);
  background: var(--color-panel);
  overflow: hidden;

  &.sidebar-collapsed {
    grid-template-columns: 1fr;
  }
}

/* ── 左侧边栏 ── */
.ai-sidebar {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-line);
  background: var(--color-surface-warm);
  overflow-y: auto;
}

.sidebar-section {
  padding: 20px;
  border-bottom: 1px solid var(--color-line);

  &:last-of-type {
    border-bottom: none;
  }
}

.sidebar-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  span {
    font-size: 12px;
    font-weight: 600;
    color: var(--color-ink-soft);
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }
}

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 11px;
  font-weight: 700;
  text-transform: none;
  letter-spacing: 0;
}

/* ── 报告发送 ── */
.report-send-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

/* ── 已授权报告列表 ── */
.authorized-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.authorized-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-success-light);
  border: 1px solid rgba(90, 154, 106, 0.15);
}

.auth-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-success);
  color: white;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.auth-no {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
  font-family: var(--font-mono);
  flex: 1;
  min-width: 0;
}

.auth-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--color-ink-muted);
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--duration-fast) ease;

  &:hover {
    background: var(--color-danger-light);
    color: var(--color-danger);
  }
}

.empty-hint {
  padding: 16px;
  text-align: center;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--color-line-strong);

  p {
    color: var(--color-ink-muted);
    font-size: 13px;
    margin: 0;
  }

  small {
    display: block;
    margin-top: 4px;
    color: var(--color-ink-faint);
    font-size: 11px;
  }
}

/* ── 快捷提问 ── */
.quick-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);
  color: var(--color-ink);
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: all var(--duration-fast) ease;

  &:hover:not(:disabled) {
    border-color: var(--color-brand-muted);
    background: var(--color-brand-light);
  }

  &:active:not(:disabled) {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.chip-icon {
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
}

/* ── 侧栏底部 ── */
.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid var(--color-line);

  p {
    margin: 0;
    font-size: 11px;
    color: var(--color-ink-faint);
    line-height: 1.6;
  }
}

/* ── 右侧聊天区域 ── */
.ai-chat {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

/* ── 聊天顶部栏 ── */
.chat-topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-line);
  background: var(--color-panel);
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-ink-soft);
  cursor: pointer;
  transition: all var(--duration-fast) ease;
  flex-shrink: 0;

  &:hover {
    border-color: var(--color-line-strong);
    color: var(--color-ink);
    background: var(--color-surface-warm);
  }
}

.topbar-info {
  flex: 1;
  min-width: 0;

  strong {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-ink);
  }

  span {
    display: block;
    font-size: 12px;
    color: var(--color-ink-muted);
    margin-top: 2px;
  }
}

.topbar-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-ink-muted);
  flex-shrink: 0;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-ink-faint);
  transition: background var(--duration-normal) ease;

  &.active {
    background: var(--color-success);
  }
}

/* ── 消息区域 ── */
.chat-messages {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* ── 欢迎状态 ── */
.welcome-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 48px 24px;
  min-height: 100%;
}

.welcome-icon {
  margin-bottom: 20px;
}

.welcome-state h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  color: var(--color-ink);
  margin: 0 0 8px;
}

.welcome-state > p {
  font-size: 14px;
  color: var(--color-ink-muted);
  max-width: 400px;
  line-height: 1.7;
  margin: 0 0 24px;
}

.welcome-suggestions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.suggestion-chip {
  padding: 8px 16px;
  border: 1px solid var(--color-line);
  border-radius: 999px;
  background: var(--color-panel);
  color: var(--color-ink-soft);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--duration-fast) ease;

  &:hover:not(:disabled) {
    border-color: var(--color-brand-muted);
    color: var(--color-brand);
    background: var(--color-brand-light);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* ── 消息行 ── */
.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  &.user {
    justify-content: flex-end;

    .message-body {
      align-items: flex-end;
    }

    .message-bubble {
      background: var(--color-brand-light);
      border-color: var(--color-line-accent);
    }

    .message-meta {
      flex-direction: row-reverse;
    }
  }

  &.assistant {
    justify-content: flex-start;
  }
}

.message-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}

.message-body {
  display: flex;
  flex-direction: column;
  max-width: min(640px, 78%);
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  span {
    font-size: 12px;
    font-weight: 600;
    color: var(--color-ink-soft);
  }

  time {
    font-size: 11px;
    color: var(--color-ink-faint);
  }
}

.message-bubble {
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--color-surface-warm);
  border: 1px solid var(--color-line);

  p {
    margin: 0;
    font-size: 14px;
    line-height: 1.75;
    color: var(--color-ink);
    white-space: pre-wrap;
  }
}

/* ── 加载动画 ── */
.loading-bubble {
  padding: 14px 20px;
}

.typing-dots {
  display: inline-flex;
  gap: 4px;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--color-ink-muted);
    animation: typingBounce 1.2s ease-in-out infinite;

    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* ── 推荐内容 ── */
.order-pitch {
  margin-top: 10px !important;
  color: var(--color-brand) !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.recommend-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);
}

.recommend-info {
  min-width: 0;

  strong {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: var(--color-ink);
  }

  p {
    margin: 4px 0 0 !important;
    font-size: 12px !important;
    color: var(--color-ink-muted) !important;
    line-height: 1.5 !important;
  }
}

.recommend-price {
  display: inline-block;
  margin-top: 2px;
  font-size: 14px;
  font-weight: 700;
  color: var(--color-brand);
  font-family: var(--font-mono);
}

/* ── 安全提示 ── */
.safety-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-warning-light);
  border: 1px solid rgba(184, 144, 80, 0.15);

  .el-icon {
    color: var(--color-warning);
    margin-top: 1px;
    flex-shrink: 0;
  }

  span {
    font-size: 12px;
    color: var(--color-ink-soft);
    line-height: 1.6;
  }
}

/* ── 输入区域 ── */
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--color-line);
  background: var(--color-panel);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-surface-warm);
  transition: border-color var(--duration-fast) ease;

  &:focus-within {
    border-color: var(--color-brand);
  }

  textarea {
    flex: 1;
    border: none;
    background: transparent;
    resize: none;
    font-size: 14px;
    line-height: 1.6;
    color: var(--color-ink);
    outline: none;
    min-height: 24px;
    max-height: 120px;
    font-family: var(--font-body);

    &::placeholder {
      color: var(--color-ink-muted);
    }
  }
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-brand);
  color: var(--color-on-brand);
  cursor: pointer;
  transition: all var(--duration-fast) ease;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    background: var(--color-brand-deep);
  }

  &:active:not(:disabled) {
    transform: scale(0.95);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.input-hint {
  margin: 6px 0 0;
  font-size: 11px;
  color: var(--color-ink-faint);
  text-align: right;
}

/* ── 响应式 ── */
@media (max-width: 860px) {
  .ai-shell {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .ai-sidebar {
    border-right: none;
    border-bottom: 1px solid var(--color-line);
  }

  .ai-chat {
    min-height: 480px;
  }

  .chat-messages {
    padding: 16px;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .welcome-state {
    padding: 32px 16px;
  }

  .message-body {
    max-width: 90%;
  }
}
</style>
