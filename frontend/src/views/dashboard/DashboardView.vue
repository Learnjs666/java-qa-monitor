<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.key" class="stat-card" :class="stat.colorClass">
        <div class="stat-icon" v-html="stat.icon"></div>
        <div class="stat-content">
          <div class="stat-value">
            <CountUp :end="overview[stat.key]" :duration="1.5" />
          </div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Project Selector -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">项目分析</h3>
        <el-select
          v-model="selectedProjectId"
          placeholder="选择项目查看详情"
          style="width: 240px"
          @change="loadCharts"
          size="large"
        >
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </div>

      <div v-if="!selectedProjectId" class="empty-tip">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none">
          <circle cx="32" cy="32" r="28" fill="#f1f5f9"/>
          <path d="M20 44L28 28L36 38L42 32" stroke="#94a3b8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p>请从上方选择一个项目以加载图表数据</p>
      </div>

      <div v-else class="charts-grid">
        <div class="chart-card">
          <div class="chart-title">项目健康度雷达图</div>
          <HealthRadar :data="radarData" />
        </div>
        <div class="chart-card">
          <div class="chart-title">问题趋势图</div>
          <TechDebtTrend :data="trendData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOverview, getHealthRadar, getTrend } from '../../api/dashboard'
import { getProjectList } from '../../api/project'
import HealthRadar from '../../components/HealthRadar.vue'
import TechDebtTrend from '../../components/TechDebtTrend.vue'

// Simple CountUp component inline
import { defineComponent, h, watch } from 'vue'

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

const overview = ref({
  totalProjects: 0, totalScans: 0, totalIssues: 0,
  errorCount: 0, warningCount: 0, infoCount: 0
})

const projects = ref<any[]>([])
const selectedProjectId = ref<number | null>(null)
const radarData = ref<any>(null)
const trendData = ref<any>(null)

const stats = [
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
.dashboard { display: flex; flex-direction: column; gap: 24px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

@media (max-width: 1400px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  border: 1px solid #f0f4f8;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}

.stat-icon {
  width: 44px; height: 44px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.stat-icon :deep(svg) { width: 22px; height: 22px; }

.blue .stat-icon { background: #eff6ff; color: #3b82f6; }
.purple .stat-icon { background: #f5f3ff; color: #8b5cf6; }
.gray .stat-icon { background: #f8fafc; color: #64748b; }
.red .stat-icon { background: #fef2f2; color: #ef4444; }
.yellow .stat-icon { background: #fffbeb; color: #f59e0b; }
.cyan .stat-icon { background: #ecfeff; color: #06b6d4; }

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 4px;
}

.red .stat-value { color: #ef4444; }
.yellow .stat-value { color: #f59e0b; }
.cyan .stat-value { color: #06b6d4; }

.stat-label { font-size: 12px; color: #94a3b8; font-weight: 500; }

.section-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  border: 1px solid #f0f4f8;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #94a3b8;
  font-size: 14px;
  gap: 12px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 1100px) { .charts-grid { grid-template-columns: 1fr; } }

.chart-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 16px;
}
</style>
