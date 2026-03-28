<template>
  <section class="add-supplier">
    <header class="header">
      <div>
        <h2>新增供应商</h2>
        <p>填写供应商信息并提交创建。</p>
      </div>
      <div class="tips">
        <span>接口：</span>
        <code>/tinymall/mall/addStore</code>
      </div>
    </header>

    <div class="card">
      <form @submit.prevent="onSubmit">
        <div class="form-grid">
          <div class="form-group">
            <label>供应商名称</label>
            <input v-model.trim="form.storeName" type="text" placeholder="如：星火供应链" required />
          </div>
          <div class="form-group">
            <label>供应商邮箱</label>
            <input v-model.trim="form.storeEmail" type="email" placeholder="如：supplier@example.com" />
          </div>
        </div>

        <div class="form-group">
          <label>供应商图片（可选）</label>
          <div class="upload-row">
            <input type="file" accept="image/*" @change="onFileChange" />
            <button type="button" class="btn-ghost" @click="clearImage" :disabled="!preview">清除图片</button>
          </div>
          <div v-if="preview" class="preview">
            <img :src="preview" alt="预览" />
            <div class="preview-info">已选择图片，将以 Base64 形式提交</div>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn" type="submit" :disabled="submitting">{{ submitting ? '提交中...' : '提交新增' }}</button>
          <button class="btn-secondary" type="button" @click="reset" :disabled="submitting">重置</button>
        </div>
      </form>

      <div v-if="result" class="result" :class="{ success: result.success, error: !result.success }">
        <strong>{{ result.success ? '创建成功' : '创建失败' }}</strong>
        <div class="msg">{{ result.msg || result.error || '请检查输入或稍后再试' }}</div>
        <div v-if="result.data" class="data">
          <div class="title">返回数据</div>
          <pre>{{ result.data }}</pre>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import api from '../../services/api'

const form = reactive({
  storeName: '',
  storeEmail: '',
  picture: ''
})

const preview = ref('')
const submitting = ref(false)
const result = ref(null)

function reset() {
  form.storeName = ''
  form.storeEmail = ''
  form.picture = ''
  preview.value = ''
  result.value = null
}

function clearImage() {
  form.picture = ''
  preview.value = ''
}

function onFileChange(event) {
  const file = event.target.files && event.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    const base64 = reader.result
    form.picture = base64 || ''
    preview.value = base64 || ''
  }
  reader.readAsDataURL(file)
}

async function onSubmit() {
  if (!form.storeName) {
    result.value = { success: false, msg: '请输入供应商名称' }
    return
  }
  submitting.value = true
  result.value = null
  try {
    const res = await api.addStore({
      storeName: form.storeName,
      storeEmail: form.storeEmail,
      picture: form.picture
    })
    result.value = res || { success: false, msg: '未知响应' }
    if (result.value && result.value.success) {
      form.storeName = ''
      form.storeEmail = ''
      form.picture = ''
      preview.value = ''
    }
  } catch (e) {
    result.value = { success: false, msg: '提交失败，请稍后重试' }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.add-supplier { display:flex; flex-direction:column; gap:1rem }
.header { display:flex; align-items:center; justify-content:space-between; gap:1rem }
.header h2 { margin:0 }
.header p { margin:0.3rem 0 0; color:#6b7280 }
.tips { font-size:0.85rem; color:#6b7280; background:#f8fafc; padding:0.4rem 0.6rem; border-radius:6px }
.tips code { color:#fb6b2a }

.card { background:#fff; border-radius:12px; padding:1rem; box-shadow:0 8px 24px rgba(2,6,23,0.06) }
.form-grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap:1rem }
.form-group { display:flex; flex-direction:column; gap:0.5rem; margin-bottom:0.75rem }
.form-group label { font-weight:600 }
.form-group input[type="text"],
.form-group input[type="email"] { padding:0.55rem 0.7rem; border:1px solid #e5e7eb; border-radius:8px }

.upload-row { display:flex; align-items:center; gap:0.75rem }
.preview { margin-top:0.75rem; background:#f8fafc; padding:0.75rem; border-radius:8px; display:flex; flex-direction:column; gap:0.5rem }
.preview img { max-width:220px; border-radius:8px; border:1px solid #e5e7eb }
.preview-info { font-size:0.8rem; color:#6b7280 }

.form-actions { display:flex; gap:0.75rem; margin-top:0.5rem }
.btn { background:linear-gradient(90deg,#fb923c,#fb6b2a); color:white; border:none; padding:0.55rem 1.2rem; border-radius:8px; cursor:pointer }
.btn:disabled { opacity:0.6; cursor:not-allowed }
.btn-secondary { background:#e2e8f0; color:#1f2937; border:none; padding:0.55rem 1.1rem; border-radius:8px; cursor:pointer }
.btn-ghost { background:#fff; color:#475569; border:1px dashed #cbd5f5; padding:0.4rem 0.8rem; border-radius:8px; cursor:pointer }

.result { margin-top:1rem; padding:0.75rem; border-radius:8px; display:flex; flex-direction:column; gap:0.4rem }
.result.success { background:#ecfdf5; color:#065f46; border:1px solid #a7f3d0 }
.result.error { background:#fef2f2; color:#991b1b; border:1px solid #fecaca }
.result .data { margin-top:0.5rem; background:#fff; border-radius:6px; padding:0.5rem }
.result pre { margin:0; white-space:pre-wrap; word-break:break-all }

@media (max-width: 900px) {
  .header { flex-direction:column; align-items:flex-start }
}
</style>
