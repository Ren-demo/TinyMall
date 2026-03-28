<template>
  <div class="products-section">
    <div class="section-header">
      <h2>商品列表</h2>
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索商品..."
          class="search-input"
        />
      </div>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredProductsAll.length === 0" class="empty-state">
      <div class="empty-state-icon">📭</div>
      <h3>暂无商品</h3>
      <p>没有找到相应的商品</p>
    </div>

    <div v-else class="products-grid">
      <div v-for="product in paginatedProducts" :key="product.GoodsID" class="product-card" @click="openDetail(product)">
        <div class="product-header">
          <img v-if="product.Picture" :src="product.Picture" alt="" class="product-img" />
          <div v-else class="product-icon">📦</div>
        </div>

        <div class="product-body">
          <h3 class="product-name">{{ product.GoodsName }}</h3>
          <div class="product-supplier">
            <small>供货商：
              <span v-if="storesMap[product.StoreID]">{{ storesMap[product.StoreID].StoreName }}</span>
              <span v-else-if="product.StoreName || product.storeName">{{ product.StoreName || product.storeName }}</span>
              <span v-else>未知</span>
            </small>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品详情页面 -->
    <teleport to="body">
      <div v-if="showDetailModal" class="detail-overlay" @click="closeDetail">
        <div class="detail-modal" @click.stop>
          <button class="detail-close" @click="closeDetail">×</button>
          <div class="detail-container">
            <div class="detail-left">
              <div class="detail-media">
                <img v-if="detailProduct && detailProduct.Picture" :src="detailProduct.Picture" alt="" />
                <div v-else class="detail-placeholder">暂无图片</div>
                <div class="detail-desc">
                  <h3>{{ detailProduct?.GoodsName }}</h3>
                  <p>{{ detailProduct?.Text || '暂无描述' }}</p>
                </div>
              </div>
              <div class="detail-actions">
                <div>
                  <div class="detail-price">¥{{ detailProduct?.Price }}</div>
                  <div class="detail-stock">库存：<span :class="{ 'text-danger': (detailProduct?.Count || 0) <= 10 }">{{ detailProduct?.Count || 0 }} 件</span></div>
                  <div class="detail-supplier">供货商：<span>{{ (storesMap[detailProduct?.StoreID]?.StoreName) || detailProduct?.StoreName || '未知' }}</span></div>
                </div>
                <div class="detail-quantity">
                  <label>数量</label>
                  <div class="qty-control">
                    <button @click="decreaseDetailQty">-</button>
                    <input type="number" v-model.number="detailQty" min="1" :max="detailProduct?.Count || 0" @blur="onDetailQtyBlur" />
                    <button @click="increaseDetailQty">+</button>
                  </div>
                  <div class="detail-total">总价：¥{{ detailTotal }}</div>
                </div>
                <div class="detail-buttons">
                  <button class="btn" @click.stop="addToCartFromDetail">🛒 加入购物车</button>
                  <button class="btn btn-primary" @click.stop="buyNowFromDetail">⚡ 直接购买</button>
                </div>
              </div>
            </div>
            <div class="detail-right">
              <CommentSection ref="commentSectionRef" v-if="detailProduct" :goodsId="detailProduct.GoodsID" @edit="handleEditComment" />
            </div>
          </div>
        </div>
    </div>
    </teleport>

    <!-- 分页 -->
    <div v-if="filteredProductsAll.length > 0" class="pagination-bar">
      <div class="pagination-left">
        <label>每页</label>
        <select v-model.number="pageSize">
          <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}</option>
        </select>
        <span class="pagination-span">共 {{ filteredProductsAll.length }} 件</span>
      </div>
      <div class="pagination-right">
        <button @click="currentPage = 1" :disabled="currentPage===1">«</button>
        <button @click="currentPage = Math.max(1, currentPage-1)" :disabled="currentPage===1">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button @click="currentPage = Math.min(totalPages, currentPage+1)" :disabled="currentPage===totalPages">下一页</button>
        <button @click="currentPage = totalPages" :disabled="currentPage===totalPages">»</button>
      </div>
    </div>

    <div v-if="notification" class="notification" :class="notificationClass">
      {{ notification }}
    </div>
    <CommentModal v-model="commentVisible" :goodsId="commentGoodsId" :orderId="commentOrderId" :existingComment="commentExisting" @submitted="onCommentModalSubmitted" />
    <AIChatWidget />
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, computed, watch, onUnmounted } from 'vue'
import api from '../../services/api'
import CommentSection from '../../components/CommentSection.vue'
import CommentModal from '../../components/CommentModal.vue'
import AIChatWidget from '../../components/AIChatWidget.vue'
import { useCartStore } from '../../stores/cart'
import { reactive } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { useRouter, useRoute } from 'vue-router'

const products = ref([])
const loading = ref(true)
const searchQuery = ref('')
const notification = ref('')
const notificationClass = ref('')
const showDetailModal = ref(false)
const detailProduct = ref(null)
const detailQty = ref(1)

const detailTotal = computed(() => {
  const price = Number(detailProduct.value?.Price) || 0
  const qty = Number(detailQty.value) || 1
  // assume price stored as number (not cents)
  return (price * qty).toFixed(2)
})

// lock body scroll when modal open
watch(showDetailModal, (val) => {
  try { document.body.style.overflow = val ? 'hidden' : '' } catch (e) {}
})

onUnmounted(() => {
  try { document.body.style.overflow = '' } catch (e) {}
})

// When using server-side pagination, `products` already contains current page rows.
// We keep a simple alias so template code remains consistent.
const filteredProductsAll = computed(() => products.value)

// Pagination (server-side)
const currentPage = ref(1)
const pageSize = ref(24)
const pageSizes = [8, 12, 24, 48]
const totalCount = ref(0)

const totalPages = computed(() => {
  return Math.max(1, Math.ceil((Number(totalCount.value) || 0) / pageSize.value))
})

// products may hold either current page rows (server pagination)
// or the full filtered dataset (local pagination when searching)
const localPagination = ref(false)
const paginatedProducts = computed(() => {
  if (localPagination.value) {
    const start = (currentPage.value - 1) * pageSize.value
    return filteredProductsAll.value.slice(start, start + pageSize.value)
  }
  return filteredProductsAll.value
})

watch([searchQuery, pageSize], () => { currentPage.value = 1 })

const cartStore = useCartStore()
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const storesMap = reactive({})
const qtyMap = reactive({})
const commentSectionRef = ref(null)
const commentVisible = ref(false)
const commentGoodsId = ref(null)
const commentOrderId = ref(null)
const commentExisting = ref(null)

async function loadProducts() {
  loading.value = true
  try {
    const q = String(searchQuery.value || '').trim()
    if (q) {
      // When user is searching, fetch the global list and do client-side filtering + pagination
      localPagination.value = true
      const all = await api.fetchProducts()
      const lc = q.toLowerCase()
      const filtered = all.filter(p => ((p.GoodsName || '') + '').toLowerCase().includes(lc) || ((p.Text || '') + '').toLowerCase().includes(lc))
      products.value = filtered.map(r => ({
        GoodsID: r.GoodsID ?? r.goodsID ?? r.goodsId ?? r.id,
        GoodsName: r.GoodsName ?? r.goodsName ?? r.name,
        StoreID: r.StoreID ?? r.storeID ?? r.storeId,
        StoreName: r.StoreName ?? r.storeName ?? r.supplierName ?? '',
        Text: r.Text ?? r.text ?? r.description,
        Count: r.Count ?? r.count ?? r.stock,
        Price: r.Price ?? r.price ?? r.unitPrice,
        Picture: r.Picture ?? r.picture ?? r.image
      }))
      totalCount.value = products.value.length
    } else {
      // No search: use server-side pagination for performance
      localPagination.value = false
      const dto = { countMin: 1 }
      const resp = await api.fetchProductsInpage({ pageNum: currentPage.value, pageSize: pageSize.value, listGoodsDto: dto })
      console.debug('[ProductList] fetchProductsInpage resp:', resp)
      if (resp && resp.success) {
        const rows = resp.rows || []
        products.value = rows.map(r => ({
          GoodsID: r.GoodsID ?? r.goodsID ?? r.goodsId ?? r.id,
          GoodsName: r.GoodsName ?? r.goodsName ?? r.name,
          StoreID: r.StoreID ?? r.storeID ?? r.storeId,
          StoreName: r.StoreName ?? r.storeName ?? r.supplierName ?? '',
          Text: r.Text ?? r.text ?? r.description,
          Count: r.Count ?? r.count ?? r.stock,
          Price: r.Price ?? r.price ?? r.unitPrice,
          Picture: r.Picture ?? r.picture ?? r.image
        }))
        totalCount.value = resp.total ?? (products.value || []).length
      } else {
        // fallback to global fetch if paginated endpoint unavailable
        localPagination.value = true
        const all = await api.fetchProducts()
        products.value = all.map(r => ({
          GoodsID: r.GoodsID ?? r.goodsID ?? r.goodsId ?? r.id,
          GoodsName: r.GoodsName ?? r.goodsName ?? r.name,
          StoreID: r.StoreID ?? r.storeID ?? r.storeId,
          StoreName: r.StoreName ?? r.storeName ?? r.supplierName ?? '',
          Text: r.Text ?? r.text ?? r.description,
          Count: r.Count ?? r.count ?? r.stock,
          Price: r.Price ?? r.price ?? r.unitPrice,
          Picture: r.Picture ?? r.picture ?? r.image
        }))
        totalCount.value = products.value.length
      }
    }
    // load stores cache
    const stores = await api.fetchStores()
    stores.forEach(s => { storesMap[s.StoreID] = s })
  } catch (e) {
    localPagination.value = true
    products.value = await api.fetchProducts()
    totalCount.value = products.value.length
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadProducts(); cartStore.init() })
onActivated(loadProducts)

watch([currentPage, pageSize], () => {
  loadProducts()
})

// when search changes, reset to page 1 and reload from server
watch(searchQuery, () => {
  currentPage.value = 1
  loadProducts()
})

function handleEditComment(c) {
  // open modal to edit existing comment
  commentExisting.value = c || null
  commentGoodsId.value = c?.goodsId ?? c?.GoodsID ?? detailProduct.value?.GoodsID
  commentOrderId.value = c?.orderId ?? c?.orderId ?? null
  commentVisible.value = true
}

function onCommentModalSubmitted(payload) {
  // refresh comments in the section
  try { commentSectionRef.value && commentSectionRef.value.loadComments && commentSectionRef.value.loadComments() } catch (e) {}
  // clear temp state
  commentExisting.value = null
  commentVisible.value = false
}

function addToCart(product) {
  if ((product.Count || 0) === 0) return
  // 未登录阻止加入购物车并提示登录
  if (!auth.isLoggedIn) {
    notification.value = '请先登录再添加购物车'
    notificationClass.value = 'error'
    setTimeout(() => { notification.value = '' }, 2000)
    // 跳转到登录页并携带当前页面为 redirect
    setTimeout(() => { router.push({ name: 'Login', query: { redirect: route.fullPath } }) }, 800)
    return
  }
  normalizeQty(product)
  const qty = qtyMap[product.GoodsID] || 1
  cartStore.addItem({ productId: product.GoodsID, name: product.GoodsName, price: product.Price, qty, storeId: product.StoreID })
  notification.value = `✓ "${product.GoodsName}" 已加入购物车`
  notificationClass.value = 'success'
  setTimeout(() => { notification.value = '' }, 2000)
}

async function buyNow(product) {
  if ((product.Count || 0) === 0) return
  normalizeQty(product)
  const qty = qtyMap[product.GoodsID] || 1
  if (!auth.username) {
    notification.value = '请先登录再购买'
    notificationClass.value = 'error'
    setTimeout(() => { notification.value = '' }, 2000)
    setTimeout(() => { router.push({ name: 'Login', query: { redirect: route.fullPath } }) }, 800)
    return
  }
  const uid = auth.userId
  if (!uid) {
    notification.value = '缺少用户ID，请重新登录'
    notificationClass.value = 'error'
    setTimeout(() => { notification.value = '' }, 2000)
    return
  }
  if (!auth.address) {
    notification.value = '请先在个人信息中填写收货地址'
    notificationClass.value = 'error'
    setTimeout(() => { notification.value = '' }, 2000)
    return
  }
  const res = await api.addOrder({ goodsId: product.GoodsID, count: qty, userId: uid, address: auth.address })
  if (res && (res.code === 1 || res.code === 200 || res.success)) {
    notification.value = `✓ 已下单 ${product.GoodsName} × ${qty}`
    notificationClass.value = 'success'
    const data = res.data || {}
    const orderId = data.orderId ?? data.OrderID ?? data.id ?? res.orderId ?? res.OrderID ?? res.id
    setTimeout(() => { notification.value = '' }, 600)
    const query = { payGoodsId: String(product.GoodsID) }
    if (orderId) query.payOrderId = String(orderId)
    await router.push({ name: 'MyOrders', query })
  } else {
    notification.value = '下单失败，请稍后重试'
    notificationClass.value = 'error'
  }
  setTimeout(() => { notification.value = '' }, 2000)
}

function openDetail(product) {
  detailProduct.value = product
  // initialize detail quantity from qtyMap or 1
  detailQty.value = qtyMap[product.GoodsID] || 1
  showDetailModal.value = true
}

function closeDetail() {
  showDetailModal.value = false
  detailProduct.value = null
}

function decreaseDetailQty() {
  if (!detailProduct.value) return
  const max = detailProduct.value.Count || 0
  let v = Number(detailQty.value) || 1
  v = Math.max(1, v - 1)
  if (max > 0) v = Math.min(v, max)
  detailQty.value = v
}

function increaseDetailQty() {
  if (!detailProduct.value) return
  const max = detailProduct.value.Count || 0
  let v = Number(detailQty.value) || 1
  v = v + 1
  if (max > 0) v = Math.min(v, max)
  detailQty.value = v
}

function onDetailQtyBlur() {
  if (!detailProduct.value) return
  const max = detailProduct.value.Count || 0
  let v = Number(detailQty.value) || 1
  if (v < 1) v = 1
  if (max > 0 && v > max) v = max
  detailQty.value = v
}

function addToCartFromDetail() {
  if (!detailProduct.value) return
  qtyMap[detailProduct.value.GoodsID] = detailQty.value || 1
  // 如果未登录，提示并跳转，不关闭详情弹窗
  if (!auth.isLoggedIn) {
    notification.value = '请先登录再添加购物车'
    notificationClass.value = 'error'
    setTimeout(() => { notification.value = '' }, 2000)
    setTimeout(() => { router.push({ name: 'Login', query: { redirect: route.fullPath } }) }, 800)
    return
  }
  addToCart(detailProduct.value)
  closeDetail()
}

async function buyNowFromDetail() {
  if (!detailProduct.value) return
  qtyMap[detailProduct.value.GoodsID] = detailQty.value || 1
  await buyNow(detailProduct.value)
  closeDetail()
}

function normalizeQty(product) {
  const max = product.Count || 0
  let val = qtyMap[product.GoodsID]
  if (!val || val < 1) val = 1
  if (max > 0 && val > max) val = max
  qtyMap[product.GoodsID] = val
}
</script>

<style scoped>
.products-section {
  width: 100%;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  margin-left: 0;
  margin-right: 0;
  box-sizing: border-box;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h2 {
  margin: 0;
}

.search-box {
  flex: 0 0 300px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #4ea7ff;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #001eff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.5rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 0rem; /* 较小的左右留白 */
  box-sizing: border-box;
}

/* Pagination (desktop / default) */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.25rem;
  gap: 1rem;
  padding: 0.6rem 0.9rem;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(2,6,23,0.06);
  border: 1px solid rgba(15,23,42,0.04);
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}
.pagination-left { display:flex; align-items:center; gap:1rem }
.pagination-left label { font-weight: 600;font-size: 1rem; color: #475569; display: inline-block; white-space: nowrap }
.pagination-span { font-weight: 600; color: #475569; font-size: 1rem;display: inline-block; white-space: nowrap }
/* .pagination-left label { font-weight: 600; color: #94a3b8; font-size: 0.9rem } */
.pagination-right { display:flex; align-items:center; gap:0.5rem }
.pagination-left select { padding:0.4rem 0.5rem; border-radius:6px; border:5px solid #e6eefc; background:rgb(255, 255, 255) }
.pagination-right button { padding:0.5rem 0.75rem; border-radius:6px; border:1px solid #3d8cda; background:rgb(0, 122, 198); cursor:pointer; transition: all .12s ease }
.pagination-right button:hover:not(:disabled) { background:#2d70b4; transform: translateY(-2px) }
.pagination-right button:disabled { opacity:0.8; cursor:not-allowed; transform:none ; font-weight: 400px;color: #fbfbfb; background-color: #3d3b45;}
.page-info { min-width:100px; text-align:center; font-weight:600; color:#334155; padding:0.25rem 0.5rem; background:#f8fafc; border-radius:6px }

.product-card {
  background: rgb(255, 255, 255);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.product-header {
  background: linear-gradient(135deg, #ff0a9d 0%, #88ff00 100%);
  padding: 0.2rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
}

.product-img {
  width: 100%;
  height: 140px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
}

.product-icon {
  font-size: 2.5rem;
}

.stock-warning {
  background: #ffc107;
  color: white;
  padding: 0.3rem 0.7rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.product-body {
  padding: 0.5rem;
  flex: 1;
}

.product-name {
  font-size: 1rem;
  font-weight: 600;
  color: #000000;
  margin: 0 0 0.1rem 0;
}

.product-supplier {
  font-size: 0.85rem;
  font-weight: 500;
  color: #9d5309;
  margin-bottom: 0.75rem;
}

.product-description {
  color: #7d756c;
  font-size: 0.9rem;
  margin: 0 0 1rem 0;
}

.product-price {
  margin-bottom: 1rem;
}

.price {
  font-size: 1.75rem;
  font-weight: 700;
  color: #667eea;
}

.product-stock {
  font-size: 0.9rem;
  color: #6c757d;
}

.stock-label {
  font-weight: 600;
}

.product-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #1289ff;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.75rem;
}

.qty-label {
  font-size: 0.9rem;
  color: #6c757d;
  min-width: 2.5rem;
}

.qty-input {
  width: 100px;
  padding: 0.4rem 0.6rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
}

.action-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.6rem;
}

.btn-primary {
  background: #f59e0b;
  box-shadow: 0 6px 14px rgba(245, 158, 11, 0.18);
}

.btn-primary:hover:not(:disabled) {
  background: #ff4d00;
}

.notification {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideIn 0.3s ease;
}

.notification.success {
  background: #d4edda;
  color: #155724;
  border-left: 4px solid #28a745;
}

.notification.error {
  background: #f8d7da;
  color: #842029;
  border-left: 4px solid #dc3545;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
  }

  .search-box {
    flex: 1;
    width: 100%;
  }


  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    margin-left: 0;
    margin-right: 0;
    left: 0;
    right: 0;
    width: 100%;
    padding: 0 0.75rem;
  }

  /* Pagination (mobile) - 堆叠布局 */
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 0.6rem;
    padding: 0.5rem;
  }
  .pagination-left, .pagination-right { width: 100%; display:flex; justify-content:space-between; align-items:center }
  .pagination-right { gap: 0.5rem; flex-wrap:wrap }
  .page-info { min-width:60px }

/* Detail modal */
.detail-overlay {
  position:fixed;
  inset:0;
  background: rgba(0,0,0,0.55);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:1200;
  padding:1rem;
}
.detail-modal {
  width: min(1100px, 96%);
  height: auto;
  max-height: 92vh;
  display: flex;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 24px 80px rgba(2,6,23,0.32);
}
.detail-close {
  position:absolute;
  right:14px;
  top:12px;
  background:transparent;
  border:none;
  font-size:26px;
  cursor:pointer;
  color:#334155;
}
.detail-media { position:relative; height:520px; background:#f6f8fb; display:flex; align-items:center; justify-content:center }
.detail-media img { width:100%; height:100%; object-fit:cover; display:block; z-index:1 }
.detail-placeholder { width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:#fff; background:#9aa4b2 }
.detail-desc {
  position:absolute;
  top:20px;
  right:20px;
  max-width:46%;
  padding:1rem 1.25rem;
  background: rgba(255,255,255,0.92);
  color: #0f172a;
  border-radius:8px;
  box-shadow: 0 8px 24px rgba(2,6,23,0.12);
  z-index:5;
}
.detail-desc h3 { margin:0 0 0.4rem 0; font-size:1.25rem }
.detail-desc p { margin:0; opacity:0.9; color:#334155 }
.detail-actions { display:flex; gap:1rem; align-items:flex-start; padding:1rem 1.25rem; flex-wrap:wrap }
.detail-price { font-size:2rem; font-weight:800; color:#0b69ff }
.detail-stock, .detail-supplier { color:#475569; font-size:0.95rem }
.detail-buttons { display:flex; gap:0.75rem; align-items:center }
.detail-buttons .btn { padding:0.8rem 1rem; border-radius:10px; font-weight:600; box-shadow:0 8px 20px rgba(2,6,23,0.08) }
.detail-buttons .btn:first-child { background:#0b69ff; color:white }
.detail-buttons .btn.btn-primary { background:#ff8a00; color:white }
.detail-buttons .btn:first-child:hover { background:#0956d1 }
.detail-buttons .btn.btn-primary:hover { background:#ff6f00 }

.detail-quantity { display:flex; flex-direction:column; gap:0.6rem }
.qty-control { display:flex; align-items:center; gap:8px }
.qty-control button { width:40px; height:40px; border-radius:8px; border:none; background:#0b69ff; color:white; font-size:18px; cursor:pointer }
.qty-control button:hover { filter:brightness(0.95) }
.qty-control input { width:96px; padding:0.5rem 0.6rem; text-align:center; border-radius:8px; border:1px solid #e6eefc; font-size:1rem }
.detail-total { color:#0f172a; font-weight:700 }

.detail-container { display:flex; gap:1rem; align-items:flex-start; height:100% }
.detail-left { flex: 1 1 0; overflow: hidden; display:flex; flex-direction:column }
.detail-right { width: 360px; max-width: 36%; overflow-y: auto; padding: 1rem; box-sizing: border-box; background: linear-gradient(180deg, rgba(250,250,252,0.6), rgba(247,249,255,0.4)); border-left:1px solid rgba(15,23,42,0.04) }

  .notification {
    bottom: 1rem;
    right: 1rem;
    left: 1rem;
  }
}
</style>

<!-- Global styles for teleported detail modal (not scoped) -->
<style>
.detail-overlay {
  position:fixed;
  inset:0;
  background: rgba(0,0,0,0.55);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:1200;
  padding:1rem;
}
.detail-modal {
  width: min(1100px, 96%);
  max-height: 92vh;
  display: flex;
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 24px 80px rgba(2,6,23,0.32);
}
.detail-close { position:absolute; right:14px; top:12px; background:transparent; border:none; font-size:26px; cursor:pointer; color:#334155 }
.detail-media { position:relative; height:520px; background:#f6f8fb; display:flex; align-items:center; justify-content:center }
.detail-media img { width:100%; height:100%; object-fit:cover; display:block; z-index:1 }
.detail-placeholder { width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:#fff; background:#9aa4b2 }
.detail-desc { position:absolute; bottom:20px; left:20px; max-width:80%; padding:1rem 1.25rem; background: rgba(0, 0, 0, 0.3); color: #0f172a; border-radius:8px; box-shadow: 0 8px 24px rgba(2,6,23,0.12); z-index:5 }
.detail-desc h3 { margin:0 0 0.4rem 0; font-size:1.25rem ; color: #ffc061;}
.detail-desc p { margin:0; opacity:0.9; color:#f1f5f9 }
.detail-actions { display:flex; gap:1rem; align-items:flex-start; padding:1rem 1.25rem; flex-wrap:wrap }
.detail-price { font-size:3rem; font-weight:800; color:#0b69ff }
.detail-stock, .detail-supplier { color:#9f5d00; font-size:0.95rem ; font-weight:400 }
.detail-buttons { position:absolute; right:20px; display:flex; gap:0.75rem }
.detail-buttons .btn:first-child { background:#0b69ff; color:white }
.detail-buttons .btn.btn-primary { background:#ff8a00; color:white }
.detail-container { display:flex; gap:1rem; align-items:flex-start; height:100% }
.detail-left { flex: 1 1 0; overflow: hidden; display:flex; flex-direction:column }
.detail-right { width: 360px; max-width: 36%; overflow-y: auto; padding: 1rem; box-sizing: border-box; background: linear-gradient(180deg, rgba(250,250,252,0.6), rgba(247,249,255,0.4)); border-left:1px solid rgba(15,23,42,0.04) }
.detail-quantity { display:flex; flex-direction:row-reverse; gap:0rem }
.detail-quantity label { font-size:0.9rem; color:#6c757d; margin-right:0.5rem }
.qty-control { display:flex; align-items:center; gap:8px }
.qty-control button { width:40px; height:40px; border-radius:8px; border:none; background:#0b69ff; color:white; font-size:18px; cursor:pointer }
.qty-control button:hover { filter:brightness(0.95) }
.qty-control input { width:96px; padding:0.5rem 0.6rem; text-align:center; border-radius:8px; border:1px solid #e6eefc; font-size:1rem }
.detail-total { color:#0f172a; font-weight:700 }
</style>  