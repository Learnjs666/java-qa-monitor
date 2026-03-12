<template>
  <div class="auth-container">
    <!-- Background animated shapes -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" width="40" height="40">
            <rect width="32" height="32" rx="8" fill="url(#grad)"/>
            <path d="M8 22L14 10L20 18L24 14" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="14" r="2" fill="#7ee8fa"/>
            <defs>
              <linearGradient id="grad" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
                <stop stop-color="#4f8cff"/>
                <stop offset="1" stop-color="#a259ff"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="auth-title">Java QA Monitor</h1>
        <p class="auth-subtitle">欢迎回来，请登录您的账号</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <div class="input-wrap">
            <span class="input-icon">
              <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
              </svg>
            </span>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" class="styled-input" />
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrap">
            <span class="input-icon">
              <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
              </svg>
            </span>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password class="styled-input" />
          </div>
        </el-form-item>
        <el-form-item>
          <button class="submit-btn" :class="{ loading }" @click="handleLogin" :disabled="loading">
            <span class="btn-spinner" v-if="loading"></span>
            <span>{{ loading ? '登录中...' : '登 录' }}</span>
          </button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <a class="link" @click="router.push('/register')">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage, type FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch { /* handled by interceptor */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 60%, #0f1a2e 100%);
  position: relative;
  overflow: hidden;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 400px; height: 400px;
  background: #4f8cff;
  top: -100px; left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 300px; height: 300px;
  background: #a259ff;
  bottom: -80px; right: -80px;
  animation-delay: 3s;
}

.shape-3 {
  width: 200px; height: 200px;
  background: #22d3ee;
  top: 50%; left: 60%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.auth-card {
  background: rgba(255,255,255,0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 20px;
  padding: 44px 48px;
  width: 420px;
  position: relative;
  z-index: 1;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.auth-logo {
  margin-bottom: 14px;
}

.auth-title {
  font-size: 22px;
  font-weight: 800;
  color: #f1f5f9;
  margin: 0 0 8px 0;
}

.auth-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.input-wrap {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #475569;
  z-index: 10;
  display: flex;
  align-items: center;
}

.styled-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.05) !important;
  border: 1px solid rgba(255,255,255,0.1) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding-left: 40px !important;
  transition: border-color 0.2s;
}

.styled-input :deep(.el-input__wrapper:hover),
.styled-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(79,140,255,0.5) !important;
}

.styled-input :deep(.el-input__inner) {
  color: #e2e8f0 !important;
  font-size: 14px;
}

.styled-input :deep(.el-input__inner::placeholder) {
  color: #475569;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #4f8cff, #a259ff);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: opacity 0.2s, transform 0.1s;
  margin-top: 4px;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: #475569;
}

.link {
  color: #4f8cff;
  cursor: pointer;
  font-weight: 600;
  margin-left: 6px;
  transition: color 0.2s;
}

.link:hover { color: #7eb6ff; text-decoration: underline; }

:deep(.el-form-item) {
  margin-bottom: 18px;
}
:deep(.el-form-item__error) {
  color: #f87171;
  font-size: 12px;
  padding-top: 4px;
}
</style>
