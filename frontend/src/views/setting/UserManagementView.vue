<template>
  <div class="sq-page">
    <div class="sq-page-header">
      <h2 class="sq-page-title">系统用户管理</h2>
      <div class="header-right">
        <div class="sq-search-wrap">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input v-model="searchQuery" class="sq-search-input" placeholder="输入用户名搜索..." />
        </div>
        <button class="sq-btn primary" @click="openDialog('CREATE')">
          添加新用户
        </button>
      </div>
    </div>

    <div class="sq-stats-banner">
      <div class="sq-stat-item">
        <span class="stat-val">{{ users.length }}</span>
        <span class="stat-label">注册用户</span>
      </div>
      <div class="stat-divider"></div>
      <div class="sq-stat-item">
        <span class="stat-val text-blue">{{ adminCount }}</span>
        <span class="stat-label">系统管理员</span>
      </div>
      <div class="stat-divider"></div>
      <div class="sq-stat-item">
        <span class="stat-val text-red">{{ users.length - activeCount }}</span>
        <span class="stat-label">封禁节点</span>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <div class="sq-panel">
      <div v-if="loading" class="sq-loading-placeholder">
        <div v-for="i in 5" :key="i" class="sq-skeleton-row"></div>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="sq-empty-state">
        无匹配的系统用户。
      </div>

      <div v-else class="sq-table">
        <div class="table-header">
          <div class="col-id">内部ID</div>
          <div class="col-user">账号标识 (Username)</div>
          <div class="col-role">系统权限</div>
          <div class="col-status">账户状态</div>
          <div class="col-time">创建时间</div>
          <div class="col-actions">操作</div>
        </div>

        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="table-row"
          :class="{ 'is-banned': !user.status }"
        >
          <div class="col-id">{{ String(user.id).padStart(4, '0') }}</div>
          <div class="col-user">
            <span class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</span>
            <span class="username">{{ user.username }}</span>
          </div>
          <div class="col-role">
            <span class="role-text" :class="user.role.toLowerCase()">
              {{ user.role === 'ADMIN' ? 'Administrator' : 'Standard User' }}
            </span>
          </div>
          <div class="col-status">
            <span class="status-dot" :class="user.status ? 'active' : 'banned'"></span>
            {{ user.status ? '正常' : '已封禁' }}
          </div>
          <div class="col-time">{{ user.createTime || '从未登录' }}</div>
          <div class="col-actions">
            <button class="sq-btn-text" @click="openDialog('EDIT', user)">配置权限</button>
            <span class="divider">|</span>
            <button 
              class="sq-btn-text" 
              :class="user.status ? 'danger' : 'success'"
              @click="toggleStatus(user)"
            >
              {{ user.status ? '封禁账号' : '解封' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 配置弹窗 -->
    <el-dialog 
      v-model="showDialog" 
      :title="dialogMode === 'CREATE' ? '添加系统用户' : '重新配置用户权限'" 
      width="450px" 
      :close-on-click-modal="false" 
      class="sq-dialog"
    >
      <el-form :model="form" label-position="top" class="sq-dialog-form">
        <el-form-item label="登录账号 (Username)">
          <el-input v-model="form.username" :disabled="dialogMode === 'EDIT'" placeholder="输入登录名" class="sq-input" />
        </el-form-item>

        <el-form-item label="登录密码" v-if="dialogMode === 'CREATE'">
          <el-input v-model="form.password" type="password" placeholder="设置初始密码" show-password class="sq-input" />
        </el-form-item>

        <el-form-item label="系统权限 (Role)">
          <el-select v-model="form.role" class="sq-select-full">
            <el-option label="Standard User (普通用户)" value="USER" />
            <el-option label="Administrator (系统管理员)" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="sq-dialog-footer">
          <button class="sq-btn default" @click="showDialog = false">取消</button>
          <button class="sq-btn primary" :disabled="submitting" @click="handleSubmit">
            <span class="btn-spinner" v-if="submitting"></span>
            {{ submitting ? '保存中...' : '保 存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, register } from '../../api/user'
import {updateUser, updateUserStatus} from "../../api/user"
import { ca } from 'element-plus/es/locale/index.mjs'

const users = ref<any[]>([])
const loading = ref(false)
const searchQuery = ref('')

const showDialog = ref(false)
const dialogMode = ref<'CREATE' | 'EDIT'>('CREATE')
const submitting = ref(false)

const form = reactive({
  id: null as number | null,
  username: '',
  password: '',
  role: 'USER'
})

// 统计计算
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  return users.value.filter(u => u.username.toLowerCase().includes(searchQuery.value.toLowerCase()))
})
const adminCount = computed(() => users.value.filter(u => u.role === 'ADMIN').length)
const activeCount = computed(() => users.value.filter(u => u.isActive).length)

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data
    console.log(users.value)
    setTimeout(() => {
      users.value = [...users.value] // 触发响应式更新
      loading.value = false
    }, 600) // 模拟网络延迟
  } catch (error) {
    loading.value = false
  }
}

function openDialog(mode: 'CREATE' | 'EDIT', userData?: any) {
  dialogMode.value = mode
  if (mode === 'EDIT' && userData) {
    form.id = userData.id
    form.username = userData.username
    form.password = '' // 编辑不展示密码
    form.role = userData.role
  } else {
    form.id = null
    form.username = ''
    form.password = ''
    form.role = 'USER'
  }
  showDialog.value = true
}

async function handleSubmit() {
  if (!form.username.trim()) {
    ElMessage.warning('REQUIRES IDENTITY_STRING (请输入用户名)')
    return
  }
  if (dialogMode.value === 'CREATE' && !form.password.trim()) {
    ElMessage.warning('REQUIRES SECURITY_KEY (请输入密码)')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'CREATE') await register(form)
    else {
        if (form.id !== null) {
            await updateUser(form.id, form.role)
        }
    }
    
    setTimeout(() => {
      ElMessage.success('DIRECTIVE_EXECUTED_SUCCESSFULLY (操作成功)')
      showDialog.value = false
      submitting.value = false
      fetchUsers() // 重新拉取
    }, 800)
  } catch (e) {
    submitting.value = false
  }
}

async function toggleStatus(user: any) {
  const actionText = user.status ? '封禁' : '解封'
  await ElMessageBox.confirm(`确认要${actionText}账户 [${user.username}] 吗？`, '安全警告', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
  try {
    await updateUserStatus(user.id, user.status)
    ElMessage.success(`账号状态已更新`)
    user.status = !user.status // 本地更新状态
  } catch (error) {
    console.error('状态更新失败', error)
  } 
}
</script>

<style scoped>
.sq-page { display: flex; flex-direction: column; gap: 20px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333; }
.sq-page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; border-bottom: 1px solid #e1e6eb; padding-bottom: 12px; }
.sq-page-title { font-size: 20px; font-weight: 600; margin: 0; color: #222222; }

.header-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.sq-search-wrap { display: flex; align-items: center; gap: 8px; border: 1px solid #cccccc; border-radius: 3px; padding: 6px 10px; background: #ffffff; transition: border-color 0.2s; }
.sq-search-wrap:focus-within { border-color: #0271b6; }
.search-icon { color: #999999; }
.sq-search-input { border: none; outline: none; font-size: 13px; color: #333333; width: 180px; }

.sq-btn { display: flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: 3px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s; border: 1px solid transparent; }
.sq-btn.primary { background-color: #0271b6; color: #ffffff; }
.sq-btn.primary:hover:not(:disabled) { background-color: #005a92; }
.sq-btn.default { background-color: #ffffff; border-color: #cccccc; color: #333333; }
.sq-btn.default:hover { border-color: #999999; background-color: #f9f9f9; }

.sq-stats-banner { display: flex; background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px; padding: 16px 24px; gap: 40px; box-shadow: 0 1px 2px rgba(0,0,0,0.03); }
.sq-stat-item { display: flex; align-items: baseline; gap: 8px; }
.stat-val { font-size: 24px; font-weight: 600; color: #333333; line-height: 1; }
.stat-label { font-size: 13px; color: #777777; }
.text-blue { color: #0271b6; }
.text-red { color: #d4333f; }
.stat-divider { width: 1px; background: #e1e6eb; }

.sq-panel { background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px; overflow-x: auto; box-shadow: 0 1px 2px rgba(0,0,0,0.03); }
.sq-loading-placeholder { display: flex; flex-direction: column; gap: 8px; padding: 16px; }
.sq-skeleton-row { height: 40px; background: #f3f4f6; animation: pulse 1.5s infinite; }
.sq-empty-state { padding: 40px; text-align: center; color: #777777; font-size: 13px; }

.sq-table { min-width: 900px; display: flex; flex-direction: column; }
.table-header { display: grid; grid-template-columns: 80px 2fr 1.5fr 120px 1.5fr 150px; padding: 10px 16px; font-size: 12px; font-weight: 600; color: #777777; border-bottom: 1px solid #e1e6eb; background-color: #fafbfc; }
.table-row { display: grid; grid-template-columns: 80px 2fr 1.5fr 120px 1.5fr 150px; padding: 12px 16px; border-bottom: 1px solid #f3f4f6; align-items: center; font-size: 13px; transition: background 0.15s; }
.table-row:hover { background-color: #f9f9fb; }
.table-row:last-child { border-bottom: none; }
.table-row.is-banned { opacity: 0.6; color: #777; }

.col-id { color: #777; font-family: Consolas, monospace; font-size: 12px; }
.col-user { display: flex; align-items: center; gap: 8px; }
.user-avatar { width: 24px; height: 24px; background: #eef2f5; color: #0271b6; font-size: 12px; font-weight: 600; display: flex; align-items: center; justify-content: center; border-radius: 50%; }
.username { font-weight: 600; color: #0271b6; }

.role-text { font-size: 12px; font-weight: 600; padding: 2px 6px; border-radius: 2px; }
.role-text.admin { color: #0271b6; background: #eef2f5; border: 1px solid #bce1f1; }
.role-text.user { color: #555555; background: #f3f4f6; border: 1px solid #e1e6eb; }

.status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
.status-dot.active { background-color: #00aa00; }
.status-dot.banned { background-color: #d4333f; }

.col-time { color: #555555; font-size: 12px; }
.col-actions { display: flex; align-items: center; gap: 8px; }
.divider { color: #cccccc; font-size: 10px; }

.sq-btn-text { background: transparent; border: none; padding: 0; font-size: 12px; font-weight: 500; color: #0271b6; cursor: pointer; }
.sq-btn-text:hover { text-decoration: underline; }
.sq-btn-text.danger { color: #d4333f; }
.sq-btn-text.success { color: #00aa00; }

:deep(.el-dialog) { border-radius: 4px; }
:deep(.el-dialog__header) { border-bottom: 1px solid #e1e6eb; padding: 16px 20px; margin: 0; }
:deep(.el-dialog__title) { font-size: 16px; font-weight: 600; color: #333; }
:deep(.el-dialog__body) { padding: 24px 20px; }
:deep(.el-dialog__footer) { border-top: 1px solid #e1e6eb; padding: 16px 20px; }
.sq-dialog-form :deep(.el-form-item__label) { padding-bottom: 4px; line-height: 1.2; font-size: 13px; font-weight: 600; color: #444; }
.sq-input :deep(.el-input__wrapper) { border-radius: 3px !important; box-shadow: none !important; border: 1px solid #cccccc !important; }
.sq-input :deep(.el-input__wrapper.is-focus) { border-color: #0271b6 !important; }
.sq-select-full :deep(.el-input__wrapper) { width: 100%; border-radius: 3px !important; box-shadow: none !important; border: 1px solid #cccccc !important; }
.sq-dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.btn-spinner { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; display: inline-block; }
</style>