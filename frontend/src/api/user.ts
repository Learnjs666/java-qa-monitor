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
