<template>
  <div class="sq-page">
    <!-- 面包屑与顶部头 -->
    <div class="sq-page-header">
      <div class="header-left">
        <button class="sq-back-btn" @click="router.back()">
          <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/></svg>
          返回项目列表
        </button>
        <h2 class="sq-page-title">{{ project?.name || '项目详情加载中...' }}</h2>
      </div>
      
      <button class="sq-btn primary" @click="handleScan" :disabled="scanning">
        <span class="btn-spinner" v-if="scanning"></span>
        <svg v-else viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
          <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd"/>
        </svg>
        执行代码扫描
      </button>
    </div>

    <!-- 项目基本信息（轻量化横幅） -->
    <div class="sq-info-banner">
      <div class="info-item">
        <span class="info-label">项目描述</span>
        <span class="info-value">{{ project?.description || '无' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">解析文件数</span>
        <span class="info-value">{{ project?.fileCount || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">创建日期</span>
        <span class="info-value">{{ formatDate(project?.createTime) }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">存储源路径</span>
        <span class="info-value code-font">{{ project?.sourcePath || '暂未初始化' }}</span>
      </div>
    </div>

    <div class="sq-two-col-layout">
      <!-- 左侧主栏：扫描记录 -->
      <div class="sq-main-col">
        <div class="sq-panel">
          <div class="panel-header">
            <h3 class="panel-title">分析执行历史</h3>
            <button class="sq-btn-text" @click="loadTasks" title="刷新记录">刷新数据</button>
          </div>

          <div class="panel-body no-padding">
            <div v-if="tasksLoading" class="sq-loading-placeholder">
              <div v-for="i in 3" :key="i" class="sq-skeleton-row"></div>
            </div>

            <div v-else-if="tasks.length === 0" class="sq-empty-state">
              <p>该项目目前没有任何活动。</p>
              <p>请上传源码并点击上方「执行代码扫描」按钮。</p>
            </div>

            <div v-else class="sq-task-table">
              <div class="table-header">
                <div class="col-id">记录 ID</div>
                <div class="col-status">执行状态</div>
                <div class="col-metrics">概览指标</div>
                <div class="col-time">触发时间</div>
                <div class="col-actions"></div>
              </div>
              
              <div v-for="task in tasks" :key="task.id" class="table-row">
                <div class="col-id">#{{ task.id }}</div>
                <div class="col-status">
                  <span class="sq-status-badge" :class="task.status.toLowerCase()">
                    {{ statusLabel(task.status) }}
                  </span>
                </div>
                <div class="col-metrics">
                  <span>{{ task.fileCount || 0 }} 文件</span>
                  <span class="divider">/</span>
                  <span :class="{'text-red': task.issueCount > 0}">{{ task.issueCount || 0 }} 问题</span>
                </div>
                <div class="col-time">{{ formatDate(task.createTime) }}</div>
                <div class="col-actions">
                  <a class="sq-link" @click="router.push(`/scan/${task.id}`)">详细指标</a>
                  <span class="divider">|</span>
                  <a class="sq-link text-orange" @click="router.push(`/issues/${task.id}`)">审查问题</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧侧边栏：上传组件 -->
      <div class="sq-side-col">
        <div class="sq-panel">
          <div class="panel-header">
            <h3 class="panel-title">源码更新</h3>
          </div>
          <div class="panel-body">
            <p class="hint-text">请上传 `.java` 源码文件或打包的 `.zip` 文件以供分析引擎读取。</p>
            <!-- 假设组件内部样式已被重置，或自然融入 -->
            <FileUpload :project-id="projectId" @uploaded="loadProject" />
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
    ElMessage.success('扫描任务已下发执行队列')
    setTimeout(() => loadTasks(), 2000)
  } finally {
    scanning.value = false
  }
}

function statusLabel(s: string) {
  return { PENDING: '队列中', RUNNING: '分析中', COMPLETED: '成功', FAILED: '失败' }[s] || s
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
}
</script>

<style scoped>
.sq-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sq-page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #222222;
}

.sq-back-btn {
  display: flex; align-items: center; gap: 4px;
  background: transparent; border: none;
  font-size: 13px; color: #555555; cursor: pointer; padding: 0;
}
.sq-back-btn:hover { color: #0271b6; text-decoration: underline; }

.sq-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: 3px;
  font-size: 13px; font-weight: 600; cursor: pointer; border: 1px solid transparent;
}
.sq-btn.primary { background-color: #0271b6; color: #ffffff; }
.sq-btn.primary:hover:not(:disabled) { background-color: #005a92; }
.sq-btn.primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-spinner { width: 12px; height: 12px; border: 2px solid rgba(255,255,255,0.4); border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 信息横幅 */
.sq-info-banner {
  display: flex;
  background-color: #f9f9fb;
  border: 1px solid #e1e6eb;
  border-radius: 2px;
  padding: 16px 20px;
  gap: 40px;
  flex-wrap: wrap;
}

.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: #777777; }
.info-value { font-size: 14px; font-weight: 500; color: #333333; }
.code-font { font-family: Consolas, Monaco, monospace; font-size: 13px; background: #eef2f5; padding: 2px 6px; border-radius: 2px; }

/* 左右分栏布局 */
.sq-two-col-layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.sq-main-col { flex: 1; min-width: 0; }
.sq-side-col { width: 320px; flex-shrink: 0; }

@media (max-width: 1000px) {
  .sq-two-col-layout { flex-direction: column; }
  .sq-side-col { width: 100%; }
}

.sq-panel {
  background: #ffffff;
  border: 1px solid #e1e6eb;
  border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.panel-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; border-bottom: 1px solid #e1e6eb; background-color: #fafbfc;
}
.panel-title { font-size: 14px; font-weight: 600; color: #333333; margin: 0; }

.panel-body { padding: 16px; }
.panel-body.no-padding { padding: 0; }

.hint-text { font-size: 13px; color: #666; margin: 0 0 16px 0; line-height: 1.5; }

.sq-btn-text { background: transparent; border: none; font-size: 12px; color: #0271b6; cursor: pointer; padding: 0; }
.sq-btn-text:hover { text-decoration: underline; }

/* 数据表格 */
.sq-task-table { display: flex; flex-direction: column; }

.table-header {
  display: flex; padding: 10px 16px; background-color: #ffffff;
  border-bottom: 1px solid #e1e6eb; font-size: 12px; font-weight: 600; color: #777777;
}

.table-row {
  display: flex; align-items: center; padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6; font-size: 13px;
}
.table-row:last-child { border-bottom: none; }
.table-row:hover { background-color: #f9f9fb; }

.col-id { width: 80px; color: #777; font-family: Consolas, monospace; }
.col-status { width: 100px; }
.col-metrics { flex: 1; display: flex; gap: 8px; }
.col-time { width: 160px; color: #555; }
.col-actions { width: 140px; text-align: right; }

.divider { color: #cccccc; margin: 0 4px; }
.text-red { color: #d4333f; font-weight: 600; }
.text-orange { color: #ed7d20 !important; }

/* 状态 Badge (SonarQube 风格) */
.sq-status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 2px;
  font-size: 11px; font-weight: 600; text-transform: uppercase;
  border: 1px solid transparent;
}
.sq-status-badge.pending { background-color: #f3f4f6; color: #555; border-color: #d1d5db; }
.sq-status-badge.running { background-color: #fef0cd; color: #d47e00; border-color: #fcdca3; }
.sq-status-badge.completed { background-color: #e5f6e5; color: #00aa00; border-color: #bce1bc; }
.sq-status-badge.failed { background-color: #fae1e3; color: #d4333f; border-color: #f2c2c5; }

.sq-link { color: #0271b6; cursor: pointer; text-decoration: none; }
.sq-link:hover { text-decoration: underline; }

.sq-loading-placeholder { padding: 16px; display: flex; flex-direction: column; gap: 8px; }
.sq-skeleton-row { height: 36px; background-color: #f3f4f6; border-radius: 2px; animation: pulse 1.5s infinite; }

.sq-empty-state {
  padding: 40px; text-align: center; color: #777777; font-size: 13px;
}
.sq-empty-state p { margin: 4px 0; }
</style>