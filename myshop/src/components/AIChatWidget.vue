<template>
  <div class="ai-chat-widget">
    <button class="chat-toggle" @click="visible = !visible">{{ visible ? '关闭' : 'AI 客服' }}</button>

    <div v-if="visible" class="chat-panel">
          <div class="chat-header">
            <strong>AI 智能客服</strong>
            <div style="display:flex; gap:8px; align-items:center">
              <button class="clear-btn" @click="handleClearSession">清除会话</button>
              <button class="close" @click="handleClose">×</button>
            </div>
          </div>
      <div class="chat-body" ref="bodyRef">
        <div v-for="(m, idx) in messages" :key="idx" :class="['msg', m.from]">
          <div class="bubble">{{ m.text }}</div>
        </div>
        <div v-if="streaming" class="msg ai">
          <div class="bubble">{{ streamingText }}</div>
        </div>
      </div>
      <div class="chat-input">
        <input v-model="input" @keydown.enter="handleSend" placeholder="请输入问题，回车发送" />
        <button @click="handleSend" :disabled="sending">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const visible = ref(false)
const input = ref('')
const messages = reactive([])
const sending = ref(false)
const streaming = ref(false)
const streamingText = ref('')
const bodyRef = ref(null)

// sessionId persist per page load
const sessionId = `sess-${Date.now()}-${Math.floor(Math.random()*10000)}`

function pushMessage(from, text) {
  messages.push({ from, text })
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    try { if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight } catch (e) {}
  })
}

async function handleSend() {
  const text = (input.value || '').trim()
  if (!text) return
  pushMessage('user', text)
  input.value = ''
  sending.value = true
  streaming.value = true
  streamingText.value = ''

  const userId = auth.userId || 0
  try {
    await api.aiChatStream({ sessionId, userId, message: text }, {
      onChunk(chunk) {
        // backend may send event-stream data or raw chunks; clean simple 'data:' prefixes
        let cleaned = (chunk || '').replace(/^data:\s*/g, '')
        // remove standalone session-id tokens like 'sess-12345...' that some backends append
        // - if chunk only contains a session token, ignore it
        const sessOnly = cleaned.match(/^\s*sess[-_:\w]+\s*$/i)
        if (sessOnly) return
        // - strip trailing sess-... patterns (common when backend appends session id)
        cleaned = cleaned.replace(/\s*sess[-_:\w]{6,}\s*$/i, '')
        // also remove any stray 'event:done' or similar labels that slipped through
        cleaned = cleaned.replace(/(^|\n)event:\s*done(\n|$)/ig, '\n')
        cleaned = cleaned.trim()
        if (!cleaned) return
        streamingText.value += (streamingText.value && !streamingText.value.endsWith('\n') ? '\n' : '') + cleaned
        scrollToBottom()
      },
      onDone() {
        // finalize streamed text into messages
        streaming.value = false
        if (streamingText.value.trim()) pushMessage('ai', streamingText.value.trim())
        streamingText.value = ''
        sending.value = false
      },
      onError(err) {
        streaming.value = false
        sending.value = false
        pushMessage('ai', '抱歉，AI 服务不可用，请稍后重试。')
        console.error('aiChatStream error', err)
      }
    })
  } catch (e) {
    streaming.value = false
    sending.value = false
    pushMessage('ai', '发送失败，请检查网络')
  }
}

async function handleClearSession() {
  try {
    const res = await api.aiClearSession(sessionId)
    const ok = res && (res.success === true || res.code === 0 || res.code === 1 || res.code === 200 || (res.data && res.data.success === true))
    if (ok) {
      // clear local messages
      messages.splice(0, messages.length)
      streamingText.value = ''
      streaming.value = false
      pushMessage('ai', (res && res.data && typeof res.data === 'string') ? res.data : '会话已清除。')
    } else {
      pushMessage('ai', '清除会话失败')
      console.warn('aiClearSession failed', res)
    }
  } catch (e) {
    pushMessage('ai', '清除会话出错')
    console.error('clear session error', e)
  }
}

// 静默清除：在关闭面板时调用，不会显示 AI 的提示消息
async function clearSessionSilent() {
  try {
    const res = await api.aiClearSession(sessionId)
    const ok = res && (res.success === true || res.code === 0 || res.code === 1 || res.code === 200 || (res.data && res.data.success === true))
    if (ok) {
      messages.splice(0, messages.length)
      streamingText.value = ''
      streaming.value = false
    }
  } catch (e) {
    console.warn('clearSessionSilent failed', e)
  }
}

// 关闭处理：在用户点击关闭时先静默清除会话再隐藏面板
async function handleClose() {
  // fire-and-forget but await to reduce race if user reopens quickly
  try {
    await clearSessionSilent()
  } catch (_) {}
  visible.value = false
}

// welcome
onMounted(() => {
  pushMessage('ai', '您好，我是智能客服，有什么可以帮您？')
})

watch(visible, (v, oldV) => {
  if (v) {
    scrollToBottom()
  } else if (oldV) {
    // 面板从显示变为隐藏时，静默清除会话（不打扰用户）
    clearSessionSilent()
  }
})
</script>

<style scoped>
.ai-chat-widget { position: fixed; left: 16px; bottom: 16px; z-index: 1600 }
.chat-toggle { background:#0b69ff; color:#fff; border:none; padding:0.6rem 0.9rem; border-radius:12px; cursor:pointer }
.chat-panel { width: 320px; max-height: 480px; background: #fff; box-shadow: 0 8px 28px rgba(2,6,23,0.2); border-radius:12px; overflow:hidden; display:flex; flex-direction:column; margin-top:8px }
.chat-header { display:flex; align-items:center; justify-content:space-between; padding:0.6rem 0.8rem; border-bottom:1px solid #eef2ff; background:#f8fafc }
.chat-body { padding:0.6rem; overflow:auto; flex:1; display:flex; flex-direction:column; gap:0.5rem }
.msg { display:flex }
.msg.user { justify-content:flex-end }
.msg.ai { justify-content:flex-start }
.bubble { max-width:80%; padding:0.5rem 0.75rem; border-radius:10px; background:#eef2ff; color:#0b1726 }
.msg.user .bubble { background:#0b69ff; color:#fff }
.chat-input { display:flex; gap:0.5rem; padding:0.6rem; border-top:1px solid #eef2ff }
.chat-input input { flex:1; padding:0.5rem; border:1px solid #e6eefc; border-radius:8px }
.chat-input button { padding:0.45rem 0.8rem; border-radius:8px; border:none; background:#0b69ff; color:#fff; cursor:pointer }
.close { background:transparent; border:none; font-size:16px; cursor:pointer }
</style>
