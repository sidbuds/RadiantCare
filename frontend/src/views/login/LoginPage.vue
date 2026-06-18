<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { roleHomeMap } from '@/router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const mounted = ref(false)
const focusedField = ref<string | null>(null)

const demoAccounts = [
  { label: '用户', username: 'user', password: '123456', icon: 'User', color: '#b87070' },
  { label: '医生', username: 'doctor', password: '123456', icon: 'FirstAidKit', color: '#22c55e' },
  { label: '运营', username: 'operator', password: '123456', icon: 'Monitor', color: '#f59e0b' },
  { label: '管理员', username: 'admin', password: '123456', icon: 'Setting', color: '#6a9a92' },
]

onMounted(() => {
  setTimeout(() => { mounted.value = true }, 100)
})

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', form.value)
    userStore.setToken(res.data.accessToken)
    userStore.setUserInfo({
      accountId: res.data.accountId,
      userId: res.data.userId,
      username: form.value.username,
      displayName: res.data.displayName,
      role: res.data.role,
    })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    if (redirect) { router.push(redirect) }
    else { router.push(roleHomeMap[res.data.role] || '/') }
  } catch {} finally { loading.value = false }
}

function useDemoAccount(username: string, password: string) {
  form.value.username = username
  form.value.password = password
}
</script>

<template>
  <div class="login-page">
    <!-- Animated background -->
    <div class="login-bg">
      <div class="bg-orb bg-orb--1"></div>
      <div class="bg-orb bg-orb--2"></div>
      <div class="bg-orb bg-orb--3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-shell" :class="{ 'is-mounted': mounted }">
      <!-- Left: Brand & Benefits -->
      <section class="login-copy">
        <div class="copy-inner">
          <span class="section-eyebrow">XIXIN HEALTH</span>
          <h1>欢迎使用<br /><span class="brand-highlight">熙心健康</span>体检平台</h1>
          <p>登录后即可管理预约、查看报告、在线咨询医生，一站式完成体检全流程。</p>

          <div class="benefit-list">
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.3s">
              <span class="benefit-icon" style="--icon-color: #b87070">
                <el-icon :size="18"><User /></el-icon>
              </span>
              <div>
                <strong>用户端</strong>
                <span>在线预约、查看报告、对比历年数据、咨询医生。</span>
              </div>
            </article>
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.4s">
              <span class="benefit-icon" style="--icon-color: #22c55e">
                <el-icon :size="18"><FirstAidKit /></el-icon>
              </span>
              <div>
                <strong>医生端</strong>
                <span>处理检查任务、录入结果、回复用户咨询。</span>
              </div>
            </article>
            <article class="benefit-card" :class="{ 'is-mounted': mounted }" style="--delay: 0.5s">
              <span class="benefit-icon" style="--icon-color: #f59e0b">
                <el-icon :size="18"><Setting /></el-icon>
              </span>
              <div>
                <strong>运营与管理端</strong>
                <span>管理套餐排班、处理订单退款、查看运营数据。</span>
              </div>
            </article>
          </div>

          <!-- Decorative stats -->
          <div class="copy-stats" :class="{ 'is-mounted': mounted }">
            <div class="stat-item">
              <span class="stat-value">4</span>
              <span class="stat-label">角色终端</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">35+</span>
              <span class="stat-label">功能页面</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">全链路</span>
              <span class="stat-label">体检服务</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Right: Login Form -->
      <section class="login-card" :class="{ 'is-mounted': mounted }">
        <div class="card-inner">
          <div class="card-header">
            <div class="card-logo">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
              </svg>
            </div>
            <h2>登录账号</h2>
            <p>输入账号密码，进入对应角色工作台。</p>
          </div>

          <el-form :model="form" @keyup.enter="handleLogin" class="login-form">
            <div class="input-group" :class="{ focused: focusedField === 'username' }">
              <label>用户名</label>
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
                @focus="focusedField = 'username'"
                @blur="focusedField = null"
              />
            </div>
            <div class="input-group" :class="{ focused: focusedField === 'password' }">
              <label>密码</label>
              <el-input
                v-model="form.password"
                placeholder="请输入密码"
                type="password"
                prefix-icon="Lock"
                size="large"
                show-password
                @focus="focusedField = 'password'"
                @blur="focusedField = null"
              />
            </div>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">进入系统</span>
              <el-icon :size="16" class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </el-form>

          <div class="demo-section">
            <div class="demo-header">
              <span class="demo-label">演示账号</span>
              <span class="demo-hint">点击快速填入</span>
            </div>
            <div class="demo-chips">
              <button
                v-for="item in demoAccounts"
                :key="item.label"
                type="button"
                class="demo-chip"
                @click="useDemoAccount(item.username, item.password)"
              >
                <span class="chip-dot" :style="{ background: item.color }"></span>
                {{ item.label }}
              </button>
            </div>
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

/* ═══════════════════════════════════════════════════════════
   ANIMATED BACKGROUND
   ═══════════════════════════════════════════════════════════ */

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

.bg-orb--3 {
  width: 300px;
  height: 300px;
  background: rgba(139, 92, 246, 0.03);
  top: 40%;
  left: 40%;
  animation-delay: -18s;
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

/* ═══════════════════════════════════════════════════════════
   LOGIN SHELL
   ═══════════════════════════════════════════════════════════ */

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

/* ═══════════════════════════════════════════════════════════
   LEFT: COPY SECTION
   ═══════════════════════════════════════════════════════════ */

.login-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px;
  position: relative;

  &::after {
    content: "";
    position: absolute;
    bottom: -20%;
    left: -20%;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(201, 164, 78, 0.03);
    filter: blur(60px);
    pointer-events: none;
  }
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

.copy-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 36px;
  padding-top: 24px;
  border-top: 1px solid var(--color-line);
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 0.4s var(--ease-out-expo), transform 0.4s var(--ease-out-expo);
  transition-delay: 0.6s;

  &.is-mounted {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-ink);
}

.stat-label {
  font-size: 11px;
  color: var(--color-ink-faint);
  letter-spacing: 0.04em;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--color-line);
}

/* ═══════════════════════════════════════════════════════════
   RIGHT: LOGIN CARD
   ═══════════════════════════════════════════════════════════ */

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
  margin-bottom: 28px;
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
  color: #111113;
  box-shadow: 0 4px 20px rgba(201, 164, 78, 0.3);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.2s ease;

  label {
    font-size: 12px;
    font-weight: 600;
    color: var(--color-ink-muted);
    letter-spacing: 0.04em;
    transition: color 0.2s ease;
  }

  &.focused label {
    color: var(--color-brand);
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;

  &::after {
    content: "";
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transform: translateX(-100%);
    transition: transform 0.6s ease;
  }

  &:hover::after {
    transform: translateX(100%);
  }
}

.btn-arrow {
  transition: transform 0.3s var(--ease-out-expo);
}

.submit-btn:hover .btn-arrow {
  transform: translateX(3px);
}

/* ═══════════════════════════════════════════════════════════
   DEMO ACCOUNTS
   ═══════════════════════════════════════════════════════════ */

.demo-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-line);
}

.demo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.demo-label {
  color: var(--color-ink-muted);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.demo-hint {
  color: var(--color-ink-faint);
  font-size: 11px;
}

.demo-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.demo-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid var(--color-line);
  border-radius: 999px;
  background: var(--color-panel);
  color: var(--color-ink-soft);
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s var(--ease-out-expo);

  &:hover {
    border-color: var(--color-brand-muted);
    background: var(--color-brand-light);
    color: var(--color-brand);
    transform: translateY(-1px);
  }
}

.chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ═══════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════ */

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
