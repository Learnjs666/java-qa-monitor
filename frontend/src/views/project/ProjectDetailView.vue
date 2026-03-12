<template>
  <div class="page">
    <!-- Back Button & Header -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/>
        </svg>
        返回
      </button>
      <h2 class="page-title">{{ project?.name || '项目详情' }}</h2>
      <button class="primary-btn" @click="handleScan" :disabled="scanning">
        <span class="btn-spinner" v-if="scanning"></span>
        <svg v-else viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/>
        </svg>
        {{ scanning ? '扫描中...' : '触发扫描' }}
      </button>
    </div>

    <!-- Project Info -->
    <div class="info-grid">
      <div class="info-card" v-for="item in projectInfoItems" :key="item.label">
        <div class="info-icon" v-html="item.icon"></div>
        <div>
          <div class="info-label">{{ item.label }}</div>
          <div class="info-value">{{ item.value }}</div>
        </div>
      </div>
    </div>

    <!-- File Upload -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">上传文件</h3>
        <span class="hint-text">支持 .java 文件和 .zip 压缩包</span>
      </div>
      <FileUpload :project-id="projectId" @uploaded="loadProject" />
    </div>

    <!-- Scan Tasks -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">扫描记录</h3>
        <button class="icon-btn" @click="loadTasks" title="刷新">
          <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
            <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/>
          </svg>
          刷新
        </button>
      </div>

      <div v-if="tasksLoading" class="loading-rows">
        <div v-for="i in 4" :key="i" class="skeleton-row"></div>
      </div>

      <div v-else-if="tasks.length === 0" class="empty-state">
        <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
          <circle cx="24" cy="24" r="20" fill="#f1f5f9"/>
          <path d="M16 32V20a2 2 0 012-2h12a2 2 0 012 2v12" stroke="#94a3b8" stroke-width="2" fill="none"/>
          <path d="M12 32h24" stroke="#94a3b8" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>暂无扫描记录，点击「触发扫描」开始</span>
      </div>

      <div v-else class="task-list">
        <div v-for="task in tasks" :key="task.id" class="task-row">
          <div class="task-id">#{{ task.id }}</div>
          <div class="task-status">
            <span class="status-badge" :class="task.status.toLowerCase()">
              <span class="status-dot"></span>
              {{ statusLabel(task.status) }}
            </span>
          </div>
          <div class="task-info">
            <span>{{ task.fileCount || 0 }} 文件</span>
            <span class="divider">·</span>
            <span>{{ task.issueCount || 0 }} 问题</span>
            <span class="divider">·</span>
            <span>{{ formatDate(task.createTime) }}</span>
          </div>
          <div class="task-actions">
            <button class="task-btn blue" @click="router.push(`/scan/${task.id}`)">查看指标</button>
            <button class="task-btn orange" @click="router.push(`/issues/${task.id}`)">查看问题</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProject } from '../../api/project'
import { runScan, getTaskList } from '../../api/scan'
import { ElMessage } from 'element-plus'
import FileUpload from '../../components/FileUpload.vue'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)
const project = ref<any>(null)
const tasks = ref<any[]>([])
const tasksLoading = ref(false)
const scanning = ref(false)

const projectInfoItems = computed(() => [
  {
    label: '项目描述',
    value: project.value?.description || '-',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd"/></svg>`
  },
  {
    label: '文件数量',
    value: `${project.value?.fileCount || 0} 个`,
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/></svg>`
  },
  {
    label: '存储路径',
    value: project.value?.sourcePath || '-',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M12.586 4.586a2 2 0 112.828 2.828l-3 3a2 2 0 01-2.828 0 1 1 0 00-1.414 1.414 4 4 0 005.656 0l3-3a4 4 0 00-5.656-5.656l-1.5 1.5a1 1 0 101.414 1.414l1.5-1.5zm-5 5a2 2 0 012.828 0 1 1 0 101.414-1.414 4 4 0 00-5.656 0l-3 3a4 4 0 105.656 5.656l1.5-1.5a1 1 0 10-1.414-1.414l-1.5 1.5a2 2 0 11-2.828-2.828l3-3z" clip-rule="evenodd"/></svg>`
  },
  {
    label: '创建时间',
    value: formatDate(project.value?.createTime),
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/></svg>`
  }
])

onMounted(() => { loadProject(); loadTasks() })

async function loadProject() {
  const res: any = await getProject(projectId)
  project.value = res.data
}

async function loadTasks() {
  tasksLoading.value = true
  try {
    const res: any = await getTaskList(projectId)
    tasks.value = res.data
  } finally {
    tasksLoading.value = false
  }
}

async function handleScan() {
  scanning.value = true
  try {
    await runScan(projectId)
    ElMessage.success('扫描任务已创建，请稍后刷新查看结果')
    setTimeout(() => loadTasks(), 2000)
  } finally {
    scanning.value = false
  }
}

function statusLabel(s: string) {
  return { PENDING: '等待中', RUNNING: '扫描中', COMPLETED: '已完成', FAILED: '失败' }[s] || s
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex; align-items: center; gap: 16px;
}

.page-title { font-size: 20px; font-weight: 800; color: #1e293b; margin: 0; flex: 1; }

.back-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff;
  color: #64748b; font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover { background: #f8fafc; color: #334155; }

.primary-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; background: linear-gradient(135deg, #4f8cff, #a259ff);
  border: none; border-radius: 10px; color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: opacity 0.2s, transform 0.1s;
}

.primary-btn:hover:not(:disabled) { opacity: 0.9; transform: translateY(-1px); }
.primary-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white; border-radius: 50%;
  animation: spin 0.7s linear infinite; display: inline-block;
}

@keyframes spin { to { transform: rotate(360deg); } }

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.info-card {
  background: #fff; border-radius: 12px; padding: 18px;
  display: flex; align-items: flex-start; gap: 12px;
  border: 1px solid #f0f4f8; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.info-icon {
  width: 38px; height: 38px; border-radius: 10px;
  background: #eff6ff; color: #3b82f6; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}

.info-icon :deep(svg) { width: 18px; height: 18px; }

.info-label { font-size: 11px; color: #94a3b8; font-weight: 500; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
.info-value { font-size: 13px; color: #1e293b; font-weight: 600; word-break: break-all; }

.section-card {
  background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05); border: 1px solid #f0f4f8;
}

.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}

.section-title { font-size: 15px; font-weight: 700; color: #1e293b; margin: 0; }
.hint-text { font-size: 12px; color: #94a3b8; }

.icon-btn {
  display: flex; align-items: center; gap: 5px;
  padding: 7px 12px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #f8fafc;
  color: #64748b; font-size: 12px; cursor: pointer; transition: all 0.2s;
}

.icon-btn:hover { background: #eff6ff; border-color: #bfdbfe; color: #3b82f6; }

.loading-rows { display: flex; flex-direction: column; gap: 10px; }

.skeleton-row {
  height: 52px; background: #f1f5f9; border-radius: 10px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 40px; gap: 10px; color: #94a3b8; font-size: 13px;
}

.task-list { display: flex; flex-direction: column; gap: 8px; }

.task-row {
  display: flex; align-items: center; gap: 16px;
  padding: 14px 18px; border-radius: 10px;
  background: #f8fafc; border: 1px solid #f0f4f8;
  transition: all 0.2s;
}

.task-row:hover { background: #eff6ff; border-color: #bfdbfe; }

.task-id { font-size: 12px; font-weight: 600; color: #94a3b8; width: 40px; flex-shrink: 0; }

.task-status { flex-shrink: 0; }

.status-badge {
  display: flex; align-items: center; gap: 5px;
  padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}

.status-dot {
  width: 6px; height: 6px; border-radius: 50%;
}

.status-badge.pending { background: #f1f5f9; color: #64748b; }
.status-badge.pending .status-dot { background: #94a3b8; }
.status-badge.running { background: #fffbeb; color: #d97706; }
.status-badge.running .status-dot { background: #f59e0b; animation: blink 1s ease-in-out infinite; }
.status-badge.completed { background: #f0fdf4; color: #16a34a; }
.status-badge.completed .status-dot { background: #22c55e; }
.status-badge.failed { background: #fef2f2; color: #dc2626; }
.status-badge.failed .status-dot { background: #ef4444; }

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

.task-info { flex: 1; font-size: 12px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.divider { color: #cbd5e1; }

.task-actions { display: flex; gap: 8px; flex-shrink: 0; }

.task-btn {
  padding: 6px 12px; border-radius: 7px; border: none;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}

.task-btn.blue { background: #eff6ff; color: #3b82f6; }
.task-btn.blue:hover { background: #3b82f6; color: #fff; }
.task-btn.orange { background: #fffbeb; color: #d97706; }
.task-btn.orange:hover { background: #f59e0b; color: #fff; }
</style>
