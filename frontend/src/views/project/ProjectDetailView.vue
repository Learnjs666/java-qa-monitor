<template>
  <div class="terminal-page">
    <div class="brutal-header">
      <div class="header-left">
        <button class="brutal-back-btn" @click="router.back()">
          <span class="arrow"><</span> 返回
        </button>
        <div class="target-info">
          <span class="target-label">项目名称 //</span>
          <h2 class="display-title">{{ project?.name || 'AWAITING_DATA' }}</h2>
        </div>
      </div>
      
      <button class="brutal-btn primary" @click="handleScan" :disabled="scanning">
        <span class="btn-spinner" v-if="scanning"></span>
        <span class="btn-text">{{ scanning ? '扫描中...' : '开始扫描' }}</span>
        <span class="btn-icon" v-if="!scanning">></span>
      </button>
    </div>

    <div class="info-bento">
      <div class="brutal-info-card" v-for="item in projectInfoItems" :key="item.label">
        <div class="card-top">
          <span class="info-icon" v-html="item.icon"></span>
          <span class="info-label">[ {{ item.label }} ]</span>
        </div>
        <div class="info-value">{{ item.value }}</div>
      </div>
    </div>

    <div class="brutal-section">
      <div class="section-top-bar">
        <span class="bar-title">模块：文件上传</span>
        <span class="bar-status">等待输入</span>
      </div>
      <div class="section-content">
        <p class="hint-text">支持的文件类型: .JAVA | .ZIP</p>
        <FileUpload :project-id="projectId" @uploaded="loadProject" />
      </div>
    </div>

    <div class="brutal-section">
      <div class="section-top-bar">
        <span class="bar-title">模块：扫描遥测</span>
        <button class="brutal-text-btn" @click="loadTasks" title="REFRESH">
          [ 刷新日志列表 ]
        </button>
      </div>

      <div class="section-content no-padding">
        <div v-if="tasksLoading" class="log-rows-container">
          <div v-for="i in 4" :key="i" class="log-skeleton"></div>
        </div>

        <div v-else-if="tasks.length === 0" class="log-empty">
          <span class="blink">_</span> 暂无扫描任务记录，点击上方按钮开始第一次扫描
        </div>

        <div v-else class="log-list">
          <div v-for="task in tasks" :key="task.id" class="log-row">
            <div class="log-col id-col">#{{ String(task.id).padStart(4, '0') }}</div>
            
            <div class="log-col status-col">
              <span class="brutal-badge" :class="task.status.toLowerCase()">
                <span class="dot"></span>
                {{ statusLabel(task.status) }}
              </span>
            </div>

            <div class="log-col meta-col">
              <div class="meta-item"><span>FILES:</span> {{ task.fileCount || 0 }}</div>
              <div class="meta-item"><span>ISSUES:</span> <span :class="{'text-danger': task.issueCount > 0}">{{ task.issueCount || 0 }}</span></div>
              <div class="meta-item"><span>TS:</span> {{ formatDate(task.createTime) }}</div>
            </div>

            <div class="log-col action-col">
              <button class="brutal-action-link" @click="router.push(`/scan/${task.id}`)">查看详情</button>
              <button class="brutal-action-link text-warning" @click="router.push(`/issues/${task.id}`)">调试问题</button>
            </div>
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
  return { PENDING: 'PNDG', RUNNING: 'EXEC', COMPLETED: 'DONE', FAILED: 'FAIL' }[s] || s
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleString('zh-CN') : '-'
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
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;

  display: flex;
  flex-direction: column;
  gap: 2rem;
  font-family: 'Space Mono', monospace;
  color: var(--clr-text-main);
}

.brutal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid var(--clr-border);
  padding-bottom: 1.5rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.brutal-back-btn {
  background: transparent;
  border: none;
  color: var(--clr-text-muted);
  font-family: 'Space Mono', monospace;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.2s;
}

.brutal-back-btn:hover { color: var(--clr-text-main); }
.brutal-back-btn .arrow { color: var(--clr-accent); }

.target-info {
  display: flex;
  flex-direction: column;
}

.target-label {
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  font-weight: 700;
}

.display-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: var(--clr-text-main);
  margin: 0;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

/* 按钮样式复用 */
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

.brutal-btn:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }

.btn-spinner {
  width: 14px; height: 14px; border: 2px solid var(--bg-dark);
  border-top-color: transparent; border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 信息矩阵 */
.info-bento {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.brutal-info-card {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  position: relative;
}

.brutal-info-card::after {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 3px; height: 100%;
  background: var(--clr-text-muted);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-icon {
  color: var(--clr-text-muted);
  display: flex;
  align-items: center;
}
.info-icon :deep(svg) { width: 16px; height: 16px; }

.info-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--clr-text-muted);
}

.info-value {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--clr-text-main);
  word-break: break-all;
  padding-left: 26px;
}

/* 模块容器 */
.brutal-section {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  display: flex;
  flex-direction: column;
  box-shadow: 8px 8px 0px rgba(0,0,0,0.3);
}

.section-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1.25rem;
  background: var(--bg-dark);
  border-bottom: 1px solid var(--clr-border);
}

.bar-title { font-weight: 700; font-size: 0.85rem; }
.bar-status { color: var(--clr-text-muted); font-size: 0.75rem; }

.brutal-text-btn {
  background: transparent;
  border: none;
  color: var(--clr-accent);
  font-family: 'Space Mono', monospace;
  font-weight: 700;
  font-size: 0.75rem;
  cursor: pointer;
}
.brutal-text-btn:hover { color: var(--clr-text-main); }

.section-content { padding: 1.5rem; }
.section-content.no-padding { padding: 0; }

.hint-text {
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  margin: 0 0 1rem 0;
}

/* 终端日志列表 */
.log-empty {
  padding: 3rem;
  text-align: center;
  color: var(--clr-text-muted);
  font-size: 0.85rem;
  background: repeating-linear-gradient(0deg, rgba(0,0,0,0.2), rgba(0,0,0,0.2) 1px, transparent 1px, transparent 2px);
}
.blink { animation: blink 1s step-end infinite; }

.log-list { display: flex; flex-direction: column; }

.log-row {
  display: flex;
  align-items: center;
  border-bottom: 1px dashed var(--clr-border);
  padding: 1rem 1.5rem;
  transition: background 0.2s;
}
.log-row:hover { background: var(--bg-card); }
.log-row:last-child { border-bottom: none; }

.log-col { flex-shrink: 0; }

.id-col { width: 80px; color: var(--clr-text-muted); font-weight: 700; }

.status-col { width: 120px; }
.brutal-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 2px 8px; border: 1px solid currentColor;
  font-size: 0.7rem; font-weight: 700;
}
.dot { width: 6px; height: 6px; background: currentColor; }

.brutal-badge.pending { color: var(--clr-text-muted); }
.brutal-badge.running { color: var(--clr-warning); animation: pulse-border 1.5s infinite; }
.brutal-badge.completed { color: var(--clr-success); }
.brutal-badge.failed { color: var(--clr-danger); }

@keyframes pulse-border { 0%, 100% { border-color: rgba(255, 170, 0, 0.3); } 50% { border-color: var(--clr-warning); } }

.meta-col {
  flex: 1; display: flex; gap: 2rem; font-size: 0.8rem; color: var(--clr-text-main);
}
.meta-item span:first-child { color: var(--clr-text-muted); margin-right: 4px; }
.text-danger { color: var(--clr-danger); font-weight: 700; }

.action-col { display: flex; gap: 1.5rem; }

.brutal-action-link {
  background: transparent; border: none; font-family: 'Space Mono', monospace;
  font-size: 0.75rem; font-weight: 700; cursor: pointer; color: var(--clr-text-muted);
  text-decoration: underline; text-underline-offset: 4px;
}
.brutal-action-link:hover { color: var(--clr-text-main); }
.brutal-action-link.text-warning:hover { color: var(--clr-warning); }

/* 骨架屏 */
.log-rows-container { padding: 1rem; display: flex; flex-direction: column; gap: 1rem; }
.log-skeleton { height: 40px; background: var(--bg-card); opacity: 0.5; animation: pulse-slow 2s infinite; }
</style>