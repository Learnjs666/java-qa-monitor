<template>
  <div class="chart-wrap">
    <div v-if="!props.data?.scores" class="chart-empty">
      <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
        <polygon points="24,6 42,38 6,38" fill="#f1f5f9" stroke="#e2e8f0" stroke-width="2"/>
      </svg>
      <span>选择项目后显示健康度数据</span>
    </div>
    <v-chart v-else :option="chartOption" autoresize />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([RadarChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<{
  data: { scores: { dimension: string; score: number }[] } | null
}>()

const chartOption = computed(() => {
  if (!props.data?.scores) return {}
  const indicators = props.data.scores.map(s => ({ name: s.dimension, max: 100 }))
  const values = props.data.scores.map(s => s.score)

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1e293b',
      borderColor: 'transparent',
      textStyle: { color: '#f1f5f9', fontSize: 12 }
    },
    radar: {
      indicator: indicators,
      shape: 'polygon',
      splitNumber: 5,
      axisName: { color: '#64748b', fontSize: 12, fontWeight: '500' },
      splitLine: { lineStyle: { color: '#f0f4f8' } },
      splitArea: { show: true, areaStyle: { color: ['rgba(248,250,252,0.8)', 'rgba(241,245,249,0.8)'] } },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '健康度',
        areaStyle: {
          opacity: 0.25,
          color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 1,
            colorStops: [{ offset: 0, color: '#4f8cff' }, { offset: 1, color: '#a259ff' }] }
        },
        lineStyle: { width: 2.5, color: '#4f8cff' },
        itemStyle: { color: '#4f8cff', borderColor: '#fff', borderWidth: 2 },
        symbol: 'circle',
        symbolSize: 6
      }]
    }]
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
