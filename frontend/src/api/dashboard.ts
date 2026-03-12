import request from './request'

export function getOverview() {
  return request.get('/dashboard/overview')
}

export function getHealthRadar(projectId: number) {
  return request.get(`/dashboard/health/${projectId}`)
}

export function getTrend(projectId: number) {
  return request.get(`/dashboard/trend/${projectId}`)
}
