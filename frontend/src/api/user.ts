import request from './request'

export function login(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}

export function register(data: { username: string; password: string; role?: string }) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUserList() {
  return request.get('/user/list')
}

export function updateUser(id: number, role: string) {
  return request.put(`/user/${id}`, { role })
}

export function updateUserStatus(id: number, status: boolean) {
  return request.put(`/user/${id}/status`, { status: !status })
}