<template>
  <div class="terminal-dashboard">
    <div class="grid-overlay"></div>

    <div class="dashboard-content">
      
      <header class="sys-header">
        <div class="sys-title">
          <span class="dot blink pulse-lime"></span>
          <span class="mono-text">系统仪表盘 // 检测概览</span>
        </div>
        <div class="sys-time">运行状态：<span class="accent-text">ACTIVE</span></div>
      </header>

      <div class="stats-grid">
        <div v-for="stat in stats" :key="stat.key" class="brutal-stat-card" :class="stat.colorClass">
          <div class="stat-top">
            <span class="stat-icon-wrapper" v-html="stat.icon"></span>
            <span class="stat-label">[ {{ stat.label }} ]</span>
          </div>
          <div class="stat-bottom">
            <span class="prefix-arrow">></span>
            <div class="stat-value">
              <CountUp :end="overview[stat.key]" :duration="1.5" />
            </div>
          </div>
        </div>
      </div>

      <div class="brutal-panel">
        <div class="panel-header">
          <h3 class="panel-title">可视化分析</h3>
          
          <div class="brutal-select-wrapper">
            <span class="select-prefix">目标：</span>
            <el-select
              v-model="selectedProjectId"
              placeholder="AWAITING_INPUT..."
              style="width: 260px"
              @change="loadCharts"
              size="large"
              class="brutal-select"
              popper-class="brutal-popper"
            >
              <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
          </div>
        </div>

        <div v-if="!selectedProjectId" class="terminal-empty">
          <div class="empty-content">
            <span class="terminal-cursor blink">_</span>
            <p class="empty-text">系统待命。</p>
            <p class="empty-subtext">要求：选择一个项目初始化数据流</p>
          </div>
        </div>

        <div v-else class="charts-grid">
          <div class="brutal-chart-card">
            <div class="chart-top-bar">
              <span class="bar-title">模块: 检测结果雷达</span>
              <span class="bar-status">在线</span>
            </div>
            <div class="chart-container">
              <HealthRadar :data="radarData" />
            </div>
          </div>
          
          <div class="brutal-chart-card">
            <div class="chart-top-bar">
              <span class="bar-title">模块：技术问题趋势</span>
              <span class="bar-status">在线</span>
            </div>
            <div class="chart-container">
              <TechDebtTrend :data="trendData" />
            </div>
          </div>
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
import { useUserStore } from '../../store/user'

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

// const overview = ref({
//   totalProjects: 0, totalScans: 0, totalIssues: 0,
//   errorCount: 0, warningCount: 0, infoCount: 0
// })

const projects = ref<any[]>([])
const selectedProjectId = ref<number | null>(null)
const radarData = ref<any>(null)
const trendData = ref<any>(null)

// const stats = [
//   {
//     key: 'totalProjects', label: '项目总数', colorClass: 'blue',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/></svg>`
//   },
//   {
//     key: 'totalScans', label: '扫描次数', colorClass: 'purple',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4 2a2 2 0 00-2 2v11a3 3 0 106 0V4a2 2 0 00-2-2H4zm1 14a1 1 0 100-2 1 1 0 000 2zm5-1.757l4.9-4.9a2 2 0 000-2.828L13.485 5.1a2 2 0 00-2.828 0L10 5.757v8.486zM16 18H9.071l6-6H16a2 2 0 012 2v2a2 2 0 01-2 2z" clip-rule="evenodd"/></svg>`
//   },
//   {
//     key: 'totalIssues', label: '问题总数', colorClass: 'gray',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
//   },
//   {
//     key: 'errorCount', label: '错误', colorClass: 'red',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>`
//   },
//   {
//     key: 'warningCount', label: '警告', colorClass: 'yellow',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
//   },
//   {
//     key: 'infoCount', label: '提示', colorClass: 'cyan',
//     icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>`
//   }
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
  console.log(userStore.role)
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
/* 引入顶级设计字体 */
@import url('https://api.fontshare.com/v2/css?f[]=clash-display@600,700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.terminal-dashboard {
  --bg-dark: #090a0f;
  --bg-panel: #11131a;
  --bg-card: #161922;
  --clr-accent: #ccff00; /* Electric Lime */
  --clr-accent-dim: rgba(204, 255, 0, 0.15);
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;
  
  /* 终端状态色映射 */
  --clr-blue: #00e5ff;
  --clr-purple: #b026ff;
  --clr-gray: #ffffff;
  --clr-red: #ff3366;
  --clr-yellow: #ffaa00;
  --clr-cyan: #ccff00;

  min-height: 100vh;
  background-color: var(--bg-dark);
  font-family: 'Space Mono', monospace;
  position: relative;
  padding: 2rem;
  color: var(--clr-text-main);
}

/* 工业风量化网格背景 */
.grid-overlay {
  position: fixed;
  inset: 0;
  background-image: 
    linear-gradient(var(--clr-border) 1px, transparent 1px),
    linear-gradient(90deg, var(--clr-border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.3;
  pointer-events: none;
  z-index: 0;
}

.dashboard-content {
  position: relative;
  z-index: 10;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* 顶部状态栏 */
.sys-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--clr-border);
  padding-bottom: 1rem;
}

.sys-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.9rem;
  color: var(--clr-text-muted);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.pulse-lime {
  background-color: var(--clr-accent);
  box-shadow: 0 0 10px var(--clr-accent);
}

.blink { animation: pulse 2s infinite; }
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.sys-time {
  font-size: 0.8rem;
  color: var(--clr-text-muted);
}

.accent-text { color: var(--clr-accent); font-weight: 700; }

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 1.5rem;
}

@media (max-width: 1400px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }

/* 粗犷风数据卡片 */
.brutal-stat-card {
  background: var(--bg-card);
  border: 1px solid var(--clr-border);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1rem;
  transition: all 0.2s ease;
  position: relative;
}

.brutal-stat-card::after {
  content: '';
  position: absolute;
  top: 0; left: 0; width: 4px; height: 100%;
  background-color: var(--theme-color);
}

.brutal-stat-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 6px 6px 0px var(--theme-color);
  border-color: var(--theme-color);
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-icon-wrapper {
  color: var(--theme-color);
  display: flex;
  align-items: center;
}

.stat-icon-wrapper :deep(svg) { width: 18px; height: 18px; }

.stat-label {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--clr-text-muted);
  text-transform: uppercase;
}

.stat-bottom {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
}

.prefix-arrow {
  color: var(--theme-color);
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 2px;
}

.stat-value {
  font-family: 'Clash Display', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1;
  color: var(--theme-color);
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.1);
}

/* 卡片颜色映射 (覆写原有逻辑产生的 class) */
.brutal-stat-card.blue { --theme-color: var(--clr-blue); }
.brutal-stat-card.purple { --theme-color: var(--clr-purple); }
.brutal-stat-card.gray { --theme-color: var(--clr-gray); }
.brutal-stat-card.red { --theme-color: var(--clr-red); }
.brutal-stat-card.yellow { --theme-color: var(--clr-yellow); }
.brutal-stat-card.cyan { --theme-color: var(--clr-cyan); }

/* 下方主体大面板 */
.brutal-panel {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  box-shadow: 12px 12px 0px rgba(0,0,0,0.5);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px dashed var(--clr-border);
  padding-bottom: 1.5rem;
}

.panel-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--clr-accent);
  margin: 0;
  letter-spacing: 0.05em;
}

/* 定制化 el-select (Brutalist) */
.brutal-select-wrapper {
  display: flex;
  align-items: center;
  background: var(--bg-dark);
  border: 1px solid var(--clr-border);
}

.select-prefix {
  padding: 0 1rem;
  color: var(--clr-text-muted);
  font-size: 0.8rem;
  font-weight: 700;
  border-right: 1px solid var(--clr-border);
}

.brutal-select :deep(.el-input__wrapper) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  border-radius: 0 !important;
}

.brutal-select :deep(.el-input__inner) {
  color: var(--clr-text-main) !important;
  font-family: 'Space Mono', monospace;
  font-size: 0.85rem;
}

.brutal-select :deep(.el-input__wrapper.is-focus) {
  background: rgba(204, 255, 0, 0.05) !important;
}

/* 空状态终端提示 */
.terminal-empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: repeating-linear-gradient(
    0deg,
    rgba(0, 0, 0, 0.15),
    rgba(0, 0, 0, 0.15) 1px,
    transparent 1px,
    transparent 2px
  );
  border: 1px solid var(--clr-border);
}

.empty-content {
  text-align: center;
}

.terminal-cursor {
  display: block;
  font-size: 3rem;
  color: var(--clr-accent);
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 1.25rem;
  color: var(--clr-text-main);
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.empty-subtext {
  font-size: 0.85rem;
  color: var(--clr-text-muted);
  margin: 0;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

@media (max-width: 1100px) { .charts-grid { grid-template-columns: 1fr; } }

.brutal-chart-card {
  background: var(--bg-card);
  border: 1px solid var(--clr-border);
  display: flex;
  flex-direction: column;
}

.chart-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: var(--bg-dark);
  border-bottom: 1px solid var(--clr-border);
  font-size: 0.75rem;
}

.bar-title {
  color: var(--clr-text-main);
  font-weight: 700;
}

.bar-status {
  color: var(--clr-accent);
  background: rgba(204, 255, 0, 0.1);
  padding: 2px 6px;
  border: 1px solid var(--clr-accent);
}

.chart-container {
  padding: 1.5rem;
  min-height: 350px;
  /* 图表内部颜色如需适配，建议在 Echarts 配置项中设为暗色模式 */
}
</style>