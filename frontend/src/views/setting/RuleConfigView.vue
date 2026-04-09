<template>
  <div class="sq-page">
    <div class="sq-page-header">
      <h2 class="sq-page-title">规则配置</h2>
      <div class="header-right">
        <div class="sq-search-wrap">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input v-model="searchText" class="sq-search-input" placeholder="搜索规则名称或编码..." />
        </div>
        <div class="sq-filter-tabs">
          <button
            v-for="cat in categories"
            :key="cat"
            class="sq-filter-tab"
            :class="{ active: activeCategory === cat }"
            @click="activeCategory = cat"
          >
            {{ cat === 'ALL' ? '全部' : cat }}
          </button>
        </div>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="sq-stats-banner">
      <div class="sq-stat-item">
        <span class="stat-val">{{ rules.length }}</span>
        <span class="stat-label">规则总数</span>
      </div>
      <div class="stat-divider"></div>
      <div class="sq-stat-item">
        <span class="stat-val text-green">{{ enabledCount }}</span>
        <span class="stat-label">已启用</span>
      </div>
      <div class="stat-divider"></div>
      <div class="sq-stat-item">
        <span class="stat-val text-gray">{{ rules.length - enabledCount }}</span>
        <span class="stat-label">已禁用</span>
      </div>
    </div>

    <!-- 规则表格 -->
    <div class="sq-panel">
      <div v-if="loading" class="sq-loading-placeholder">
        <div v-for="i in 6" :key="i" class="sq-skeleton-row"></div>
      </div>

      <div v-else-if="filteredRules.length === 0" class="sq-empty-state">
        暂无匹配规则
      </div>

      <div v-else class="sq-table">
        <div class="table-header">
          <div class="col-name">规则名称</div>
          <div class="col-code">规则编码</div>
          <div class="col-cat">分类</div>
          <div class="col-desc">描述</div>
          <div class="col-sev">级别</div>
          <div class="col-thr">阈值</div>
          <div class="col-ena">状态</div>
        </div>

        <div
          v-for="row in filteredRules"
          :key="row.id"
          class="table-row"
          :class="{ disabled: !row.enabled }"
        >
          <div class="col-name rule-name">{{ row.ruleName }}</div>
          <div class="col-code rule-code">{{ row.ruleCode }}</div>
          <div class="col-cat"><span class="cat-badge">{{ row.category }}</span></div>
          <div class="col-desc rule-desc" :title="row.description">{{ row.description }}</div>
          <div class="col-sev">
            <el-select v-model="row.severity" size="small" @change="handleUpdate(row)" class="sq-select-mini">
              <el-option value="ERROR"><span class="sev-opt error">ERROR</span></el-option>
              <el-option value="WARNING"><span class="sev-opt warning">WARNING</span></el-option>
              <el-option value="INFO"><span class="sev-opt info">INFO</span></el-option>
            </el-select>
          </div>
          <div class="col-thr">
            <el-input-number
              v-if="row.thresholdValue !== null"
              v-model="row.thresholdValue"
              size="small"
              :min="1"
              controls-position="right"
              @change="handleUpdate(row)"
              class="sq-input-number-mini"
            />
            <span v-else class="no-threshold">-</span>
          </div>
          <div class="col-ena">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleUpdate(row)"
              style="--el-switch-on-color: #00aa00; --el-switch-off-color: #cccccc;"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const rules = ref<any[]>([])
const loading = ref(false)
const searchText = ref('')
const activeCategory = ref('ALL')

const categories = computed(() => {
  const cats = new Set(rules.value.map(r => r.category).filter(Boolean))
  return ['ALL', ...Array.from(cats)]
})

const filteredRules = computed(() => {
  let list = rules.value
  if (activeCategory.value !== 'ALL') list = list.filter(r => r.category === activeCategory.value)
  if (searchText.value) {
    const q = searchText.value.toLowerCase()
    list = list.filter(r => r.ruleName?.toLowerCase().includes(q) || r.ruleCode?.toLowerCase().includes(q))
  }
  return list
})

const enabledCount = computed(() => rules.value.filter(r => r.enabled).length)

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await request.get('/rule/list')
    rules.value = res.data
  } finally {
    loading.value = false
  }
})

async function handleUpdate(row: any) {
  try {
    await request.put(`/rule/${row.id}`, {
      severity: row.severity,
      enabled: row.enabled,
      thresholdValue: row.thresholdValue
    })
    ElMessage.success('更新成功')
  } catch { /* error handled by interceptor */ }
}
</script>

<style scoped>
.sq-page { display: flex; flex-direction: column; gap: 20px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333; }
.sq-page-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; border-bottom: 1px solid #e1e6eb; padding-bottom: 12px; }
.sq-page-title { font-size: 20px; font-weight: 600; margin: 0; color: #222222; }

.header-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }

.sq-search-wrap {
  display: flex; align-items: center; gap: 8px;
  border: 1px solid #cccccc; border-radius: 3px; padding: 6px 10px;
  background: #ffffff; transition: border-color 0.2s;
}
.sq-search-wrap:focus-within { border-color: #0271b6; }
.search-icon { color: #999999; }
.sq-search-input { border: none; outline: none; font-size: 13px; color: #333333; width: 180px; }

.sq-filter-tabs { display: flex; border: 1px solid #cccccc; border-radius: 3px; overflow: hidden; }
.sq-filter-tab {
  padding: 6px 14px; border: none; border-right: 1px solid #cccccc; background: #ffffff;
  color: #555555; font-size: 12px; font-weight: 500; cursor: pointer; transition: background 0.15s;
}
.sq-filter-tab:last-child { border-right: none; }
.sq-filter-tab:hover { background-color: #f9f9fb; }
.sq-filter-tab.active { background-color: #eef2f5; color: #0271b6; font-weight: 600; }

.sq-stats-banner {
  display: flex; background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px;
  padding: 16px 24px; gap: 40px; box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.sq-stat-item { display: flex; align-items: baseline; gap: 8px; }
.stat-val { font-size: 24px; font-weight: 600; color: #333333; line-height: 1; }
.stat-label { font-size: 13px; color: #777777; }
.text-green { color: #00aa00; }
.text-gray { color: #999999; }
.stat-divider { width: 1px; background: #e1e6eb; }

.sq-panel { background: #ffffff; border: 1px solid #e1e6eb; border-radius: 2px; overflow-x: auto; box-shadow: 0 1px 2px rgba(0,0,0,0.03); }

.sq-loading-placeholder { display: flex; flex-direction: column; gap: 8px; padding: 16px; }
.sq-skeleton-row { height: 40px; background: #f3f4f6; animation: pulse 1.5s infinite; }

.sq-empty-state { padding: 40px; text-align: center; color: #777777; font-size: 13px; }

.sq-table { min-width: 900px; display: flex; flex-direction: column; }
.table-header {
  display: grid; grid-template-columns: 160px 200px 100px 1fr 110px 100px 80px;
  padding: 10px 16px; font-size: 12px; font-weight: 600; color: #777777;
  border-bottom: 1px solid #e1e6eb; background-color: #fafbfc;
}
.table-row {
  display: grid; grid-template-columns: 160px 200px 100px 1fr 110px 100px 80px;
  padding: 12px 16px; border-bottom: 1px solid #f3f4f6;
  align-items: center; font-size: 13px; transition: background 0.15s;
}
.table-row:hover { background-color: #f9f9fb; }
.table-row:last-child { border-bottom: none; }
.table-row.disabled { opacity: 0.6; }

.rule-name { font-weight: 600; color: #333333; }
.rule-code { font-family: Consolas, monospace; color: #555555; font-size: 12px; }
.rule-desc { color: #666666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 12px; }

.cat-badge {
  background: #f3f4f6; border: 1px solid #e1e6eb; padding: 2px 6px;
  border-radius: 2px; font-size: 11px; color: #555555;
}

.sq-select-mini :deep(.el-input__wrapper) { border-radius: 3px !important; box-shadow: none !important; border: 1px solid #cccccc !important; height: 28px; }
.sq-select-mini :deep(.el-input__inner) { font-size: 12px; color: #333; }

.sev-opt { font-weight: 600; font-size: 11px; }
.sev-opt.error { color: #d4333f; }
.sev-opt.warning { color: #ed7d20; }
.sev-opt.info { color: #00aa00; }

.sq-input-number-mini :deep(.el-input__wrapper) { border-radius: 3px !important; box-shadow: none !important; border: 1px solid #cccccc !important; height: 28px; }
.no-threshold { color: #cccccc; }
</style>