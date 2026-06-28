<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { downloadDoctorSharedReportPdf, getDoctorConsultation, getDoctorConsultationTodo, replyConsultation } from '@/api/consultation'
import { saveBlob } from '@/utils/download'
import { ElMessage } from 'element-plus'

const consultations = ref<any[]>([])
const current = ref<any | null>(null)
const replies = ref<any[]>([])
const loading = ref(true)
const detailLoading = ref(false)
const sending = ref(false)
const mounted = ref(false)
const replyText = ref('')

const currentNo = computed(() => current.value?.consultationNo || '')
const isClosed = computed(() => current.value?.consultationStatus === 3)

function statusLabel(status: number) {
  if (status === 2) return '已回复'
  if (status === 3) return '已关闭'
  return '待回复'
}

function statusType(status: number) {
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  return 'warning'
}

async function loadList(selectNo?: string) {
  loading.value = true
  try {
    const res: any = await getDoctorConsultationTodo()
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
    const res: any = await getDoctorConsultation(no)
    current.value = res.data?.consultation
    replies.value = res.data?.replies || []
  } finally {
    detailLoading.value = false
  }
}

async function sendReply() {
  if (!currentNo.value || !replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  sending.value = true
  try {
    await replyConsultation(currentNo.value, { replyContent: replyText.value.trim() })
    replyText.value = ''
    await loadList(currentNo.value)
  } finally {
    sending.value = false
  }
}

async function downloadSharedReport(reportNo: string) {
  if (!currentNo.value) return
  const blob = await downloadDoctorSharedReportPdf(currentNo.value, reportNo)
  saveBlob(blob, `${reportNo}.pdf`)
}

onMounted(() => {
  mounted.value = true
  loadList()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">CONSULTATION</span>
        <h2>咨询对话</h2>
      </div>
      <p>查看用户问题上下文，并在同一会话中连续回复。</p>
    </div>

    <section class="consult-shell" :class="{ 'is-mounted': mounted }">
      <aside class="conversation-list" v-loading="loading">
        <button
          v-for="item in consultations"
          :key="item.consultationNo"
          class="conversation-item"
          :class="{ active: item.consultationNo === currentNo }"
          @click="selectConsultation(item.consultationNo)"
        >
          <div class="item-main">
            <strong>{{ item.consultationTitle || '报告咨询' }}</strong>
            <span>{{ item.reportNo }} · {{ item.createdAt }}</span>
          </div>
          <el-tag :type="statusType(item.consultationStatus)" size="small">
            {{ statusLabel(item.consultationStatus) }}
          </el-tag>
        </button>
        <el-empty v-if="!loading && consultations.length === 0" description="暂无待处理咨询" />
      </aside>

      <main class="chat-panel" v-loading="detailLoading">
        <template v-if="current">
          <header class="chat-header">
            <div>
              <strong>{{ current.consultationTitle || '报告咨询' }}</strong>
              <span>报告 {{ current.reportNo }} · {{ current.consultationType }}</span>
            </div>
            <el-tag :type="statusType(current.consultationStatus)">
              {{ statusLabel(current.consultationStatus) }}
            </el-tag>
          </header>

          <div class="message-list">
            <div
              v-for="reply in replies"
              :key="reply.id"
              class="message-row"
              :class="{ mine: reply.replyRole === 'DOCTOR' }"
            >
              <div class="message-bubble">
                <div class="message-meta">{{ reply.replyRole === 'DOCTOR' ? '我' : reply.replyUserName || '用户' }}</div>
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
                <time>{{ reply.replyTime || reply.createdAt }}</time>
              </div>
            </div>
          </div>

          <footer class="chat-input">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="3"
              :disabled="isClosed"
              placeholder="输入回复内容"
              @keydown.ctrl.enter.prevent="sendReply"
            />
            <el-button type="primary" :loading="sending" :disabled="isClosed" @click="sendReply">发送</el-button>
          </footer>
        </template>
        <el-empty v-else description="选择一个咨询" />
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.consult-shell {
  display: grid;
  grid-template-columns: 330px minmax(0, 1fr);
  gap: 16px;
  min-height: 620px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
}

.consult-shell.is-mounted {
  opacity: 1;
  transform: translateY(0);
}

.conversation-list,
.chat-panel {
  background: var(--color-panel);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
}

.conversation-list {
  padding: 12px;
  overflow: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-ink);
  text-align: left;
  cursor: pointer;
}

.conversation-item.active,
.conversation-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--color-line);
}

.item-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.item-main strong,
.item-main span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-main span {
  color: var(--color-ink-muted);
  font-size: 12px;
}

.chat-panel {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.chat-header,
.chat-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-line);
}

.chat-header span {
  display: block;
  margin-top: 4px;
  color: var(--color-ink-muted);
  font-size: 12px;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.message-row {
  display: flex;
  margin-bottom: 14px;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: min(640px, 78%);
  padding: 12px 14px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--color-line);
}

.message-row.mine .message-bubble {
  background: var(--color-brand-light);
  border-color: rgba(201, 164, 78, 0.25);
}

.message-meta {
  margin-bottom: 6px;
  color: var(--color-ink-muted);
  font-size: 12px;
}

.message-bubble p {
  margin: 0;
  color: var(--color-ink);
  line-height: 1.7;
  white-space: pre-wrap;
}

.message-bubble time {
  display: block;
  margin-top: 8px;
  color: var(--color-ink-faint);
  font-size: 11px;
}

.chat-input {
  border-top: 1px solid var(--color-line);
  border-bottom: 0;
}

.chat-input .el-button {
  align-self: stretch;
}

.report-attachment {
  display: grid;
  gap: 4px;
  width: 100%;
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-ink);
  text-align: left;
  cursor: pointer;
}

.report-attachment span {
  color: var(--color-ink-muted);
  font-size: 12px;
}

.report-attachment strong {
  font-size: 13px;
}

@media (max-width: 900px) {
  .consult-shell {
    grid-template-columns: 1fr;
  }
}
</style>
