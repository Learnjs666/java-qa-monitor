<template>
  <div class="terminal-layout">
    <div class="global-grid"></div>

    <aside class="brutal-sidebar-wrap" :class="{ 'is-collapsed': isSidebarCollapsed }">
      <Sidebar @toggle="handleSidebarToggle" />
    </aside>
    
    <div class="brutal-content-wrap">
      <header class="brutal-header-wrap">
        <Topbar />
      </header>
      <main class="brutal-main-wrap">
        <RouterView v-slot="{ Component }">
          <Transition name="glitch-slide" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Topbar from './Topbar.vue'

// 管理侧边栏的折叠状态
const isSidebarCollapsed = ref(false)

// 接收 Sidebar 传出的折叠状态
function handleSidebarToggle(collapsed: boolean) {
  isSidebarCollapsed.value = collapsed
}
</script>

<style scoped>
/* 全局注入顶级设计字体 */
@import url('https://api.fontshare.com/v2/css?f[]=clash-display@600,700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.terminal-layout {
  --bg-dark: #090a0f;
  --bg-panel: #11131a;
  --clr-accent: #ccff00;
  --clr-text-main: #ffffff;
  --clr-text-muted: #6b7280;
  --clr-border: #272a35;

  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: var(--bg-dark);
  font-family: 'Space Mono', monospace;
  position: relative;
  color: var(--clr-text-main);
}

.global-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(var(--clr-border) 1px, transparent 1px),
    linear-gradient(90deg, var(--clr-border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.2;
  pointer-events: none;
  z-index: 0;
}

/* 侧边栏外部容器 */
.brutal-sidebar-wrap {
  width: 260px; /* 默认展开宽度 */
  flex-shrink: 0;
  height: 100vh;
  position: relative;
  z-index: 10;
  border-right: 1px solid var(--clr-border);
  /* 增加机械感平滑过渡动画 */
  transition: width 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

/* 折叠后的宽度 */
.brutal-sidebar-wrap.is-collapsed {
  width: 80px; 
}

.brutal-content-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
  z-index: 10;
}

.brutal-header-wrap {
  flex-shrink: 0;
  border-bottom: 1px solid var(--clr-border);
}

.brutal-main-wrap {
  flex: 1;
  overflow-y: auto;
  /* 自定义硬核滚动条 */
  scrollbar-width: thin;
  scrollbar-color: var(--clr-accent) var(--bg-dark);
}

.brutal-main-wrap::-webkit-scrollbar {
  width: 8px;
}
.brutal-main-wrap::-webkit-scrollbar-track {
  background: var(--bg-dark);
  border-left: 1px solid var(--clr-border);
}
.brutal-main-wrap::-webkit-scrollbar-thumb {
  background: var(--clr-accent);
}

/* 机械感切换动画 */
.glitch-slide-enter-active,
.glitch-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

.glitch-slide-enter-from {
  opacity: 0;
  transform: translateX(10px) scale(0.99);
  filter: contrast(1.5) grayscale(1);
}

.glitch-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px) scale(0.99);
}
</style>