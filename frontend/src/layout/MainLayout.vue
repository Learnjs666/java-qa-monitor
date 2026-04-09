<template>
  <div class="sq-layout">
    <aside class="sq-sidebar-wrap" :class="{ 'is-collapsed': isSidebarCollapsed }">
      <Sidebar @toggle="handleSidebarToggle" />
    </aside>
    
    <div class="sq-content-wrap">
      <header class="sq-header-wrap">
        <Topbar />
      </header>
      <main class="sq-main-wrap">
        <div class="main-container">
          <RouterView v-slot="{ Component }">
            <Transition name="fade" mode="out-in">
              <component :is="Component" />
            </Transition>
          </RouterView>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Topbar from './Topbar.vue'

const isSidebarCollapsed = ref(false)

function handleSidebarToggle(collapsed: boolean) {
  isSidebarCollapsed.value = collapsed
}
</script>

<style scoped>
.sq-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #f3f4f6; /* SonarQube 标准背景色 */
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.sq-sidebar-wrap {
  width: 240px;
  flex-shrink: 0;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e1e6eb;
  transition: width 0.3s ease;
  z-index: 20;
}

.sq-sidebar-wrap.is-collapsed {
  width: 64px; 
}

.sq-content-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.sq-header-wrap {
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #e1e6eb;
  z-index: 10;
}

.sq-main-wrap {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cccccc #f3f4f6;
}

.main-container {
  padding: 24px 32px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 简单的淡入淡出，没有花哨的偏移 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>