<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="32" height="32" rx="8" fill="url(#grad)"/>
          <path d="M8 22L14 10L20 18L24 14" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="24" cy="14" r="2" fill="#7ee8fa"/>
          <defs>
            <linearGradient id="grad" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
              <stop stop-color="#4f8cff"/>
              <stop offset="1" stop-color="#a259ff"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <span class="logo-text">Java QA Monitor</span>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-icon" v-html="item.icon"></span>
        <span class="nav-label">{{ item.label }}</span>
        <span class="nav-indicator" v-if="isActive(item.path)"></span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="version-badge">v1.0.0</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
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
  }
]

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.sidebar {
  height: 100%;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255,255,255,0.06);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 14px;
  font-weight: 700;
  color: #f1f5f9;
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px;
  border-radius: 10px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  transition: all 0.2s ease;
  cursor: pointer;
}

.nav-item:hover {
  background: rgba(255,255,255,0.06);
  color: #e2e8f0;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(79,140,255,0.2) 0%, rgba(162,89,255,0.15) 100%);
  color: #7eb6ff;
}

.nav-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.nav-label {
  flex: 1;
}

.nav-indicator {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #4f8cff;
  box-shadow: 0 0 6px #4f8cff;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.version-badge {
  font-size: 11px;
  color: #475569;
  text-align: center;
}
</style>
