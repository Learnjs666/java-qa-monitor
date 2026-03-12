<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">项目列表</h2>
      <button class="primary-btn" @click="showCreateDialog = true">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        新建项目
      </button>
    </div>

    <!-- Project Grid -->
    <div v-if="loading" class="loading-grid">
      <div v-for="i in 6" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else-if="projects.length === 0" class="empty-state">
      <svg viewBox="0 0 64 64" width="72" height="72" fill="none">
        <circle cx="32" cy="32" r="28" fill="#f1f5f9"/>
        <path d="M20 40V28a2 2 0 012-2h6l2 2h12a2 2 0 012 2v10a2 2 0 01-2 2H22a2 2 0 01-2-2z" stroke="#94a3b8" stroke-width="2" fill="none"/>
      </svg>
      <h3>暂无项目</h3>
      <p>创建您的第一个项目开始质量监控</p>
      <button class="primary-btn" @click="showCreateDialog = true">创建项目</button>
    </div>

    <div v-else class="project-grid">
      <div
        v-for="project in projects"
        :key="project.id"
        class="project-card"
        @click="router.push(`/project/${project.id}`)"
      >
        <div class="project-card-header">
          <div class="project-avatar">{{ getInitials(project.name) }}</div>
          <div class="project-meta">
            <h3 class="project-name">{{ project.name }}</h3>
            <span class="project-date">{{ formatDate(project.createTime) }}</span>
          </div>
        </div>
        <p class="project-desc">{{ project.description || '暂无描述' }}</p>
        <div class="project-stats">
          <div class="project-stat">
            <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/></svg>
            <span>{{ project.fileCount || 0 }} 个文件</span>
          </div>
        </div>
        <div class="project-actions" @click.stop>
          <button class="action-btn primary" @click="router.push(`/project/${project.id}`)">查看详情</button>
          <button class="action-btn danger" @click="handleDelete(project.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建项目" width="480px" :close-on-click-modal="false" border-radius="16px">
      <el-form :model="createForm" label-width="88px" style="padding: 8px 0">
        <el-form-item label="项目名称">
          <el-input v-model="createForm.name" placeholder="请输入项目名称" size="large" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入项目描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="showCreateDialog = false">取消</button>
          <button class="primary-btn" :disabled="creating" @click="handleCreate">
            <span class="btn-spinner" v-if="creating"></span>
            {{ creating ? '创建中...' : '确认创建' }}
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
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 20px; font-weight: 800; color: #1e293b; margin: 0;
}

.primary-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; background: linear-gradient(135deg, #4f8cff, #a259ff);
  border: none; border-radius: 10px; color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
}

.primary-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.primary-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.loading-grid, .project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 180px; background: #f1f5f9; border-radius: 14px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 80px 20px; gap: 12px; text-align: center;
  background: #fff; border-radius: 16px; border: 2px dashed #e2e8f0;
}

.empty-state h3 { font-size: 16px; font-weight: 700; color: #334155; margin: 0; }
.empty-state p { font-size: 14px; color: #94a3b8; margin: 0 0 8px 0; }

.project-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #f0f4f8;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.2s;
  display: flex; flex-direction: column; gap: 12px;
}

.project-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(79,140,255,0.1);
  border-color: rgba(79,140,255,0.3);
}

.project-card-header { display: flex; align-items: center; gap: 12px; }

.project-avatar {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(135deg, #4f8cff, #a259ff);
  color: #fff; font-size: 14px; font-weight: 800;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

.project-meta { flex: 1; min-width: 0; }

.project-name {
  font-size: 15px; font-weight: 700; color: #1e293b;
  margin: 0 0 3px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.project-date { font-size: 12px; color: #94a3b8; }

.project-desc {
  font-size: 13px; color: #64748b; line-height: 1.5; margin: 0;
  overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}

.project-stats { display: flex; gap: 16px; }

.project-stat {
  display: flex; align-items: center; gap: 5px;
  font-size: 12px; color: #94a3b8;
}

.project-actions {
  display: flex; gap: 8px; margin-top: 4px;
}

.action-btn {
  flex: 1; padding: 8px 0; border-radius: 8px; border: none;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}

.action-btn.primary { background: #eff6ff; color: #3b82f6; }
.action-btn.primary:hover { background: #3b82f6; color: #fff; }
.action-btn.danger { background: #fef2f2; color: #ef4444; }
.action-btn.danger:hover { background: #ef4444; color: #fff; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }

.cancel-btn {
  padding: 9px 18px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff;
  color: #64748b; font-size: 14px; cursor: pointer; transition: all 0.2s;
}

.cancel-btn:hover { background: #f8fafc; border-color: #cbd5e1; }

.btn-spinner {
  width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite;
  display: inline-block;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
