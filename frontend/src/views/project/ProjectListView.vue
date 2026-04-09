<template>
  <div class="sq-page">
    <div class="sq-page-header">
      <h2 class="sq-page-title">项目管理</h2>
      <button class="sq-btn primary" @click="showCreateDialog = true">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
        </svg>
        添加新项目
      </button>
    </div>

    <!-- 加载骨架屏 -->
    <div v-if="loading" class="sq-loading-list">
      <div v-for="i in 4" :key="i" class="sq-skeleton-row"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="projects.length === 0" class="sq-empty-state">
      <div class="empty-icon">📂</div>
      <h3>您的工作区目前为空</h3>
      <p>点击右上角的按钮配置您的第一个代码审计项目。</p>
    </div>

    <!-- SonarQube 风格的纵向项目列表 -->
    <div v-else class="sq-project-list">
      <div
        v-for="project in projects"
        :key="project.id"
        class="sq-project-row"
        @click="router.push(`/project/${project.id}`)"
      >
        <div class="row-left">
          <div class="project-icon">
            {{ getInitials(project.name) }}
          </div>
          <div class="project-info">
            <h3 class="project-name">{{ project.name }}</h3>
            <p class="project-desc">{{ project.description || '无详细描述' }}</p>
          </div>
        </div>
        
        <div class="row-right">
          <div class="project-metrics">
            <div class="metric-item">
              <span class="metric-val">{{ project.fileCount || 0 }}</span>
              <span class="metric-label">受控文件</span>
            </div>
            <div class="metric-item">
              <span class="metric-val">{{ formatDate(project.createTime) }}</span>
              <span class="metric-label">接入时间</span>
            </div>
          </div>
          
          <div class="project-actions" @click.stop>
            <button class="sq-btn-text" @click="router.push(`/project/${project.id}`)">查看指标</button>
            <button class="sq-btn-text danger" @click="handleDelete(project.id)">移除配置</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建项目弹窗 -->
    <el-dialog v-model="showCreateDialog" title="创建代码审计项目" width="500px" :close-on-click-modal="false" class="sq-dialog">
      <el-form :model="createForm" label-position="top" class="sq-dialog-form">
        <el-form-item label="项目名称（唯一标识）">
          <el-input v-model="createForm.name" placeholder="例如: e-commerce-backend" class="sq-input" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="添加关于该代码库的业务描述..." class="sq-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="sq-dialog-footer">
          <button class="sq-btn default" @click="showCreateDialog = false">取消</button>
          <button class="sq-btn primary" :disabled="creating" @click="handleCreate">
            <span class="btn-spinner" v-if="creating"></span>
            {{ creating ? '正在保存...' : '确 认' }}
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
  await ElMessageBox.confirm('确定要删除该项目吗？关联的扫描记录将被一并清除。', '危险操作确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await deleteProject(id)
  ElMessage.success('删除成功')
  await loadProjects()
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleDateString('zh-CN') : '-'
}

function getInitials(name: string) {
  return name ? name.slice(0, 2).toUpperCase() : 'PR'
}
</script>

<style scoped>
.sq-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  color: #333333;
}

.sq-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e1e6eb;
  padding-bottom: 12px;
}

.sq-page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #222222;
}

.sq-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.sq-btn.primary {
  background-color: #0271b6;
  color: #ffffff;
}
.sq-btn.primary:hover:not(:disabled) { background-color: #005a92; }

.sq-btn.default {
  background-color: #ffffff;
  border-color: #cccccc;
  color: #333333;
}
.sq-btn.default:hover { border-color: #999999; background-color: #f9f9f9; }

.sq-loading-list { display: flex; flex-direction: column; gap: 12px; }
.sq-skeleton-row { height: 80px; background-color: #ffffff; border: 1px solid #e1e6eb; opacity: 0.6; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 0.6; } 50% { opacity: 0.3; } }

.sq-empty-state {
  background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px;
  padding: 60px 20px; text-align: center;
}
.empty-icon { font-size: 40px; margin-bottom: 16px; opacity: 0.5; }
.sq-empty-state h3 { font-size: 16px; margin: 0 0 8px 0; color: #333; }
.sq-empty-state p { font-size: 13px; color: #777; margin: 0; }

.sq-project-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sq-project-row {
  background: #ffffff;
  border: 1px solid #e1e6eb;
  border-radius: 2px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.sq-project-row:hover {
  border-color: #c9d1d9;
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.row-left { display: flex; align-items: center; gap: 16px; }

.project-icon {
  width: 48px; height: 48px;
  background-color: #f3f4f6;
  border: 1px solid #e1e6eb;
  color: #0271b6;
  font-size: 16px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  border-radius: 2px;
}

.project-info { display: flex; flex-direction: column; gap: 4px; }

.project-name {
  font-size: 16px; font-weight: 600; color: #0271b6; margin: 0;
}
.sq-project-row:hover .project-name { text-decoration: underline; }

.project-desc {
  font-size: 13px; color: #777777; margin: 0;
  max-width: 400px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.row-right { display: flex; align-items: center; gap: 40px; }

.project-metrics { display: flex; gap: 32px; }

.metric-item { display: flex; flex-direction: column; align-items: flex-end; }
.metric-val { font-size: 16px; font-weight: 600; color: #333333; }
.metric-label { font-size: 12px; color: #777777; margin-top: 2px; }

.project-actions { display: flex; gap: 16px; border-left: 1px solid #e1e6eb; padding-left: 24px; }

.sq-btn-text {
  background: transparent; border: none; padding: 0;
  font-size: 13px; font-weight: 500; color: #0271b6; cursor: pointer;
}
.sq-btn-text:hover { text-decoration: underline; }
.sq-btn-text.danger { color: #d4333f; }

/* Dialog Styles */
:deep(.el-dialog) { border-radius: 4px; }
:deep(.el-dialog__header) { border-bottom: 1px solid #e1e6eb; padding: 16px 20px; margin: 0; }
:deep(.el-dialog__title) { font-size: 16px; font-weight: 600; color: #333; }
:deep(.el-dialog__body) { padding: 24px 20px; }
:deep(.el-dialog__footer) { border-top: 1px solid #e1e6eb; padding: 16px 20px; }

.sq-dialog-form :deep(.el-form-item__label) { padding-bottom: 4px; line-height: 1.2; font-size: 13px; font-weight: 600; color: #444; }

.sq-input :deep(.el-input__wrapper), .sq-input :deep(.el-textarea__inner) {
  border-radius: 3px !important; box-shadow: none !important; border: 1px solid #cccccc !important;
}
.sq-input :deep(.el-input__wrapper.is-focus), .sq-input :deep(.el-textarea__inner:focus) {
  border-color: #0271b6 !important;
}

.sq-dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.btn-spinner { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; display: inline-block; }
</style>