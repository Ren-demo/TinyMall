<template>
  <div class="cart-section">
    <div class="section-header">
      <h2>购物车</h2>
    </div>

    <div v-if="cartStore.items.length === 0" class="empty-state">
      <div class="empty-state-icon">🛒</div>
      <h3>购物车为空</h3>
      <p>快去添加一些商品吧</p>
      <router-link to="/user/products" class="btn">继续购物</router-link>
    </div>

    <div v-else>
      <div class="cart-table-wrapper">
        <table class="cart-table">
          <thead>
            <tr>
              <th>商品名称</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, idx) in cartStore.items" :key="item.productId" class="cart-item">
              <td class="product-name-col">
                <span class="product-icon">📦</span>
                <div>
                  <div>{{ item.name }}</div>
                  <div style="font-size:0.85rem;color:#6b7280">供应商：
                    <span v-if="storesMap[item.storeId]">{{ storesMap[item.storeId].StoreName }}</span>
                    <span v-else-if="item.storeName">{{ item.storeName }}</span>
                    <span v-else>未知</span>
                  </div>
                </div>
              </td>
              <td class="price-col">¥{{ (Number(item.price) || 0).toFixed(2) }}</td>
              <td class="qty-col">
                <div class="qty-control">
                  <button @click="decreaseQty(idx)" class="btn-qty">−</button>
                  <span class="qty-display">{{ item.qty }}</span>
                  <button @click="increaseQty(idx)" class="btn-qty">+</button>
                </div>
              </td>
              <td class="subtotal-col">
                <strong>¥{{ (item.qty * (Number(item.price) || 0)).toFixed(2) }}</strong>
              </td>
              <td class="action-col">
                <button @click="remove(idx)" class="btn btn-sm btn-danger">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="cart-summary">
        <div class="summary-card">
          <div class="summary-row">
            <span class="summary-label">商品总数：</span>
            <span class="summary-value">{{ totalItems }} 件</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">收货地址：</span>
            <span class="summary-value">
              <span v-if="auth.address">{{ auth.address }}</span>
              <span v-else class="text-muted">请先在个人信息中填写地址</span>
            </span>
          </div>
          <div class="summary-row">
            <span class="summary-label">商品总价：</span>
            <span class="summary-value">¥{{ subtotal }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">运费：</span>
            <span class="summary-value text-success">免费</span>
          </div>
          <div class="summary-row total">
            <span class="summary-label">合计：</span>
            <span class="summary-value">¥{{ total }}</span>
          </div>
        </div>

        <div class="cart-actions">
          <router-link to="/user/products" class="btn btn-outline">继续购物</router-link>
          <button @click="checkout" class="btn btn-success">
            💳 提交订单
          </button>
        </div>
      </div>

      <div v-if="checkoutMsg" class="alert" :class="checkoutMsg.type">
        {{ checkoutMsg.text }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, reactive } from 'vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../services/api'
import { useCartStore } from '../../stores/cart'

const auth = useAuthStore()
const cartStore = useCartStore()
const storesMap = reactive({})

async function loadCart() {
  await cartStore.load()
  try {
    const rows = await api.fetchStores()
    rows.forEach(s => { storesMap[s.StoreID] = s })
  } catch (e) {
    // ignore
  }
  // ensure each item has storeId/storeName (fallback: derive from product)
  for (const it of cartStore.items) {
    if (!it.storeName && it.storeId && storesMap[it.storeId]) {
      it.storeName = storesMap[it.storeId].StoreName
    }
    if (!it.storeId) {
      try {
        const sid = await api.findStoreIdByGoodsId(it.productId)
        if (sid) {
          it.storeId = sid
          if (!it.storeName && storesMap[sid]) it.storeName = storesMap[sid].StoreName
        }
      } catch (e) {}
    }
  }
}

function onStorage(e) {
  if (e.key === 'cart') cartStore.load ? cartStore.load() : cartStore.init()
}

onMounted(() => {
  loadCart()
  window.addEventListener('storage', onStorage)
})

onActivated(() => {
  loadCart()
})

onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorage)
})

const checkoutMsg = ref(null)

const totalItems = computed(() => cartStore.totalItems())
const subtotal = computed(() => +(cartStore.subtotal()).toFixed(2))
const total = computed(() => subtotal.value)

function increaseQty(idx) {
  const item = cartStore.items[idx]
  if (!item) return
  cartStore.increase(item.productId)
}

function decreaseQty(idx) {
  const item = cartStore.items[idx]
  if (!item) return
  cartStore.decrease(item.productId)
}

function remove(idx) {
  const item = cartStore.items[idx]
  if (!item) return
  cartStore.remove(item.productId)
}

async function saveCart() {
  await cartStore.save()
}

function checkout() {
  checkoutMsg.value = { type: 'alert-info', text: '正在提交订单...' }
  const uid = auth.userId
  if (!uid) {
    checkoutMsg.value = { type: 'alert-danger', text: '请先登录再提交订单' }
    setTimeout(() => { checkoutMsg.value = null }, 3000)
    return
  }
  if (!auth.address) {
    checkoutMsg.value = { type: 'alert-danger', text: '请先在个人信息中填写收货地址' }
    setTimeout(() => { checkoutMsg.value = null }, 3000)
    return
  }
  const items = cartStore.items.map(i => ({ goodsId: i.productId, count: i.qty }))
  Promise.all(items.map(it => api.addOrderOnCart({ goodsId: it.goodsId, count: it.count, userId: uid, address: auth.address }))).then(async (results) => {
    const ok = results && results.every(r => r && (r.code === 1 || r.code === 200 || r.success))
    if (ok) {
      checkoutMsg.value = { type: 'alert-success', text: '✓ 订单提交成功！' }
      await cartStore.clear()
      localStorage.removeItem('cart')
    } else {
      checkoutMsg.value = { type: 'alert-danger', text: '提交订单失败，请重试' }
    }
    setTimeout(() => { checkoutMsg.value = null }, 3000)
  }).catch(() => {
    checkoutMsg.value = { type: 'alert-danger', text: '提交订单失败，请检查网络' }
    setTimeout(() => { checkoutMsg.value = null }, 3000)
  })
}
</script>

<style scoped>
.cart-section {
  width: 100%;
}

.section-header {
  margin-bottom: 2rem;
}

.section-header h2 {
  margin: 0;
}

.cart-table-wrapper {
  background: white;
  border-radius: 12px;
  overflow: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
}

.cart-table {
  width: 100%;
  border-collapse: collapse;
}

.cart-table thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cart-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
}

.cart-item {
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.3s ease;
}

.cart-item:hover {
  background-color: #f8f9fa;
}

.cart-table td {
  padding: 1rem;
  vertical-align: middle;
}

.product-name-col {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.product-icon {
  font-size: 1.5rem;
}

.price-col,
.subtotal-col {
  text-align: right;
  font-weight: 600;
}

.qty-col {
  text-align: center;
}

.qty-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: #f8f9fa;
  padding: 0.25rem;
  border-radius: 6px;
  width: fit-content;
  margin: 0 auto;
}

.btn-qty {
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  color: #667eea;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-qty:hover {
  background: #667eea;
  color: white;
}

.qty-display {
  min-width: 30px;
  text-align: center;
  font-weight: 600;
}

.action-col {
  text-align: center;
}

.cart-summary {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 2rem;
  align-items: start;
}

.summary-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
}

.summary-row.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 1rem;
  margin: 0.5rem -1.5rem -1.5rem -1.5rem;
  border-radius: 0 0 12px 12px;
}

.summary-label {
  font-weight: 600;
  color: #6c757d;
}

.summary-row.total .summary-label {
  color: white;
}

.summary-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
}

.summary-row.total .summary-value {
  color: white;
  font-size: 1.5rem;
}

.cart-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-actions .btn {
  width: 100%;
  justify-content: center;
  font-weight: 600;
}

.alert {
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .cart-summary {
    grid-template-columns: 1fr;
  }

  .cart-table {
    font-size: 0.9rem;
  }

  .cart-table th,
  .cart-table td {
    padding: 0.75rem;
  }

  .price-col,
  .subtotal-col {
    display: none;
  }
}
</style>