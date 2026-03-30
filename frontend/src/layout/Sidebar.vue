<template>
  <div class="terminal-sidebar" :class="{ 'is-collapsed': isCollapsed }">
    
    <div class="brand-header">
      <div class="brand-top-row">
        <div class="brand-glitch" v-if="!isCollapsed">
          <span class="brand-text">QA_MONITOR</span>
          <span class="blink-cursor">_</span>
        </div>
        <div class="brand-glitch-mini" v-else>QA_</div>
        
        <button class="collapse-toggle" @click="toggleCollapse" :title="isCollapsed ? 'EXPAND' : 'COLLAPSE'">
          <svg v-if="!isCollapsed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square"><path d="M15 18l-6-6 6-6"/></svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="square"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
      
      <div class="system-status" v-if="!isCollapsed">
        <span class="dot pulse-lime"></span> ONLINE
      </div>
      <div class="system-status-mini" v-else title="SYSTEM ONLINE">
        <span class="dot pulse-lime"></span>
      </div>
    </div>

    <nav class="brutal-nav">
      <div class="nav-label-top" v-if="!isCollapsed">ROOT_DIRECTORY //</div>
      <div class="nav-label-top mini" v-else>ROOT/</div>

      <router-link
        v-for="item in visibleNavItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ 'is-active': isActive(item.path) }"
        :title="isCollapsed ? item.label : ''"
      >
        <div class="nav-content">
          <span class="nav-prefix" v-if="!isActive(item.path) && !isCollapsed">></span>
          <span class="nav-prefix active-prefix" v-else-if="!isCollapsed">[*]</span>
          
          <span class="nav-icon" v-html="item.icon"></span>
          
          <span class="nav-text" v-if="!isCollapsed">{{ item.label.toUpperCase() }}</span>
        </div>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="footer-data" v-if="!isCollapsed">
        <span>SYS.BUILD</span>
        <span class="highlight">v1.0.0</span>
      </div>
      <div class="footer-data-mini" v-else title="SYS.BUILD v1.0.0">
        <span class="highlight">v1</span>
      </div>
      
      <div class="barcode-mini" v-if="!isCollapsed">
        <svg viewBox="0 0 50 10" preserveAspectRatio="none">
          <path d="M0,0 h1 v10 h-1 Z M2,0 h2 v10 h-2 Z M5,0 h1 v10 h-1 Z M7,0 h3 v10 h-3 Z M11,0 h1 v10 h-1 Z M14,0 h2 v10 h-2 Z M18,0 h1 v10 h-1 Z M20,0 h3 v10 h-3 Z M25,0 h1 v10 h-1 Z M28,0 h2 v10 h-2 Z M32,0 h4 v10 h-4 Z M38,0 h1 v10 h-1 Z M41,0 h2 v10 h-2 Z M44,0 h1 v10 h-1 Z M47,0 h3 v10 h-3 Z" fill="currentColor"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const userStore = useUserStore()

// 控制折叠状态
const isCollapsed = ref(false)
const emit = defineEmits(['toggle'])

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  // 向外抛出事件，以便 MainLayout 可以动态调整包裹容器的宽度
  emit('toggle', isCollapsed.value)
}

// 导航配置表
const allNavItems = [
  {
    path: '/dashboard',
    label: '仪表盘',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z"/><path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z"/></svg>`
  },
  {
    path: '/project',
    label: '项目管理',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z"/></svg>`
  },
  {
    path: '/rules',
    label: '规则配置',
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clip-rule="evenodd"/></svg>`
  },
  {
    path: '/users',
    label: '用户管理',
    adminOnly: true, // 标记为仅管理员可见
    icon: `<svg viewBox="0 0 20 20" fill="currentColor"><path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/></svg>`
  }
]

// 计算属性：根据角色过滤可见的菜单项
const visibleNavItems = computed(() => {
  const isAdmin = userStore.role === 'ADMIN'
  return allNavItems.filter(item => !item.adminOnly || isAdmin)
})

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.terminal-sidebar {
  height: 100%;
  background-color: var(--bg-panel);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.25, 1, 0.5, 1);
  width: 260px; /* 默认展开宽度 */
  overflow: hidden;
}

.terminal-sidebar.is-collapsed {
  width: 80px; /* 折叠后的宽度 */
}

/* 顶部品牌区 */
.brand-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--clr-border);
  background: repeating-linear-gradient(
    45deg,
    rgba(0,0,0,0),
    rgba(0,0,0,0) 2px,
    rgba(204, 255, 0, 0.03) 2px,
    rgba(204, 255, 0, 0.03) 4px
  );
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-height: 90px;
}

.is-collapsed .brand-header {
  padding: 1.5rem 0;
  align-items: center;
}

.brand-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.is-collapsed .brand-top-row {
  flex-direction: column;
  gap: 1rem;
}

.brand-glitch {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--clr-accent);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.brand-glitch-mini {
  font-family: 'Clash Display', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--clr-accent);
}

.blink-cursor {
  animation: blink 1s step-end infinite;
  margin-left: 4px;
}

@keyframes blink { 50% { opacity: 0; } }

/* 折叠开关按钮 */
.collapse-toggle {
  background: transparent;
  border: 1px solid var(--clr-border);
  color: var(--clr-text-muted);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.collapse-toggle:hover {
  color: var(--clr-text-main);
  border-color: var(--clr-accent);
  background: rgba(204, 255, 0, 0.05);
}

.collapse-toggle svg { width: 16px; height: 16px; }

/* 状态灯 */
.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  white-space: nowrap;
}

.system-status-mini {
  margin-top: 8px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--clr-accent);
}

.pulse-lime {
  box-shadow: 0 0 8px var(--clr-accent);
}

/* 导航区域 */
.brutal-nav {
  flex: 1;
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.is-collapsed .brutal-nav {
  padding: 2rem 0.5rem;
  align-items: center;
}

.nav-label-top {
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  margin-bottom: 1rem;
  opacity: 0.5;
  white-space: nowrap;
}

.nav-label-top.mini {
  font-size: 0.6rem;
  text-align: center;
}

.nav-item {
  display: block;
  text-decoration: none;
  position: relative;
  width: 100%;
}

.is-collapsed .nav-item {
  width: 48px;
  height: 48px;
}

.nav-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0.8rem 1rem;
  border: 1px solid transparent;
  color: var(--clr-text-muted);
  font-size: 0.85rem;
  font-weight: 700;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.is-collapsed .nav-content {
  padding: 0;
  width: 100%;
  height: 100%;
  justify-content: center;
}

/* 悬停与激活态 */
.nav-item:hover .nav-content {
  border-color: var(--clr-border);
  color: var(--clr-text-main);
  background: var(--bg-dark);
}

.nav-item.is-active .nav-content {
  background: var(--clr-accent);
  color: #000;
  border-color: var(--clr-accent);
  box-shadow: 4px 4px 0px rgba(0,0,0,0.5);
  transform: translate(-2px, -2px);
}

.is-collapsed .nav-item.is-active .nav-content {
  box-shadow: 2px 2px 0px rgba(0,0,0,0.5);
  transform: translate(-1px, -1px);
}

.nav-prefix {
  font-weight: 700;
  opacity: 0.5;
  width: 12px;
}

.nav-item.is-active .active-prefix {
  opacity: 1;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon :deep(svg) {
  width: 16px;
  height: 16px;
}

.is-collapsed .nav-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

/* 底部区域 */
.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid var(--clr-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 70px;
}

.is-collapsed .sidebar-footer {
  padding: 1rem 0;
  justify-content: center;
}

.footer-data {
  display: flex;
  flex-direction: column;
  font-size: 0.7rem;
  color: var(--clr-text-muted);
  white-space: nowrap;
}

.footer-data-mini {
  font-size: 0.7rem;
  color: var(--clr-text-muted);
  text-align: center;
}

.footer-data .highlight, .footer-data-mini .highlight {
  color: var(--clr-text-main);
  font-weight: 700;
}

.barcode-mini {
  width: 40px;
  height: 15px;
  color: var(--clr-text-muted);
  opacity: 0.3;
  flex-shrink: 0;
}
</style>