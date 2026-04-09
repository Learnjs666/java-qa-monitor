<template>
  <div class="sq-topbar">
    <div class="topbar-left">
      <!-- 移除冗余的面包屑文字，保持与 SonarQube 一致的简洁感 -->
    </div>
    <div class="topbar-right">
      <div class="user-profile">
        <div class="avatar">{{ avatarChar }}</div>
        <div class="user-info">
          <span class="username">{{ userStore.username || '用户' }}</span>
        </div>
      </div>
      <button class="logout-btn" @click="handleLogout" title="退出登录">
        <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16">
          <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getUserInfo } from '../api/user'

const userStore = useUserStore()
const router = useRouter()

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
.sq-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #eef2f5;
  color: #0271b6;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #c9d1d9;
}

.username {
  font-size: 13px;
  font-weight: 500;
  color: #333333;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 3px;
  border: 1px solid transparent;
  background: transparent;
  color: #777777;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: #f3f4f6;
  color: #d4333f;
}
</style>