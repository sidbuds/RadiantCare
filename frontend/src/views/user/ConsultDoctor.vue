<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  closeConsultation,
  createConsultation,
  getMyConsultation,
  getMyConsultations,
  sendConsultationMessage,
} from '@/api/consultation'
import { getMyReports } from '@/api/report'
import { ElMessage } from 'element-plus'

const consultations = ref<any[]>([])
const reports = ref<any[]>([])
const current = ref<any | null>(null)
const replies = ref<any[]>([])
const loading = ref(true)
const detailLoading = ref(false)
const creating = ref(false)
const sending = ref(false)
const dialogVisible = ref(false)
const messageText = ref('')

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
  if (reports.value.length === 0) {
    await loadReports()
  }
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
  if (!currentNo.value || !messageText.value.trim()) {
    return
  }
  sending.value = true
  try {
    await sendConsultationMessage(currentNo.value, { replyContent: messageText.value.trim() })
    messageText.value = ''
    await loadList(currentNo.value)
  } finally {
    sending.value = false
  }
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
      <el-button type="primary" @click="openDialog">
        <el-icon><Plus /></el-icon>
        发起咨询
      </el-button>
    </div>

    <section class="consult-shell">
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
            <div>
              <strong>{{ current.consultationTitle || '报告咨询' }}</strong>
              <span>报告 {{ current.reportNo }} · {{ current.doctorName }}</span>
            </div>
            <el-button v-if="!isClosed" link type="primary" @click="shareProfile">分享档案</el-button>
            <el-button v-if="!isClosed" link type="danger" @click="handleClose">关闭咨询</el-button>
          </header>

          <div class="message-list">
            <div
              v-for="reply in replies"
              :key="reply.id"
              class="message-row"
              :class="{ mine: reply.replyRole === 'USER' }"
            >
              <div class="message-bubble">
                <div class="message-meta">{{ reply.replyRole === 'USER' ? '我' : reply.replyUserName || current.doctorName }}</div>
                <p>{{ reply.replyContent }}</p>
                <time>{{ reply.replyTime || reply.createdAt }}</time>
              </div>
            </div>
          </div>

          <footer class="chat-input">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="3"
              :disabled="isClosed"
              placeholder="继续描述你的问题"
              @keydown.ctrl.enter.prevent="sendMessage"
            />
            <el-button type="primary" :loading="sending" :disabled="isClosed" @click="sendMessage">发送</el-button>
          </footer>
        </template>
        <el-empty v-else description="选择或发起一个咨询" />
      </main>
    </section>

    <el-dialog v-model="dialogVisible" title="发起咨询" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="报告编号">
          <el-select v-model="form.reportNo" placeholder="请选择报告" style="width: 100%" filterable>
            <el-option
              v-for="item in reportOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
          <el-input v-model="form.consultationTitle" placeholder="例如：血脂偏高需要注意什么" />
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
.consult-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 16px;
  min-height: 620px;
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

@media (max-width: 860px) {
  .consult-shell {
    grid-template-columns: 1fr;
  }
}
</style>
