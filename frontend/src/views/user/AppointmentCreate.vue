<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPackages, listCenters, getCenterSlots } from '@/api/public'
import { createAppointment } from '@/api/appointment'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoading } from '@/composables'
import type { PackageItem, ExamCenter, TimeSlot } from '@/types/api'

const route = useRoute()
const router = useRouter()
const { loading, mounted, execute } = useLoading()

const packages = ref<PackageItem[]>([])
const centers = ref<ExamCenter[]>([])
const slots = ref<TimeSlot[]>([])

const formRef = ref<FormInstance>()
const form = ref({
  packageId: route.query.packageId ? Number(route.query.packageId) : undefined,
  centerCode: '',
  appointDate: '',
  timeSlotCode: '',
  remark: '',
})

const rules = computed<FormRules>(() => ({
  packageId: [{ required: true, message: '请选择体检套餐', trigger: 'change' }],
  centerCode: [{ required: true, message: '请选择体检中心', trigger: 'change' }],
  appointDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  timeSlotCode: slots.value.length > 0
    ? [{ required: true, message: '请选择时段', trigger: 'change' }]
    : [],
}))

onMounted(async () => {
  const [pkgRes, centerRes] = await Promise.all([listPackages(), listCenters()])
  packages.value = pkgRes.data || []
  centers.value = centerRes.data || []
})

watch(() => [form.value.centerCode, form.value.appointDate], async () => {
  if (form.value.centerCode && form.value.appointDate) {
    const res = await getCenterSlots(form.value.centerCode, form.value.appointDate)
    slots.value = res.data || []
  }
})

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  execute(async () => {
    const res = await createAppointment({
      ...form.value,
      packageId: form.value.packageId!,
    })
    ElMessage.success('预约成功')
    router.push(`/user/appointments/${res.data.appointmentNo}`)
  })
}
</script>

<template>
  <div class="page-container">
    <div class="detail-header" :class="{ 'is-mounted': mounted }">
      <el-button class="back-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="form-card glass-panel" :class="{ 'is-mounted': mounted }">
      <div class="form-header">
        <span class="section-eyebrow">NEW APPOINTMENT</span>
        <h2>创建预约</h2>
        <p>选择套餐、体检中心和时间，完成预约。</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="create-form">
        <el-form-item label="体检套餐" prop="packageId">
          <el-select v-model="form.packageId" placeholder="选择套餐" style="width: 100%;">
            <el-option v-for="p in packages" :key="p.id" :label="`${p.packageName} - ¥${p.price}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="体检中心" prop="centerCode">
          <el-select v-model="form.centerCode" placeholder="选择中心" style="width: 100%;">
            <el-option v-for="c in centers" :key="c.centerCode" :label="c.centerName" :value="c.centerCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="appointDate">
          <el-date-picker v-model="form.appointDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" :disabled-date="(date: Date) => date < new Date(new Date().setHours(0,0,0,0))" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlotCode">
          <div class="slot-grid">
            <button
              v-for="s in slots"
              :key="s.timeSlotCode"
              type="button"
              class="slot-btn"
              :class="{ active: form.timeSlotCode === s.timeSlotCode }"
              @click="form.timeSlotCode = s.timeSlotCode"
            >
              <span class="slot-time">{{ s.timeSlotCode }}</span>
              <span class="slot-count">剩余 {{ s.capacityTotal - s.capacityUsed - s.capacityLocked }}</span>
            </button>
          </div>
          <el-empty v-if="slots.length === 0 && form.centerCode && form.appointDate" description="该日期暂无可用时段" :image-size="60" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="选填" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" class="submit-btn">
            提交预约
            <el-icon class="btn-arrow"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-header {
  margin-bottom: 20px;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.back-btn {
  background: var(--color-panel) !important;
  border: 1px solid var(--color-line) !important;
  color: var(--color-ink-soft) !important;

  &:hover {
    border-color: var(--color-line-strong) !important;
    color: var(--color-ink) !important;
  }
}

.form-card {
  padding: 36px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.1s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  margin-bottom: 32px;

  h2 {
    font-family: var(--font-display);
    font-size: 28px;
    font-weight: 700;
    color: var(--color-ink);
    margin-top: 12px;
    background: linear-gradient(135deg, var(--color-ink) 0%, rgba(240, 236, 228, 0.7) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    margin-top: 8px;
    color: var(--color-ink-muted);
    font-size: 14px;
  }
}

.create-form {
  max-width: 600px;
}

.slot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.slot-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 18px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-sm);
  background: var(--color-panel);
  cursor: pointer;
  transition: all 0.2s var(--ease-out-expo);

  &:hover {
    border-color: var(--color-brand-muted);
    background: var(--color-brand-light);
  }

  &.active {
    border-color: var(--color-brand);
    background: var(--color-brand-light);
    box-shadow: 0 0 0 2px rgba(201, 164, 78, 0.15);

    .slot-time {
      color: var(--color-brand);
    }
  }

  .slot-time {
    font-size: 13px;
    font-weight: 600;
    color: var(--color-ink);
  }

  .slot-count {
    font-size: 11px;
    color: var(--color-ink-muted);
  }
}

.submit-btn .btn-arrow {
  transition: transform 0.3s var(--ease-out-expo);
}

.submit-btn:hover .btn-arrow {
  transform: translateX(3px);
}
</style>
