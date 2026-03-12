<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/>
        </svg>
        返回
      </button>
      <h2 class="page-title">问题列表 <span class="task-id-badge">#{{ taskId }}</span></h2>

      <!-- Filter Buttons -->
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeFilter === tab.value }"
          @click="activeFilter = tab.value"
        >
          <span class="tab-dot" :class="tab.value.toLowerCase()"></span>
          {{ tab.label }}
          <span class="tab-count">{{ tabCount(tab.value) }}</span>
        </button>
      </div>
    </div>

    <!-- Search & Stats -->
    <div class="toolbar">
      <div class="search-wrap">
        <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon">
          <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
        </svg>
        <input v-model="searchText" class="search-input" placeholder="搜索文件名或描述..." />
      </div>
      <div class="stats-row">
        <span class="stat-chip error">
          <span>错误</span>
          <strong>{{ tabCount('ERROR') }}</strong>
        </span>
        <span class="stat-chip warning">
          <span>警告</span>
          <strong>{{ tabCount('WARNING') }}</strong>
        </span>
        <span class="stat-chip info">
          <span>提示</span>
          <strong>{{ tabCount('INFO') }}</strong>
        </span>
      </div>
    </div>

    <!-- Issues List -->
    <div class="section-card">
      <div v-if="loading" class="loading-rows">
        <div v-for="i in 6" :key="i" class="skeleton-row"></div>
      </div>

      <div v-else-if="filteredIssues.length === 0" class="empty-state">
        <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
          <circle cx="24" cy="24" r="20" fill="#f0fdf4"/>
          <path d="M16 24l5 5 11-11" stroke="#22c55e" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>太棒了！没有发现任何问题</span>
      </div>

      <div v-else class="issues-list">
        <div
          v-for="issue in filteredIssues"
          :key="issue.id"
          class="issue-row"
          :class="issue.severity.toLowerCase()"
        >
          <div class="issue-severity">
            <span class="severity-icon" :class="issue.severity.toLowerCase()">
              <!-- ERROR icon -->
              <svg v-if="issue.severity === 'ERROR'" viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
              </svg>
              <!-- WARNING icon -->
              <svg v-else-if="issue.severity === 'WARNING'" viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
              <!-- INFO icon -->
              <svg v-else viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
              </svg>
            </span>
          </div>

          <div class="issue-main">
            <div class="issue-top">
              <span class="issue-file">{{ issue.fileName }}</span>
              <span class="issue-line">第 {{ issue.lineNumber }} 行</span>
            </div>
            <div class="issue-message">{{ issue.message }}</div>
            <div class="issue-rule">
              <svg viewBox="0 0 20 20" fill="currentColor" width="12" height="12">
                <path fill-rule="evenodd" d="M17.707 9.293a1 1 0 010 1.414l-7 7a1 1 0 01-1.414 0l-7-7A.997.997 0 012 10V5a3 3 0 013-3h5c.256 0 .512.098.707.293l7 7zM5 6a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
              </svg>
              {{ issue.ruleCode }}
            </div>
          </div>

          <div class="issue-badge">
            <span class="severity-badge" :class="issue.severity.toLowerCase()">
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
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex; align-items: center; gap: 14px; flex-wrap: wrap;
}

.page-title {
  font-size: 20px; font-weight: 800; color: #1e293b; margin: 0;
  display: flex; align-items: center; gap: 10px;
}

.task-id-badge {
  font-size: 14px; font-weight: 600; color: #94a3b8;
  background: #f1f5f9; border-radius: 6px; padding: 2px 8px;
}

.back-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff;
  color: #64748b; font-size: 13px; cursor: pointer; transition: all 0.2s;
}

.back-btn:hover { background: #f8fafc; color: #334155; }

.filter-tabs { display: flex; gap: 6px; margin-left: auto; }

.filter-tab {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff;
  color: #64748b; font-size: 13px; font-weight: 500; cursor: pointer;
  transition: all 0.2s;
}

.filter-tab:hover { border-color: #93c5fd; color: #3b82f6; }
.filter-tab.active { background: #eff6ff; border-color: #93c5fd; color: #3b82f6; font-weight: 700; }

.tab-dot {
  width: 6px; height: 6px; border-radius: 50%;
}

.tab-dot.error { background: #ef4444; }
.tab-dot.warning { background: #f59e0b; }
.tab-dot.info { background: #06b6d4; }
.tab-dot.all { background: #94a3b8; }

.tab-count {
  background: #f1f5f9; color: #475569; border-radius: 20px;
  padding: 1px 7px; font-size: 11px; font-weight: 700;
}

.filter-tab.active .tab-count { background: #dbeafe; color: #3b82f6; }

.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px; flex-wrap: wrap;
}

.search-wrap {
  display: flex; align-items: center; gap: 8px;
  border: 1px solid #e2e8f0; border-radius: 8px; padding: 8px 14px;
  background: #fff; transition: border-color 0.2s; min-width: 260px;
}

.search-wrap:focus-within { border-color: #93c5fd; }

.search-icon { color: #94a3b8; flex-shrink: 0; }

.search-input {
  border: none; outline: none; background: transparent;
  font-size: 13px; color: #334155; width: 200px;
}

.stats-row { display: flex; gap: 10px; }

.stat-chip {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 12px; border-radius: 8px;
  font-size: 12px; font-weight: 500;
}

.stat-chip strong { font-size: 14px; font-weight: 800; }

.stat-chip.error { background: #fef2f2; color: #ef4444; }
.stat-chip.warning { background: #fffbeb; color: #d97706; }
.stat-chip.info { background: #ecfeff; color: #0891b2; }

.section-card {
  background: #fff; border-radius: 16px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05); border: 1px solid #f0f4f8;
}

.loading-rows { display: flex; flex-direction: column; gap: 8px; }
.skeleton-row { height: 70px; background: #f1f5f9; border-radius: 10px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }

.empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 56px; gap: 12px; color: #94a3b8; font-size: 14px;
}

.issues-list { display: flex; flex-direction: column; gap: 8px; }

.issue-row {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 14px 16px; border-radius: 10px;
  border-left: 3px solid transparent; background: #f8fafc;
  transition: all 0.2s;
}

.issue-row:hover { transform: translateX(2px); }
.issue-row.error { border-left-color: #ef4444; background: #fef9f9; }
.issue-row.warning { border-left-color: #f59e0b; background: #fffdf5; }
.issue-row.info { border-left-color: #06b6d4; background: #f9fdfe; }

.issue-severity { flex-shrink: 0; padding-top: 2px; }

.severity-icon {
  width: 28px; height: 28px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
}

.severity-icon.error { background: #fef2f2; color: #ef4444; }
.severity-icon.warning { background: #fffbeb; color: #f59e0b; }
.severity-icon.info { background: #ecfeff; color: #06b6d4; }

.issue-main { flex: 1; min-width: 0; }

.issue-top {
  display: flex; align-items: center; gap: 10px; margin-bottom: 5px;
}

.issue-file {
  font-size: 12px; font-weight: 600; color: #475569;
  font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.issue-line {
  font-size: 11px; color: #94a3b8;
  background: #f1f5f9; padding: 2px 6px; border-radius: 4px; flex-shrink: 0;
}

.issue-message {
  font-size: 13px; color: #334155; line-height: 1.5; margin-bottom: 6px;
}

.issue-rule {
  display: flex; align-items: center; gap: 5px;
  font-size: 11px; color: #94a3b8; font-family: monospace;
}

.issue-badge { flex-shrink: 0; padding-top: 2px; }

.severity-badge {
  padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700;
}

.severity-badge.error { background: #fef2f2; color: #ef4444; }
.severity-badge.warning { background: #fffbeb; color: #d97706; }
.severity-badge.info { background: #ecfeff; color: #0891b2; }
</style>
