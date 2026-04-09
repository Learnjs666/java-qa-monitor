<template>
  <div class="terminal-page">
    <div class="page-header">
      <div class="header-title-group">
        <span class="blinking-cursor">_</span>
        <h2 class="display-title">项目列表</h2>
      </div>
      <button class="brutal-btn primary" @click="showCreateDialog = true">
        <span class="btn-icon">+</span>
        <span class="btn-text">新增项目</span>
      </button>
    </div>

    <div v-if="loading" class="matrix-grid">
      <div v-for="i in 6" :key="i" class="brutal-skeleton"></div>
    </div>

    <div v-else-if="projects.length === 0" class="terminal-empty-state">
      <div class="empty-glitch">[ 还没有创建项目 ]</div>
      <p class="empty-subtext">系统需要一个新的项目实体来启动检测。</p>
      <button class="brutal-btn outline" @click="showCreateDialog = true">
        创建第一个项目
      </button>
    </div>

    <div v-else class="matrix-grid">
      <div
        v-for="project in projects"
        :key="project.id"
        class="brutal-project-card"
        @click="router.push(`/project/${project.id}`)"
      >
        <div class="card-top-bar">
          <span class="hex-id">ID: 0x{{ String(project.id).padStart(4, '0') }}</span>
          <span class="date-badge">{{ formatDate(project.createTime) }}</span>
        </div>
        
        <div class="card-body">
          <div class="avatar-block">[{{ getInitials(project.name) }}]</div>
          <div class="project-info">
            <h3 class="project-name">{{ project.name }}</h3>
            <p class="project-desc">{{ project.description || 'NO_DESCRIPTION_PROVIDED' }}</p>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="stat-block">
            <span class="stat-label">FILES:</span>
            <span class="stat-value">{{ project.fileCount || 0 }}</span>
          </div>
          <div class="actions" @click.stop>
            <button class="brutal-icon-btn text-accent" @click="router.push(`/project/${project.id}`)">进入</button>
            <button class="brutal-icon-btn text-danger" @click="handleDelete(project.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="showCreateDialog" 
      title="创建新项目" 
      width="500px" 
      :close-on-click-modal="false" 
      class="brutal-dialog"
    >
      <el-form :model="createForm" label-position="top" class="brutal-form">
        <el-form-item label="项目名称">
          <div class="input-wrapper">
            <span class="input-prefix">名字</span>
            <el-input v-model="createForm.name" placeholder="请输入项目名称" size="large" class="brutal-input" />
          </div>
        </el-form-item>
        <el-form-item label="项目描述">
          <div class="input-wrapper textarea-wrapper">
            <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入项目描述" class="brutal-input" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="brutal-btn outline-muted" @click="showCreateDialog = false">取消</button>
          <button class="brutal-btn primary" :disabled="creating" @click="handleCreate">
            <span class="btn-spinner" v-if="creating"></span>
            {{ creating ? '创建中...' : '创建项目' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProjectList, createProject, deleteProject } from '../../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const projects = ref<any[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)
const createForm = reactive({ name: '', description: '' })

onMounted(() => loadProjects())

async function loadProjects() {
  loading.value = true
  try {
    const res: any = await getProjectList()
    projects.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createForm.name.trim()) { ElMessage.warning('请输入项目名称'); return }
  creating.value = true
  try {
    await createProject(createForm)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    createForm.name = ''; createForm.description = ''
    await loadProjects()
  } finally {
    creating.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除该项目吗？删除后数据将无法恢复。', '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await deleteProject(id)
  ElMessage.success('删除成功')
  await loadProjects()
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleDateString('zh-CN') : ''
}

function getInitials(name: string) {
  return name ? name.slice(0, 2).toUpperCase() : '??'
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
  --clr-danger: #ff3366;
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;

  display: flex;
  flex-direction: column;
  gap: 2rem;
  font-family: 'Space Mono', monospace;
  color: var(--clr-text-main);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid var(--clr-border);
  padding-bottom: 1.5rem;
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

/* 硬核按钮组件 */
.brutal-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
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

.brutal-btn.outline {
  background: transparent;
  border: 1px solid var(--clr-accent);
  color: var(--clr-accent);
  box-shadow: 4px 4px 0px rgba(204, 255, 0, 0.2);
}

.brutal-btn.outline:hover {
  background: rgba(204, 255, 0, 0.1);
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px rgba(204, 255, 0, 0.2);
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

.brutal-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* 网格布局 */
.matrix-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.5rem;
}

.brutal-skeleton {
  height: 200px;
  background: repeating-linear-gradient(45deg, var(--bg-card), var(--bg-card) 10px, var(--bg-panel) 10px, var(--bg-panel) 20px);
  border: 1px solid var(--clr-border);
  opacity: 0.5;
  animation: pulse-slow 2s infinite;
}

@keyframes pulse-slow { 0%, 100% { opacity: 0.3; } 50% { opacity: 0.7; } }

/* 空状态 */
.terminal-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: var(--bg-card);
  border: 1px dashed var(--clr-border);
  text-align: center;
}

.empty-glitch {
  font-family: 'Clash Display', sans-serif;
  font-size: 2rem;
  color: var(--clr-text-muted);
  margin-bottom: 1rem;
}

.empty-subtext {
  font-size: 0.85rem;
  color: var(--clr-text-muted);
  margin-bottom: 2rem;
}

/* 项目卡片 */
.brutal-project-card {
  background: var(--bg-card);
  border: 1px solid var(--clr-border);
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.brutal-project-card::before {
  content: '';
  position: absolute;
  top: -1px; left: -1px;
  width: 0; height: 3px;
  background: var(--clr-accent);
  transition: width 0.3s ease;
}

.brutal-project-card:hover {
  border-color: var(--clr-text-muted);
  transform: translateY(-4px);
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
}

.brutal-project-card:hover::before { width: calc(100% + 2px); }

.card-top-bar {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background: var(--bg-dark);
  border-bottom: 1px solid var(--clr-border);
  font-size: 0.7rem;
  font-weight: 700;
  color: var(--clr-text-muted);
}

.card-body {
  padding: 1.5rem 1rem;
  display: flex;
  gap: 1rem;
  flex: 1;
}

.avatar-block {
  width: 48px;
  height: 48px;
  background: var(--bg-dark);
  border: 1px solid var(--clr-text-main);
  color: var(--clr-text-main);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
  box-shadow: 3px 3px 0px var(--clr-text-main);
  flex-shrink: 0;
}

.brutal-project-card:hover .avatar-block {
  border-color: var(--clr-accent);
  color: var(--clr-accent);
  box-shadow: 3px 3px 0px var(--clr-accent);
}

.project-info { flex: 1; min-width: 0; }

.project-name {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.25rem;
  color: var(--clr-text-main);
  margin: 0 0 0.5rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-desc {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 0.8rem;
  color: var(--clr-text-muted);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 1rem;
  border-top: 1px dashed var(--clr-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-block {
  font-size: 0.8rem;
  color: var(--clr-text-muted);
}

.stat-value {
  color: var(--clr-text-main);
  font-weight: 700;
  margin-left: 4px;
}

.actions { display: flex; gap: 1rem; }

.brutal-icon-btn {
  background: transparent;
  border: none;
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.text-accent { color: var(--clr-text-muted); }
.text-accent:hover { color: var(--clr-accent); }
.text-danger { color: var(--clr-text-muted); }
.text-danger:hover { color: var(--clr-danger); }

/* 深度定制的 Dialog 样式 */
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
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #090a0f;
  border: 1px solid #272a35;
  transition: all 0.2s ease;
}
.input-wrapper:focus-within {
  border-color: #ccff00;
  box-shadow: 4px 4px 0px rgba(204, 255, 0, 0.15);
}
.input-prefix {
  padding-left: 1rem;
  font-size: 0.85rem;
  color: #ccff00;
  user-select: none;
}
.brutal-input :deep(.el-input__wrapper), .brutal-input :deep(.el-textarea__inner) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: #ffffff !important;
  font-family: 'Space Mono', monospace !important;
  border-radius: 0 !important;
}
.textarea-wrapper { padding: 0.5rem; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 1rem; }
.btn-spinner {
  width: 14px; height: 14px; border: 2px solid #090a0f;
  border-top-color: transparent; border-radius: 50%;
  animation: spin 0.6s linear infinite; display: inline-block;
}
</style>