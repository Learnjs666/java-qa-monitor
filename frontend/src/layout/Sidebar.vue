<template>
  <div class="sq-sidebar" :class="{ 'is-collapsed': isCollapsed }">
    <div class="brand-header">
      <div class="brand-content">
        <svg class="brand-icon" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 16h8l4-8 4 16 4-8h4" stroke="#0271b6" stroke-width="3" stroke-linecap="square" stroke-linejoin="miter"/>
        </svg>
        <span class="brand-text" v-if="!isCollapsed">QA Monitor</span>
      </div>
      <button class="collapse-toggle" @click="toggleCollapse" :title="isCollapsed ? '展开' : '收起'">
        <svg v-if="!isCollapsed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
      </button>
    </div>

    <nav class="sq-nav">
      <router-link
        v-for="item in visibleNavItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ 'is-active': isActive(item.path) }"
        :title="isCollapsed ? item.label : ''"
      >
        <span class="nav-icon" v-html="item.icon"></span>
        <span class="nav-text" v-if="!isCollapsed">{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)
const emit = defineEmits(['toggle'])

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  emit('toggle', isCollapsed.value)
}

const allNavItems = [
  {
    path: '/dashboard', label: '仪表盘',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>`
  },
  {
    path: '/project', label: '项目管理',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>`
  },
  {
    path: '/rules', label: '规则配置',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9c.26.6.8.98 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>`
  },
  {
    path: '/users', label: '用户管理', adminOnly: true,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`
  }
]

const visibleNavItems = computed(() => {
  const isAdmin = userStore.role === 'ADMIN'
  return allNavItems.filter(item => !item.adminOnly || isAdmin)
})

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.sq-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.brand-header {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e1e6eb;
}

.is-collapsed .brand-header {
  flex-direction: column;
  padding: 16px 0;
  height: auto;
  gap: 16px;
}

.brand-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.brand-text {
  font-size: 15px;
  font-weight: 600;
  color: #333333;
  white-space: nowrap;
}

.collapse-toggle {
  background: transparent;
  border: none;
  color: #777777;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 3px;
}
.collapse-toggle:hover { background-color: #f3f4f6; color: #333333; }
.collapse-toggle svg { width: 16px; height: 16px; }

.sq-nav {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 3px;
  color: #444444;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.15s;
}

.is-collapsed .nav-item {
  justify-content: center;
  padding: 10px 0;
}

.nav-item:hover {
  background-color: #f9f9fb;
  color: #0271b6;
}

.nav-item.is-active {
  background-color: #eef2f5;
  color: #0271b6;
  font-weight: 600;
}

.nav-icon {
  display: flex;
  align-items: center;
}
.nav-icon :deep(svg) { width: 16px; height: 16px; }

.is-collapsed .nav-icon :deep(svg) { width: 20px; height: 20px; }
</style>