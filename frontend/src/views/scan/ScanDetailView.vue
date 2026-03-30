<template>
  <div class="terminal-page">
    <div class="page-header">
      <div class="header-left">
        <button class="brutal-back-btn" @click="router.back()">
          <span class="arrow"><</span> BACK
        </button>
        <div class="header-title-group">
          <h2 class="display-title">SCAN_METRICS</h2>
          <span class="task-id-badge">TASK_#{{ taskId }}</span>
          <span class="blinking-cursor">_</span>
        </div>
      </div>
    </div>

    <div class="summary-bento" v-if="task">
      <div class="brutal-stat-block">
        <span class="stat-label">PROCESS_STATUS</span>
        <span class="brutal-badge" :class="task.status.toLowerCase()">
          <span class="dot"></span>
          {{ statusLabel(task.status) }}
        </span>
      </div>
      
      <div class="brutal-stat-block">
        <span class="stat-label">FILES_SCANNED</span>
        <span class="stat-val">{{ task.fileCount || 0 }}</span>
      </div>
      
      <div class="brutal-stat-block highlight-block">
        <span class="stat-label">ISSUES_FOUND</span>
        <span class="stat-val text-accent">{{ task.issueCount || 0 }}</span>
      </div>
    </div>

    <div class="brutal-panel">
      <div class="panel-header">
        <h3 class="panel-title">FILE_TELEMETRY_DATA</h3>
        <div class="brutal-search-wrap">
          <span class="search-prefix">FILTER></span>
          <input v-model="searchText" class="brutal-search-input" placeholder="FILENAME..." />
        </div>
      </div>

      <div v-if="loading" class="terminal-loading">
        <div v-for="i in 5" :key="i" class="brutal-skeleton-row"></div>
      </div>

      <div v-else-if="filteredMetrics.length === 0" class="terminal-empty">
        <span class="blink">_</span> NO_METRICS_AVAILABLE.
      </div>

      <div v-else class="brutal-metrics-table">
        <div class="metrics-head">
          <div class="col-file">FILE_PATH</div>
          <div class="col-num">LINES_OF_CODE</div>
          <div class="col-num">METHODS</div>
          <div class="col-complex">MAX_CYCLOMATIC_COMPLEXITY</div>
        </div>
        
        <div
          v-for="row in filteredMetrics"
          :key="row.id"
          class="metrics-row"
        >
          <div class="col-file file-name">
            <span class="file-prefix">></span>
            {{ row.fileName }}
          </div>
          <div class="col-num">{{ row.lineCount }}</div>
          <div class="col-num">{{ row.methodCount }}</div>
          <div class="col-complex">
            <span class="brutal-complexity-badge" :class="complexityClass(row.maxCyclomaticComplexity)">
              [{{ row.maxCyclomaticComplexity }}]
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTask, getMetrics } from '../../api/scan'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.taskId)
const task = ref<any>(null)
const metrics = ref<any[]>([])
const loading = ref(false)
const searchText = ref('')

const filteredMetrics = computed(() => {
  if (!searchText.value) return metrics.value
  return metrics.value.filter(m => m.fileName?.toLowerCase().includes(searchText.value.toLowerCase()))
})

onMounted(async () => {
  loading.value = true
  try {
    const [taskRes, metricsRes] = await Promise.all([getTask(taskId), getMetrics(taskId)]) as any[]
    task.value = taskRes.data
    metrics.value = metricsRes.data
  } finally {
    loading.value = false
  }
})

function statusLabel(s: string) {
  return { PENDING: 'PENDING', RUNNING: 'EXECUTING', COMPLETED: 'SUCCESS', FAILED: 'FAILED' }[s] || s
}

function complexityClass(val: number) {
  if (val > 10) return 'danger'
  if (val > 5) return 'warning'
  return 'success'
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

  display: flex; flex-direction: column; gap: 2rem;
  font-family: 'Space Mono', monospace; color: var(--clr-text-main);
}

.page-header {
  border-bottom: 2px solid var(--clr-border); padding-bottom: 1.5rem;
}

.header-left { display: flex; flex-direction: column; gap: 0.75rem; }

.brutal-back-btn {
  background: transparent; border: none; color: var(--clr-text-muted);
  font-family: 'Space Mono', monospace; font-weight: 700; cursor: pointer;
  display: flex; align-items: center; gap: 8px; transition: color 0.2s;
  align-self: flex-start;
}
.brutal-back-btn:hover { color: var(--clr-text-main); }
.brutal-back-btn .arrow { color: var(--clr-accent); }

.header-title-group { display: flex; align-items: center; gap: 12px; }

.display-title {
  font-family: 'Clash Display', sans-serif; font-size: 2rem; font-weight: 700;
  color: var(--clr-text-main); margin: 0; letter-spacing: 0.05em;
}

.task-id-badge {
  background: var(--clr-text-main); color: var(--bg-dark);
  padding: 2px 8px; font-weight: 700; font-size: 0.85rem;
}

.blinking-cursor { font-size: 2rem; color: var(--clr-accent); animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }

/* 数据快照 Bento */
.summary-bento {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem;
}

.brutal-stat-block {
  background: var(--bg-panel); border: 1px solid var(--clr-border);
  padding: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem;
  position: relative;
}
.brutal-stat-block::after {
  content: ''; position: absolute; top: 0; left: 0; width: 3px; height: 100%;
  background: var(--clr-text-muted);
}
.highlight-block::after { background: var(--clr-accent); }

.stat-label { font-size: 0.75rem; color: var(--clr-text-muted); font-weight: 700; }
.stat-val { font-family: 'Clash Display', sans-serif; font-size: 2.5rem; font-weight: 700; line-height: 1; }
.text-accent { color: var(--clr-accent); text-shadow: 0 0 15px rgba(204,255,0,0.3); }

.brutal-badge {
  display: inline-flex; align-items: center; gap: 8px; width: fit-content;
  padding: 4px 10px; border: 1px solid currentColor; font-size: 0.85rem; font-weight: 700;
}
.dot { width: 8px; height: 8px; background: currentColor; }
.brutal-badge.pending { color: var(--clr-text-muted); }
.brutal-badge.running { color: var(--clr-warning); animation: pulse-border 1.5s infinite; }
.brutal-badge.completed { color: var(--clr-success); }
.brutal-badge.failed { color: var(--clr-danger); }

@keyframes pulse-border { 0%, 100% { border-color: rgba(255, 170, 0, 0.3); } 50% { border-color: var(--clr-warning); } }

/* 数据网格面板 */
.brutal-panel {
  background: var(--bg-panel); border: 1px solid var(--clr-border);
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5); overflow-x: auto; min-height: 400px;
}

.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1rem 1.5rem; border-bottom: 1px solid var(--clr-border); background: var(--bg-dark);
}

.panel-title { font-size: 0.9rem; font-weight: 700; color: var(--clr-text-main); margin: 0; }

.brutal-search-wrap {
  display: flex; align-items: center; background: var(--bg-card);
  border: 1px solid var(--clr-border); transition: all 0.2s ease;
}
.brutal-search-wrap:focus-within { border-color: var(--clr-accent); }
.search-prefix { padding: 0 0.75rem; font-size: 0.75rem; color: var(--clr-accent); font-weight: 700; border-right: 1px solid var(--clr-border); }
.brutal-search-input { border: none; outline: none; background: transparent; color: var(--clr-text-main); font-family: 'Space Mono', monospace; font-size: 0.75rem; padding: 0.4rem 0.75rem; width: 220px; }

.terminal-loading { padding: 1.5rem; display: flex; flex-direction: column; gap: 0.5rem; }
.brutal-skeleton-row { height: 45px; background: var(--bg-card); opacity: 0.5; animation: pulse-slow 2s infinite; }

.terminal-empty { padding: 4rem; text-align: center; color: var(--clr-text-muted); font-size: 0.85rem; }

.brutal-metrics-table { min-width: 800px; display: flex; flex-direction: column; }

.metrics-head {
  display: grid; grid-template-columns: 1fr 140px 120px 220px;
  padding: 1rem 1.5rem; font-size: 0.75rem; font-weight: 700;
  color: var(--clr-accent); border-bottom: 1px solid var(--clr-border);
}

.metrics-row {
  display: grid; grid-template-columns: 1fr 140px 120px 220px;
  padding: 1rem 1.5rem; font-size: 0.85rem; border-bottom: 1px dashed var(--clr-border);
  transition: background 0.2s; align-items: center;
}
.metrics-row:hover { background: var(--bg-card); }
.metrics-row:last-child { border-bottom: none; }

.col-num { color: var(--clr-text-main); font-weight: 700; text-align: right; padding-right: 2rem; }
.col-complex { text-align: center; }

.file-name {
  display: flex; align-items: center; gap: 10px; color: var(--clr-text-muted);
  word-break: break-all;
}
.file-prefix { color: var(--clr-accent); font-weight: 700; }
.metrics-row:hover .file-name { color: var(--clr-text-main); }

.brutal-complexity-badge { font-weight: 700; font-size: 0.85rem; }
.brutal-complexity-badge.danger { color: var(--clr-danger); }
.brutal-complexity-badge.warning { color: var(--clr-warning); }
.brutal-complexity-badge.success { color: var(--clr-success); }
</style>