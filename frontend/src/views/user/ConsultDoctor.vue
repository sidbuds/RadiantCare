<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  closeConsultation,
  createConsultation,
  getMyConsultation,
  getMyConsultations,
  sendConsultationMessage,
} from '@/api/consultation'
import { getMyReports } from '@/api/report'
import { downloadReportPdf } from '@/api/report'
import { saveBlob } from '@/utils/download'
import { ElMessage } from 'element-plus'

const router = useRouter()
const consultations = ref<any[]>([])
const reports = ref<any[]>([])
const current = ref<any | null>(null)
const replies = ref<any[]>([])
const loading = ref(true)
const detailLoading = ref(false)
const creating = ref(false)
const sending = ref(false)
const sendingReport = ref(false)
const dialogVisible = ref(false)
const messageText = ref('')
const selectedReportNo = ref('')

const form = ref({
  reportNo: '',
  sourceType: 'REPORT_DETAIL',
  consultationType: 'ABNORMAL_INDEX',
  consultationTitle: '',
  consultationContent: '',
  priorityLevel: 1,
})

const currentNo = computed(() => current.value?.consultationNo || '')
const isClosed = computed(() => current.value?.consultationStatus === 3)
const reportOptions = computed(() => reports.value.map((item) => ({
  label: `${item.reportNo}${item.packageName ? ` - ${item.packageName}` : ''}`,
  value: item.reportNo,
})))

function statusLabel(status: number) {
  if (status === 2) return '医生已回复'
  if (status === 3) return '已关闭'
  return '等待医生'
}

function statusType(status: number) {
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  return 'warning'
}

async function loadReports() {
  const res: any = await getMyReports()
  reports.value = res.data || []
}

async function loadList(selectNo?: string) {
  loading.value = true
  try {
    const res: any = await getMyConsultations()
    consultations.value = res.data || []
    const nextNo = selectNo || currentNo.value || consultations.value[0]?.consultationNo
    if (nextNo) {
      await selectConsultation(nextNo)
    } else {
      current.value = null
      replies.value = []
    }
  } finally {
    loading.value = false
  }
}

async function selectConsultation(no: string) {
  detailLoading.value = true
  try {
    const res: any = await getMyConsultation(no)
    current.value = res.data?.consultation
    replies.value = res.data?.replies || []
  } finally {
    detailLoading.value = false
  }
}

async function openDialog() {
  if (reports.value.length === 0) await loadReports()
  if (reports.value.length === 0) {
    ElMessage.warning('当前没有可咨询的已发布报告')
    return
  }
  form.value = {
    reportNo: reports.value[0].reportNo || '',
    sourceType: 'REPORT_DETAIL',
    consultationType: 'ABNORMAL_INDEX',
    consultationTitle: '',
    consultationContent: '',
    priorityLevel: 1,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.reportNo || !form.value.consultationContent) {
    ElMessage.warning('请选择报告并填写咨询内容')
    return
  }
  creating.value = true
  try {
    const res: any = await createConsultation(form.value)
    ElMessage.success('咨询已发起，系统已匹配医生')
    dialogVisible.value = false
    await loadList(res.data?.consultationNo)
  } finally {
    creating.value = false
  }
}

async function sendMessage() {
  if (!currentNo.value || !messageText.value.trim()) return
  sending.value = true
  try {
    await sendConsultationMessage(currentNo.value, { replyContent: messageText.value.trim() })
    messageText.value = ''
    await loadList(currentNo.value)
  } finally {
    sending.value = false
  }
}

async function sendReport() {
  if (!currentNo.value || !selectedReportNo.value) {
    ElMessage.warning('请选择要发送的报告')
    return
  }
  sendingReport.value = true
  try {
    await sendConsultationMessage(currentNo.value, {
      replyContent: `我发送了一份体检报告：${selectedReportNo.value}`,
      messageType: 'REPORT_PDF',
      refReportNo: selectedReportNo.value,
    })
    selectedReportNo.value = ''
    await loadList(currentNo.value)
  } finally {
    sendingReport.value = false
  }
}

function goAiAssistant() {
  router.push('/user/ai-assistant')
}

async function downloadSharedReport(reportNo: string) {
  const blob = await downloadReportPdf(reportNo)
  saveBlob(blob, `${reportNo}.pdf`)
}

async function handleClose() {
  if (!currentNo.value) return
  await closeConsultation(currentNo.value)
  ElMessage.success('咨询已关闭')
  await loadList(currentNo.value)
}

async function shareProfile() {
  if (!currentNo.value) return
  try {
    const res = await import('@/api/user')
    const profileRes = await res.getHealthProfile()
    if (profileRes.data && Object.keys(profileRes.data).length > 0) {
      const text = [
        profileRes.data.allergyHistory ? '过敏史: ' + profileRes.data.allergyHistory : '',
        profileRes.data.medicalHistory ? '既往病史: ' + profileRes.data.medicalHistory : '',
        profileRes.data.familyHistory ? '家族病史: ' + profileRes.data.familyHistory : '',
        profileRes.data.medicationHistory ? '用药史: ' + profileRes.data.medicationHistory : '',
      ].filter(Boolean).join('\n')
      if (text) {
        await sendConsultationMessage(currentNo.value, { replyContent: '[健康档案分享]\n' + text })
        await loadList(currentNo.value)
      }
    }
  } catch {}
}

onMounted(() => {
  loadReports()
  loadList()
})
</script>

<template>
  <div class="page-container consult-page">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CONSULT</span>
        <h2>医生咨询</h2>
      </div>
      <div class="header-actions">
        <el-button @click="goAiAssistant">
          <el-icon><ChatDotRound /></el-icon>
          智能助手
        </el-button>
        <el-button type="primary" @click="openDialog">
          <el-icon><Plus /></el-icon>
          发起咨询
        </el-button>
      </div>
    </div>

    <section class="consult-shell">
      <aside class="conversation-list" v-loading="loading">
        <div class="sidebar-head">
          <span>咨询记录</span>
          <span class="count-badge">{{ consultations.length }}</span>
        </div>
        <button
          v-for="item in consultations"
          :key="item.consultationNo"
          class="conversation-item"
          :class="{ active: item.consultationNo === currentNo }"
          @click="selectConsultation(item.consultationNo)"
        >
          <div class="item-main">
            <strong>{{ item.consultationTitle || '报告咨询' }}</strong>
            <span>{{ item.doctorName || '待分配医生' }}</span>
          </div>
          <el-tag :type="statusType(item.consultationStatus)" size="small">
            {{ statusLabel(item.consultationStatus) }}
          </el-tag>
        </button>
        <el-empty v-if="!loading && consultations.length === 0" description="暂无咨询" />
      </aside>

      <main class="chat-panel" v-loading="detailLoading">
        <template v-if="current">
          <header class="chat-header">
            <div class="chat-header-info">
              <strong>{{ current.consultationTitle || '报告咨询' }}</strong>
              <span>报告 {{ current.reportNo }} · {{ current.doctorName }}</span>
            </div>
            <div class="chat-header-actions">
              <el-button v-if="!isClosed" link type="primary" @click="shareProfile">分享档案</el-button>
              <el-button v-if="!isClosed" link type="danger" @click="handleClose">关闭咨询</el-button>
            </div>
          </header>

          <div class="message-list">
            <div
              v-for="reply in replies"
              :key="reply.id"
              class="message-row"
              :class="{ mine: reply.replyRole === 'USER' }"
            >
              <div class="message-bubble">
                <div class="message-meta">
                  <span>{{ reply.replyRole === 'USER' ? '我' : reply.replyUserName || current.doctorName }}</span>
                  <time>{{ reply.replyTime || reply.createdAt }}</time>
                </div>
                <p>{{ reply.replyContent }}</p>
                <button
                  v-if="reply.messageType === 'REPORT_PDF' && reply.refReportNo"
                  class="report-attachment"
                  type="button"
                  @click="downloadSharedReport(reply.refReportNo)"
                >
                  <span>体检报告 PDF</span>
                  <strong>{{ reply.refReportNo }}</strong>
                </button>
              </div>
            </div>
          </div>

          <footer class="chat-input">
            <div class="input-row">
              <el-input
                v-model="messageText"
                type="textarea"
                :rows="2"
                :disabled="isClosed"
                placeholder="继续描述你的问题，Ctrl + Enter 发送"
                @keydown.ctrl.enter.prevent="sendMessage"
              />
              <el-button type="primary" :loading="sending" :disabled="isClosed" @click="sendMessage">发送</el-button>
            </div>
            <div class="report-send-bar">
              <el-select v-model="selectedReportNo" :disabled="isClosed" placeholder="选择报告发送给医生" filterable clearable>
                <el-option v-for="item in reportOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button :loading="sendingReport" :disabled="isClosed || !selectedReportNo" @click="sendReport">
                发送报告
              </el-button>
            </div>
          </footer>
        </template>
        <div v-else class="empty-chat">
          <div class="empty-icon">
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
          <h3>选择或发起一个咨询</h3>
          <p>在左侧选择已有咨询，或点击右上角发起新咨询</p>
        </div>
      </main>
    </section>

    <el-dialog v-model="dialogVisible" title="发起咨询" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="报告编号">
          <el-select v-model="form.reportNo" placeholder="请选择报告" style="width: 100%" filterable>
            <el-option v-for="item in reportOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题类型">
          <el-select v-model="form.consultationType" style="width: 100%">
            <el-option label="异常指标" value="ABNORMAL_INDEX" />
            <el-option label="报告解读" value="REPORT_INTERPRETATION" />
            <el-option label="复查建议" value="RECHECK_ADVICE" />
            <el-option label="其他问题" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.consultationTitle" placeholder="例如：血脂偏高需要注意什么？" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.consultationContent" type="textarea" :rows="5" placeholder="请描述异常项、症状和想确认的问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
/* ── 页面头部 ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ── 主布局 ── */
.consult-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 0;
  height: calc(100vh - var(--header-height) - 180px);
  min-height: 560px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-lg);
  background: var(--color-panel);
  overflow: hidden;
}

/* ── 左侧会话列表 ── */
.conversation-list {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-line);
  background: var(--color-surface-warm);
  overflow-y: auto;
  padding: 0;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--color-line);

  span:first-child {
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

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 14px 16px;
  border: none;
  border-bottom: 1px solid var(--color-line);
  background: transparent;
  color: var(--color-ink);
  text-align: left;
  cursor: pointer;
  transition: background var(--duration-fast) ease;

  &:hover {
    background: rgba(255, 255, 255, 0.04);
  }

  &.active {
    background: var(--color-brand-light);
    border-left: 2px solid var(--color-brand);
  }
}

.item-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;

  strong,
  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    font-size: 13px;
    font-weight: 600;
    color: var(--color-ink);
  }

  span {
    font-size: 12px;
    color: var(--color-ink-muted);
  }
}

/* ── 右侧聊天面板 ── */
.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-line);
  background: var(--color-panel);
}

.chat-header-info {
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

.chat-header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ── 消息列表 ── */
.message-list {
  flex: 1;
  min-height: 0;
  padding: 20px;
  overflow-y: auto;
}

.message-row {
  display: flex;
  margin-bottom: 14px;

  &.mine {
    justify-content: flex-end;

    .message-bubble {
      background: var(--color-brand-light);
      border-color: var(--color-line-accent);
    }

    .message-meta {
      flex-direction: row-reverse;
    }
  }
}

.message-bubble {
  max-width: min(640px, 78%);
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--color-surface-warm);
  border: 1px solid var(--color-line);
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;

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

.message-bubble p {
  margin: 0;
  color: var(--color-ink);
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.report-attachment {
  display: grid;
  gap: 4px;
  width: 100%;
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);
  color: var(--color-ink);
  text-align: left;
  cursor: pointer;
  transition: border-color var(--duration-fast) ease;

  &:hover {
    border-color: var(--color-brand-muted);
  }

  span {
    color: var(--color-ink-muted);
    font-size: 12px;
  }

  strong {
    font-size: 13px;
    font-family: var(--font-mono);
  }
}

/* ── 输入区域 ── */
.chat-input {
  padding: 16px 20px;
  border-top: 1px solid var(--color-line);
  background: var(--color-panel);
}

.input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
}

.report-send-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-top: 10px;
}

/* ── 空状态 ── */
.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 48px 24px;
  flex: 1;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-chat h3 {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 400;
  color: var(--color-ink);
  margin: 0 0 8px;
}

.empty-chat p {
  font-size: 14px;
  color: var(--color-ink-muted);
  margin: 0;
}

/* ── 响应式 ── */
@media (max-width: 860px) {
  .consult-shell {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .conversation-list {
    border-right: none;
    border-bottom: 1px solid var(--color-line);
    max-height: 240px;
  }

  .chat-panel {
    min-height: 400px;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .chat-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .input-row,
  .report-send-bar {
    grid-template-columns: 1fr;
  }

  .message-bubble {
    max-width: 90%;
  }
}
</style>
