<template>
  <div class="main-layout">
    <aside class="sidebar-wrap">
      <Sidebar />
    </aside>
    <div class="content-wrap">
      <header class="header-wrap">
        <Topbar />
      </header>
      <main class="main-wrap">
        <RouterView v-slot="{ Component }">
          <Transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Topbar from './Topbar.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f1f5f9;
}

.sidebar-wrap {
  width: 220px;
  flex-shrink: 0;
  height: 100vh;
  overflow-y: auto;
}

.content-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header-wrap {
  flex-shrink: 0;
}

.main-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.22s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
