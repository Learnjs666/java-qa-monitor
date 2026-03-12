import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '../api/user'
import { setToken, removeToken } from '../utils/token'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const username = ref('')
  const role = ref('')

  async function login(form: { username: string; password: string }) {
    const res: any = await loginApi(form)
    token.value = res.data.token
    username.value = res.data.username
    role.value = res.data.role
    setToken(res.data.token)
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    removeToken()
  }

  function setUserInfo(info: { username: string; role: string }) {
    username.value = info.username
    role.value = info.role
  }

  return { token, username, role, login, logout, setUserInfo }
})
