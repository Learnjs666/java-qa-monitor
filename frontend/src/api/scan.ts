import request from './request'

export function runScan(projectId: number) {
  return request.post(`/scan/run/${projectId}`)
}

export function getTaskList(projectId: number) {
  return request.get(`/scan/task/list/${projectId}`)
}

export function getTask(taskId: number) {
  return request.get(`/scan/task/${taskId}`)
}

export function getIssues(taskId: number) {
  return request.get(`/scan/issues/${taskId}`)
}

export function getMetrics(taskId: number) {
  return request.get(`/scan/metrics/${taskId}`)
}
