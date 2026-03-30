<template>
  <div class="terminal-page">
    <div class="page-header">
      <div class="header-title-group">
        <span class="blinking-cursor">_</span>
        <h2 class="display-title">ENTITY_CONTROL // USERS</h2>
      </div>
      <div class="header-right">
        <div class="brutal-search-wrap">
          <span class="search-prefix">QUERY></span>
          <input v-model="searchQuery" class="brutal-search-input" placeholder="USERNAME..." />
        </div>
        <button class="brutal-btn primary" @click="openDialog('CREATE')">
          <span class="btn-icon">+</span>
          <span class="btn-text">ALLOCATE_ENTITY</span>
        </button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="brutal-stat-block default">
        <span class="stat-label">TOTAL_ENTITIES</span>
        <span class="stat-val">{{ users.length }}</span>
      </div>
      <div class="brutal-stat-block admin">
        <span class="stat-label">SYS_ADMINS</span>
        <span class="stat-val">{{ adminCount }}</span>
      </div>
      <div class="brutal-stat-block active">
        <span class="stat-label">ACTIVE_USERS</span>
        <span class="stat-val">{{ activeCount }}</span>
      </div>
      <div class="brutal-stat-block banned">
        <span class="stat-label">RESTRICTED</span>
        <span class="stat-val">{{ users.length - activeCount }}</span>
      </div>
    </div>

    <div class="brutal-table-panel">
      <div v-if="loading" class="terminal-loading">
        <div v-for="i in 5" :key="i" class="brutal-skeleton-row"></div>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="terminal-empty">
        <span class="blink">_</span> NO_ENTITIES_FOUND.
      </div>

      <div v-else class="brutal-data-table">
        <div class="table-header">
          <div class="col-id">UID</div>
          <div class="col-user">IDENTITY_NAME</div>
          <div class="col-role">CLEARANCE_LEVEL</div>
          <div class="col-status">NODE_STATUS</div>
          <div class="col-time">LAST_TELEMETRY</div>
          <div class="col-actions">DIRECTIVES</div>
        </div>

        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="table-row"
          :class="{ 'is-banned': !user.status }"
        >
          <div class="col-id">0x{{ String(user.id).padStart(4, '0') }}</div>
          
          <div class="col-user">
            <div class="user-identity">
              <span class="user-avatar">[{{ user.username.charAt(0).toUpperCase() }}]</span>
              <span class="username">{{ user.username }}</span>
            </div>
          </div>
          
          <div class="col-role">
            <span class="role-badge" :class="user.role.toLowerCase()">
              {{ user.role === 'ADMIN' ? '[ ROOT_ADMIN ]' : '[ STANDARD_USER ]' }}
            </span>
          </div>
          
          <div class="col-status">
            <span class="status-indicator" :class="user.status ? 'active' : 'banned'">
              <span class="dot"></span>
              {{ user.status ? 'ONLINE' : 'TERMINATED' }}
            </span>
          </div>

          <div class="col-time">{{ user.updateTime || 'NEVER' }}</div>

          <div class="col-actions">
            <button class="brutal-action-link text-warning" @click="openDialog('EDIT', user)">[ CONFIGURE ]</button>
            <button 
              class="brutal-action-link" 
              :class="user.status ? 'text-danger' : 'text-success'"
              @click="toggleStatus(user)"
            >
              {{ user.status ? '[ BAN_ENTITY ]' : '[ RESTORE ]' }}
            </button>
          </div>
        </div>
      </div>
      <div class="table-footer">
        <span>END_OF_STREAM // {{ filteredUsers.length }} RECORDS RENDERED.</span>
      </div>
    </div>

    <el-dialog 
      v-model="showDialog" 
      :title="dialogMode === 'CREATE' ? 'INITIALIZE_NEW_ENTITY' : 'RECONFIGURE_ENTITY'" 
      width="480px" 
      :close-on-click-modal="false" 
      class="brutal-dialog"
    >
      <el-form :model="form" label-position="top" class="brutal-form">
        <el-form-item label="IDENTITY_STRING (用户名)">
          <div class="input-wrapper">
            <span class="input-prefix">USR></span>
            <el-input v-model="form.username" :disabled="dialogMode === 'EDIT'" placeholder="ENTER_USERNAME..." size="large" class="brutal-input" />
          </div>
        </el-form-item>

        <el-form-item label="SECURITY_KEY (密码)" v-if="dialogMode === 'CREATE'">
          <div class="input-wrapper">
            <span class="input-prefix">PWD></span>
            <el-input v-model="form.password" type="password" placeholder="ASSIGN_PASSWORD..." size="large" show-password class="brutal-input" />
          </div>
        </el-form-item>

        <el-form-item label="CLEARANCE_LEVEL (权限级别)">
          <div class="brutal-select-wrapper full-width">
            <span class="select-prefix">ROLE:</span>
            <el-select
              v-model="form.role"
              size="large"
              class="brutal-select"
              popper-class="brutal-popper"
            >
              <el-option label="[ STANDARD_USER ]" value="USER" />
              <el-option label="[ ROOT_ADMIN ]" value="ADMIN" />
            </el-select>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <button class="brutal-btn outline-muted" @click="showDialog = false">ABORT</button>
          <button class="brutal-btn primary" :disabled="submitting" @click="handleSubmit">
            <span class="btn-spinner" v-if="submitting"></span>
            {{ submitting ? 'PROCESSING...' : 'EXECUTE_OVERRIDE' }}
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

// ==== 模拟数据与接口 (请在对接真实后端时替换) ====
const mockUsers = [
  { id: 1, username: 'admin_root', role: 'ADMIN', isActive: true, lastLogin: '2026-03-30 08:12:00' },
  { id: 2, username: 'dev_ops_01', role: 'USER', isActive: true, lastLogin: '2026-03-29 14:30:22' },
  { id: 3, username: 'qa_tester_99', role: 'USER', isActive: false, lastLogin: '2026-02-15 09:00:11' },
  { id: 4, username: 'system_bot', role: 'USER', isActive: true, lastLogin: '2026-03-30 12:01:05' }
]
// =================================================

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

onMounted(() => {
  fetchUsers()
})

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
    else await register(form)
    
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
  const actionText = user.isActive ? 'TERMINATE (禁用)' : 'RESTORE (恢复)'
  await ElMessageBox.confirm(
    `CONFIRM DIRECTIVE: ${actionText} ENTITY [${user.username}] ?`,
    'SYSTEM_WARNING',
    {
      confirmButtonText: 'CONFIRM',
      cancelButtonText: 'ABORT',
      type: 'warning',
      customClass: 'brutal-message-box' // 如果你想在全局深定制 MessageBox，可预留此 class
    }
  )
  
  // TODO: await toggleUserStatus(user.id, !user.isActive)
  ElMessage.success(`ENTITY_STATUS_UPDATED`)
  user.isActive = !user.isActive // 模拟前端状态切换
}
</script>

<style scoped>
@import url('https://api.fontshare.com/v2/css?f[]=clash-display@600,700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.terminal-page {
  --bg-dark: #090a0f;
  --bg-panel: #11131a;
  --bg-card: #161922;
  --clr-accent: #ccff00;
  --clr-success: #00e5ff;
  --clr-warning: #ffaa00;
  --clr-danger: #ff3366;
  --clr-admin: #a259ff; /* 紫色代表高权限 Root */
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;

  display: flex;
  flex-direction: column;
  gap: 2rem;
  font-family: 'Space Mono', monospace;
  color: var(--clr-text-main);
  padding-bottom: 2rem;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid var(--clr-border);
  padding-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.blinking-cursor {
  font-size: 2rem;
  color: var(--clr-accent);
  animation: blink 1s step-end infinite;
}

@keyframes blink { 50% { opacity: 0; } }

.display-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--clr-text-main);
  margin: 0;
  letter-spacing: 0.05em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

/* 搜索框 */
.brutal-search-wrap {
  display: flex;
  align-items: center;
  background: var(--bg-dark);
  border: 1px solid var(--clr-border);
  transition: all 0.2s ease;
}

.brutal-search-wrap:focus-within {
  border-color: var(--clr-accent);
  box-shadow: 4px 4px 0px rgba(204, 255, 0, 0.15);
}

.search-prefix {
  padding: 0 0.75rem;
  font-size: 0.8rem;
  color: var(--clr-accent);
  font-weight: 700;
  border-right: 1px solid var(--clr-border);
}

.brutal-search-input {
  border: none;
  outline: none;
  background: transparent;
  color: var(--clr-text-main);
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  padding: 0.6rem 0.75rem;
  width: 220px;
}

/* 按钮 */
.brutal-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  font-family: 'Space Mono', monospace;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  border: none;
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.brutal-btn.primary {
  background: var(--clr-text-main);
  color: var(--bg-dark);
  box-shadow: 4px 4px 0px var(--clr-accent);
}

.brutal-btn.primary:hover:not(:disabled) {
  background: var(--clr-accent);
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px var(--clr-accent);
}

.brutal-btn.primary:active:not(:disabled) {
  transform: translate(4px, 4px);
  box-shadow: 0px 0px 0px transparent;
}

.brutal-btn.outline-muted {
  background: transparent;
  border: 1px solid var(--clr-border);
  color: var(--clr-text-muted);
}
.brutal-btn.outline-muted:hover {
  border-color: var(--clr-text-main);
  color: var(--clr-text-main);
}
.brutal-btn:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.brutal-stat-block {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  position: relative;
}

.brutal-stat-block::after {
  content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%;
}
.brutal-stat-block.default::after { background: var(--clr-text-muted); }
.brutal-stat-block.admin::after { background: var(--clr-admin); }
.brutal-stat-block.active::after { background: var(--clr-success); }
.brutal-stat-block.banned::after { background: var(--clr-danger); }

.stat-label { font-size: 0.75rem; color: var(--clr-text-muted); font-weight: 700; }
.stat-val { font-family: 'Clash Display', sans-serif; font-size: 2.5rem; font-weight: 700; line-height: 1; color: var(--clr-text-main); }
.brutal-stat-block.admin .stat-val { color: var(--clr-admin); text-shadow: 0 0 10px rgba(162,89,255,0.3); }

/* 数据表格 */
.brutal-table-panel {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
  overflow-x: auto;
}

.terminal-loading { padding: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem; }
.brutal-skeleton-row { height: 50px; background: var(--bg-card); opacity: 0.5; animation: pulse-slow 2s infinite; }
@keyframes pulse-slow { 0%, 100% { opacity: 0.3; } 50% { opacity: 0.7; } }

.terminal-empty { padding: 4rem; text-align: center; color: var(--clr-text-muted); font-size: 0.85rem; }

.brutal-data-table { min-width: 900px; display: flex; flex-direction: column; }

.table-header {
  display: grid;
  grid-template-columns: 80px 2fr 1.5fr 1.5fr 1.5fr 200px;
  padding: 1rem 1.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--clr-accent);
  background: var(--bg-dark);
  border-bottom: 1px solid var(--clr-border);
}

.table-row {
  display: grid;
  grid-template-columns: 80px 2fr 1.5fr 1.5fr 1.5fr 200px;
  padding: 1rem 1.5rem;
  align-items: center;
  border-bottom: 1px dashed var(--clr-border);
  transition: background 0.2s;
  font-size: 0.85rem;
}

.table-row:hover { background: var(--bg-card); }
.table-row:last-child { border-bottom: none; }
.table-row.is-banned { opacity: 0.6; background: rgba(255,51,102,0.03); filter: grayscale(0.5); }

.col-id { color: var(--clr-text-muted); font-weight: 700; }

.user-identity { display: flex; align-items: center; gap: 10px; }
.user-avatar { font-weight: 800; color: var(--clr-accent); }
.username { font-weight: 700; color: var(--clr-text-main); }

.role-badge { font-weight: 700; font-size: 0.75rem; }
.role-badge.admin { color: var(--clr-admin); }
.role-badge.user { color: var(--clr-text-muted); }

.status-indicator { display: inline-flex; align-items: center; gap: 6px; font-weight: 700; font-size: 0.75rem; }
.status-indicator .dot { width: 6px; height: 6px; background: currentColor; }
.status-indicator.active { color: var(--clr-success); }
.status-indicator.banned { color: var(--clr-danger); }

.col-time { color: var(--clr-text-muted); font-size: 0.8rem; }

.col-actions { display: flex; gap: 1rem; }

.brutal-action-link {
  background: transparent; border: none; font-family: 'Space Mono', monospace;
  font-size: 0.75rem; font-weight: 700; cursor: pointer; text-decoration: none;
}
.brutal-action-link.text-warning { color: var(--clr-warning); }
.brutal-action-link.text-warning:hover { color: #fff; background: var(--clr-warning); }
.brutal-action-link.text-danger { color: var(--clr-danger); }
.brutal-action-link.text-danger:hover { color: #fff; background: var(--clr-danger); }
.brutal-action-link.text-success { color: var(--clr-success); }
.brutal-action-link.text-success:hover { color: #000; background: var(--clr-success); }

.table-footer {
  padding: 0.75rem 1.5rem;
  background: var(--bg-dark);
  border-top: 1px solid var(--clr-border);
  font-size: 0.7rem;
  color: var(--clr-text-muted);
  font-weight: 700;
}

/* 弹窗与表单覆写 */
:deep(.el-dialog) {
  background: #11131a !important;
  border: 1px solid #272a35 !important;
  border-radius: 0 !important;
  box-shadow: 16px 16px 0px rgba(0,0,0,0.8) !important;
}
:deep(.el-dialog__title) {
  font-family: 'Space Mono', monospace !important;
  color: #ccff00 !important;
  font-weight: 700 !important;
}
:deep(.el-form-item__label) {
  color: #6b7280 !important;
  font-family: 'Space Mono', monospace !important;
  font-size: 0.8rem !important;
  line-height: 1.2 !important;
  padding-bottom: 6px !important;
}

.input-wrapper {
  display: flex; align-items: center; background: #090a0f;
  border: 1px solid #272a35; transition: all 0.2s ease; width: 100%;
}
.input-wrapper:focus-within { border-color: #ccff00; box-shadow: 4px 4px 0px rgba(204,255,0,0.15); }

.input-prefix {
  padding-left: 1rem; font-size: 0.85rem; color: #ccff00; user-select: none;
}

.brutal-input :deep(.el-input__wrapper) {
  background: transparent !important; border: none !important;
  box-shadow: none !important; border-radius: 0 !important;
}
.brutal-input :deep(.el-input__inner) {
  color: #ffffff !important; font-family: 'Space Mono', monospace !important; font-size: 0.85rem;
}

.brutal-select-wrapper { display: flex; align-items: center; background: #090a0f; border: 1px solid #272a35; }
.brutal-select-wrapper.full-width { width: 100%; }
.select-prefix { padding-left: 1rem; font-size: 0.85rem; color: #ccff00; font-weight: 700; border-right: 1px solid #272a35; }
.brutal-select :deep(.el-input__wrapper) { background: transparent !important; border: none !important; box-shadow: none !important; border-radius: 0 !important; }
.brutal-select :deep(.el-input__inner) { color: #ffffff !important; font-family: 'Space Mono', monospace; font-size: 0.85rem; font-weight: 700; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 1rem; }
.btn-spinner {
  width: 14px; height: 14px; border: 2px solid #090a0f;
  border-top-color: transparent; border-radius: 50%;
  animation: spin 0.6s linear infinite; display: inline-block;
}
</style>