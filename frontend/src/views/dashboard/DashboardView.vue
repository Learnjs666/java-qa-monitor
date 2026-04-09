<template>
  <div class="sq-dashboard">
    <!-- Header -->
    <div class="sq-page-header">
      <h1 class="sq-page-title">系统大盘概览</h1>
      
      <!-- 项目选择器作为全局上下文 -->
      <div class="sq-context-selector">
        <span class="selector-label">当前分析项目：</span>
        <el-select
          v-model="selectedProjectId"
          placeholder="请选择项目以查看图表"
          class="sq-select"
          @change="loadCharts"
        >
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </div>
    </div>

    <!-- Stats Quality Gate like Cards -->
    <div class="sq-stats-grid">
      <div v-for="stat in stats" :key="stat.key" class="sq-stat-card">
        <div class="stat-indicator" :class="stat.colorClass"></div>
        <div class="stat-content">
          <div class="stat-main">
            <span class="stat-value">
              <CountUp :end="overview[stat.key]" :duration="1.5" />
            </span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
          <div class="stat-icon" :class="stat.colorClass" v-html="stat.icon"></div>
        </div>
      </div>
    </div>

    <!-- Charts Layout -->
    <div class="sq-charts-layout">
      <div v-if="!selectedProjectId" class="sq-empty-panel">
        <svg viewBox="0 0 64 64" width="48" height="48" fill="none">
          <circle cx="32" cy="32" r="28" fill="#f3f4f6"/>
          <path d="M20 44L28 28L36 38L42 32" stroke="#a3a3a3" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>未选择项目上下文，请在右上角选择项目以加载深度分析数据。</p>
      </div>

      <div v-else class="sq-charts-grid">
        <div class="sq-panel">
          <div class="panel-header">
            <h3 class="panel-title">项目健康度雷达图</h3>
          </div>
          <div class="panel-body">
            <HealthRadar :data="radarData" />
          </div>
        </div>
        
        <div class="sq-panel">
          <div class="panel-header">
            <h3 class="panel-title">问题发现趋势</h3>
          </div>
          <div class="panel-body">
            <TechDebtTrend :data="trendData" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineComponent, h, watch } from 'vue'
import { getOverview, getHealthRadar, getTrend } from '../../api/dashboard'
import { getProjectList } from '../../api/project'
import HealthRadar from '../../components/HealthRadar.vue'
import TechDebtTrend from '../../components/TechDebtTrend.vue'
import { useUserStore } from '../../store/user'

const CountUp = defineComponent({
  props: { end: { type: Number, default: 0 }, duration: { type: Number, default: 1.5 } },
  setup(props) {
    const current = ref(0)
    function animate(target: number) {
      const start = 0
      const startTime = performance.now()
      const dur = props.duration * 1000
      const step = (now: number) => {
        const elapsed = now - startTime
        const progress = Math.min(elapsed / dur, 1)
        current.value = Math.round(start + (target - start) * easeOut(progress))
        if (progress < 1) requestAnimationFrame(step)
      }
      requestAnimationFrame(step)
    }
    function easeOut(t: number) { return 1 - Math.pow(1 - t, 3) }
    watch(() => props.end, (v) => animate(v), { immediate: true })
    return () => h('span', current.value)
  }
})

// const overview = ref({
//   totalProjects: 0, totalScans: 0, totalIssues: 0,
//   errorCount: 0, warningCount: 0, infoCount: 0
// })

const projects = ref<any[]>([])
const selectedProjectId = ref<number | null>(null)
const radarData = ref<any>(null)
const trendData = ref<any>(null)

// const stats = [
//   { key: 'totalProjects', label: '纳管项目总数', colorClass: 'blue', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/></svg>` },
//   { key: 'totalScans', label: '累计扫描次数', colorClass: 'blue', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 2a2 2 0 00-2 2v11a3 3 0 106 0V4a2 2 0 00-2-2H4zm1 14a1 1 0 100-2 1 1 0 000 2zm5-1.757l4.9-4.9a2 2 0 000-2.828L13.485 5.1a2 2 0 00-2.828 0L10 5.757v8.486zM16 18H9.071l6-6H16a2 2 0 012 2v2a2 2 0 01-2 2z" clip-rule="evenodd"/></svg>` },
//   { key: 'totalIssues', label: '发现问题总数', colorClass: 'blue', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>` },
//   { key: 'errorCount', label: '严重错误 (Error)', colorClass: 'red', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>` },
//   { key: 'warningCount', label: '警告 (Warning)', colorClass: 'orange', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>` },
//   { key: 'infoCount', label: '提示 (Info)', colorClass: 'green', icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>` }
// ]

// 1. 定义严谨的数据大屏接口约束
interface OverviewData {
  totalProjects: number;
  totalScans: number;
  totalIssues: number;
  errorCount: number;
  warningCount: number;
  infoCount: number;
}

// 2. 为 overview 数据源注入类型
const overview = ref<OverviewData>({
  totalProjects: 0, totalScans: 0, totalIssues: 0,
  errorCount: 0, warningCount: 0, infoCount: 0
})

// 3. 约束卡片配置项，声明 key 必须严格隶属于 OverviewData 的键
interface StatConfig {
  key: keyof OverviewData;
  label: string;
  colorClass: string;
  icon: string;
}

// 4. 应用约束到数组
const stats: StatConfig[] = [
  {
    key: 'totalProjects', label: '项目总数', colorClass: 'blue',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/></svg>`
  },
  {
    key: 'totalScans', label: '扫描次数', colorClass: 'purple',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 2a2 2 0 00-2 2v11a3 3 0 106 0V4a2 2 0 00-2-2H4zm1 14a1 1 0 100-2 1 1 0 000 2zm5-1.757l4.9-4.9a2 2 0 000-2.828L13.485 5.1a2 2 0 00-2.828 0L10 5.757v8.486zM16 18H9.071l6-6H16a2 2 0 012 2v2a2 2 0 01-2 2z" clip-rule="evenodd"/></svg>`
  },
  {
    key: 'totalIssues', label: '问题总数', colorClass: 'gray',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
  },
  {
    key: 'errorCount', label: '错误', colorClass: 'red',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>`
  },
  {
    key: 'warningCount', label: '警告', colorClass: 'yellow',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
  },
  {
    key: 'infoCount', label: '提示', colorClass: 'cyan',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>`
  }
]

const userStore = useUserStore()  // 测试用户信息

onMounted(async () => {
  const [overviewRes, projectRes] = await Promise.all([
    getOverview(), getProjectList()
  ]) as any[]
  overview.value = overviewRes.data
  projects.value = projectRes.data
})

async function loadCharts() {
  if (!selectedProjectId.value) return
  const [radarRes, trendRes] = await Promise.all([
    getHealthRadar(selectedProjectId.value),
    getTrend(selectedProjectId.value)
  ]) as any[]
  radarData.value = radarRes.data
  trendData.value = trendRes.data
}
</script>

<style scoped>
.sq-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  color: #333333;
}

.sq-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #e1e6eb;
}

.sq-page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #222222;
}

.sq-context-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selector-label {
  font-size: 13px;
  font-weight: 600;
  color: #444444;
}

.sq-select :deep(.el-input__wrapper) {
  border-radius: 3px !important;
  box-shadow: none !important;
  border: 1px solid #cccccc !important;
}
.sq-select :deep(.el-input__wrapper.is-focus) {
  border-color: #0271b6 !important;
}

/* 状态卡片网格 */
.sq-stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

@media (max-width: 1400px) { .sq-stats-grid { grid-template-columns: repeat(3, 1fr); gap: 16px; } }
@media (max-width: 900px) { .sq-stats-grid { grid-template-columns: repeat(2, 1fr); } }

.sq-stat-card {
  background: #ffffff;
  border: 1px solid #e1e6eb;
  border-radius: 2px;
  display: flex;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.stat-indicator {
  width: 4px;
  flex-shrink: 0;
}

.stat-indicator.blue { background-color: #0271b6; }
.stat-indicator.red { background-color: #d4333f; }
.stat-indicator.orange { background-color: #ed7d20; }
.stat-indicator.green { background-color: #00aa00; }

.stat-content {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.stat-main {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  color: #333333;
}

.stat-label {
  font-size: 12px;
  color: #777777;
  margin-top: 4px;
}

.stat-icon {
  width: 24px; height: 24px;
  opacity: 0.2;
}
.stat-icon.blue { color: #0271b6; }
.stat-icon.red { color: #d4333f; }
.stat-icon.orange { color: #ed7d20; }
.stat-icon.green { color: #00aa00; }
.stat-icon :deep(svg) { width: 100%; height: 100%; }


/* 图表区域 */
.sq-charts-layout {
  margin-top: 8px;
}

.sq-empty-panel {
  background: #ffffff;
  border: 1px dashed #cccccc;
  border-radius: 2px;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #777777;
  font-size: 14px;
}

.sq-empty-panel svg {
  margin-bottom: 16px;
}

.sq-charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1100px) { .sq-charts-grid { grid-template-columns: 1fr; } }

.sq-panel {
  background: #ffffff;
  border: 1px solid #e1e6eb;
  border-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e1e6eb;
  background-color: #fafbfc;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #444444;
  margin: 0;
}

.panel-body {
  padding: 20px;
  min-height: 300px;
}
</style>