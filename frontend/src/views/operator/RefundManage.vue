<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperatorRefunds, approveRefund, rejectRefund } from '@/api/operator'
import { ElMessage } from 'element-plus'

const refunds = ref<any[]>([])
const loading = ref(true)
const mounted = ref(false)
const filters = ref({ applyStatus: '' })
const auditDialog = ref(false)
const auditRemark = ref('')
const currentAction = ref<'approve' | 'reject'>('approve')
const currentNo = ref('')

onMounted(() => {
  mounted.value = true
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.applyStatus !== '') params.applyStatus = filters.value.applyStatus
    const res: any = await getOperatorRefunds(params)
    refunds.value = res.data || []
  } catch {} finally { loading.value = false }
}

function openAudit(no: string, action: 'approve' | 'reject') {
  currentNo.value = no
  currentAction.value = action
  auditRemark.value = ''
  auditDialog.value = true
}

async function handleAudit() {
  try {
    if (currentAction.value === 'approve') {
      await approveRefund(currentNo.value, { auditRemark: auditRemark.value })
    } else {
      await rejectRefund(currentNo.value, { auditRemark: auditRemark.value })
    }
    ElMessage.success(currentAction.value === 'approve' ? '已通过' : '已驳回')
    auditDialog.value = false
    await loadData()
  } catch {}
}

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">REFUND</span>
        <h2>退款审核</h2>
      </div>
      <p>审核用户退款申请，通过或驳回并填写意见。</p>
    </div>

    <div class="filter-bar" :class="{ 'is-mounted': mounted }">
      <el-select v-model="filters.applyStatus" placeholder="状态" style="width: 120px;" clearable>
        <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <section class="table-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-table :data="refunds" v-loading="loading">
        <el-table-column prop="applyNo" label="申请编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.applyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="200">
          <template #default="{ row }">
            <span class="mono-text">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.applyStatus]?.type as any">{{ statusMap[row.applyStatus]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <template v-if="row.applyStatus === 0">
              <el-button type="success" link @click="openAudit(row.applyNo, 'approve')">通过</el-button>
              <el-button type="danger" link @click="openAudit(row.applyNo, 'reject')">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="auditDialog" :title="currentAction === 'approve' ? '通过退款' : '驳回退款'" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审核意见">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button :type="currentAction === 'approve' ? 'success' : 'danger'" @click="handleAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.table-shell {
  padding: 20px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-tag) {
  border-radius: 999px;
}

:deep(.el-tag--success) {
  background: var(--color-success-light);
  color: var(--color-success);
  border: none;
}

:deep(.el-tag--warning) {
  background: var(--color-warning-light);
  color: var(--color-warning);
  border: none;
}

:deep(.el-tag--danger) {
  background: var(--color-danger-light);
  color: var(--color-danger);
  border: none;
}
</style>
