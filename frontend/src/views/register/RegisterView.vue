<template>
  <div class="terminal-auth-viewport">
    <div class="grid-overlay"></div>

    <div class="bento-container">

      <div class="status-panel">
        <div class="panel-header">
          <span class="dot blink pulse-blue"></span>
          <span class="mono-text">NEW_ENTITY // INITIALIZATION</span>
        </div>

        <div class="brand-hero">
          <h1 class="display-title">SYSTEM<br>ACCESS<br>REQUEST</h1>
          <div class="barcode">
            <svg viewBox="0 0 100 20" preserveAspectRatio="none">
              <path d="M0,0 h1 v20 h-1 Z M3,0 h3 v20 h-3 Z M8,0 h1 v20 h-1 Z M11,0 h4 v20 h-4 Z M17,0 h2 v20 h-2 Z M21,0 h1 v20 h-1 Z M24,0 h3 v20 h-3 Z M29,0 h1 v20 h-1 Z M33,0 h2 v20 h-2 Z M37,0 h5 v20 h-5 Z M44,0 h1 v20 h-1 Z M47,0 h3 v20 h-3 Z M52,0 h2 v20 h-2 Z M55,0 h4 v20 h-4 Z M62,0 h1 v20 h-1 Z M65,0 h3 v20 h-3 Z M70,0 h2 v20 h-2 Z M74,0 h5 v20 h-5 Z M81,0 h1 v20 h-1 Z M84,0 h3 v20 h-3 Z M89,0 h2 v20 h-2 Z M93,0 h4 v20 h-4 Z M99,0 h1 v20 h-1 Z" fill="currentColor" />
            </svg>
          </div>
        </div>

        <div class="tech-specs">
          <div class="spec-row"><span>ACTION:</span><span>REGISTRATION</span></div>
          <div class="spec-row"><span>CLEARANCE:</span><span>PENDING</span></div>
          <div class="spec-row"><span>ENCRYPTION:</span><span>AES-256</span></div>
        </div>
      </div>

      <div class="form-panel">
        <div class="form-header">
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">加入 Java QA Monitor，配置您的专属监控环境。</p>
        </div>

        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            @keyup.enter="handleRegister"
            class="terminal-form"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <span class="input-prefix">用户</span>
              <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
                  size="large"
                  class="brutal-input"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <span class="input-prefix">密码</span>
              <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="密码（至少6位）"
                  size="large"
                  show-password
                  class="brutal-input"
              />
            </div>
          </el-form-item>

          <el-form-item class="action-item">
            <button class="brutal-submit" :class="{ loading }" @click.prevent="handleRegister" :disabled="loading">
              <span class="btn-spinner" v-if="loading"></span>
              <span class="btn-text">{{ loading ? '注册中...' : '创建账号' }}</span>
              <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14m-7-7h14" stroke-linecap="square"/></svg>
            </button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span class="mono-text">已有账号？</span>
          <button class="register-link" @click="router.push('/login')">前往登录</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../../api/user'
import { ElMessage, type FormInstance } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch { /* error handled by interceptor */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 引入顶级设计字体 */
@import url('https://api.fontshare.com/v2/css?f[]=clash-display@600,700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Noto+Sans+SC:wght@400;500&display=swap');

.terminal-auth-viewport {
  --bg-dark: #090a0f;
  --bg-panel: #11131a;
  --clr-accent: #ccff00; /* Electric Lime */
  --clr-accent-dim: rgba(204, 255, 0, 0.15);
  --clr-secondary: #00e5ff; /* Cyan for Register diff */
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;
  --clr-error: #ff3366;

  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-dark);
  font-family: 'Space Mono', monospace;
  position: relative;
  overflow: hidden;
  padding: 2rem;
}

/* 工业风量化网格背景 */
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(var(--clr-border) 1px, transparent 1px),
      linear-gradient(90deg, var(--clr-border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.3;
  pointer-events: none;
  z-index: 0;
}

/* 主体 Bento 容器 */
.bento-container {
  position: relative;
  z-index: 10;
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  box-shadow: -16px 16px 0px rgba(0,0,0,0.5); /* 注册页阴影方向微调，与登录页形成对称感 */
}

/* ==================== 左侧：监控面板 ==================== */
.status-panel {
  flex: 1;
  border-right: 1px solid var(--clr-border);
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: radial-gradient(circle at top right, #1a1d27 0%, var(--bg-panel) 100%);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.8rem;
  color: var(--clr-text-muted);
}

.dot {
  width: 8px;
  height: 8px;
  background-color: var(--clr-secondary);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--clr-secondary);
}

.pulse-blue {
  background-color: var(--clr-secondary);
  box-shadow: 0 0 10px var(--clr-secondary);
}

.blink {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.display-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 4.5rem;
  font-weight: 700;
  line-height: 0.9;
  letter-spacing: 0.02em;
  color: var(--clr-text-main);
  margin: 0 0 2rem 0;
  text-transform: uppercase;
}

.barcode {
  width: 100px;
  height: 30px;
  color: var(--clr-text-muted);
  opacity: 0.5;
}

.tech-specs {
  border-top: 1px solid var(--clr-border);
  padding-top: 1.5rem;
}

.spec-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  margin-bottom: 0.5rem;
  letter-spacing: 0.05em;
}

.spec-row span:last-child {
  color: var(--clr-text-main);
}

/* ==================== 右侧：表单面板 ==================== */
.form-panel {
  flex: 0 0 450px;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 3rem;
}

.form-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--clr-accent);
  margin: 0 0 0.5rem 0;
  letter-spacing: 0.05em;
}

.form-subtitle {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 0.85rem;
  color: var(--clr-text-muted);
  margin: 0;
}

/* 彻底覆盖 Element Plus 样式 */
.terminal-form {
  width: 100%;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--bg-dark);
  border: 1px solid var(--clr-border);
  transition: all 0.2s ease;
  margin-bottom: 8px; /* 给表单项之间增加一点呼吸空间 */
}

.input-wrapper:focus-within {
  border-color: var(--clr-accent);
  box-shadow: 4px 4px 0px var(--clr-accent-dim);
}

.input-prefix {
  padding-left: 1rem;
  font-size: 0.85rem;
  color: var(--clr-accent);
  user-select: none;
  white-space: nowrap;
}

.brutal-input :deep(.el-input__wrapper) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 1rem;
  height: 54px;
}

.brutal-input :deep(.el-input__inner) {
  color: var(--clr-text-main) !important;
  font-family: 'Space Mono', monospace;
  font-size: 0.9rem;
}

.brutal-input :deep(.el-input__inner::placeholder) {
  color: #3f4455;
}

/* 错误信息样式覆盖 */
:deep(.el-form-item__error) {
  color: var(--clr-error);
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem;
  padding-top: 2px;
  position: static;
}

/* 工业风提交按钮 */
.action-item {
  margin-top: 24px;
  margin-bottom: 0;
}

.brutal-submit {
  width: 100%;
  height: 56px;
  background: var(--clr-text-main);
  color: var(--bg-dark);
  border: none;
  font-family: 'Space Mono', monospace;
  font-weight: 700;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
  box-shadow: 6px 6px 0px var(--clr-accent);
}

.brutal-submit:hover:not(:disabled) {
  transform: translate(2px, 2px);
  box-shadow: 4px 4px 0px var(--clr-accent);
  background: var(--clr-accent);
}

.brutal-submit:active:not(:disabled) {
  transform: translate(6px, 6px);
  box-shadow: 0px 0px 0px var(--clr-accent);
}

.brutal-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--bg-dark);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部区域 */
.form-footer {
  margin-top: 2.5rem;
  padding-top: 1.5rem;
  border-top: 1px dashed var(--clr-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
}

.mono-text {
  color: var(--clr-text-muted);
}

.register-link {
  background: transparent;
  border: none;
  color: var(--clr-accent);
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.register-link:hover {
  color: var(--clr-text-main);
}

/* 响应式断点适配 */
@media (max-width: 850px) {
  .bento-container {
    flex-direction: column;
    box-shadow: -8px 8px 0px rgba(0,0,0,0.5);
  }
  .status-panel {
    border-right: none;
    border-bottom: 1px solid var(--clr-border);
    padding: 2rem;
  }
  .display-title {
    font-size: 3rem;
  }
  .form-panel {
    flex: auto;
    padding: 2rem;
  }
}
</style>