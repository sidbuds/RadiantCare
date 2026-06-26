<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHealthProfile, saveHealthProfile } from '@/api/user'
import type { HealthProfile } from '@/api/user'
import { ElMessage } from 'element-plus'

const profile = ref<HealthProfile>({
  allergyHistory: null,
  medicalHistory: null,
  familyHistory: null,
  medicationHistory: null,
  smokingStatus: 0,
  drinkingStatus: 0,
  remark: null,
})
const loading = ref(true)
const saving = ref(false)
const mounted = ref(false)

async function loadProfile() {
  loading.value = true
  try {
    const res = await getHealthProfile()
    if (res.data && Object.keys(res.data).length > 0) {
      profile.value = { ...profile.value, ...res.data }
    }
  } catch {} finally { loading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    await saveHealthProfile(profile.value)
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}

onMounted(() => { mounted.value = true; loadProfile() })
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <span class="section-eyebrow">HEALTH PROFILE</span>
        <h2>健康档案</h2>
      </div>
      <p>记录您的病史、过敏史等健康信息，便于医生提供更精准的建议。</p>
    </div>

    <section v-loading="loading" class="form-shell data-card" :class="{ 'is-mounted': mounted }">
      <el-form label-width="120px">
        <el-form-item label="过敏史">
          <el-input v-model="profile.allergyHistory" type="textarea" :rows="3" placeholder="如：青霉素过敏、花粉过敏" />
        </el-form-item>
        <el-form-item label="既往病史">
          <el-input v-model="profile.medicalHistory" type="textarea" :rows="3" placeholder="如：2020年阑尾炎手术" />
        </el-form-item>
        <el-form-item label="家族病史">
          <el-input v-model="profile.familyHistory" type="textarea" :rows="3" placeholder="如：父亲高血压、母亲糖尿病" />
        </el-form-item>
        <el-form-item label="用药史">
          <el-input v-model="profile.medicationHistory" type="textarea" :rows="3" placeholder="如：降压药（长期）" />
        </el-form-item>
        <el-form-item label="吸烟状态">
          <el-radio-group v-model="profile.smokingStatus">
            <el-radio :value="0">不吸烟</el-radio>
            <el-radio :value="1">已戒烟</el-radio>
            <el-radio :value="2">吸烟</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="饮酒状态">
          <el-radio-group v-model="profile.drinkingStatus">
            <el-radio :value="0">不饮酒</el-radio>
            <el-radio :value="1">偶尔饮酒</el-radio>
            <el-radio :value="2">经常饮酒</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="profile.remark" type="textarea" :rows="2" placeholder="其他需要补充的健康信息" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<style scoped lang="scss">
.form-shell {
  max-width: 640px;
  padding: 36px;
  opacity: 0;
  transform: translateY(16px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
