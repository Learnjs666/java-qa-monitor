<template>
  <div class="terminal-topbar">
    <div class="process-indicator">
      <span class="process-label">当前进程</span>
      <span class="separator">//</span>
      <h1 class="page-title">{{ pageTitle.toUpperCase() }}</h1>
    </div>

    <div class="system-controls">
      <div class="user-block">
        <div class="avatar-square">[{{ avatarChar }}]</div>
        <div class="user-details">
          <span class="role-label">用户</span>
          <span class="username">{{ userStore.username || 'UNKNOWN_ENTITY' }}</span>
        </div>
      </div>
      
      <button class="brutal-logout-btn" @click="handleLogout">
        <span class="btn-text">退出</span>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
          <path d="M18.36 6.64a9 9 0 1 1-12.73 0M12 2v10" stroke-linecap="square" stroke-linejoin="miter"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { getUserInfo } from '../api/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const pageTitles: Record<string, string> = {
  '/dashboard': 'Dashboard_Metrics',
  '/project': 'Project_Matrix',
  '/rules': 'Rule_Configurations',
}

const pageTitle = computed(() => {
  return pageTitles[route.path] || pageTitles[Object.keys(pageTitles).find(k => route.path.startsWith(k)) || ''] || 'Standby_Mode'
})

const avatarChar = computed(() => {
  const name = userStore.username || '?'
  return name.charAt(0).toUpperCase()
})

onMounted(async () => {
  if (!userStore.username) {
    try {
      const res: any = await getUserInfo()
      userStore.setUserInfo({ username: res.data.username, role: res.data.role })
    } catch { /* ignore */ }
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.terminal-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  padding: 0 2rem;
  background-color: var(--bg-panel);
}

.process-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
}

.process-label {
  font-size: 0.75rem;
  color: var(--clr-text-muted);
  font-weight: 700;
}

.separator {
  color: var(--clr-accent);
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--clr-text-main);
  letter-spacing: 0.05em;
}

.system-controls {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.user-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 2rem;
  border-right: 1px dashed var(--clr-border);
}

.avatar-square {
  width: 36px;
  height: 36px;
  background: var(--bg-dark);
  border: 1px solid var(--clr-accent);
  color: var(--clr-accent);
  font-size: 1rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 0px rgba(204, 255, 0, 0.2);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.role-label {
  font-size: 0.65rem;
  color: var(--clr-text-muted);
}

.username {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--clr-text-main);
}

/* 终止按钮 (Brutalist style) */
.brutal-logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--clr-border);
  color: var(--clr-text-muted);
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.brutal-logout-btn:hover {
  background: #ff3366; /* Danger Red */
  border-color: #ff3366;
  color: #fff;
  box-shadow: 4px 4px 0px rgba(255, 51, 102, 0.3);
  transform: translate(-2px, -2px);
}

.brutal-logout-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 0px 0px 0px transparent;
}
</style>