<template>
  <div class="topbar">
    <div class="topbar-left">
      <div class="breadcrumb">
        <span class="page-title">{{ pageTitle }}</span>
      </div>
    </div>
    <div class="topbar-right">
      <div class="user-info">
        <div class="avatar">{{ avatarChar }}</div>
        <span class="username">{{ userStore.username || '用户' }}</span>
      </div>
      <button class="logout-btn" @click="handleLogout">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
        </svg>
        退出
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
  '/dashboard': '仪表盘',
  '/project': '项目管理',
  '/rules': '规则配置',
}

const pageTitle = computed(() => {
  return pageTitles[route.path] || pageTitles[Object.keys(pageTitles).find(k => route.path.startsWith(k)) || ''] || '页面'
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
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.topbar-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f8cff, #a259ff);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #ef4444;
}
</style>
