# 熙心健康体检平台 — 前端 UI 风格规范

> 本文档是所有页面原型和正式前端开发的**唯一视觉规范来源**。  
> 生成任何新页面前，必须先加载本文件，并严格遵循其中定义的设计令牌、组件结构和交互模式。

---

## 1. 设计令牌（Design Tokens）

### 1.1 色彩体系

| 令牌名称 | 色值 | 用途 |
|---|---|---|
| `--color-primary` | `#00897b` | 主色：按钮、链接、导航高亮、进度条 |
| `--color-primary-dark` | `#00695c` | 主色渐变终止色、按钮 hover |
| `--color-primary-darker` | `#004d40` | 品牌面板渐变深色 |
| `--color-primary-light` | `#4db6ac` | 浅色辅助、已完成状态 |
| `--color-primary-bg` | `#e0f2f1` | 浅色背景标签、图标底色 |
| `--color-primary-bg-light` | `#f0faf9` | 选中态卡片背景、高亮区域背景 |
| `--color-primary-border` | `#b2dfdb` | hover 边框、步骤线完成色 |
| `--color-danger` | `#e65100` | 价格文字、异常偏高、警告 |
| `--color-danger-bg` | `#fff3e0` | 异常标签背景 |
| `--color-success` | `#2e7d32` | 正常状态、完成标签 |
| `--color-success-bg` | `#e8f5e9` | 正常标签背景 |
| `--color-info` | `#1565c0` | 偏低指标、链接辅助 |
| `--color-info-bg` | `#e3f2fd` | 待确认状态标签 |
| `--color-warning-bg` | `#fff3e0` | 待回复/待处理状态 |
| `--color-text-primary` | `#1a1a1a` | 标题文字 |
| `--color-text-regular` | `#333333` | 正文文字 |
| `--color-text-secondary` | `#666666` | 次要文字、按钮文字 |
| `--color-text-placeholder` | `#999999` | 占位符、辅助说明 |
| `--color-text-disabled` | `#bbbbbb` | 禁用态、未激活步骤 |
| `--color-bg-page` | `#f0f4f3` | 页面整体背景 |
| `--color-bg-card` | `#ffffff` | 卡片背景 |
| `--color-bg-gray` | `#f5f5f5` | 灰色按钮背景、等待态底色 |
| `--color-border-light` | `#f0f0f0` | 卡片内部分割线 |
| `--color-border` | `#e8e8e8` | 未选中卡片边框 |

### 1.2 渐变

| 名称 | 值 | 用途 |
|---|---|---|
| 顶部导航渐变 | `linear-gradient(135deg, #00897b, #00695c)` | `.top-nav` 背景 |
| 品牌面板渐变 | `linear-gradient(160deg, #00897b 0%, #00695c 50%, #004d40 100%)` | 登录页左侧 |
| 页面背景渐变 | `linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 30%, #80cbc4 60%, #4db6ac 100%)` | 登录页 body |
| 概览卡片渐变 | `linear-gradient(135deg, #00897b, #00695c)` | 体检任务概览等全宽信息卡 |
| 浅绿背景渐变 | `linear-gradient(135deg, #f0faf9, #e0f2f1)` | 结论框、建议卡片 |
| 选中项背景渐变 | `linear-gradient(135deg, #e0f2f1, #f0faf9)` | 已选套餐信息区域 |
| 主按钮渐变 | `linear-gradient(135deg, #00897b, #00695c)` | 所有主操作按钮 |

### 1.3 字体

```css
font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
```

### 1.4 字号规范

| 令牌 | 大小 | 用途 |
|---|---|---|
| `--font-size-page-title` | `24px` | 页面主标题 h1 |
| `--font-size-section-title` | `18px` | 区块标题 h2 |
| `--font-size-card-title` | `15-17px` | 卡片标题 h3/h4 |
| `--font-size-body` | `14px` | 正文内容 |
| `--font-size-caption` | `13px` | 次要说明 |
| `--font-size-small` | `12px` | 标签、时间、辅助文字 |
| `--font-size-mini` | `11px` | 极小标签 |

### 1.5 圆角

| 令牌 | 值 | 用途 |
|---|---|---|
| `--radius-page-card` | `16px` | 页面主卡片、section-card |
| `--radius-inner-card` | `12px` | 卡片内部子区域 |
| `--radius-button` | `10px` | 按钮、输入框 |
| `--radius-tag` | `12px` / `20px` | 标签/胶囊按钮 |
| `--radius-avatar` | `50%` | 头像 |
| `--radius-icon-box` | `8px` / `14px` | 图标容器 |

### 1.6 阴影

| 令牌 | 值 | 用途 |
|---|---|---|
| `--shadow-card` | `0 2px 12px rgba(0,0,0,0.05)` | 普通卡片 |
| `--shadow-card-hover` | `0 4px 20px rgba(0,137,123,0.1)` | 卡片 hover |
| `--shadow-nav` | `0 2px 12px rgba(0,0,0,0.12)` | 顶部导航 |
| `--shadow-button` | `0 6px 20px rgba(0,105,92,0.3)` | 主按钮 hover |
| `--shadow-dropdown` | `0 2px 8px rgba(0,0,0,0.06)` | 下拉、弹出 |

### 1.7 间距

| 令牌 | 值 | 用途 |
|---|---|---|
| `--space-page-padding` | `24px 20px` | `.page-container` 内边距 |
| `--space-card-padding` | `28px` | `.section-card` 内边距 |
| `--space-card-margin` | `20px` | 卡片之间间距 |
| `--space-header-margin` | `24px` | 页面标题与内容间距 |
| `--space-grid-gap` | `20px` | 网格布局间距 |

---

## 2. 页面骨架结构

### 2.1 HTML 模板

所有页面必须包含以下三个顶层区域：

```
<body>
  <header class="top-nav">  <!-- 固定顶部导航 -->
  <div class="page-container">  <!-- 主内容区 -->
  <!-- 可选：弹窗、侧滑面板等覆盖层 -->
</body>
```

### 2.2 页面最大宽度

```css
.page-container {
  max-width: 960px;   /* 通用内容页 */
  /* max-width: 1200px; 仅套餐列表等宽页面使用 */
  margin: 0 auto;
  padding: 24px 20px 60px;
}
```

### 2.3 标准页面头

```html
<div class="page-header">
  <h1>页面标题</h1>
  <p>页面描述（可选）</p>
</div>
```

- 若页面有右侧操作按钮，使用 `display: flex; justify-content: space-between; align-items: center;`

---

## 3. 顶部导航栏（Top Nav）

### 3.1 结构

```html
<header class="top-nav">
  <a class="nav-brand">
    <svg viewBox="0 0 24 24">...</svg>  <!-- 地球图标 -->
    熙心健康
  </a>
  <nav class="nav-links">
    <a class="nav-link active">当前页</a>
    <a class="nav-link">其他页</a>
  </nav>
  <div class="nav-user">
    <span>{{ displayName }}</span>
    <div class="nav-avatar">{{ 首字 }}</div>
  </div>
</header>
```

### 3.2 样式

```css
.top-nav {
  background: linear-gradient(135deg, #00897b, #00695c);
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
  position: sticky;
  top: 0;
  z-index: 100;
}
```

### 3.3 品牌 Logo SVG

```html
<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
</svg>
```

### 3.4 导航链接交互

```css
.nav-link {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}
.nav-link:hover, .nav-link.active {
  background: rgba(255,255,255,0.15);
  color: #fff;
}
```

---

## 4. 卡片组件

### 4.1 通用 section-card

```css
.section-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.section-card h2 {
  font-size: 18px;
  color: #1a1a1a;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
```

### 4.2 可点击卡片（套餐卡片、预约卡片、报告卡片）

- 默认 `border: 2px solid transparent`
- hover 时 `border-color: #b2dfdb` + `box-shadow: 0 4px 20px rgba(0,137,123,0.1)`
- 可选 `transform: translateY(-4px)` 用于悬浮效果

### 4.3 选中态卡片

```css
.card.selected {
  border-color: #00897b;
  background: #f0faf9;
}
```

---

## 5. 按钮规范

### 5.1 主按钮（.btn-primary / .btn-pay / .btn-new / .btn-view）

```css
.btn-primary {
  padding: 12px 40px;
  background: linear-gradient(135deg, #00897b, #00695c);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary:hover {
  box-shadow: 0 6px 20px rgba(0,105,92,0.3);
  transform: translateY(-1px);
}
```

### 5.2 次按钮（.btn-secondary）

```css
.btn-secondary {
  padding: 12px 32px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-secondary:hover { background: #eee; }
```

### 5.3 辅助按钮（.btn-detail / .btn-share / .btn-compare）

```css
.btn-detail {
  padding: 7px 20px;
  background: #e0f2f1;
  color: #00897b;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}
```

### 5.4 危险/警告按钮（.btn-cancel）

```css
.btn-cancel {
  padding: 7px 20px;
  background: #fff3e0;
  color: #e65100;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
}
```

---

## 6. 表单与输入

### 6.1 Element Plus 输入框覆盖

```css
.el-input__wrapper {
  border-radius: 10px;
  padding: 4px 12px;
  transition: all 0.3s ease;
}
.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #00897b inset;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(0,137,123,0.3) inset;
}
```

### 6.2 表单项间距

```css
.el-form-item { margin-bottom: 22px; }
```

---

## 7. 标签（Tag / Badge）

### 7.1 状态标签

```css
.status-tag {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.status-pending   { background: #e3f2fd; color: #1565c0; }
.status-confirmed { background: #e8f5e9; color: #2e7d32; }
.status-completed { background: #f3e5f5; color: #7b1fa2; }
.status-cancelled { background: #fafafa; color: #999; }
.status-replied   { background: #e8f5e9; color: #2e7d32; }
```

### 7.2 内容标签

```css
.card-tag {
  background: #e0f2f1;
  color: #00897b;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}
```

### 7.3 异常/正常指标标签

```css
.report-tag.normal   { background: #e8f5e9; color: #2e7d32; }
.report-tag.abnormal { background: #fff3e0; color: #e65100; }
```

---

## 8. 数据表格

### 8.1 标准表格样式

```css
.metric-table {
  width: 100%;
  border-collapse: collapse;
}
.metric-table th {
  text-align: left;
  font-size: 12px;
  color: #999;
  font-weight: 500;
  padding: 10px 12px;
  border-bottom: 2px solid #f0f0f0;
}
.metric-table td {
  padding: 12px;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}
.metric-table tr:last-child td {
  border-bottom: none;
}
```

### 8.2 指标状态着色

| 状态 | 文字色 | 指示点色 | 用途 |
|---|---|---|---|
| 正常 | `#2e7d32` | `#4caf50` | 指标在参考范围内 |
| 偏高 | `#e65100` | `#ff9800` | 高于参考上限 |
| 偏低 | `#1565c0` | `#2196f3` | 低于参考下限 |
| 高危 | `#c62828` | `#f44336` | 严重异常 |

---

## 9. 时间线（Timeline）

用于导检路线、流程步骤。

```css
.timeline { position: relative; padding-left: 40px; }
.timeline::before {
  content: "";
  position: absolute;
  left: 15px; top: 8px; bottom: 8px;
  width: 2px;
  background: #e0e0e0;
}
.timeline-dot {
  position: absolute;
  left: -40px; top: 20px;
  width: 30px; height: 30px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
  z-index: 1;
}
.timeline-dot.done    { background: #4caf50; color: #fff; }
.timeline-dot.active  { background: #00897b; color: #fff; }
.timeline-dot.waiting { background: #e0e0e0; color: #999; }
```

**当前步骤脉冲动画：**

```css
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(0,137,123,0.3); }
  50%      { box-shadow: 0 0 0 8px rgba(0,137,123,0); }
}
.timeline-dot.active { animation: pulse 2s infinite; }
```

---

## 10. 步骤条（Step Bar）

用于预约流程等多步骤页面。

```css
.step-item { display: flex; align-items: center; gap: 8px; padding: 10px 24px; font-size: 14px; color: #bbb; }
.step-item.active { color: #00897b; font-weight: 600; }
.step-item.done   { color: #00897b; }
.step-num { width: 28px; height: 28px; border-radius: 50%; background: #e0e0e0; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; }
.step-item.active .step-num { background: #00897b; }
.step-item.done .step-num   { background: #4db6ac; }
.step-line { width: 60px; height: 2px; background: #e0e0e0; }
.step-line.done { background: #4db6ac; }
```

---

## 11. 进度条

```css
.progress-bar {
  height: 10px;
  background: rgba(255,255,255,0.2);  /* 深色背景上 */
  /* background: #e0e0e0; 浅色背景上 */
  border-radius: 5px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: #fff;  /* 深色背景上 */
  /* background: #00897b; 浅色背景上 */
  border-radius: 5px;
  transition: width 0.5s ease;
}
```

---

## 12. 空状态

```html
<div class="empty-state">
  <div class="empty-icon">📦</div>  <!-- 使用 emoji -->
  <p>暂无数据</p>
</div>
```

```css
.empty-state { text-align: center; padding: 80px 20px; color: #ccc; }
.empty-state .empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-state p { font-size: 15px; }
```

---

## 13. 图标容器

用于套餐卡片、检查项列表等场景的图标底色方块。

```css
/* 大图标容器 */
.icon-box-lg {
  width: 60px; height: 60px;
  background: #fff;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

/* 小图标容器 */
.icon-box-sm {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: #e0f2f1;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
}
```

---

## 14. 提示横幅（Tip Banner）

用于体检任务中的当前位置提示。

```css
.current-tip {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid #00897b;
}
```

---

## 15. 结论/建议框

```css
.conclusion-box {
  background: linear-gradient(135deg, #f0faf9, #e0f2f1);
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #00897b;
}
.conclusion-box h3 { font-size: 15px; color: #00695c; margin-bottom: 8px; }
.conclusion-box p  { font-size: 14px; color: #333; line-height: 1.8; }
```

---

## 16. 聊天面板

用于咨询医生页面的右侧滑出面板。

```css
.chat-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  z-index: 200;
  display: flex;
  justify-content: flex-end;
}
.chat-panel {
  width: 520px;
  max-width: 100%;
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease;
}
@keyframes slideIn {
  from { transform: translateX(100%); }
  to   { transform: translateX(0); }
}
```

**消息气泡：**
- 用户消息：右对齐，渐变主色背景 `#00897b → #00695c`，白色文字
- 医生消息：左对齐，白色背景，深色文字

---

## 17. 响应式断点

```css
@media (max-width: 768px) {
  .top-nav { padding: 0 16px; }
  .nav-links { display: none; }       /* 隐藏导航链接 */
  .page-container { padding: 16px 12px 40px; }
  .package-grid { grid-template-columns: 1fr; }  /* 单列 */
  .filter-bar { flex-direction: column; }
  .info-grid { grid-template-columns: 1fr; }
  .stat-row { flex-direction: column; }
  .summary-top { flex-direction: column; gap: 12px; }
  .summary-actions { flex-wrap: wrap; }
  .appointment-card { flex-direction: column; }
  .report-card { flex-direction: column; }
  .chat-panel { width: 100%; }
}
```

---

## 18. CDN 依赖

每个 HTML 页面必须在 `<head>` 中引入以下 CDN：

```html
<!-- Vue 3 -->
<script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>
<!-- Element Plus 样式 -->
<link rel="stylesheet" href="https://unpkg.com/element-plus/dist/index.css" />
<!-- Element Plus JS -->
<script src="https://unpkg.com/element-plus"></script>
<!-- Element Plus 图标 -->
<script src="https://unpkg.com/@element-plus/icons-vue"></script>
<!-- ECharts（仅需要图表的页面引入） -->
<script src="https://unpkg.com/echarts@5/dist/echarts.min.js"></script>
```

### Vue 3 应用初始化模板

```javascript
const { createApp, ref, computed, onMounted } = Vue;
const { ElMessage, ElMessageBox } = ElementPlus;

const app = createApp({
  setup() {
    const userInfo = ref({ displayName: '用户' });
    const avatarLetter = computed(() => userInfo.value.displayName?.[0] || 'U');

    onMounted(() => {
      const stored = localStorage.getItem('userInfo');
      if (stored) { try { userInfo.value = JSON.parse(stored); } catch {} }
    });

    return { userInfo, avatarLetter /*, ...*/ };
  },
});

// 注册图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}
app.use(ElementPlus);
app.mount('#app');
```

---

## 19. 用户信息与认证

- Token 存储键：`localStorage.token`
- 用户信息存储键：`localStorage.userInfo`
- 用户信息结构：`{ accountId, userId, username, displayName, role }`
- 角色值：`USER` | `DOCTOR` | `OPERATOR` | `ADMIN`
- API 请求头：`Authorization: Bearer {token}`

---

## 20. 状态颜色速查

| 业务状态 | 标签色 | 文字色 | 背景色 |
|---|---|---|---|
| 待确认/待回复 | blue | `#1565c0` | `#e3f2fd` |
| 已确认/已回复 | green | `#2e7d32` | `#e8f5e9` |
| 已完成/正常 | green | `#2e7d32` | `#e8f5e9` |
| 进行中 | teal | `#00897b` | `#e0f2f1` |
| 异常/偏高 | orange | `#e65100` | `#fff3e0` |
| 高危/严重 | red | `#c62828` | `#ffebee` |
| 已取消/已关闭 | gray | `#999` | `#fafafa` |
| 禁用/等待 | gray | `#bbb` | `#f5f5f5` |

---

## 21. 页面清单与对应文件

| 页面 | 文件名 | 主要组件模式 |
|---|---|---|
| 登录页 | `login.html` | 品牌面板 + 表单卡片 |
| 套餐列表 | `package-list.html` | 网格卡片 + 筛选栏 |
| 预约页 | `appointment.html` | 步骤条 + 表单 + 时段选择 |
| 订单确认 | `order-confirm.html` | 信息网格 + 费用明细表 |
| 我的预约 | `my-appointments.html` | 日期卡片列表 + 状态筛选 |
| 我的体检任务 | `my-exam-task.html` | 概览卡片 + 进度条 + 时间线 |
| 导检路线 | `exam-guide.html` | 地图占位 + 时间线 |
| 报告列表 | `report-list.html` | 报告卡片列表 + 年份筛选 |
| 报告详情 | `report-detail.html` | 结论框 + 统计行 + 指标表格 |
| 历年对比 | `report-compare.html` | 选择器 + ECharts 图表 + 对比表 |
| 咨询医生 | `doctor-consultation.html` | 咨询列表 + 聊天侧滑面板 |
| 医生端待检任务 | `doctor-exam-tasks.html` | 任务列表 + 科室筛选 |
| 医生端结果录入 | `doctor-result-entry.html` | 结构化表单 + 指标录入 |
| 运营端套餐管理 | `operator-packages.html` | 表格 + 新建弹窗 |
| 管理端任务生成 | `admin-task-generate.html` | 表单 + 批量操作 |

---

## 22. 生成新页面时的检查清单

生成每个新页面前，逐项确认：

- [ ] `<head>` 引入了正确的 CDN 资源
- [ ] 页面背景色为 `#f0f4f3`
- [ ] 顶部导航使用标准渐变 `linear-gradient(135deg, #00897b, #00695c)`
- [ ] 导航栏高度 `60px`，sticky 定位
- [ ] 品牌 Logo SVG 使用标准地球图标
- [ ] 页面内容包裹在 `.page-container` 中，`max-width: 960px`（宽页面 1200px）
- [ ] 卡片圆角 `16px`，内边距 `28px`，阴影 `0 2px 12px rgba(0,0,0,0.05)`
- [ ] 主按钮使用渐变 `linear-gradient(135deg, #00897b, #00695c)`
- [ ] 字体族为 `"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif`
- [ ] 所有交互元素有 `transition: all 0.2s`
- [ ] 包含 `@media (max-width: 768px)` 响应式适配
- [ ] `onMounted` 中读取 `localStorage.userInfo` 恢复登录状态
- [ ] 注册了 Element Plus 图标组件
- [ ] 状态标签使用第 20 节的速查色值
