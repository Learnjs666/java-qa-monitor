<template>
  <div class="chart-wrap">
    <div v-if="!props.data?.points?.length" class="chart-empty">
      <svg viewBox="0 0 48 24" width="60" height="30" fill="none">
        <path d="M2 22L12 10L22 18L34 8L46 14" stroke="#e2e8f0" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <span>选择项目后显示趋势数据</span>
    </div>
    <v-chart v-else :option="chartOption" autoresize />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const props = defineProps<{
  data: { points: { date: string; errorCount: number; warningCount: number; infoCount: number }[] } | null
}>()

const chartOption = computed(() => {
  if (!props.data?.points?.length) return {}
  const dates = props.data.points.map(p => p.date)
  const errors = props.data.points.map(p => p.errorCount)
  const warnings = props.data.points.map(p => p.warningCount)
  const infos = props.data.points.map(p => p.infoCount)

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1e293b',
      borderColor: 'transparent',
      textStyle: { color: '#f1f5f9', fontSize: 12 },
      axisPointer: { type: 'cross', label: { backgroundColor: '#1e293b' } }
    },
    legend: {
      data: ['错误', '警告', '提示'],
      top: 0,
      textStyle: { color: '#64748b', fontSize: 12 },
      icon: 'circle'
    },
    grid: { left: '2%', right: '3%', bottom: '4%', top: '14%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f4f8', type: 'dashed' } },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [
      {
        name: '错误', type: 'line', data: errors,
        itemStyle: { color: '#ef4444' },
        lineStyle: { color: '#ef4444', width: 2.5 },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(239,68,68,0.2)' }, { offset: 1, color: 'rgba(239,68,68,0)' }] } },
        smooth: true, symbol: 'circle', symbolSize: 5
      },
      {
        name: '警告', type: 'line', data: warnings,
        itemStyle: { color: '#f59e0b' },
        lineStyle: { color: '#f59e0b', width: 2.5 },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(245,158,11,0.2)' }, { offset: 1, color: 'rgba(245,158,11,0)' }] } },
        smooth: true, symbol: 'circle', symbolSize: 5
      },
      {
        name: '提示', type: 'line', data: infos,
        itemStyle: { color: '#06b6d4' },
        lineStyle: { color: '#06b6d4', width: 2.5 },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(6,182,212,0.2)' }, { offset: 1, color: 'rgba(6,182,212,0)' }] } },
        smooth: true, symbol: 'circle', symbolSize: 5
      }
    ]
  }
})
</script>

<style scoped>
.chart-wrap {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #94a3b8;
  font-size: 13px;
}
</style>
