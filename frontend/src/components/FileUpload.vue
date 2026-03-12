<template>
  <div class="upload-zone">
    <el-upload
      :action="`/api/project/${props.projectId}/upload`"
      name="files"
      :headers="headers"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :on-progress="handleProgress"
      multiple
      accept=".java,.zip"
      drag
      class="custom-uploader"
    >
      <div class="upload-content">
        <div class="upload-icon-wrap" :class="{ uploading: isUploading }">
          <svg viewBox="0 0 48 48" fill="none" width="48" height="48">
            <circle cx="24" cy="24" r="22" fill="url(#uploadGrad)" opacity="0.12"/>
            <path d="M24 30V18M18 24l6-6 6 6" stroke="url(#uploadGrad)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 36h20" stroke="url(#uploadGrad)" stroke-width="2.5" stroke-linecap="round"/>
            <defs>
              <linearGradient id="uploadGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                <stop stop-color="#4f8cff"/>
                <stop offset="1" stop-color="#a259ff"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="upload-text">
          <span class="upload-main">将文件拖拽到此处，或 <em>点击上传</em></span>
          <span class="upload-hint">支持 .java 源文件 和 .zip 压缩包</span>
        </div>
      </div>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { getToken } from '../utils/token'
import { ElMessage } from 'element-plus'

const props = defineProps<{ projectId: number }>()
const emit = defineEmits(['uploaded'])
const isUploading = ref(false)

const headers = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

function beforeUpload(file: File) {
  const isValid = file.name.endsWith('.java') || file.name.endsWith('.zip')
  if (!isValid) ElMessage.error('只支持 .java 和 .zip 文件')
  return isValid
}

function handleProgress() {
  isUploading.value = true
}

function handleSuccess() {
  isUploading.value = false
  ElMessage.success('文件上传成功')
  emit('uploaded')
}

function handleError() {
  isUploading.value = false
  ElMessage.error('文件上传失败')
}
</script>

<style scoped>
.upload-zone {
  border-radius: 12px;
  overflow: hidden;
}

.custom-uploader :deep(.el-upload-dragger) {
  background: #f8fafc;
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  padding: 36px;
  transition: all 0.2s;
}

.custom-uploader :deep(.el-upload-dragger:hover),
.custom-uploader :deep(.el-upload-dragger.is-dragover) {
  background: #eff6ff;
  border-color: #93c5fd;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-icon-wrap {
  transition: transform 0.3s;
}

.upload-icon-wrap.uploading {
  animation: bounce 0.8s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.upload-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.upload-main {
  font-size: 14px;
  color: #475569;
}

.upload-main em {
  font-style: normal;
  color: #4f8cff;
  font-weight: 600;
}

.upload-hint {
  font-size: 12px;
  color: #94a3b8;
}
</style>
