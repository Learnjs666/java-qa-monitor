<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">规则配置</h2>
      <div class="header-right">
        <div class="search-wrap">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" class="search-icon">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input v-model="searchText" class="search-input" placeholder="搜索规则名称或编码..." />
        </div>
        <div class="filter-tabs">
          <button
            v-for="cat in categories"
            :key="cat"
            class="filter-tab"
            :class="{ active: activeCategory === cat }"
            @click="activeCategory = cat"
          >
            {{ cat === 'ALL' ? '全部' : cat }}
          </button>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="rule-stats">
      <div class="rule-stat">
        <span class="stat-label">规则总数</span>
        <span class="stat-val">{{ rules.length }}</span>
      </div>
      <div class="rule-stat enabled">
        <span class="stat-label">已启用</span>
        <span class="stat-val">{{ enabledCount }}</span>
      </div>
      <div class="rule-stat disabled">
        <span class="stat-label">已禁用</span>
        <span class="stat-val">{{ rules.length - enabledCount }}</span>
      </div>
    </div>

    <!-- Rules Table -->
    <div class="section-card">
      <div v-if="loading" class="loading-rows">
        <div v-for="i in 6" :key="i" class="skeleton-row"></div>
      </div>

      <div v-else-if="filteredRules.length === 0" class="empty-state">
        <span>暂无匹配规则</span>
      </div>

      <div v-else class="rules-table">
        <div class="rules-head">
          <div class="col-name">规则名称</div>
          <div class="col-code">规则编码</div>
          <div class="col-cat">分类</div>
          <div class="col-desc">描述</div>
          <div class="col-sev">级别</div>
          <div class="col-thr">阈值</div>
          <div class="col-ena">启用</div>
        </div>

        <div
          v-for="row in filteredRules"
          :key="row.id"
          class="rules-row"
          :class="{ disabled: !row.enabled }"
        >
          <div class="col-name rule-name">{{ row.ruleName }}</div>
          <div class="col-code rule-code">{{ row.ruleCode }}</div>
          <div class="col-cat">
            <span class="cat-badge">{{ row.category }}</span>
          </div>
          <div class="col-desc rule-desc" :title="row.description">{{ row.description }}</div>
          <div class="col-sev">
            <el-select
              v-model="row.severity"
              size="small"
              @change="handleUpdate(row)"
              class="sev-select"
            >
              <el-option value="ERROR">
                <span class="sev-opt error">错误</span>
              </el-option>
              <el-option value="WARNING">
                <span class="sev-opt warning">警告</span>
              </el-option>
              <el-option value="INFO">
                <span class="sev-opt info">提示</span>
              </el-option>
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
              style="width: 80px"
            />
            <span v-else class="no-threshold">-</span>
          </div>
          <div class="col-ena">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleUpdate(row)"
              active-color="#4f8cff"
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
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
}

.page-title { font-size: 20px; font-weight: 800; color: #1e293b; margin: 0; }

.header-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }

.search-wrap {
  display: flex; align-items: center; gap: 8px;
  border: 1px solid #e2e8f0; border-radius: 8px; padding: 8px 14px;
  background: #fff; transition: border-color 0.2s;
}

.search-wrap:focus-within { border-color: #93c5fd; }
.search-icon { color: #94a3b8; flex-shrink: 0; }

.search-input {
  border: none; outline: none; background: transparent;
  font-size: 13px; color: #334155; width: 180px;
}

.filter-tabs { display: flex; gap: 4px; }

.filter-tab {
  padding: 7px 14px; border-radius: 8px;
  border: 1px solid #e2e8f0; background: #fff;
  color: #64748b; font-size: 12px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}

.filter-tab:hover { border-color: #93c5fd; color: #3b82f6; }
.filter-tab.active { background: #eff6ff; border-color: #93c5fd; color: #3b82f6; font-weight: 700; }

.rule-stats {
  display: flex; gap: 16px;
}

.rule-stat {
  background: #fff; border-radius: 12px; padding: 16px 24px;
  display: flex; flex-direction: column; gap: 4px;
  border: 1px solid #f0f4f8; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  min-width: 100px;
}

.stat-label { font-size: 11px; color: #94a3b8; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-val { font-size: 22px; font-weight: 800; color: #1e293b; }
.rule-stat.enabled .stat-val { color: #22c55e; }
.rule-stat.disabled .stat-val { color: #94a3b8; }

.section-card {
  background: #fff; border-radius: 16px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05); border: 1px solid #f0f4f8;
  overflow-x: auto;
}

.loading-rows { display: flex; flex-direction: column; gap: 8px; }
.skeleton-row { height: 52px; background: #f1f5f9; border-radius: 8px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }

.empty-state {
  display: flex; align-items: center; justify-content: center;
  padding: 48px; color: #94a3b8; font-size: 13px;
}

.rules-table { min-width: 900px; }

.rules-head {
  display: grid;
  grid-template-columns: 140px 200px 100px 1fr 110px 90px 80px;
  padding: 8px 14px;
  font-size: 11px; font-weight: 600; color: #94a3b8;
  text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 1px solid #f0f4f8;
}

.rules-row {
  display: grid;
  grid-template-columns: 140px 200px 100px 1fr 110px 90px 80px;
  padding: 13px 14px;
  border-bottom: 1px solid #f8fafc;
  align-items: center; transition: background 0.15s;
}

.rules-row:hover { background: #f8fafc; }
.rules-row:last-child { border-bottom: none; }
.rules-row.disabled { opacity: 0.5; }

.rule-name { font-size: 13px; font-weight: 600; color: #334155; }
.rule-code { font-size: 11px; font-family: monospace; color: #64748b; }
.rule-desc {
  font-size: 12px; color: #64748b;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  padding-right: 12px;
}

.cat-badge {
  display: inline-block; padding: 3px 8px; border-radius: 6px;
  background: #f1f5f9; color: #475569; font-size: 11px; font-weight: 600;
}

.sev-select { width: 90px; }

.sev-opt { font-size: 12px; font-weight: 600; }
.sev-opt.error { color: #ef4444; }
.sev-opt.warning { color: #f59e0b; }
.sev-opt.info { color: #06b6d4; }

.no-threshold { color: #cbd5e1; font-size: 13px; }
</style>
