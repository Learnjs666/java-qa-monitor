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
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="7" r="4" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <el-input v-model="form.username" placeholder="用户名" size="large" class="styled-input" />
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrap">
            <span class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password class="styled-input" />
          </div>
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="rememberMe" label="记住我" size="small" />
          <span class="forgot-link">忘记密码？</span>
        </div>

        <el-form-item>
          <button class="submit-btn" :class="{ loading }" @click="handleLogin" :disabled="loading">
            <span class="btn-spinner" v-if="loading"></span>
            <span>{{ loading ? '正在验证...' : '开启监控之旅' }}</span>
          </button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <span class="link" @click="router.push('/register')">立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rememberMe = ref(false)

// 为 rules 添加 FormRules 类型定义，修复可能的类型推断报红
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  
  try {
    // 显式捕获 validate 的 Promise 结果，防止未捕获异常
    const isValid = await formRef.value.validate().catch(() => false)
    if (!isValid) return
    
    loading.value = true
    // 确保传递的是普通对象，有时 reactive 对象在某些严格 TS 场景下会报红
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error: any) {
    // 为 error 指定 any 类型或进行类型收窄，修复 catch 块中 unknown 类型的报红
    console.debug('Login interaction issue:', error)
  } finally {
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
  background: #0b0f1a;
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
  filter: blur(100px);
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;
}

.shape-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #3b82f6 0%, transparent 70%);
  top: -10%; left: -5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 450px; height: 450px;
  background: radial-gradient(circle, #8b5cf6 0%, transparent 70%);
  bottom: -5%; right: -5%;
  animation-delay: -5s;
}

.shape-3 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, #06b6d4 0%, transparent 70%);
  top: 40%; left: 50%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

.auth-card {
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  padding: 48px;
  width: 440px;
  position: relative;
  z-index: 1;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    inset 0 1px 1px rgba(255, 255, 255, 0.1);
}

.auth-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 24px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(255,255,255,0.2), transparent, rgba(255,255,255,0.1));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.auth-header {
  text-align: center;
  margin-bottom: 40px;
}

.auth-logo {
  margin-bottom: 20px;
  display: inline-flex;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.auth-title {
  font-size: 28px;
  letter-spacing: -0.5px;
  font-weight: 800;
  background: linear-gradient(to right, #fff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 8px 0;
}

.auth-subtitle {
  font-size: 15px;
  color: #94a3b8;
  margin: 0;
}

.input-wrap {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  z-index: 10;
  display: flex;
  align-items: center;
  transition: color 0.3s;
}

.input-wrap:focus-within .input-icon {
  color: #3b82f6;
}

.styled-input :deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.4) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 14px !important;
  box-shadow: none !important;
  padding-left: 44px !important;
  height: 52px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.styled-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.2) !important;
  background: rgba(15, 23, 42, 0.6) !important;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6 !important;
  background: rgba(15, 23, 42, 0.8) !important;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
}

.styled-input :deep(.el-input__inner) {
  color: #f1f5f9 !important;
  font-size: 15px;
}

.styled-input :deep(.el-input__inner::placeholder) {
  color: #475569;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -8px;
  margin-bottom: 24px;
}

:deep(.el-checkbox) {
  height: auto;
}

:deep(.el-checkbox__label) {
  color: #94a3b8;
  font-size: 13px;
}

:deep(.el-checkbox__inner) {
  background-color: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
}

.forgot-link {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #60a5fa;
}

.submit-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
  border-radius: 14px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;
}

.submit-btn::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
}

.submit-btn:hover:not(:disabled)::after {
  left: 100%;
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
  margin-top: 32px;
  font-size: 14px;
  color: #94a3b8;
}

.link {
  color: #3b82f6;
  cursor: pointer;
  font-weight: 700;
  margin-left: 6px;
  transition: all 0.2s;
}

.link:hover { 
  color: #60a5fa;
  text-decoration: none;
  border-bottom: 1px solid #60a5fa;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}
:deep(.el-form-item__error) {
  color: #f87171;
  font-size: 12px;
  padding-top: 4px;
}
</style>
