<script setup lang="ts">
import { ref, watch } from 'vue'
import { getOperatorRefunds, approveRefund, rejectRefund } from '@/api/operator'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoading, useStatusTag, usePagination, refundStatusMap } from '@/composables'
import type { RefundApply } from '@/types/api'

const { loading, mounted, execute } = useLoading()
const { getLabel, getType } = useStatusTag(refundStatusMap)
const { pageNum, pageSize, total, onPageChange, onSizeChange } = usePagination()

const refunds = ref<RefundApply[]>([])
const filters = ref({ applyStatus: '' })
const auditDialog = ref(false)
const auditFormRef = ref<FormInstance>()
const auditRemark = ref('')
const submitLoading = ref(false)
const currentAction = ref<'approve' | 'reject'>('approve')
const currentNo = ref('')

const auditRules: FormRules = {
  auditRemark: [
    { required: true, message: '请输入审核意见', trigger: 'blur' },
    { max: 500, message: '审核意见最长 500 个字符', trigger: 'blur' },
  ],
}

function loadData() {
  execute(async () => {
    const params: Record<string, string | number> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (filters.value.applyStatus !== '') params.applyStatus = filters.value.applyStatus
    const res = await getOperatorRefunds(params)
    refunds.value = res.data?.list || []
    total.value = res.data?.total || 0
  })
}

watch([pageNum, pageSize], loadData)

function openAudit(no: string, action: 'approve' | 'reject') {
  currentNo.value = no
  currentAction.value = action
  auditRemark.value = ''
  auditDialog.value = true
  auditFormRef.value?.clearValidate()
}

async function handleAudit() {
  if (!auditFormRef.value) return
  const valid = await auditFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (currentAction.value === 'approve') {
      await approveRefund(currentNo.value, { auditRemark: auditRemark.value })
    } else {
      await rejectRefund(currentNo.value, { auditRemark: auditRemark.value })
    }
    ElMessage.success(currentAction.value === 'approve' ? '已通过' : '已驳回')
    auditDialog.value = false
    await loadData()
  } catch (error) {
    console.error('审核退款失败', error)
  } finally { submitLoading.value = false }
}

loadData()
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
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
        <el-option label="退款中" :value="3" />
        <el-option label="已退款" :value="4" />
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
            <el-tag :type="getType(row.applyStatus)">{{ getLabel(row.applyStatus) }}</el-tag>
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

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="auditDialog" :title="currentAction === 'approve' ? '通过退款' : '驳回退款'" width="400px">
      <el-form ref="auditFormRef" :model="{ auditRemark }" :rules="auditRules" label-width="80px">
        <el-form-item label="审核意见" prop="auditRemark">
          <el-input v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button :type="currentAction === 'approve' ? 'success' : 'danger'" :loading="submitLoading" @click="handleAudit">确定</el-button>
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-line);
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
