<template>
  <teleport to="body">
    <div v-if="modelValue" class="comment-modal-overlay" @click.self="close">
      <div class="comment-modal">
        <button class="close" @click="close">×</button>
        <h3>撰写评价</h3>
        <div class="form-row">
          <label>评分：</label>
          <select v-model.number="form.star">
            <option v-for="s in [5,4,3,2,1]" :key="s" :value="s">{{ s }} 星</option>
          </select>
        </div>
        <div class="form-row">
          <textarea v-model="form.content" rows="4" placeholder="写下你的评价..."></textarea>
        </div>
        <div class="form-row">
          <label>图片（可选）：</label>
          <input type="file" @change="onFileSelect" multiple />
          <div class="upload-list">
            <template v-for="(f, i) in selectedFiles" :key="i">
              <div style="display:inline-flex;align-items:center;gap:6px;padding:6px;background:#f8f9fa;border-radius:6px;">
                <img v-if="filePreview(i)" :src="filePreview(i)" style="width:48px;height:48px;object-fit:cover;border-radius:4px;margin-right:6px" />
                <span style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ f.name }}</span>
                <button class="btn btn-outline" style="margin-left:6px;padding:4px 6px" @click.prevent="removeSelectedFile(i)">移除</button>
              </div>
            </template>
            <template v-if="form.imgUrl && form.imgUrl.length">
              <span v-for="(u, idx) in form.imgUrl" :key="'u-'+idx" style="display:inline-flex;align-items:center;gap:6px;">
                <img :src="u" style="width:36px;height:36px;object-fit:cover;border-radius:4px;margin-right:4px" />
                <button class="btn btn-outline" @click.prevent="removeExistingImage(u)">删除</button>
              </span>
            </template>
          </div>
        </div>
        <div class="form-row actions">
          <button class="btn" @click="submit" :disabled="uploading">{{ uploading ? '上传中...' : '提交评价' }}</button>
          <button class="btn btn-outline" @click="close" :disabled="uploading">取消</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '../services/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  goodsId: { type: [String, Number], default: null },
  orderId: { type: [String, Number], default: null },
  // existingComment: optional object when editing an existing comment
  existingComment: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'submitted'])

const form = ref({ 
  orderId: null, 
  imgUrl: [], // 临时存储图片URL数组
  star: 5, 
  content: '' 
})
const uploading = ref(false)
const selectedFiles = ref([])
const filePreviews = ref([]) // local object URLs for previews
// 原始图片快照（打开编辑时的列表）
const originalImgs = ref([])
// 用户在编辑时标记为删除，但只有提交成功后才真正发送删除请求
const pendingDeletes = ref([])

// 修复：初始化表单时严格处理existingComment的字段类型
watch(() => props.modelValue, (v) => {
  if (v) {
    // 初始化表单. 编辑时从existingComment填充
    form.value.orderId = props.orderId || (props.existingComment?.orderId) || null
    // 统一处理图片URL：优先取images，其次imgUrl，确保是数组
    const existingImgs = props.existingComment 
      ? (props.existingComment.images || (Array.isArray(props.existingComment.imgUrl) ? props.existingComment.imgUrl : [props.existingComment.imgUrl])) 
      : []
    // 过滤空值，确保数组元素都是字符串
    form.value.imgUrl = existingImgs.filter(url => typeof url === 'string' && url.trim())
    // 处理评分：确保是数字
    form.value.star = props.existingComment 
      ? Number(props.existingComment.star ?? props.existingComment.stars ?? 5) 
      : 5
    // 处理内容：确保是字符串
    form.value.content = props.existingComment 
      ? String(props.existingComment.content ?? props.existingComment.text ?? '') 
      : ''
    selectedFiles.value = []
    filePreviews.value = []
    // 保存当前打开时的已有图片快照，便于后续比较哪些被删除
    originalImgs.value = form.value.imgUrl.slice()
    pendingDeletes.value = []
  }
})

function close() {
  emit('update:modelValue', false)
}

function onFileSelect(e) {
  const files = e.target.files ? Array.from(e.target.files) : []
  if (!files.length) return
  // 只过滤图片文件，对无效文件给出警告
  const imgs = []
  const bad = []
  for (const f of files) {
    if (f && f.type && f.type.startsWith('image/')) imgs.push(f)
    else {
      // 如果类型缺失，检查扩展名
      const name = f && f.name ? String(f.name).toLowerCase() : ''
      if (name.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/)) imgs.push(f)
      else bad.push(f.name || '未知文件')
    }
  }
  if (bad.length) alert('以下文件不是图片，已被忽略：' + bad.join(', '))
  // 合并多次选择的文件，避免覆盖之前选择的
  const existingKeys = new Set(selectedFiles.value.map(f => f.name + '::' + f.size))
  for (const f of imgs) {
    const key = f.name + '::' + f.size
    if (!existingKeys.has(key)) {
      selectedFiles.value.push(f)
      try {
        const url = URL.createObjectURL(f)
        filePreviews.value.push(url)
      } catch (e) {
        filePreviews.value.push('')
      }
      existingKeys.add(key)
    }
  }
}

function filePreview(idx) {
  return filePreviews.value[idx] || ''
}

function removeSelectedFile(idx) {
  if (idx < 0 || idx >= selectedFiles.value.length) return
  try { 
    const url = filePreviews.value[idx]; 
    if (url) URL.revokeObjectURL(url) 
  } catch (e) {}
  selectedFiles.value.splice(idx, 1)
  filePreviews.value.splice(idx, 1)
}

// 从各种可能的对象/Proxy 中提取字符串 id
function normalizeId(val) {
  if (!val && val !== 0) return ''
  if (typeof val === 'string') return val
  if (typeof val === 'number') return String(val)
  try {
    // reactive Proxy 的属性访问仍然可用
    if (val && typeof val === 'object') {
      if (val.id) return String(val.id)
      if (val._id) return String(val._id)
      if (val.$oid) return String(val.$oid)
      if (val.toString && typeof val.toString === 'function') {
        const s = val.toString()
        if (s && s !== '[object Object]') return s
      }
    }
  } catch (e) {}
  return ''
}

async function submit() {
  uploading.value = true
  try {
    const uploadedUrls = []
    const orderIdToUse = form.value.orderId || null
    if (!orderIdToUse) {
      alert('无法上传：缺少订单号 (orderId)')
      return
    }

    // 上传选中的文件
    for (const f of selectedFiles.value || []) {
      try {
        const resp = await api.uploadCommentImage(orderIdToUse, f)
        console.debug('[CommentModal] upload resp:', resp)
        if (!resp) { alert('上传失败：未收到服务端响应'); return }
        let imgUrl = ''
        if (typeof resp === 'string') imgUrl = resp
        else if (resp.data) {
          const data = resp.data
          if (typeof data === 'string') imgUrl = data
          else if (data.url || data.path || data.picture) imgUrl = data.url || data.path || data.picture
          else if (Array.isArray(data) && data.length && typeof data[0] === 'string') imgUrl = data[0]
        }
        if (imgUrl && typeof imgUrl === 'string') uploadedUrls.push(imgUrl.trim())
        else { alert('上传返回格式异常：' + JSON.stringify(resp)); return }
      } catch (uploadErr) {
        console.error('[CommentModal] 图片上传失败:', uploadErr)
        alert('图片上传失败：' + (uploadErr.message || '网络错误'))
        return
      }
    }

    // 合并并去重
    const allImgs = [...new Set([...(form.value.imgUrl || []), ...uploadedUrls])].filter(Boolean)
    form.value.imgUrl = allImgs

    // 编辑或新增
    if (props.existingComment && props.existingComment.id) {
      const updateDto = {
        id: normalizeId(props.existingComment.id || props.existingComment._id || props.existingComment),
        _id: normalizeId(props.existingComment._id || props.existingComment.id || props.existingComment),
        star: Math.max(1, Math.min(5, Number(form.value.star))),
        content: String(form.value.content).trim(),
        images: allImgs,
        imgUrl: JSON.stringify(allImgs || [])
      }
      console.debug('[CommentModal] updateCom payload:', updateDto)
      const res = await api.updateComment(updateDto)
      console.debug('[CommentModal] updateCom resp:', res)
      const isSuccess = res && (res.code === 1 || res.code === 200 || res.success)
      if (isSuccess) {
        // 提交成功后再执行实际的删除请求（pendingDeletes 中记录的 URL）
        for (const url of (pendingDeletes.value || [])) {
          try { await api.deleteCommentImage(url); console.debug('[CommentModal] deleted', url) } catch (e) { console.warn('[CommentModal] delete failed', url, e) }
        }
        pendingDeletes.value = []
        emit('submitted', { success: true, res })
        alert('评论修改成功！')
      } else {
        emit('submitted', { success: false, res })
        alert('评论修改失败：' + (res?.msg || res?.error || '未知错误'))
      }
    } else {
      const addDto = {
        orderId: Number(orderIdToUse),
        star: Math.max(1, Math.min(5, Number(form.value.star))),
        content: String(form.value.content).trim(),
        images: allImgs,
        imgUrl: allImgs,
        goodsId: props.goodsId ? Number(props.goodsId) : 0
      }
      console.debug('[CommentModal] addCom payload:', addDto)
      const res = await api.addComment(addDto)
      const isSuccess = res && (res.code === 1 || res.code === 200 || res.success)
      if (isSuccess) {
        emit('submitted', { success: true, res })
        alert('评论提交成功！')
      } else {
        emit('submitted', { success: false, res })
        alert('评论提交失败：' + (res?.msg || res?.error || '未知错误'))
      }
    }

    close()
  } catch (mainErr) {
    console.error('[CommentModal] 提交评价异常:', mainErr)
    alert('提交失败：' + mainErr.message)
  } finally {
    uploading.value = false
    // 清理预览URL和选中的文件
    filePreviews.value.forEach(url => { try { if (url) URL.revokeObjectURL(url) } catch (e) {} })
    selectedFiles.value = []
    filePreviews.value = []
  }
}

// 修复：删除现有图片的逻辑优化 — 只在本地标记为待删除，真正的删除在提交后执行
function removeExistingImage(url) {
  if (!url || typeof url !== 'string') return false
  // 明确告知用户：删除将在提交后生效
  if (!confirm('确定删除该图片？删除将在提交后生效，取消将不会删除。')) return false

  // 仅从当前显示列表移除，并记录到 pendingDeletes，由 submit() 在成功后执行实际删除请求
  form.value.imgUrl = (form.value.imgUrl || []).filter(x => x !== url)
  if (!pendingDeletes.value.includes(url)) pendingDeletes.value.push(url)
  return true
}
</script>

<style scoped>
.comment-modal-overlay {
  position: fixed; inset:0; background: rgba(0,0,0,0.5); display:flex; align-items:center; justify-content:center; z-index:1400;
}
.comment-modal { 
  width:480px; 
  background:white; 
  border-radius:8px; 
  padding:1rem; 
  box-shadow:0 20px 50px rgba(0,0,0,0.3); 
  position:relative;
  box-sizing: border-box;
}
.comment-modal .close { 
  position:absolute; 
  right:12px; 
  top:10px; 
  border:none; 
  background:transparent; 
  font-size:20px;
  cursor:pointer;
  color:#666;
}
.comment-modal .close:hover { color:#f59e0b; }
.form-row { 
  margin-top:0.8rem;
  width:100%;
  box-sizing: border-box;
}
.form-row label {
  display:block;
  margin-bottom:0.3rem;
  color:#333;
  font-size:14px;
}
textarea { 
  width:100%; 
  min-height:90px; 
  padding:0.5rem;
  border:1px solid #e5e7eb;
  border-radius:6px;
  box-sizing: border-box;
  font-size:14px;
}
select {
  padding:0.4rem 0.6rem;
  border:1px solid #e5e7eb;
  border-radius:6px;
  font-size:14px;
  color:#333;
}
.upload-list { 
  margin-top:0.5rem; 
  display:flex; 
  gap:0.8rem; 
  flex-wrap:wrap;
}
.upload-list span { 
  background:#f9fafb; 
  padding:0.3rem 0.6rem; 
  border-radius:6px;
  align-items:center;
}
.actions { 
  display:flex; 
  gap:0.8rem; 
  justify-content:flex-end; 
  margin-top:1rem;
}
.btn { 
  background:#f59e0b; 
  color:white; 
  padding:0.6rem 1rem; 
  border-radius:6px; 
  border:none;
  cursor:pointer;
  font-size:14px;
}
.btn:disabled {
  background:#d1d5db;
  cursor:not-allowed;
}
.btn-outline { 
  background:transparent; 
  border:1px solid #f59e0b; 
  color:#f59e0b;
  padding:0.6rem 1rem; 
  border-radius:6px;
  cursor:pointer;
  font-size:14px;
}
.btn-outline:disabled {
  border-color:#d1d5db;
  color:#d1d5db;
  cursor:not-allowed;
}
</style>