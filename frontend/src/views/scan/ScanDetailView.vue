<template>
  <div class="sq-page">
    <div class="sq-page-header">
      <button class="sq-back-btn" @click="router.back()">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/></svg>
        返回
      </button>
      <h2 class="sq-page-title">扫描指标详细信息 <span class="task-id-badge">#{{ taskId }}</span></h2>
    </div>

    <!-- 任务总结横幅 -->
    <div class="sq-summary-banner" v-if="task">
      <div class="summary-item">
        <span class="summary-label">执行状态</span>
        <span class="sq-status-badge" :class="task.status.toLowerCase()">
          {{ statusLabel(task.status) }}
        </span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <span class="summary-label">文件总数</span>
        <span class="summary-val">{{ task.fileCount || 0 }}</span>
      </div>
      <div class="summary-divider"></div>
      <div class="summary-item">
        <span class="summary-label">代码问题</span>
        <span class="summary-val text-red">{{ task.issueCount || 0 }}</span>
      </div>
    </div>

    <!-- 指标表格面板 -->
    <div class="sq-panel">
      <div class="panel-header">
        <h3 class="panel-title">文件复杂度分析</h3>
        <div class="sq-search-wrap">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon"><path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/></svg>
          <input v-model="searchText" class="sq-search-input" placeholder="输入文件名过滤..." />
        </div>
      </div>

      <div class="panel-body no-padding">
        <div v-if="loading" class="sq-loading-placeholder">
          <div v-for="i in 5" :key="i" class="sq-skeleton-row"></div>
        </div>

        <div v-else-if="filteredMetrics.length === 0" class="sq-empty-state">
          该次扫描未收集到任何指标数据。
        </div>

        <div v-else class="sq-metrics-table">
          <div class="metrics-head">
            <div class="col-file">文件路径</div>
            <div class="col-num">代码行 (NCLOC)</div>
            <div class="col-num">方法数</div>
            <div class="col-complex">最大圈复杂度</div>
          </div>
          <div
            v-for="row in filteredMetrics"
            :key="row.id"
            class="metrics-row"
          >
            <div class="col-file file-name">
              <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="file-icon"><path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clip-rule="evenodd"/></svg>
              {{ row.fileName }}
            </div>
            <div class="col-num">{{ row.lineCount }}</div>
            <div class="col-num">{{ row.methodCount }}</div>
            <div class="col-complex">
              <span class="complexity-val" :class="complexityClass(row.maxCyclomaticComplexity)">
                {{ row.maxCyclomaticComplexity }}
              </span>
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
  return { PENDING: '队列中', RUNNING: '分析中', COMPLETED: '执行成功', FAILED: '执行失败' }[s] || s
}

function complexityClass(val: number) {
  if (val > 10) return 'danger'
  if (val > 5) return 'warning'
  return 'success'
}
</script>

<style scoped>
.sq-page { display: flex; flex-direction: column; gap: 20px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333; }
.sq-page-header { display: flex; align-items: center; gap: 16px; border-bottom: 1px solid #e1e6eb; padding-bottom: 12px; }
.sq-back-btn { display: flex; align-items: center; gap: 4px; background: transparent; border: none; font-size: 13px; color: #555555; cursor: pointer; padding: 0; }
.sq-back-btn:hover { color: #0271b6; text-decoration: underline; }

.sq-page-title { font-size: 20px; font-weight: 600; margin: 0; color: #222222; display: flex; align-items: center; gap: 8px; }
.task-id-badge { font-size: 14px; font-weight: 400; color: #777777; font-family: Consolas, monospace; }

.sq-summary-banner {
  display: flex; align-items: center; gap: 32px; padding: 16px 24px;
  background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.summary-item { display: flex; align-items: center; gap: 12px; }
.summary-label { font-size: 13px; color: #777777; }
.summary-val { font-size: 20px; font-weight: 600; color: #333333; }
.text-red { color: #d4333f; }
.summary-divider { width: 1px; height: 30px; background: #e1e6eb; }

.sq-status-badge {
  padding: 2px 8px; border-radius: 2px; font-size: 11px; font-weight: 600; border: 1px solid transparent; text-transform: uppercase;
}
.sq-status-badge.pending { background-color: #f3f4f6; color: #555; border-color: #d1d5db; }
.sq-status-badge.running { background-color: #fef0cd; color: #d47e00; border-color: #fcdca3; }
.sq-status-badge.completed { background-color: #e5f6e5; color: #00aa00; border-color: #bce1bc; }
.sq-status-badge.failed { background-color: #fae1e3; color: #d4333f; border-color: #f2c2c5; }

.sq-panel { background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px; box-shadow: 0 1px 2px rgba(0,0,0,0.03); }
.panel-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #e1e6eb; background-color: #fafbfc; }
.panel-title { font-size: 14px; font-weight: 600; color: #333333; margin: 0; }
.panel-body.no-padding { padding: 0; }

.sq-search-wrap { display: flex; align-items: center; gap: 8px; border: 1px solid #cccccc; border-radius: 3px; padding: 6px 10px; background: #ffffff; transition: border-color 0.2s; }
.sq-search-wrap:focus-within { border-color: #0271b6; }
.search-icon { color: #999999; }
.sq-search-input { border: none; outline: none; font-size: 13px; color: #333333; width: 200px; }

.sq-loading-placeholder { padding: 16px; display: flex; flex-direction: column; gap: 8px; }
.sq-skeleton-row { height: 40px; background: #f3f4f6; animation: pulse 1.5s infinite; }

.sq-empty-state { padding: 40px; text-align: center; color: #777777; font-size: 13px; }

.sq-metrics-table { display: flex; flex-direction: column; }
.metrics-head { display: grid; grid-template-columns: 1fr 140px 120px 140px; padding: 10px 16px; font-size: 12px; font-weight: 600; color: #777777; border-bottom: 1px solid #e1e6eb; background-color: #fafbfc; }
.metrics-row { display: grid; grid-template-columns: 1fr 140px 120px 140px; padding: 12px 16px; border-bottom: 1px solid #f3f4f6; font-size: 13px; align-items: center; transition: background 0.15s; }
.metrics-row:hover { background-color: #f9f9fb; }
.metrics-row:last-child { border-bottom: none; }

.col-num { color: #555555; text-align: right; padding-right: 20px; font-family: Consolas, monospace; }
.col-complex { text-align: center; }

.file-name { display: flex; align-items: center; gap: 8px; color: #0271b6; font-family: Consolas, monospace; font-size: 13px; cursor: pointer; }
.file-name:hover { text-decoration: underline; }
.file-icon { color: #999999; }

.complexity-val { font-weight: 600; font-family: Consolas, monospace; }
.complexity-val.danger { color: #d4333f; }
.complexity-val.warning { color: #ed7d20; }
.complexity-val.success { color: #00aa00; }
</style>