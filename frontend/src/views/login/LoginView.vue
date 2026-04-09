<template>
  <div class="sq-auth-container">
    <div class="sq-auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <!-- 替换为简约的蓝色标识 -->
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" width="48" height="48">
            <path d="M4 16h8l4-8 4 16 4-8h4" stroke="#0271b6" stroke-width="3" stroke-linecap="square" stroke-linejoin="miter"/>
          </svg>
        </div>
        <h1 class="auth-title">Java QA Monitor</h1>
        <p class="auth-subtitle">代码质量与安全审计平台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin" class="sq-form">
        <el-form-item prop="username">
          <div class="sq-input-wrap">
            <label class="sq-label">用户名</label>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" class="sq-input" />
          </div>
        </el-form-item>
        
        <el-form-item prop="password">
          <div class="sq-input-wrap">
            <label class="sq-label">密码</label>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password class="sq-input" />
          </div>
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="rememberMe" class="sq-checkbox" label="记住登录状态" size="small" />
          <span class="forgot-link">忘记密码？</span>
        </div>

        <el-form-item>
          <button class="sq-submit-btn" :class="{ loading }" @click.prevent="handleLogin" :disabled="loading">
            <span class="btn-spinner" v-if="loading"></span>
            <span>{{ loading ? '正在验证身份...' : '登 录' }}</span>
          </button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span class="link" @click="router.push('/register')">创建新账号</span>
      </div>
    </div>
    <div class="auth-copyright">
      &copy; 2026 Java QA Monitor. All rights reserved.
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

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  
  try {
    const isValid = await formRef.value.validate().catch(() => false)
    if (!isValid) return
    
    loading.value = true
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error: any) {
    console.debug('Login interaction issue:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.sq-auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6; /* SonarQube 标准浅灰背景 */
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.sq-auth-card {
  background: #ffffff;
  border: 1px solid #e1e6eb;
  border-radius: 4px;
  padding: 40px 48px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.auth-title {
  font-size: 24px;
  font-weight: 600;
  color: #333333;
  margin: 0 0 8px 0;
}

.auth-subtitle {
  font-size: 14px;
  color: #777777;
  margin: 0;
}

.sq-input-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sq-label {
  font-size: 13px;
  font-weight: 600;
  color: #444444;
  text-align: left;
}

/* 覆盖 Element Plus 样式以符合 SonarQube 的方正感 */
.sq-input :deep(.el-input__wrapper) {
  background-color: #ffffff !important;
  border: 1px solid #cccccc !important;
  border-radius: 3px !important;
  box-shadow: none !important;
  height: 40px;
  transition: border-color 0.2s;
}

.sq-input :deep(.el-input__wrapper.is-focus),
.sq-input :deep(.el-input__wrapper:hover) {
  border-color: #0271b6 !important; /* SonarQube Blue */
}

.sq-input :deep(.el-input__inner) {
  color: #333333 !important;
  font-size: 14px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -5px;
  margin-bottom: 24px;
}

.sq-checkbox :deep(.el-checkbox__label) {
  color: #555555;
  font-size: 13px;
}

.forgot-link {
  font-size: 13px;
  color: #0271b6;
  cursor: pointer;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.sq-submit-btn {
  width: 100%;
  height: 40px;
  background-color: #0271b6;
  border: 1px solid #0271b6;
  border-radius: 3px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background-color 0.2s;
}

.sq-submit-btn:hover:not(:disabled) {
  background-color: #005a92;
  border-color: #005a92;
}

.sq-submit-btn:disabled {
  background-color: #8bbce1;
  border-color: #8bbce1;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.4);
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
  padding-top: 20px;
  border-top: 1px solid #e1e6eb;
}

.link {
  color: #0271b6;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.link:hover { 
  text-decoration: underline;
}

.auth-copyright {
  margin-top: 40px;
  font-size: 12px;
  color: #999999;
}
</style>