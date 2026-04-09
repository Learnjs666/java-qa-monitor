<template>
  <div class="sq-auth-container">
    <div class="sq-auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" width="48" height="48">
            <path d="M4 16h8l4-8 4 16 4-8h4" stroke="#0271b6" stroke-width="3" stroke-linecap="square" stroke-linejoin="miter"/>
          </svg>
        </div>
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">加入 Java QA Monitor 开始代码质量审计</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleRegister" class="sq-form">
        <el-form-item prop="username">
          <div class="sq-input-wrap">
            <label class="sq-label">用户名</label>
            <el-input v-model="form.username" placeholder="请输入您的用户名" size="large" class="sq-input" />
          </div>
        </el-form-item>
        
        <el-form-item prop="password">
          <div class="sq-input-wrap">
            <label class="sq-label">密码</label>
            <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" size="large" show-password class="sq-input" />
          </div>
        </el-form-item>

        <el-form-item class="action-item">
          <button class="sq-submit-btn" :class="{ loading }" @click.prevent="handleRegister" :disabled="loading">
            <span class="btn-spinner" v-if="loading"></span>
            <span>{{ loading ? '正在注册...' : '创 建 账 号' }}</span>
          </button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        <span class="text-muted">已有账号？</span>
        <span class="link" @click="router.push('/login')">前往登录</span>
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
.sq-auth-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
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

.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { margin-bottom: 16px; display: flex; justify-content: center; }
.auth-title { font-size: 24px; font-weight: 600; color: #333333; margin: 0 0 8px 0; }
.auth-subtitle { font-size: 14px; color: #777777; margin: 0; }

.sq-input-wrap { width: 100%; display: flex; flex-direction: column; gap: 6px; }
.sq-label { font-size: 13px; font-weight: 600; color: #444444; text-align: left; }

.sq-input :deep(.el-input__wrapper) {
  background-color: #ffffff !important;
  border: 1px solid #cccccc !important;
  border-radius: 3px !important;
  box-shadow: none !important;
  height: 40px;
  transition: border-color 0.2s;
}
.sq-input :deep(.el-input__wrapper.is-focus),
.sq-input :deep(.el-input__wrapper:hover) { border-color: #0271b6 !important; }
.sq-input :deep(.el-input__inner) { color: #333333 !important; font-size: 14px; }

.action-item { margin-top: 16px; margin-bottom: 0; }

.sq-submit-btn {
  width: 100%; height: 40px; background-color: #0271b6; border: 1px solid #0271b6;
  border-radius: 3px; color: #ffffff; font-size: 14px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px; transition: background-color 0.2s;
}
.sq-submit-btn:hover:not(:disabled) { background-color: #005a92; border-color: #005a92; }
.sq-submit-btn:disabled { background-color: #8bbce1; border-color: #8bbce1; cursor: not-allowed; }

.btn-spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer { text-align: center; margin-top: 24px; padding-top: 20px; border-top: 1px solid #e1e6eb; font-size: 13px; }
.text-muted { color: #777777; margin-right: 6px; }
.link { color: #0271b6; cursor: pointer; font-weight: 500; }
.link:hover { text-decoration: underline; }

.auth-copyright { margin-top: 40px; font-size: 12px; color: #999999; }
</style>