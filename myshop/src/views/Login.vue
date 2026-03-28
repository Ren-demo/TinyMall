<template>
	<div class="login-container">
		<div class="login-card">
			<div class="forgot-link">
				<button class="link-small" @click="mode = 'forgot'">忘记密码？</button>
			</div>
			<div class="login-header">
				<h1>MyShop</h1>
				<p>{{ mode === 'login' ? '欢迎登录电商平台' : mode === 'register' ? '创建新账号' : '重置密码' }}</p>
			</div>

			<div class="mode-toggle">
				<button :class="['tab', mode === 'login' ? 'active' : '']" @click="mode = 'login'">登录</button>
				<button :class="['tab', mode === 'register' ? 'active' : '']" @click="mode = 'register'">注册</button>
			</div>

			<form v-if="mode === 'login'" @submit.prevent="onLogin" class="login-form">
				<div class="form-group">
					<label for="login-email">邮箱</label>
					<input
						id="login-email"
						name="email"
						autocomplete="username"
						v-model="loginEmail"
						type="email"
						placeholder="请输入邮箱"
						required
					/>
				</div>

				<div class="form-group">
					<label for="login-password">密码</label>
					<input
						id="login-password"
						name="password"
						autocomplete="current-password"
						v-model="loginPassword"
						type="password"
						placeholder="请输入密码"
						required
					/>
				</div>

				<button type="submit" class="btn btn-login" :disabled="loading">
					{{ loading ? '登录中...' : '登 录' }}
				</button>
			</form>

			<form v-else-if="mode === 'register'" @submit.prevent="onRegister" class="register-form">
				<div class="form-group">
					<label for="reg-email">邮箱</label>
					<div class="email-row">
						<input id="reg-email" v-model="email" type="email" autocomplete="email" placeholder="请输入邮箱" required />
						<button type="button" class="btn-small" @click="getCode" :disabled="sending || !email">{{ sending ? '发送中...' : '获取验证码' }}</button>
					</div>
				</div>

				<div class="form-group code-group">
					<label style="color: #4f46e5; font-weight: 600; display: flex; align-items: center; gap: 0.5rem;">验证码</label>
					<input v-model="code" type="text" placeholder="点击上方获取验证码后输入（任意4-6位）" />
				</div>

				<div class="form-group">
					<label>用户名</label>
					<input v-model="regUsername" type="text" required />
				</div>

				<div class="form-group">
					<label>密码</label>
					<input v-model="regPassword" type="password" autocomplete="new-password" required />
				</div>

				<div class="form-group">
					<label>确认密码</label>
					<input v-model="regConfirm" type="password" autocomplete="new-password" required />
				</div>

				<div class="form-actions">
					<button type="submit" class="btn btn-login">注册并登录</button>
				</div>
			</form>

			<form v-else-if="mode === 'forgot'" @submit.prevent="onResetPassword" class="forgot-form">
				<div class="form-group">
					<label>邮箱（用于接收验证码）</label>
						<input v-model="forgotEmail" type="email" autocomplete="email" placeholder="请输入注册邮箱" required />
				</div>

				<div class="form-group email-row">
					<input v-model="forgotCode" type="text" placeholder="验证码" required />
					<button type="button" class="btn-small" @click="sendForgotCode" :disabled="forgotSending">{{ forgotSending ? '发送中...' : '发送验证码' }}</button>
				</div>

				<div class="form-group">
					<label>新密码</label>
					<input v-model="forgotPassword" type="password" autocomplete="new-password" required />
				</div>

				<div class="form-group">
					<label>确认新密码</label>
					<input v-model="forgotConfirm" type="password" required />
				</div>

				<div class="form-actions" style="display:flex; gap:0.5rem; align-items:center">
					<button type="submit" class="btn btn-login">重置并登录</button>
					<button type="button" class="link-small" @click="mode = 'login'">返回登录</button>
				</div>
			</form>

			<div v-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

		</div>

			<!-- 隐匿触发输入（右下角，输入 admin 显示浮窗） -->
						<input
							class="admin-trigger"
							v-model="hiddenTrigger"
							@input="checkAdminTrigger"
							@keyup.enter="checkAdminTrigger"
							@blur="checkAdminTrigger"
							placeholder=""
							aria-hidden="true"
						/>

			<!-- Admin 浮窗 -->
			<div v-if="showAdminPanel" class="admin-panel">
				<div class="admin-panel-header">管理快速入口</div>
				<div class="form-group">
					<label>供应商路径（x）</label>
					<input v-model="supplierX" type="text" placeholder="在此输入 x，例如 123 或 supplier-id" />
				</div>
				<div style="display:flex; gap:0.5rem; margin-top:0.5rem">
					<button type="button" class="btn-small" @click="goToMiddle">中间商 (middle)</button>
					<button type="button" class="btn-small" @click="goToSupplier">供应商 (supplier/x)</button>
				</div>
				<button type="button" class="link-small" style="margin-top:0.5rem" @click="showAdminPanel = false">关闭</button>
			</div>
		</div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'
import api from '../services/api'

const mode = ref('login')

// login fields
const loginEmail = ref('')
const loginPassword = ref('')

// register fields
const email = ref('')
const code = ref('')
const regUsername = ref('')
const regPassword = ref('')
const regConfirm = ref('')
const sending = ref(false)

const error = ref('')
const loading = ref(false)

// forgot password fields
const forgotEmail = ref('')
const forgotCode = ref('')
const forgotPassword = ref('')
const forgotConfirm = ref('')
const forgotSending = ref(false)

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

// Admin quick-entry: 隐匿输入 + 弹窗
const hiddenTrigger = ref('')
const showAdminPanel = ref(false)
const supplierX = ref('')

function checkAdminTrigger() {
	// 当精确输入 admin 时显示浮窗并清空触发输入
	if ((hiddenTrigger.value || '').trim() === 'admin') {
		hiddenTrigger.value = ''
		showAdminPanel.value = true
	}
}

function goToMiddle() {
	showAdminPanel.value = false
	router.push('/middle')
}

function goToSupplier() {
	const x = (supplierX.value || '').trim()
	if (!x) {
		alert('请输入供应商路径 x（不能为空）')
		return
	}
	showAdminPanel.value = false
	router.push(`/supplier/${x}`)
}

async function onLogin() {
	error.value = ''
 	if (!loginEmail.value.trim()) { error.value = '请输入邮箱'; return }
 	if (!loginPassword.value.trim()) { error.value = '请输入密码'; return }
 	loading.value = true
 	try {
 		const res = await auth.login({ email: loginEmail.value, password: loginPassword.value, role: 'user' })
 		if (!res || !res.success) {
 			error.value = '登录失败，请检查邮箱或密码'
 			loading.value = false
 			return
 		}
 		// 登录成功
 		console.log('✓ 登录成功，Token 已获取并保存')
 		console.log('  当前用户:', auth.username)
 		console.log('  当前角色:', auth.role)
 		// 清空登录表单
 		loginEmail.value = ''
 		loginPassword.value = ''
 		error.value = ''
 		loading.value = false
 		// 优先使用 redirect 查询参数返回原页面
 		const redirect = route.query.redirect || '/user'
 		router.push(redirect)
 	} catch (e) {
 		error.value = '登录出错，请重试'
 		loading.value = false
 	}
}

async function getCode() {
	error.value = ''
	if (!email.value) { error.value = '请输入邮箱'; return }
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

async function sendForgotCode() {
	error.value = ''
 	if (!forgotEmail.value) { error.value = '请输入邮箱'; return }
 	forgotSending.value = true
 	const res = await api.sendVerificationCode(forgotEmail.value)
 	forgotSending.value = false
 	if (res && res.success) {
 		alert('验证码已发送⚠️请查收邮箱（如果没有收到，请检查垃圾邮件）')
 	} else {
 		error.value = '验证码发送失败'
 	}
}

async function onResetPassword() {
 	error.value = ''
	if (!forgotEmail.value) { error.value = '请输入邮箱'; return }
	if (!forgotCode.value) { error.value = '请输入验证码'; return }
	if (forgotPassword.value !== forgotConfirm.value) { error.value = '两次密码不一致'; return }
	loading.value = true
	let res = null
	try {
		res = await api.resetPassword({ email: forgotEmail.value, code: forgotCode.value, newPassword: forgotPassword.value })
	} catch (e) {
		console.error('onResetPassword error:', e)
		res = null
	} finally {
		loading.value = false
	}
	if (!res) { error.value = '重置失败，后端无响应'; return }
	if (!res.success) {
		error.value = res.error || res.msg || '重置失败，验证码可能错误或已过期'
		return
	}
	// 成功：如果后端返回 token，则直接登录并跳转；否则提示用户去登录页面
	if (res.token) {
		auth.token = res.token
		auth.role = res.role || 'user'
		auth.username = res.username || ''
		localStorage.setItem('token', res.token)
		localStorage.setItem('role', auth.role)
		localStorage.setItem('username', auth.username)
		const redirect = route.query.redirect || '/user'
		router.push(redirect)
	} else {
		alert(res.msg || '重置成功，请使用新密码登录')
		// 切回登录面板并清理表单
		mode.value = 'login'
		forgotEmail.value = ''
		forgotCode.value = ''
		forgotPassword.value = ''
		forgotConfirm.value = ''
	}
}

async function onRegister() {
	error.value = ''
	if (!email.value) { error.value = '请输入邮箱'; return }
	if (!regUsername.value) { error.value = '请输入用户名'; return }
	if (regPassword.value !== regConfirm.value) { error.value = '两次密码不一致'; return }
	if (!code.value) { error.value = '请输入验证码'; return }
	
	loading.value = true
	try {
		const res = await api.logon({ 
			email: email.value, 
			username: regUsername.value, 
			password: regPassword.value, 
			verifyCode: code.value 
		})
		loading.value = false
		
		const ok = res && (res.code !== undefined ? res.code === 1 : res.success)
		if (!ok) { 
			error.value = res && (res.msg || res.error) ? `注册失败: ${res.msg || res.error}` : '注册失败，请重试'
			return 
		}
		
		// 注册成功提示
		alert('✓ 注册成功！请使用邮箱和密码登录')
		
		// 清空注册表单
		email.value = ''
		code.value = ''
		regUsername.value = ''
		regPassword.value = ''
		regConfirm.value = ''
		error.value = ''
		
		// 切换回登录标签页
		mode.value = 'login'
		// 清空登录表单
		loginEmail.value = ''
		loginPassword.value = ''
	} catch (e) {
		loading.value = false
		error.value = '注册出错，请重试'
		console.error('onRegister error:', e)
	}
}
</script>

<style scoped>
.login-container {
	display: flex;
	justify-content: center;
	align-items: center;
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 1rem;
	flex: 1;
}

.login-card {
	background: white;
	border-radius: 12px;
	box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
	padding: 2rem;
	width: 100%;
	max-width: 520px;
	position: relative;
}

.forgot-link { position: absolute; right: 1.25rem; top: 1rem }
.link-small { background: none; border: none; color: #6b7280; font-size: 0.85rem; cursor: pointer }
.link-small:hover { color: #374151 }

.login-header { text-align: center; margin-bottom: 1rem }
.mode-toggle { display:flex; gap:0.5rem; margin-bottom:1rem; justify-content:center }
.tab { padding:0.5rem 1.25rem; border-radius:8px; border:1px solid #e6e9ee; background:#ffffff; cursor:pointer; color:#334155 }
.tab:hover { box-shadow: 0 4px 12px rgba(51,65,85,0.08); transform: translateY(-1px) }
.tab.active { background: linear-gradient(135deg,#4f46e5,#7c3aed); color:white; border-color:transparent; box-shadow: 0 8px 24px rgba(79,70,229,0.18) }
.email-row { display:flex; gap:0.5rem }
.email-row input { flex:1 }
.btn-small { padding:0.45rem 0.75rem; background:#4f46e5; color:white; border-radius:6px; border:none; box-shadow:0 6px 18px rgba(79,70,229,0.12); cursor:pointer; font-size:0.85rem; white-space:nowrap; font-weight:500 }
.btn-small:disabled { opacity:0.6; cursor:not-allowed }
.btn-small:hover:not(:disabled) { background:#3b36c9 }
.code-group { background: #f0f9ff; padding: 0.75rem; border-radius: 6px; border-left: 3px solid #4f46e5; }
.code-group input { padding: 0.5rem 0.75rem; border: 1px solid #bfdbfe; border-radius: 6px; width: 100%; }
.form-group { margin-bottom: 0.9rem }
.btn-login { width:100%; padding:0.75rem; background:linear-gradient(135deg,#5b21b6,#7c3aed); color:white; border:none; border-radius:8px; box-shadow: 0 10px 30px rgba(124,58,237,0.18); font-weight:700 }
.btn-login:disabled { opacity:0.75; cursor:not-allowed }
.btn-login:focus { outline: 3px solid rgba(124,58,237,0.12) }
.alert-danger { color:#b00020; margin-top:0.5rem }
.login-tips { background:#f0f4ff; border-radius:8px; padding:0.9rem; margin-top:1rem }
.login-tips ul { list-style:none; padding-left:0 }

/* 隐匿触发输入（右下角） */
.admin-trigger {
	position: fixed;
	right: 12px;
	bottom: 12px;
	width: 72px;
	height: 28px;
	opacity: 0.04;
	background: transparent;
	border: none;
	outline: none;
	z-index: 9999;
	caret-color: transparent;
	color: transparent;
}
.admin-trigger:focus {
	opacity: 1;
	background: rgba(79,70,229,0.06);
	border-radius: 6px;
	caret-color: #111;
	color: #111;
	outline: 2px solid rgba(79,70,229,0.15);
}

/* Admin 浮窗 */
.admin-panel {
	position: fixed;
	right: 12px;
	bottom: 44px;
	width: 300px;
	background: #ffffff;
	border-radius: 8px;
	box-shadow: 0 10px 30px rgba(2,6,23,0.2);
	padding: 0.75rem;
	z-index: 10000;
	font-size: 0.95rem;
}
.admin-panel-header { font-weight:700; margin-bottom:0.5rem }
.admin-panel .form-group { margin-bottom:0.5rem }
.admin-panel input { width: 100%; padding:0.45rem 0.5rem; border-radius:6px; border:1px solid #e6e9ee }
</style>