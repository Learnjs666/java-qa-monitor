import request from './request'

export function createProject(data: { name: string; description?: string }) {
  return request.post('/project', data)
}

export function getProjectList() {
  return request.get('/project/list')
}

export function getProject(id: number) {
  return request.get(`/project/${id}`)
}

export function deleteProject(id: number) {
  return request.delete(`/project/${id}`)
}

export function uploadFiles(projectId: number, formData: FormData) {
  return request.post(`/project/${projectId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
