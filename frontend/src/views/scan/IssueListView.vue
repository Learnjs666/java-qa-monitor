<template>
  <div class="sq-page">
    <div class="sq-page-header">
      <div class="header-left">
        <button class="sq-back-btn" @click="router.back()">
          <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/></svg>
          返回
        </button>
        <h2 class="sq-page-title">问题列表 <span class="task-id-badge">#{{ taskId }}</span></h2>
      </div>

      <!-- 过滤器 -->
      <div class="sq-filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="sq-filter-tab"
          :class="{ active: activeFilter === tab.value }"
          @click="activeFilter = tab.value"
        >
          {{ tab.label }} <span class="tab-count">({{ tabCount(tab.value) }})</span>
        </button>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="sq-toolbar">
      <div class="sq-search-wrap">
        <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
        <input v-model="searchText" class="sq-search-input" placeholder="搜索文件名或描述..." />
      </div>
    </div>

    <!-- 问题列表 -->
    <div class="sq-issues-container">
      <div v-if="loading" class="sq-loading-placeholder">
        <div v-for="i in 6" :key="i" class="sq-skeleton-row"></div>
      </div>

      <div v-else-if="filteredIssues.length === 0" class="sq-empty-state">
        太棒了！该过滤条件下没有发现任何问题。
      </div>

      <div v-else class="sq-issues-list">
        <div
          v-for="issue in filteredIssues"
          :key="issue.id"
          class="sq-issue-card"
          :class="issue.severity.toLowerCase()"
        >
          <div class="issue-left">
            <span class="severity-icon" :class="issue.severity.toLowerCase()">
              <svg v-if="issue.severity === 'ERROR'" viewBox="0 0 20 20" fill="currentColor" width="14" height="14"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
              <svg v-else-if="issue.severity === 'WARNING'" viewBox="0 0 20 20" fill="currentColor" width="14" height="14"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>
              <svg v-else viewBox="0 0 20 20" fill="currentColor" width="14" height="14"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>
            </span>
          </div>

          <div class="issue-main">
            <div class="issue-top">
              <span class="issue-file">{{ issue.fileName }}</span>
              <span class="issue-line">Line {{ issue.lineNumber }}</span>
            </div>
            <div class="issue-message">{{ issue.message }}</div>
            <div class="issue-bottom">
              <span class="issue-rule">Rule: {{ issue.ruleCode }}</span>
              <span class="severity-text" :class="issue.severity.toLowerCase()">{{ severityLabel(issue.severity) }}</span>
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
.sq-page { display: flex; flex-direction: column; gap: 20px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333; }
.sq-page-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e1e6eb; padding-bottom: 12px; flex-wrap: wrap; gap: 12px; }
.header-left { display: flex; align-items: center; gap: 16px; }
.sq-back-btn { display: flex; align-items: center; gap: 4px; background: transparent; border: none; font-size: 13px; color: #555555; cursor: pointer; padding: 0; }
.sq-back-btn:hover { color: #0271b6; text-decoration: underline; }

.sq-page-title { font-size: 20px; font-weight: 600; margin: 0; color: #222222; display: flex; align-items: center; gap: 8px; }
.task-id-badge { font-size: 14px; font-weight: 400; color: #777777; font-family: Consolas, monospace; }

.sq-filter-tabs { display: flex; gap: 4px; }
.sq-filter-tab { padding: 6px 12px; border-radius: 2px; border: 1px solid transparent; background: transparent; color: #555; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.sq-filter-tab:hover { background-color: #e1e6eb; }
.sq-filter-tab.active { border-color: #e1e6eb; background-color: #ffffff; color: #0271b6; font-weight: 600; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.tab-count { font-weight: 400; color: #999; }

.sq-toolbar { display: flex; justify-content: space-between; align-items: center; }
.sq-search-wrap { display: flex; align-items: center; gap: 8px; border: 1px solid #cccccc; border-radius: 3px; padding: 6px 10px; background: #ffffff; width: 300px; transition: border-color 0.2s; }
.sq-search-wrap:focus-within { border-color: #0271b6; }
.search-icon { color: #999999; }
.sq-search-input { border: none; outline: none; font-size: 13px; width: 100%; color: #333; }

.sq-issues-container { display: flex; flex-direction: column; }

.sq-loading-placeholder { display: flex; flex-direction: column; gap: 8px; }
.sq-skeleton-row { height: 70px; background: #ffffff; border: 1px solid #e1e6eb; animation: pulse 1.5s infinite; }

.sq-empty-state { padding: 60px; text-align: center; color: #00aa00; font-size: 14px; background: #ffffff; border: 1px solid #e1e6eb; }

.sq-issues-list { display: flex; flex-direction: column; gap: 8px; }
.sq-issue-card {
  display: flex; align-items: flex-start; gap: 12px; padding: 12px 16px;
  background: #ffffff; border: 1px solid #e1e6eb; border-left: 4px solid;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02); transition: box-shadow 0.2s;
}
.sq-issue-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

.sq-issue-card.error { border-left-color: #d4333f; }
.sq-issue-card.warning { border-left-color: #ed7d20; }
.sq-issue-card.info { border-left-color: #00aa00; }

.issue-left { padding-top: 2px; }
.severity-icon.error { color: #d4333f; }
.severity-icon.warning { color: #ed7d20; }
.severity-icon.info { color: #00aa00; }

.issue-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.issue-top { display: flex; align-items: center; gap: 12px; }
.issue-file { font-size: 13px; font-weight: 600; color: #0271b6; font-family: Consolas, monospace; cursor: pointer; }
.issue-file:hover { text-decoration: underline; }
.issue-line { font-size: 11px; background: #f3f4f6; padding: 2px 6px; border: 1px solid #e1e6eb; border-radius: 2px; color: #555; }

.issue-message { font-size: 13px; color: #333333; line-height: 1.4; }

.issue-bottom { display: flex; align-items: center; gap: 16px; font-size: 12px; }
.issue-rule { color: #777777; }
.severity-text { font-weight: 600; text-transform: uppercase; font-size: 11px; }
.severity-text.error { color: #d4333f; }
.severity-text.warning { color: #ed7d20; }
.severity-text.info { color: #00aa00; }
</style>