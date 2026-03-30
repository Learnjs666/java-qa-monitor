<template>
  <div class="terminal-page">
    <div class="page-header">
      <div class="header-left">
        <button class="brutal-back-btn" @click="router.back()">
          <span class="arrow"><</span> BACK
        </button>
        <div class="header-title-group">
          <h2 class="display-title">ISSUE_TRACKER</h2>
          <span class="task-id-badge">TASK_#{{ taskId }}</span>
          <span class="blinking-cursor">_</span>
        </div>
      </div>

      <div class="brutal-filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="brutal-filter-tab"
          :class="[{ active: activeFilter === tab.value }, tab.value.toLowerCase()]"
          @click="activeFilter = tab.value"
        >
          <span class="tab-label">[ {{ tab.value === 'ALL' ? 'GLOBAL' : tab.label }} ]</span>
          <span class="tab-count">{{ tabCount(tab.value) }}</span>
        </button>
      </div>
    </div>

    <div class="toolbar">
      <div class="brutal-search-wrap">
        <span class="search-prefix">QUERY></span>
        <input v-model="searchText" class="brutal-search-input" placeholder="INPUT_FILE_OR_DESC..." />
      </div>
      
      <div class="stats-row">
        <span class="brutal-stat-chip error">
          <span class="chip-label">ERRORS</span>
          <strong class="chip-val">{{ tabCount('ERROR') }}</strong>
        </span>
        <span class="brutal-stat-chip warning">
          <span class="chip-label">WARNINGS</span>
          <strong class="chip-val">{{ tabCount('WARNING') }}</strong>
        </span>
        <span class="brutal-stat-chip info">
          <span class="chip-label">INFO</span>
          <strong class="chip-val">{{ tabCount('INFO') }}</strong>
        </span>
      </div>
    </div>

    <div class="brutal-panel">
      <div v-if="loading" class="terminal-loading">
        <div v-for="i in 6" :key="i" class="brutal-skeleton-row"></div>
      </div>

      <div v-else-if="filteredIssues.length === 0" class="terminal-empty">
        <div class="empty-glitch">[ SYSTEM_CLEAR ]</div>
        <span>NO_ANOMALIES_DETECTED. ALL_SYSTEMS_NOMINAL.</span>
      </div>

      <div v-else class="issues-log-stream">
        <div
          v-for="issue in filteredIssues"
          :key="issue.id"
          class="brutal-issue-row"
          :class="issue.severity.toLowerCase()"
        >
          <div class="issue-severity">
            <span class="severity-square" :class="issue.severity.toLowerCase()">
              {{ issue.severity === 'ERROR' ? 'ERR' : issue.severity === 'WARNING' ? 'WRN' : 'INF' }}
            </span>
          </div>

          <div class="issue-main">
            <div class="issue-top">
              <span class="issue-file">{{ issue.fileName }}</span>
              <span class="issue-line">LN: {{ issue.lineNumber }}</span>
            </div>
            <div class="issue-message">> {{ issue.message }}</div>
            <div class="issue-rule">
              <span class="rule-prefix">RULE_VIOLATION:</span> {{ issue.ruleCode }}
            </div>
          </div>

          <div class="issue-badge">
            <span class="brutal-severity-badge" :class="issue.severity.toLowerCase()">
              {{ severityLabel(issue.severity) }}
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
import { getIssues } from '../../api/scan'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.taskId)
const issues = ref<any[]>([])
const loading = ref(false)
const searchText = ref('')
const activeFilter = ref('ALL')

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '错误', value: 'ERROR' },
  { label: '警告', value: 'WARNING' },
  { label: '提示', value: 'INFO' }
]

const filteredIssues = computed(() => {
  let list = issues.value
  if (activeFilter.value !== 'ALL') list = list.filter(i => i.severity === activeFilter.value)
  if (searchText.value) {
    const q = searchText.value.toLowerCase()
    list = list.filter(i => i.fileName?.toLowerCase().includes(q) || i.message?.toLowerCase().includes(q))
  }
  return list
})

function tabCount(v: string) {
  if (v === 'ALL') return issues.value.length
  return issues.value.filter(i => i.severity === v).length
}

function severityLabel(s: string) {
  return { ERROR: '错误', WARNING: '警告', INFO: '提示' }[s] || s
}

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await getIssues(taskId)
    issues.value = res.data
  } finally {
    loading.value = false
  }
})
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
  display: flex; align-items: flex-end; justify-content: space-between;
  border-bottom: 2px solid var(--clr-border); padding-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;
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

.brutal-filter-tabs { display: flex; gap: 0.5rem; }

.brutal-filter-tab {
  display: flex; align-items: center; gap: 8px;
  background: transparent; border: 1px solid var(--clr-border);
  color: var(--clr-text-muted); font-family: 'Space Mono', monospace;
  font-size: 0.75rem; font-weight: 700; padding: 0.4rem 0.75rem;
  cursor: pointer; transition: all 0.2s;
}

.brutal-filter-tab:hover { border-color: var(--clr-text-main); color: var(--clr-text-main); }
.brutal-filter-tab.active { background: var(--clr-accent); color: #000; border-color: var(--clr-accent); box-shadow: 3px 3px 0px rgba(204,255,0,0.2); transform: translate(-1px, -1px); }
.brutal-filter-tab.active.error { background: var(--clr-danger); border-color: var(--clr-danger); box-shadow: 3px 3px 0px rgba(255,51,102,0.2); }
.brutal-filter-tab.active.warning { background: var(--clr-warning); border-color: var(--clr-warning); box-shadow: 3px 3px 0px rgba(255,170,0,0.2); }
.brutal-filter-tab.active.info { background: var(--clr-success); border-color: var(--clr-success); box-shadow: 3px 3px 0px rgba(0,229,255,0.2); }

.tab-count { background: rgba(0,0,0,0.2); padding: 1px 6px; font-size: 0.7rem; }

.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }

.brutal-search-wrap {
  display: flex; align-items: center; background: var(--bg-dark);
  border: 1px solid var(--clr-border); transition: all 0.2s ease; min-width: 300px;
}
.brutal-search-wrap:focus-within { border-color: var(--clr-accent); box-shadow: 4px 4px 0px rgba(204,255,0,0.15); }
.search-prefix { padding: 0 0.75rem; font-size: 0.8rem; color: var(--clr-accent); font-weight: 700; border-right: 1px solid var(--clr-border); }
.brutal-search-input { border: none; outline: none; background: transparent; color: var(--clr-text-main); font-family: 'Space Mono', monospace; font-size: 0.8rem; padding: 0.5rem 0.75rem; flex: 1; }
.brutal-search-input::placeholder { color: var(--clr-text-muted); }

.stats-row { display: flex; gap: 1rem; }

.brutal-stat-chip {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px; border: 1px solid currentColor;
  font-size: 0.75rem; font-weight: 700;
}
.chip-val { font-family: 'Clash Display', sans-serif; font-size: 1.1rem; }
.brutal-stat-chip.error { color: var(--clr-danger); background: rgba(255,51,102,0.05); }
.brutal-stat-chip.warning { color: var(--clr-warning); background: rgba(255,170,0,0.05); }
.brutal-stat-chip.info { color: var(--clr-success); background: rgba(0,229,255,0.05); }

.brutal-panel {
  background: var(--bg-panel); border: 1px solid var(--clr-border);
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5); min-height: 400px;
}

.terminal-loading { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
.brutal-skeleton-row { height: 70px; background: var(--bg-card); opacity: 0.5; border-left: 3px solid var(--clr-border); animation: pulse-slow 2s infinite; }
@keyframes pulse-slow { 0%, 100% { opacity: 0.3; } 50% { opacity: 0.7; } }

.terminal-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 6rem 2rem; color: var(--clr-success); font-size: 0.85rem; text-align: center;
}
.empty-glitch { font-family: 'Clash Display', sans-serif; font-size: 2rem; margin-bottom: 1rem; }

.issues-log-stream { display: flex; flex-direction: column; }

.brutal-issue-row {
  display: flex; align-items: flex-start; gap: 1rem;
  padding: 1.25rem 1.5rem; border-bottom: 1px dashed var(--clr-border);
  border-left: 3px solid transparent; background: var(--bg-panel);
  transition: all 0.2s;
}
.brutal-issue-row:hover { background: var(--bg-card); transform: translateX(4px); }
.brutal-issue-row:last-child { border-bottom: none; }

.brutal-issue-row.error { border-left-color: var(--clr-danger); }
.brutal-issue-row.warning { border-left-color: var(--clr-warning); }
.brutal-issue-row.info { border-left-color: var(--clr-success); }

.issue-severity { flex-shrink: 0; }
.severity-square {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; font-weight: 800; font-size: 0.8rem;
  border: 1px solid currentColor; background: rgba(0,0,0,0.2);
}
.severity-square.error { color: var(--clr-danger); }
.severity-square.warning { color: var(--clr-warning); }
.severity-square.info { color: var(--clr-success); }

.issue-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }

.issue-top { display: flex; align-items: center; gap: 12px; }
.issue-file { font-size: 0.85rem; font-weight: 700; color: var(--clr-text-main); word-break: break-all; }
.issue-line { font-size: 0.7rem; color: #000; background: var(--clr-text-muted); padding: 1px 6px; font-weight: 700; }

.issue-message { font-family: 'Noto Sans SC', sans-serif; font-size: 0.85rem; color: var(--clr-text-muted); }

.issue-rule { font-size: 0.7rem; color: var(--clr-text-muted); }
.rule-prefix { color: var(--clr-accent); }

.issue-badge { flex-shrink: 0; }
.brutal-severity-badge {
  padding: 4px 8px; font-size: 0.7rem; font-weight: 700; border: 1px solid currentColor;
}
.brutal-severity-badge.error { color: var(--clr-danger); }
.brutal-severity-badge.warning { color: var(--clr-warning); }
.brutal-severity-badge.info { color: var(--clr-success); }
</style>