<template>
  <div class="register-container">
    <div class="card">
      <h2>注册账户</h2>
      <form @submit.prevent="onRegister">
        <div class="form-group">
          <label>邮箱</label>
          <div class="email-row">
            <input v-model="email" type="email" name="email" autocomplete="email" placeholder="请输入邮箱" required />
            <button type="button" class="btn-small" @click="getCode" :disabled="sending || !email">{{ sending ? '发送中...' : '获取验证码' }}</button>
          </div>
        </div>

        <div class="form-group code-group">
          <label style="color: #4f46e5; font-weight: 600; display: flex; align-items: center; gap: 0.5rem;">验证码</label>
          <input v-model="code" type="text" placeholder="点击上方获取验证码后输入（任意4-6位）" />
        </div>

        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" required />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" autocomplete="new-password" required />
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input v-model="confirm" type="password" required />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn">注册</button>
        </div>
      </form>

      <div v-if="error" class="alert">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../services/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const code = ref('')
const username = ref('')
const password = ref('')
const confirm = ref('')
const sending = ref(false)
const error = ref('')

const router = useRouter()
const auth = useAuthStore()

async function getCode() {
  if (!email.value) { error.value = '请输入邮箱'; return }
  error.value = ''
  sending.value = true
  try {
    const res = await api.sendVerificationCode(email.value)
    sending.value = false
    if (res && res.success) {
      error.value = ''
      console.log('✓ 验证码已发送到:', email.value)
      console.log('💡 演示环境提示：验证码为任意4-6位数字')
    } else {
      error.value = res && res.error ? res.error : '验证码发送失败，请重试'
    }
  } catch (e) {
    sending.value = false
    error.value = '网络错误，请重试'
    console.error('getCode error:', e)
  }
}

async function onRegister() {
  error.value = ''
  if (password.value !== confirm.value) { error.value = '两次密码不一致'; return }
  if (!code.value) { error.value = '请输入验证码'; return }
  
  try {
    const res = await api.register({ email: email.value, username: username.value, password: password.value, code: code.value })
    const ok = res && (res.code !== undefined ? res.code === 1 : res.success)
    if (!ok) { 
      error.value = res && (res.msg || res.error) ? (res.msg || res.error) : '注册失败，请重试'
      return 
    }
    
    // 注册成功提示
    alert('✓ 注册成功！请使用邮箱和密码登录')
    
    // 清空所有输入框
    email.value = ''
    code.value = ''
    username.value = ''
    password.value = ''
    confirm.value = ''
    error.value = ''
    
    // 跳转到登录页面
    router.push('/login')
  } catch (e) {
    error.value = '注册出错，请重试'
    console.error('onRegister error:', e)
  }
}
</script>

<style scoped>
.register-container { display:flex; justify-content:center; align-items:center; min-height:60vh }
.card { background:white; padding:1.5rem; border-radius:8px; width:380px }
.form-group { margin-bottom:0.75rem }
.email-row { display: flex; gap: 0.5rem; align-items: stretch }
.email-row input { flex: 1; padding: 0.5rem 0.75rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.9rem }
.btn-small { padding: 0.5rem 0.75rem; background: #4f46e5; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 0.85rem; white-space: nowrap; font-weight: 500 }
.btn-small:hover:not(:disabled) { background: #3b36c9; box-shadow: 0 4px 12px rgba(79,70,229,0.3) }
.btn-small:disabled { opacity: 0.6; cursor: not-allowed }
.code-group { background: #f0f9ff; padding: 0.75rem; border-radius: 6px; border-left: 3px solid #4f46e5; }
.code-group input { padding: 0.5rem 0.75rem; border: 1px solid #bfdbfe; border-radius: 6px; width: 100%; }
.form-actions { margin-top:1rem }
.alert { color:#b00020; padding: 0.5rem; background: #ffebee; border-radius: 4px }
</style>
