<template>
  <div class="comment-section">
    <h4>评论区</h4>
    <div v-if="loading">加载评论中...</div>
    <div v-else>
      <div v-if="comments.length===0" class="empty">暂无评论</div>
      <div class="comment-list">
        <div v-for="c in comments" :key="c.id || c.commentId" class="comment-item">
          <div class="meta">
            <strong>{{ c.username || '用户id：' + (c.userId || '匿名') }} </strong>
            <div class="meta-right">
              <span class="stars">{{ (c.star || c.stars || 0) }} ★</span>
              <button v-if="isOwn(c)" class="btn" @click="editComment(c)">编辑</button>
            </div>
          </div>
          <div class="content">{{ c.content || c.text || '' }}</div>
          <div class="images" v-if="(c.images || c.imgUrl || c.pictures || []).length">
            <img v-for="(u, idx) in (c.images || c.imgUrl || c.pictures || [])" :key="idx" :src="u" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const emit = defineEmits(['edit'])
const props = defineProps({ goodsId: { type: [String, Number], required: true } })
const comments = ref([])
const loading = ref(false)
const auth = useAuthStore()
const canComment = computed(() => !!auth.username)

const form = ref({ orderId: null, imgUrl: [], star: 5, content: '' })

async function loadComments() {
  loading.value = true
  try {
    const data = await api.fetchComments(props.goodsId)
    if (Array.isArray(data)) comments.value = data
    else if (data && Array.isArray(data.list)) comments.value = data.list
    else if (data && Array.isArray(data.data)) comments.value = data.data
    else comments.value = data ? (Array.isArray(data) ? data : []) : []
  } catch (e) {
    comments.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadComments)
watch(() => props.goodsId, () => { loadComments() })

async function onFileChange(e) {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  // upload via API (use orderId 0 if unknown)
  const resp = await api.uploadCommentImage(form.value.orderId || 0, f)
  if (resp && (resp.code === 0 || resp.code === 200 || resp.success) && resp.data) {
    // try common fields
    const url = resp.data.url || resp.data.path || resp.data.picture || resp.data[0] || resp.data
    if (url) form.value.imgUrl.push(url)
  } else if (resp && resp.data && typeof resp.data === 'string') {
    form.value.imgUrl.push(resp.data)
  }
}

async function submitComment() {
  const payload = {
    orderId: form.value.orderId,
    imgUrl: form.value.imgUrl,
    star: form.value.star,
    content: form.value.content,
    goodsId: props.goodsId
  }
  const res = await api.addComment(payload)
  if (res && (res.code === 0 || res.code === 200 || res.success)) {
    form.value.content = ''
    form.value.imgUrl = []
    form.value.star = 5
    await loadComments()
    return
  }
  // fallback: optimistic add
  comments.value.unshift({ username: auth.username || '我', star: form.value.star, content: form.value.content, images: form.value.imgUrl.slice() })
  form.value.content = ''
  form.value.imgUrl = []
}

function isOwn(c) {
  if (!c) return false
  // check by numeric userId or username
  const cid = c.userId ?? c.userId ?? c.userId
  try {
    if (cid && auth.userId && String(cid) === String(auth.userId)) return true
  } catch (e) {}
  const name = (c.username || c.user || c.userName || c.nickName || '')
  if (name && auth.username && String(name) === String(auth.username)) return true
  return false
}

function editComment(c) {
  emit('edit', c)
}

// expose loadComments so parent can refresh after edits
defineExpose({ loadComments })
</script>

<style scoped>
.comment-section { padding: 1rem; border-left: 1px solid #e6eef8; height:100%; display:flex; flex-direction:column; box-sizing:border-box; background: #fbfdff; }
.comment-section > h4 { margin:0 0 0.5rem 0; padding-bottom:0.25rem; border-bottom:1px solid #eef3fb }
.comment-list { display:flex; flex-direction:column; gap:0.75rem; flex:1 1 auto; overflow:auto; padding-right:0.25rem }
.comment-item { padding:0.6rem; background:#ffffff; border-radius:8px; box-shadow: 0 1px 4px rgba(20,40,80,0.04) }
.comment-item img { width:64px; height:64px; object-fit:cover; margin-right:0.25rem }
.meta { display:flex; justify-content:space-between; gap:0.5rem }
.meta-right { display:flex; align-items:center; gap:0.5rem }
.btn-delete { background:transparent; border:1px solid #f3a0a0; color:#c33; padding:0.18rem 0.45rem; border-radius:6px; cursor:pointer }
.row { margin-top:0.5rem }
textarea { width:100%; padding:0.5rem }
.upload-list { margin-top:0.25rem; display:flex; gap:0.5rem; flex-wrap:wrap }
.upload-list span { background:#f0f0f0; padding:0.25rem 0.5rem; border-radius:4px }
.hint { margin-top:0.5rem; color:#888 }
</style>
