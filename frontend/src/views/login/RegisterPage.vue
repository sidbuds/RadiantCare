<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { roleHomeMap } from '@/router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const form = ref({ username: '', password: '', confirmPassword: '', name: '', mobile: '' })
const loading = ref(false)
const mounted = ref(false)

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 50, message: '用户名长度为 4-50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 128, message: '密码长度为 6-128 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== form.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名最长 50 个字符', trigger: 'blur' },
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

onMounted(() => {
  setTimeout(() => { mounted.value = true }, 100)
})

async function handleRegister() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register({
      username: form.value.username,
      password: form.value.password,
      name: form.value.name,
      mobile: form.value.mobile,
    })
    userStore.setToken(res.data.accessToken)
    userStore.setUserInfo({
      accountId: res.data.accountId,
      userId: res.data.userId,
      username: form.value.username,
      displayName: res.data.displayName,
      role: res.data.role,
    })
    ElMessage.success('注册成功')
    router.push(roleHomeMap[res.data.role] || '/')
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb--1"></div>
      <div class="bg-orb bg-orb--2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-shell" :class="{ 'is-mounted': mounted }">
      <!-- Left: Brand -->
      <section class="login-copy">
        <div class="copy-inner">
          <span class="section-eyebrow">XIXIN HEALTH</span>
          <h1>注册<span class="brand-highlight">熙心健康</span>账号</h1>
          <p>注册后即可在线预约体检、查看报告、咨询医生，享受一站式健康管理服务。</p>

          <div class="benefit-list">
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.3s">
              <span class="benefit-icon" style="--icon-color: #b87070">
                <el-icon :size="18"><Calendar /></el-icon>
              </span>
              <div>
                <strong>在线预约</strong>
                <span>选择套餐、中心和时间，轻松完成体检预约。</span>
              </div>
            </article>
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.4s">
              <span class="benefit-icon" style="--icon-color: #22c55e">
                <el-icon :size="18"><Document /></el-icon>
              </span>
              <div>
                <strong>报告查看</strong>
                <span>体检报告在线查看，历年数据对比分析。</span>
              </div>
            </article>
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.5s">
              <span class="benefit-icon" style="--icon-color: #f59e0b">
                <el-icon :size="18"><ChatDotRound /></el-icon>
              </span>
              <div>
                <strong>咨询医生</strong>
                <span>基于报告在线咨询医生，获取专业建议。</span>
              </div>
            </article>
          </div>
        </div>
      </section>

      <!-- Right: Register Form -->
      <section class="login-card" :class="{ 'is-mounted': mounted }">
        <div class="card-inner">
          <div class="card-header">
            <div class="card-logo">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
              </svg>
            </div>
            <h2>创建账号</h2>
            <p>填写信息完成注册，立即体验健康管理服务。</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister" class="login-form">
            <el-form-item prop="username">
              <div class="input-group">
                <label>用户名</label>
                <el-input v-model="form.username" placeholder="4-50个字符，字母数字下划线" prefix-icon="User" size="large" />
              </div>
            </el-form-item>
            <el-form-item prop="name">
              <div class="input-group">
                <label>姓名</label>
                <el-input v-model="form.name" placeholder="请输入真实姓名" prefix-icon="Postcard" size="large" />
              </div>
            </el-form-item>
            <el-form-item prop="mobile">
              <div class="input-group">
                <label>手机号</label>
                <el-input v-model="form.mobile" placeholder="请输入手机号" prefix-icon="Phone" size="large" />
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-group">
                <label>密码</label>
                <el-input v-model="form.password" placeholder="至少6个字符" type="password" prefix-icon="Lock" size="large" show-password />
              </div>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <div class="input-group">
                <label>确认密码</label>
                <el-input v-model="form.confirmPassword" placeholder="再次输入密码" type="password" prefix-icon="Lock" size="large" show-password />
              </div>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
              <span>注册</span>
              <el-icon :size="16" class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form>

          <div class="login-link">
            已有账号？<router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  padding: 24px;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  animation: loginOrbFloat 25s ease-in-out infinite alternate;
}

.bg-orb--1 {
  width: 500px;
  height: 400px;
  background: rgba(201, 164, 78, 0.06);
  top: -15%;
  left: -10%;
}

.bg-orb--2 {
  width: 400px;
  height: 500px;
  background: rgba(56, 189, 248, 0.04);
  bottom: -20%;
  right: -5%;
  animation-delay: -10s;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.01) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.01) 1px, transparent 1px);
  background-size: 80px 80px;
  mask-image: radial-gradient(ellipse 50% 50% at 50% 50%, black 20%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse 50% 50% at 50% 50%, black 20%, transparent 70%);
}

@keyframes loginOrbFloat {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -30px) scale(1.15); }
  100% { transform: translate(-20px, 20px) scale(0.9); }
}

.login-shell {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 28px;
  width: min(1100px, 100%);
  min-height: calc(100vh - 48px);
  margin: 0 auto;
  position: relative;
  z-index: 1;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo);

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-copy,
.login-card {
  border: 1px solid var(--color-line);
  border-radius: var(--radius-xl);
  background: var(--color-panel);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.08), transparent);
  }
}

.login-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px;
}

.copy-inner {
  position: relative;
}

.login-copy h1 {
  margin-top: 18px;
  font-family: var(--font-display);
  font-size: clamp(34px, 3.5vw, 46px);
  line-height: 1.1;
  font-weight: 900;
  color: var(--color-ink);
  letter-spacing: -0.02em;
}

.brand-highlight {
  background: linear-gradient(135deg, var(--color-brand), #e8c56a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-copy > .copy-inner > p {
  max-width: 480px;
  margin-top: 16px;
  color: var(--color-ink-soft);
  font-size: 14px;
  line-height: 1.8;
}

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.benefit-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-md);
  background: var(--color-panel);
  transition: all 0.3s var(--ease-out-expo);
  opacity: 0;
  transform: translateX(-16px);

  &.is-mounted {
    opacity: 1;
    transform: translateX(0);
    transition-delay: var(--delay);
  }

  &:hover {
    border-color: var(--color-line-strong);
    background: rgba(255,255,255,0.04);
    transform: translateX(4px);
  }

  strong {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: var(--color-ink);
    margin-bottom: 3px;
  }

  span {
    color: var(--color-ink-muted);
    font-size: 12px;
    line-height: 1.6;
  }
}

.benefit-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--icon-color, var(--color-brand)) 12%, transparent);
  color: var(--icon-color, var(--color-brand));
  flex-shrink: 0;
}

.login-card {
  align-self: center;
  padding: 40px;
  opacity: 0;
  transform: translateX(24px);
  transition: opacity 0.5s var(--ease-out-expo), transform 0.5s var(--ease-out-expo);
  transition-delay: 0.2s;

  &.is-mounted {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-inner {
  position: relative;
}

.card-header {
  margin-bottom: 24px;
  text-align: center;

  h2 {
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 700;
    color: var(--color-ink);
    margin-top: 14px;
  }

  p {
    margin-top: 8px;
    color: var(--color-ink-muted);
    font-size: 13px;
  }
}

.card-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-deep));
  color: var(--color-on-brand);
  box-shadow: 0 4px 20px rgba(201, 164, 78, 0.3);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 2px;

  :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  :deep(.el-form-item__error) {
    padding-top: 2px;
  }
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;

  label {
    font-size: 12px;
    font-weight: 600;
    color: var(--color-ink-muted);
    letter-spacing: 0.04em;
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-arrow {
  transition: transform 0.3s var(--ease-out-expo);
}

.submit-btn:hover .btn-arrow {
  transform: translateX(3px);
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--color-ink-muted);

  a {
    color: var(--color-brand);
    text-decoration: none;
    font-weight: 600;

    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .login-copy {
    display: none;
  }

  .login-card {
    max-width: 440px;
    margin: 0 auto;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 28px;
  }

  .login-bg {
    display: none;
  }
}
</style>
