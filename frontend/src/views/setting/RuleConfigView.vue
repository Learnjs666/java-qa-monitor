<template>
  <div class="terminal-page">
    <div class="page-header">
      <div class="header-title-group">
        <span class="blinking-cursor">_</span>
        <h2 class="display-title">RULE_CONFIG // REGISTRY</h2>
      </div>
      <div class="header-right">
        <div class="brutal-search-wrap">
          <span class="search-prefix">QUERY></span>
          <input v-model="searchText" class="brutal-search-input" placeholder="INPUT_KEYWORD..." />
        </div>
        <div class="brutal-filter-tabs">
          <button
            v-for="cat in categories"
            :key="cat"
            class="brutal-filter-tab"
            :class="{ active: activeCategory === cat }"
            @click="activeCategory = cat"
          >
            [{{ cat === 'ALL' ? 'GLOBAL' : cat }}]
          </button>
        </div>
      </div>
    </div>

    <div class="rule-stats-grid">
      <div class="brutal-stat-block default">
        <span class="stat-label">TOTAL_RULES</span>
        <span class="stat-val">{{ rules.length }}</span>
      </div>
      <div class="brutal-stat-block enabled">
        <span class="stat-label">ACTIVE_NODES</span>
        <span class="stat-val">{{ enabledCount }}</span>
      </div>
      <div class="brutal-stat-block disabled">
        <span class="stat-label">OFFLINE_NODES</span>
        <span class="stat-val">{{ rules.length - enabledCount }}</span>
      </div>
    </div>

    <div class="brutal-table-panel">
      <div v-if="loading" class="terminal-loading">
        <div v-for="i in 6" :key="i" class="brutal-skeleton-row"></div>
      </div>

      <div v-else-if="filteredRules.length === 0" class="terminal-empty">
        <span class="blink">_</span> NO_MATCHING_RULES_FOUND. CHECK YOUR QUERY PARAMETERS.
      </div>

      <div v-else class="brutal-rules-table">
        <div class="table-header">
          <div class="col-name">RULE_ID</div>
          <div class="col-code">IDENTIFIER</div>
          <div class="col-cat">NAMESPACE</div>
          <div class="col-desc">DESCRIPTION_PAYLOAD</div>
          <div class="col-sev">SEVERITY</div>
          <div class="col-thr">THRESHOLD</div>
          <div class="col-ena">STATUS</div>
        </div>

        <div
          v-for="row in filteredRules"
          :key="row.id"
          class="table-row"
          :class="{ 'is-disabled': !row.enabled }"
        >
          <div class="col-name rule-name">> {{ row.ruleName }}</div>
          <div class="col-code rule-code">{{ row.ruleCode }}</div>
          <div class="col-cat">
            <span class="cat-badge">{{ row.category }}</span>
          </div>
          <div class="col-desc rule-desc" :title="row.description">{{ row.description }}</div>
          <div class="col-sev">
            <div class="brutal-select-wrapper">
              <el-select
                v-model="row.severity"
                size="small"
                @change="handleUpdate(row)"
                class="brutal-select"
                popper-class="brutal-popper"
              >
                <el-option value="ERROR"><span class="sev-opt error">[ ERROR ]</span></el-option>
                <el-option value="WARNING"><span class="sev-opt warning">[ WARN ]</span></el-option>
                <el-option value="INFO"><span class="sev-opt info">[ INFO ]</span></el-option>
              </el-select>
            </div>
          </div>
          <div class="col-thr">
            <el-input-number
              v-if="row.thresholdValue !== null"
              v-model="row.thresholdValue"
              size="small"
              :min="1"
              controls-position="right"
              @change="handleUpdate(row)"
              class="brutal-number-input"
            />
            <span v-else class="no-threshold">N/A</span>
          </div>
          <div class="col-ena">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleUpdate(row)"
              style="--el-switch-on-color: var(--clr-success); --el-switch-off-color: var(--clr-border);"
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
@import url('https://api.fontshare.com/v2/css?f[]=clash-display@600,700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.terminal-page {
  --bg-dark: #090a0f;
  --bg-panel: #11131a;
  --bg-card: #161922;
  --clr-accent: #ccff00;
  --clr-success: #00e5ff;
  --clr-warning: #ffaa00;
  --clr-danger: #ff3366;
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;

  display: flex;
  flex-direction: column;
  gap: 2rem;
  font-family: 'Space Mono', monospace;
  color: var(--clr-text-main);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid var(--clr-border);
  padding-bottom: 1.5rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.blinking-cursor {
  font-size: 2rem;
  color: var(--clr-accent);
  animation: blink 1s step-end infinite;
}

@keyframes blink { 50% { opacity: 0; } }

.display-title {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--clr-text-main);
  margin: 0;
  letter-spacing: 0.05em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

/* 工业风搜索框 */
.brutal-search-wrap {
  display: flex;
  align-items: center;
  background: var(--bg-dark);
  border: 1px solid var(--clr-border);
  transition: all 0.2s ease;
}

.brutal-search-wrap:focus-within {
  border-color: var(--clr-accent);
  box-shadow: 4px 4px 0px rgba(204, 255, 0, 0.15);
}

.search-prefix {
  padding: 0 0.75rem;
  font-size: 0.8rem;
  color: var(--clr-accent);
  font-weight: 700;
  border-right: 1px solid var(--clr-border);
}

.brutal-search-input {
  border: none;
  outline: none;
  background: transparent;
  color: var(--clr-text-main);
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  padding: 0.5rem 0.75rem;
  width: 200px;
}
.brutal-search-input::placeholder { color: var(--clr-text-muted); }

/* 工业风过滤标签 */
.brutal-filter-tabs { display: flex; gap: 0.5rem; }

.brutal-filter-tab {
  background: transparent;
  border: 1px solid var(--clr-border);
  color: var(--clr-text-muted);
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.brutal-filter-tab:hover {
  border-color: var(--clr-text-main);
  color: var(--clr-text-main);
}

.brutal-filter-tab.active {
  background: var(--clr-accent);
  color: #000;
  border-color: var(--clr-accent);
  box-shadow: 3px 3px 0px rgba(204, 255, 0, 0.2);
  transform: translate(-1px, -1px);
}

/* 统计模块 */
.rule-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.brutal-stat-block {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  position: relative;
}
.brutal-stat-block::after {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 3px; height: 100%;
}

.brutal-stat-block.default::after { background: var(--clr-text-muted); }
.brutal-stat-block.enabled::after { background: var(--clr-success); }
.brutal-stat-block.disabled::after { background: var(--clr-danger); }

.stat-label {
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  font-weight: 700;
}

.stat-val {
  font-family: 'Clash Display', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1;
  color: var(--clr-text-main);
}
.brutal-stat-block.enabled .stat-val { color: var(--clr-success); text-shadow: 0 0 10px rgba(0,229,255,0.3); }
.brutal-stat-block.disabled .stat-val { color: var(--clr-danger); }

/* 数据表格模块 */
.brutal-table-panel {
  background: var(--bg-panel);
  border: 1px solid var(--clr-border);
  box-shadow: 8px 8px 0px rgba(0,0,0,0.5);
  overflow-x: auto;
  min-height: 400px;
}

.terminal-loading { padding: 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
.brutal-skeleton-row { height: 50px; background: var(--bg-card); opacity: 0.5; animation: pulse-slow 2s infinite; }
@keyframes pulse-slow { 0%, 100% { opacity: 0.3; } 50% { opacity: 0.7; } }

.terminal-empty {
  padding: 4rem; text-align: center; color: var(--clr-text-muted); font-size: 0.85rem;
}

.brutal-rules-table { min-width: 1000px; }

.table-header {
  display: grid;
  grid-template-columns: 180px 220px 120px 1fr 120px 100px 80px;
  padding: 1rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--clr-accent);
  background: var(--bg-dark);
  border-bottom: 1px solid var(--clr-border);
}

.table-row {
  display: grid;
  grid-template-columns: 180px 220px 120px 1fr 120px 100px 80px;
  padding: 1rem;
  align-items: center;
  border-bottom: 1px dashed var(--clr-border);
  transition: background 0.2s;
}
.table-row:hover { background: var(--bg-card); }
.table-row:last-child { border-bottom: none; }
.table-row.is-disabled { opacity: 0.4; filter: grayscale(1); }

.rule-name { font-weight: 700; color: var(--clr-text-main); font-size: 0.85rem; }
.rule-code { color: var(--clr-text-muted); font-size: 0.8rem; }
.rule-desc {
  font-family: 'Noto Sans SC', sans-serif;
  font-size: 0.8rem;
  color: var(--clr-text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  padding-right: 1rem;
}

.cat-badge {
  display: inline-block; padding: 2px 6px; border: 1px solid var(--clr-text-muted);
  color: var(--clr-text-muted); font-size: 0.7rem; font-weight: 700;
}

/* 深度定制组件样式 */
.brutal-select-wrapper { display: inline-block; width: 100px; }
.brutal-select :deep(.el-input__wrapper) {
  background: var(--bg-dark) !important;
  border: 1px solid var(--clr-border) !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}
.brutal-select :deep(.el-input__inner) {
  color: var(--clr-text-main) !important;
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
}

.sev-opt { font-weight: 700; font-family: 'Space Mono', monospace; }
.sev-opt.error { color: var(--clr-danger); }
.sev-opt.warning { color: var(--clr-warning); }
.sev-opt.info { color: var(--clr-success); }

/* 重写 input-number */
.brutal-number-input :deep(.el-input__wrapper) {
  background: var(--bg-dark) !important;
  border: 1px solid var(--clr-border) !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}
.brutal-number-input :deep(.el-input__inner) {
  color: var(--clr-text-main) !important;
  font-family: 'Space Mono', monospace;
}
.brutal-number-input :deep(.el-input-number__increase),
.brutal-number-input :deep(.el-input-number__decrease) {
  background: var(--bg-panel) !important;
  border-color: var(--clr-border) !important;
  color: var(--clr-text-main) !important;
  border-radius: 0 !important;
}

.no-threshold { color: var(--clr-border); font-size: 0.85rem; }
</style>