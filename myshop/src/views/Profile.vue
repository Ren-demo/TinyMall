<template>
  <section class="profile">
    <header class="profile-header">
      <h1>个人信息</h1>
      <p class="subtitle">管理用户名、邮箱与密码</p>
    </header>

    <div class="card">
      <h2>头像设置</h2>
      <div class="avatar-section">
        <div class="avatar-preview" :class="{ empty: !avatarPreview }">
          <img v-if="avatarPreview" :src="avatarPreview" alt="avatar" />
          <span v-else>{{ (username || 'U').slice(0, 1).toUpperCase() }}</span>
        </div>
        <div class="avatar-controls">
          <label class="field-label">头像图片地址（URL）</label>
          <input v-model.trim="avatarUrl" type="text" placeholder="https://..." />
          <div class="upload-row">
            <label class="field-label">或上传图片</label>
            <input type="file" accept="image/png,image/jpeg,image/jpg,image/webp,image/gif" @change="onAvatarFileChange" />
            <span v-if="avatarError" class="message error">{{ avatarError }}</span>
            <span v-if="avatarUploading" class="message">头像上传中...</span>
          </div>
          <div class="avatar-actions">
            <button class="btn btn-secondary" type="button" @click="applyAvatar" :disabled="!avatarUrl && !selectedFile">应用头像</button>
            <button class="btn btn-outline" type="button" @click="clearAvatar" :disabled="!avatarPreview">移除头像</button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2>基础信息</h2>
      <form class="form" @submit.prevent="saveProfile">
        <div class="form-group">
          <label>用户名</label>
          <input v-model.trim="username" type="text" required />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model.trim="email" type="email" required />
        </div>
        <div class="form-group">
          <label>收货地址</label>
          <input v-model.trim="address" type="text" placeholder="填写默认收货地址" />
        </div>
        <div class="form-group">
          <label>新密码（可选）</label>
          <input v-model.trim="newPassword" type="password" autocomplete="new-password" />
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input v-model.trim="confirmPassword" type="password" autocomplete="new-password" />
        </div>
        <div class="form-actions">
          <button class="btn" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存修改' }}</button>
          <span v-if="message" class="message success">{{ message }}</span>
          <span v-if="error" class="message error">{{ error }}</span>
        </div>
      </form>
      <p class="hint">修改密码不要求输入原密码，将在保存时一并提交。</p>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const username = ref(auth.username || '')
const email = ref(auth.email || '')
const address = ref(auth.address || '')
const storedPicture = localStorage.getItem('userPicture') || ''
const avatarUrl = ref(auth.userPicture || storedPicture || '')
const avatarPreview = ref(auth.userPicture || storedPicture || '')
const avatarError = ref('')
const avatarUploading = ref(false)
const selectedFile = ref(null)
const localPreviewUrl = ref('')
const saving = ref(false)
const message = ref('')
const error = ref('')

const newPassword = ref('')
const confirmPassword = ref('')

async function saveProfile() {
  message.value = ''
  error.value = ''
  if (newPassword.value || confirmPassword.value) {
    if (newPassword.value !== confirmPassword.value) {
      error.value = '两次输入的密码不一致'
      return
    }
    if (newPassword.value.length < 6) {
      error.value = '新密码至少 6 位'
      return
    }
  }
  saving.value = true
  try {
    // 如果用户选中了本地文件但未点击“应用头像”，在提交保存前先上传替换为后端返回的持久 URL
    let finalPicture = avatarPreview.value || avatarUrl.value || auth.userPicture || localStorage.getItem('userPicture') || sessionStorage.getItem('userPicture') || ''
    if (selectedFile.value) {
      avatarUploading.value = true
      const up = await api.uploadUserPicture(auth.userId, selectedFile.value)
      avatarUploading.value = false
      if (up && up.success) {
        const d = up.data || {}
        const url = d.userPicture || d.picture || d.avatar || d.url || d
        if (url) {
          finalPicture = url
          avatarPreview.value = url
          avatarUrl.value = url
          selectedFile.value = null
        }
      } else {
        saving.value = false
        avatarError.value = (up && up.msg) || (up && up.error) || '头像上传失败，请重试'
        return
      }
    }
    // 避免把临时 blob: URL 写入数据库
    if (finalPicture && String(finalPicture).startsWith('blob:')) {
      saving.value = false
      error.value = '检测到临时预览地址，请先上传图片后再保存'
      return
    }
    const currentPicture = finalPicture
    const res = await api.updateUserInfo({
      userId: auth.userId || undefined,
      userName: username.value,
      userEmail: email.value,
      address: address.value,
      userPicture: currentPicture,
      userPwd: newPassword.value || auth.password || undefined
    })
    if (res && res.success) {
      auth.username = username.value
      auth.email = email.value
      auth.address = address.value
      auth.userPicture = currentPicture || auth.userPicture || ''
      sessionStorage.setItem('username', auth.username)
      sessionStorage.setItem('email', auth.email)
      if (auth.address) sessionStorage.setItem('address', auth.address)
      else sessionStorage.removeItem('address')
      if (auth.userPicture) sessionStorage.setItem('userPicture', auth.userPicture)
      else sessionStorage.removeItem('userPicture')
      localStorage.setItem('username', auth.username)
      localStorage.setItem('email', auth.email)
      if (auth.address) localStorage.setItem('address', auth.address)
      else localStorage.removeItem('address')
      if (auth.userPicture) localStorage.setItem('userPicture', auth.userPicture)
      else localStorage.removeItem('userPicture')
      if (newPassword.value) {
        auth.password = newPassword.value
        localStorage.setItem('password', auth.password)
      }
      newPassword.value = ''
      confirmPassword.value = ''
      message.value = '个人信息已更新'
    } else {
      error.value = (res && res.msg) || '保存失败，请重试'
    }
  } catch (e) {
    error.value = '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

function applyAvatar() {
  avatarError.value = ''
  message.value = ''
  if (selectedFile.value) {
    if (!auth.userId) {
      avatarError.value = '请先登录后再上传头像'
      return
    }
    avatarUploading.value = true
    api.uploadUserPicture(auth.userId, selectedFile.value).then((res) => {
      if (res && res.success) {
        const data = res.data || {}
        const url = data.userPicture || data.picture || data.avatar || data.url || data
        if (url) {
          avatarPreview.value = url
          avatarUrl.value = url
          auth.userPicture = url
          // Only persist backend-returned or relative URLs, never blob: or data: URLs
          if (typeof url === 'string' && (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/'))) {
            localStorage.setItem('userPicture', url)
            sessionStorage.setItem('userPicture', url)
          } else {
            // don't persist temporary urls
            console.warn('拒绝持久化非持久图片 URL:', url)
          }
          message.value = '头像已更新'
        } else {
          message.value = '头像已上传'
        }
        selectedFile.value = null
      } else {
        avatarError.value = (res && res.msg) || '头像上传失败'
      }
    }).catch(() => {
      avatarError.value = '头像上传失败'
    }).finally(() => {
      if (localPreviewUrl.value && localPreviewUrl.value.startsWith('blob:')) {
        try { URL.revokeObjectURL(localPreviewUrl.value) } catch (e) {}
      }
      localPreviewUrl.value = ''
      avatarUploading.value = false
    })
    return
  }
  // 如果 avatarUrl 是临时 blob: 地址，禁止直接应用
  if (avatarUrl.value && String(avatarUrl.value).startsWith('blob:')) {
    avatarError.value = '请先上传图片，不要直接使用临时预览地址'
    return
  }
  if (!avatarUrl.value) return
  avatarPreview.value = avatarUrl.value
  auth.userPicture = avatarUrl.value
  if (typeof avatarUrl.value === 'string' && (avatarUrl.value.startsWith('http://') || avatarUrl.value.startsWith('https://') || avatarUrl.value.startsWith('/'))) {
    localStorage.setItem('userPicture', avatarUrl.value)
    sessionStorage.setItem('userPicture', avatarUrl.value)
  } else {
    console.warn('拒绝持久化非持久图片 URL:', avatarUrl.value)
  }
  message.value = '头像已更新'
}

function clearAvatar() {
  avatarUrl.value = ''
  avatarPreview.value = ''
  selectedFile.value = null
  if (localPreviewUrl.value && localPreviewUrl.value.startsWith('blob:')) {
    try { URL.revokeObjectURL(localPreviewUrl.value) } catch (e) {}
  }
  localPreviewUrl.value = ''
}

function onAvatarFileChange(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  avatarError.value = ''
  if (!file.type || !file.type.startsWith('image/')) {
    avatarError.value = '请选择图片文件（PNG/JPG/WEBP/GIF）'
    e.target.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    avatarError.value = '图片过大，请选择 2MB 以内的图片'
    e.target.value = ''
    return
  }
  if (!auth.userId) {
    avatarError.value = '请先登录后再上传头像'
    e.target.value = ''
    return
  }
  if (localPreviewUrl.value && localPreviewUrl.value.startsWith('blob:')) {
    try { URL.revokeObjectURL(localPreviewUrl.value) } catch (e) {}
  }
  const localUrl = URL.createObjectURL(file)
  localPreviewUrl.value = localUrl
  selectedFile.value = file
  avatarPreview.value = localUrl
  avatarUrl.value = localUrl
  e.target.value = ''
}

onMounted(() => {
  // 如果 auth 或本地存储里保存了临时 blob: URL，清理它们，避免误保存
  try {
    const storePic = localStorage.getItem('userPicture') || sessionStorage.getItem('userPicture') || ''
    if (storePic && String(storePic).startsWith('blob:')) {
      localStorage.removeItem('userPicture')
      sessionStorage.removeItem('userPicture')
      if (auth.userPicture && String(auth.userPicture).startsWith('blob:')) auth.userPicture = ''
      avatarPreview.value = ''
      avatarUrl.value = ''
      avatarError.value = '检测到旧的临时预览地址，已清除，请重新上传并点击应用头像'
    }
  } catch (e) {}
})
</script>

<style scoped>
.profile {
  max-width: 880px;
  margin: 0 auto;
  padding: 1.5rem;
}

.profile-header {
  margin-bottom: 1.5rem;
}

.profile-header h1 {
  margin: 0 0 0.3rem 0;
  font-size: 1.8rem;
}

.subtitle {
  margin: 0;
  color: #6b7280;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  margin-bottom: 1.25rem;
}

.card h2 {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
}

.avatar-section {
  display: flex;
  gap: 1.5rem; 
  align-items: center;
  flex-wrap: wrap;
}

.avatar-preview {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #eef2ff;
  border: 2px solid #c7d2fe;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #4338ca;
  font-weight: 700;
  font-size: 2rem;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.upload-row {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field-label {
  font-weight: 600;
  color: #374151;
}

.avatar-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-group input {
  padding: 0.65rem 0.85rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.btn {
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.65rem 1.2rem;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.18);
}

.btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #10b981;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.18);
}

.btn-secondary:hover:not(:disabled) {
  background: #0f9f75;
}

.btn-outline {
  background: transparent;
  color: #2563eb;
  border: 1px solid #93c5fd;
  box-shadow: none;
}

.btn-outline:hover:not(:disabled) {
  background: #eff6ff;
}

.message {
  font-size: 0.95rem;
}

.message.success {
  color: #0f9f75;
}

.message.error {
  color: #b00020;
}

.hint {
  color: #6b7280;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}
</style>
