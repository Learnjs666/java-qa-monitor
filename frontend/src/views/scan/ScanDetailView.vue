<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/>
        </svg>
        返回
      </button>
      <h2 class="page-title">扫描指标 <span class="task-id-badge">#{{ taskId }}</span></h2>
    </div>

    <!-- Task Summary -->
    <div class="summary-bar" v-if="task">
      <div class="summary-item">
        <span class="summary-label">状态</span>
        <span class="status-badge" :class="task.status.toLowerCase()">
          <span class="status-dot"></span>
          {{ statusLabel(task.status) }}
        </span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <span class="summary-label">扫描文件数</span>
        <span class="summary-value">{{ task.fileCount || 0 }}</span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <span class="summary-label">发现问题数</span>
        <span class="summary-value highlight">{{ task.issueCount || 0 }}</span>
      </div>
    </div>

    <!-- Metrics Table -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">文件指标详情</h3>
        <div class="search-wrap">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input v-model="searchText" class="search-input" placeholder="搜索文件名..." />
        </div>
      </div>

      <div v-if="loading" class="loading-rows">
        <div v-for="i in 5" :key="i" class="skeleton-row"></div>
      </div>

      <div v-else-if="filteredMetrics.length === 0" class="empty-state">
        <span>暂无指标数据</span>
      </div>

      <div v-else class="metrics-table">
        <div class="metrics-head">
          <div class="col-file">文件名</div>
          <div class="col-num">代码行数</div>
          <div class="col-num">方法数</div>
          <div class="col-complex">最大圈复杂度</div>
        </div>
        <div
          v-for="row in filteredMetrics"
          :key="row.id"
          class="metrics-row"
        >
          <div class="col-file file-name">
            <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="file-icon">
              <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/>
            </svg>
            {{ row.fileName }}
          </div>
          <div class="col-num">{{ row.lineCount }}</div>
          <div class="col-num">{{ row.methodCount }}</div>
          <div class="col-complex">
            <span class="complexity-badge" :class="complexityClass(row.maxCyclomaticComplexity)">
              {{ row.maxCyclomaticComplexity }}
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
  return { PENDING: '等待中', RUNNING: '扫描中', COMPLETED: '已完成', FAILED: '失败' }[s] || s
}

function complexityClass(val: number) {
  if (val > 10) return 'danger'
  if (val > 5) return 'warning'
  return 'success'
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; align-items: center; gap: 14px; }

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

.summary-bar {
  background: #fff; border-radius: 12px; padding: 18px 24px;
  display: flex; align-items: center; gap: 24px;
  border: 1px solid #f0f4f8; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.summary-item { display: flex; align-items: center; gap: 10px; }
.summary-label { font-size: 12px; color: #94a3b8; font-weight: 500; }
.summary-value { font-size: 18px; font-weight: 800; color: #1e293b; }
.summary-value.highlight { color: #3b82f6; }
.summary-divider { width: 1px; height: 28px; background: #f0f4f8; }

.status-badge {
  display: flex; align-items: center; gap: 5px;
  padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-badge.pending { background: #f1f5f9; color: #64748b; }
.status-badge.pending .status-dot { background: #94a3b8; }
.status-badge.running { background: #fffbeb; color: #d97706; }
.status-badge.running .status-dot { background: #f59e0b; animation: blink 1s ease-in-out infinite; }
.status-badge.completed { background: #f0fdf4; color: #16a34a; }
.status-badge.completed .status-dot { background: #22c55e; }
.status-badge.failed { background: #fef2f2; color: #dc2626; }
.status-badge.failed .status-dot { background: #ef4444; }

@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.4} }

.section-card {
  background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05); border: 1px solid #f0f4f8;
}

.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}

.section-title { font-size: 15px; font-weight: 700; color: #1e293b; margin: 0; }

.search-wrap {
  display: flex; align-items: center; gap: 8px;
  border: 1px solid #e2e8f0; border-radius: 8px; padding: 7px 12px;
  background: #f8fafc; transition: border-color 0.2s;
}

.search-wrap:focus-within { border-color: #93c5fd; background: #fff; }

.search-icon { color: #94a3b8; flex-shrink: 0; }

.search-input {
  border: none; outline: none; background: transparent;
  font-size: 13px; color: #334155; width: 180px;
}

.loading-rows { display: flex; flex-direction: column; gap: 8px; }
.skeleton-row { height: 46px; background: #f1f5f9; border-radius: 8px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }

.empty-state {
  display: flex; align-items: center; justify-content: center;
  padding: 40px; color: #94a3b8; font-size: 13px;
}

.metrics-table { display: flex; flex-direction: column; gap: 0; }

.metrics-head {
  display: grid;
  grid-template-columns: 1fr 100px 80px 140px;
  padding: 8px 14px; font-size: 11px; font-weight: 600;
  color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 1px solid #f0f4f8;
}

.metrics-row {
  display: grid;
  grid-template-columns: 1fr 100px 80px 140px;
  padding: 12px 14px; font-size: 13px;
  border-bottom: 1px solid #f8fafc; transition: background 0.15s;
  align-items: center;
}

.metrics-row:hover { background: #f8fafc; }
.metrics-row:last-child { border-bottom: none; }

.col-num { color: #475569; text-align: right; }
.col-complex { text-align: center; }

.file-name {
  display: flex; align-items: center; gap: 7px;
  color: #334155; font-weight: 500; font-family: monospace; font-size: 12px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.file-icon { color: #94a3b8; flex-shrink: 0; }

.complexity-badge {
  display: inline-block; padding: 3px 10px; border-radius: 6px;
  font-size: 12px; font-weight: 700;
}

.complexity-badge.danger { background: #fef2f2; color: #ef4444; }
.complexity-badge.warning { background: #fffbeb; color: #f59e0b; }
.complexity-badge.success { background: #f0fdf4; color: #22c55e; }
</style>
