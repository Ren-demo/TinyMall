<template>
  <div class="inventory-section">
    <div class="section-header">
      <div class="title-row">
        <h2>库存管理</h2>
        <button v-if="canAddGoods" class="btn btn-sm btn-primary" @click="openAddModal">➕ 新增商品</button>
      </div>
      <div class="inventory-stats">
        <div class="stat-mini">
          <span class="stat-label">总商品数：</span>
          <span class="stat-value">{{ products.length }}</span>
        </div>
        <div class="stat-mini">
          <span class="stat-label">总库存：</span>
          <span class="stat-value">{{ totalStock }}</span>
        </div>
        <div class="stat-mini">
          <span class="stat-label">库存预警：</span>
          <span class="stat-value text-danger">{{ warningCount }}</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else class="inventory-grid">
      <div v-for="product in products" :key="product.GoodsID" class="inventory-card">
        <div class="card-header">
          <h3 class="product-name">{{ product.GoodsName }}</h3>
          <span
            :class="['stock-badge', product.Count <= 10 ? 'warning' : '']"
          >
            {{ product.Count <= 10 ? '⚠️ 低库存' : '✓ 充足' }}
          </span>
        </div>

        <div class="card-body">
          <div class="product-image">
            <img :src="product.Picture || fallbackImage" :alt="product.GoodsName" @error="onImageError" />
          </div>
          <div class="stock-display">
            <div class="current-stock">
              <span class="stock-label">当前库存</span>
              <span class="stock-number">{{ product.Count }}</span>
              <span class="stock-unit">件</span>
            </div>
          </div>

          <div class="stock-info">
            <div class="info-row">
              <span class="info-label">商品 ID：</span>
              <span class="info-value">#{{ product.GoodsID }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">单价：</span>
              <span class="info-value">¥{{ product.Price }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">库存价值：</span>
              <span class="info-value">¥{{ (product.Count * product.Price).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <div class="footer-actions">
            <button v-if="canAddGoods" @click="openEditModal(product)" class="btn btn-sm btn-outline">
              ✏️ 修改
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="successMsg" class="alert alert-success">
      ✓ {{ successMsg }}
    </div>

    <div v-if="showAddModal" class="modal-mask">
      <div class="modal">
        <div class="modal-header">
          <h3>新增商品</h3>
          <button class="btn-close" @click="closeAddModal">×</button>
        </div>
        <form class="modal-body" @submit.prevent="submitAddGoods">
          <div class="form-grid">
            <label class="form-item">
              <span>商品名称</span>
              <input v-model.trim="addForm.goodsName" type="text" placeholder="请输入商品名称" required />
            </label>
            <label class="form-item">
              <span>商品描述</span>
              <input v-model.trim="addForm.text" type="text" placeholder="简要描述" />
            </label>
            <label class="form-item">
              <span>库存数量</span>
              <input v-model.number="addForm.count" type="number" min="0" placeholder="0" />
            </label>
            <label class="form-item">
              <span>单价（¥）</span>
              <input v-model.number="addForm.price" type="number" min="0" step="0.01" placeholder="0.00" />
            </label>
            <label class="form-item">
              <span>图片</span>
              <input type="file" accept="image/*" @change="onAddFileChange" />
            </label>
            <div v-if="addPreview" class="preview">
              <img :src="addPreview" alt="预览" />
              <button type="button" class="btn-link" @click="clearAddFile">清除图片</button>
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="closeAddModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="adding">{{ adding ? '提交中...' : '提交' }}</button>
          </div>
        </form>
        <div v-if="addResult" class="modal-result" :class="{ success: addResult.success, error: !addResult.success }">
          {{ addResult.msg || addResult.error || addResult.details || (addResult.success ? '新增成功' : '新增失败') }}
        </div>
      </div>
    </div>

    <div v-if="showEditModal" class="modal-mask">
      <div class="modal">
        <div class="modal-header">
          <h3>修改商品信息</h3>
          <button class="btn-close" @click="closeEditModal">×</button>
        </div>
        <form class="modal-body" @submit.prevent="submitEditGoods">
          <div class="form-grid">
            <label class="form-item">
              <span>商品名称</span>
              <input v-model.trim="editForm.goodsName" type="text" placeholder="请输入商品名称" />
            </label>
            <label class="form-item">
              <span>商品描述</span>
              <input v-model.trim="editForm.text" type="text" placeholder="简要描述" />
            </label>
            <label class="form-item">
              <span>库存变更（增/减）</span>
              <input v-model.number="editForm.delta" type="number" placeholder="如 10 或 -5" />
            </label>
            <label class="form-item">
              <span>单价（¥）</span>
              <input v-model.number="editForm.price" type="number" min="0" step="0.01" placeholder="0.00" />
            </label>
            <label class="form-item">
              <span>更换图片（可选）</span>
              <input type="file" accept="image/*" @change="onEditFileChange" />
            </label>
            <div v-if="editPreview" class="preview">
              <img :src="editPreview" alt="预览" />
              <button type="button" class="btn-link" @click="clearEditFile">清除图片</button>
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="closeEditModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="editing">{{ editing ? '保存中...' : '保存修改' }}</button>
          </div>
        </form>
        <div v-if="editResult" class="modal-result" :class="{ success: editResult.success, error: !editResult.success }">
          {{ editResult.msg || editResult.error || editResult.details || (editResult.success ? '修改成功' : '修改失败') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../../services/api'

const products = ref([])
const loading = ref(true)
const successMsg = ref('')
const showAddModal = ref(false)
const addPreview = ref('')
const adding = ref(false)
const addResult = ref(null)
const showEditModal = ref(false)
const editing = ref(false)
const editResult = ref(null)
const editPreview = ref('')
const editForm = ref({
  goodsId: null,
  goodsName: '',
  text: '',
  price: null,
  delta: 0,
  file: null
})
const addForm = ref({
  goodsName: '',
  text: '',
  count: 0,
  price: 0,
  file: null
})
const fallbackImage = '/assets/prod-a.jpg'

const totalStock = computed(() =>
  products.value.reduce((sum, p) => sum + (p.Count || 0), 0)
)

const warningCount = computed(() =>
  products.value.filter(p => (p.Count || 0) <= 10).length
)

const props = defineProps({ storeId: { type: [String, Number], default: null } })
const canAddGoods = computed(() => props.storeId !== null && props.storeId !== undefined && props.storeId !== '')

onMounted(async () => {
  await loadInventory()
})

async function loadInventory() {
  let list = []
  if (props.storeId) {
    list = await api.listGoods(props.storeId)
  } else {
    list = await api.fetchInventory()
  }
  products.value = list
  loading.value = false
}


function showSuccess(msg) {
  successMsg.value = msg
  setTimeout(() => {
    successMsg.value = ''
  }, 2000)
}

function openAddModal() {
  showAddModal.value = true
  addResult.value = null
}

function closeAddModal() {
  showAddModal.value = false
  resetAddForm()
}

function resetAddForm() {
  addForm.value = { goodsName: '', text: '', count: 0, price: 0, file: null }
  addPreview.value = ''
  adding.value = false
  addResult.value = null
}

function openEditModal(product) {
  if (!product) return
  editForm.value = {
    goodsId: product.GoodsID,
    goodsName: product.GoodsName || '',
    text: product.Text || '',
    price: product.Price ?? null,
    delta: 0,
    file: null
  }
  editPreview.value = ''
  editResult.value = null
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  editResult.value = null
  editPreview.value = ''
  editing.value = false
}

function onEditFileChange(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  editForm.value.file = file
  const reader = new FileReader()
  reader.onload = () => { editPreview.value = reader.result || '' }
  reader.readAsDataURL(file)
}

function clearEditFile() {
  editForm.value.file = null
  editPreview.value = ''
}

function onImageError(e) {
  if (e && e.target) {
    e.target.src = fallbackImage
  }
}

function onAddFileChange(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  addForm.value.file = file
  const reader = new FileReader()
  reader.onload = () => { addPreview.value = reader.result || '' }
  reader.readAsDataURL(file)
}

function clearAddFile() {
  addForm.value.file = null
  addPreview.value = ''
}

async function submitAddGoods() {
  if (!addForm.value.goodsName) {
    addResult.value = { success: false, msg: '请输入商品名称' }
    return
  }
  adding.value = true
  addResult.value = null
  try {
    const res = await api.addGoods({
      file: addForm.value.file,
      goodsName: addForm.value.goodsName,
      storeId: props.storeId,
      text: addForm.value.text,
      count: addForm.value.count,
      price: addForm.value.price
    })
    addResult.value = res || { success: false, msg: '未知响应' }
    if (addResult.value.success) {
      await loadInventory()
      showSuccess('新增商品成功')
      closeAddModal()
    }
  } catch (e) {
    console.error('[Inventory.submitAddGoods] exception:', e && e.message)
    addResult.value = { success: false, msg: '提交失败，请稍后重试', details: e && e.message }
  } finally {
    adding.value = false
  }
}

async function submitEditGoods() {
  if (!editForm.value.goodsId) {
    editResult.value = { success: false, msg: '商品ID缺失' }
    return
  }
  editing.value = true
  editResult.value = null
  try {
    const delta = Number(editForm.value.delta || 0)
    const res = await api.updateGoods({
      goodsId: editForm.value.goodsId,
      goodsName: editForm.value.goodsName,
      text: editForm.value.text,
      price: editForm.value.price,
      count: delta !== 0 ? Math.abs(delta) : null,
      add: delta !== 0 ? delta > 0 : null,
      file: null
    })
    if (editForm.value.file) {
      const picRes = await api.updateGoodsPic(editForm.value.goodsId, editForm.value.file)
      try { console.debug('[Inventory.submitEditGoods] picRes:', picRes) } catch (e) {}
      if (picRes && !picRes.success) {
        editResult.value = picRes
        return
      }
    }
    try { console.debug('[Inventory.submitEditGoods] res:', res) } catch (e) {}
    editResult.value = res || { success: false, msg: '未知响应' }
    if (editResult.value.success) {
      await loadInventory()
      showSuccess('商品信息已更新')
      closeEditModal()
    }
  } catch (e) {
    console.error('[Inventory.submitEditGoods] exception:', e && e.message)
    editResult.value = { success: false, msg: '提交失败，请稍后重试', details: e && e.message }
  } finally {
    editing.value = false
  }
}
</script>

<style scoped>
.inventory-section {
  width: 100%;
}

.section-header {
  margin-bottom: 2rem;
}

.section-header h2 {
  margin: 0 0 1rem 0;
}

.title-row { display:flex; align-items:center; justify-content:space-between; gap:1rem }

.inventory-stats {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.stat-label {
  font-weight: 600;
  color: #6c757d;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #28a745;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.btn-primary { background:#2563eb; color:white }
.btn-primary:hover { background:#1d4ed8 }
.btn-outline { background:white; border:1px solid #cbd5e1; color:#334155 }
.btn-outline:hover { border-color:#94a3b8 }
.btn-link { background:none; border:none; color:#2563eb; cursor:pointer; padding:0 }
.btn-close { background:none; border:none; font-size:1.4rem; cursor:pointer }

.modal-mask { position:fixed; inset:0; background:rgba(15,23,42,0.45); display:flex; align-items:center; justify-content:center; z-index:50 }
.modal { background:white; width:min(680px, 92vw); border-radius:12px; box-shadow:0 20px 60px rgba(15,23,42,0.25); overflow:hidden }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:0.9rem 1rem; border-bottom:1px solid #e5e7eb }
.modal-body { padding:1rem }
.form-grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap:0.75rem }
.form-item { display:flex; flex-direction:column; gap:0.4rem; font-size:0.9rem }
.form-item input { padding:0.45rem 0.6rem; border:1px solid #e5e7eb; border-radius:6px }
.preview { display:flex; flex-direction:column; gap:0.35rem; align-items:flex-start }
.preview img { max-width:180px; border-radius:8px; border:1px solid #e5e7eb }
.modal-actions { margin-top:1rem; display:flex; justify-content:flex-end; gap:0.75rem }
.modal-result { padding:0.65rem 1rem; border-top:1px dashed #e5e7eb }
.modal-result.success { color:#065f46; background:#ecfdf5 }
.modal-result.error { color:#991b1b; background:#fef2f2 }
.footer-actions { display:flex; gap:0.5rem; align-items:center }


    .inventory-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 0.6rem;
}

.inventory-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.18s ease;
}

.inventory-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  background: linear-gradient(135deg, #22c55e 0%, #10b981 100%);
  color: white;
  padding: 0.45rem 0.6rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  margin: 0;
  font-size: 0.98rem;
}

.stock-badge {
  padding: 0.25rem 0.6rem;
  background: rgba(255, 255, 255, 0.24);
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 600;
}

.stock-badge.warning {
  background: #f59e0b;
  color: white;
}

.card-body {
  padding: 0.45rem 0.6rem;
}

.product-image { display:flex; justify-content:center; margin-bottom:0.45rem }
.product-image img { width:100%; max-width:200px; height:120px; object-fit:cover; border-radius:8px; border:1px solid #eef2f6; background:#f8fafc }

.stock-display {
  text-align: center;
  margin-bottom: 0.45rem;
  padding: 0.35rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.current-stock {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stock-label { font-size: 0.72rem; color:#6c757d; margin-bottom:0.2rem }
.stock-number { font-size: 1.0rem; font-weight:700; color:#16a34a; line-height:1 }
.stock-unit { font-size:0.8rem; color:#6c757d; margin-left:0.08rem }

.stock-info {
  display: grid;
  gap: 0.5rem;
}

.info-row { display:flex; justify-content:space-between; padding:0.35rem; background:#f8f9fa; border-radius:6px }

.info-label {
  font-weight: 600;
  color: #6c757d;
}

.info-value {
  color: #2c3e50;
  font-weight: 600;
}

.card-footer { padding: 0.45rem; border-top: 1px solid #eef2f6 }

.card-footer button {
  width: 100%;
}

.alert {
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .inventory-grid {
    grid-template-columns: 1fr;
  }

  .inventory-stats {
    flex-direction: column;
    gap: 0.75rem;
  }

}
</style>