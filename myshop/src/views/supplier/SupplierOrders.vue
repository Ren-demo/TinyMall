<template>
  <div class="supplier-orders">
    <div class="orders-head">
      <h3>待发货订单</h3>
      <div class="controls">
        <button @click="createMock()" class="btn-create">生成模拟订单</button>
      </div>
    </div>

    <div v-if="loading" class="empty">加载中...</div>
    <div v-else-if="orders.length===0" class="empty">暂无待发货订单</div>

    <div v-else class="orders-list">
      <div v-for="o in orders" :key="o.id" class="order-item">
        <div>
          <div class="order-meta">
            <strong>订单 #{{ o.id }}</strong>
            <div class="muted">买家：{{ o.user }} • 商品数：{{ o.items.length }}</div>
            <div class="muted">下单时间：{{ o.time }}</div>
            <div class="muted">收货地址：{{ o.address || '暂无' }}</div>
          </div>
          <ul class="items-list">
            <li v-for="it in o.items" :key="it.productId">{{ it.name || ('商品 ' + it.productId) }} x {{ it.qty }}</li>
          </ul>
          <div class="order-total">订单总价：¥{{ formatPrice(getOrderTotal(o)) }}</div>
        </div>
        <div class="order-actions">
          <button @click="markShipped(o)" class="btn-ship">标记为已发货</button>
        </div>
      </div>
    </div>

    <div v-if="msg" class="msg">{{ msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../services/api'

const route = useRoute()
const supplierId = route.params.supplierId || route.query.supplierId
const orders = ref([])
const loading = ref(true)
const msg = ref('')

async function reload() {
  loading.value = true
  const res = await api.fetchSupplierOrders(supplierId)
  orders.value = (res || []).filter(o => o.status === '中间商已接受')
  loading.value = false
}

async function createMock() {
  const res = await api.createSupplierMockOrder(supplierId)
  if (res && res.success) {
    await reload()
    msg.value = `已生成模拟订单 #${res.order.id}`
    setTimeout(() => { msg.value = '' }, 3000)
  }
}

onMounted(async () => {
  await reload()
})

async function markShipped(o) {
  await api.deliveryOrder(o.id)
  await reload()
  msg.value = `订单 #${o.id} 已标记为已发货`
  setTimeout(() => { msg.value = '' }, 3000)
}

function getOrderTotal(order) {
  const total = Number(order?.totalPrice ?? order?.total ?? order?.amount ?? order?.orderTotal ?? order?.TotalPrice ?? order?.Total ?? order?.Amount)
  if (Number.isFinite(total)) return total
  const items = order?.items || []
  return items.reduce((s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0), 0)
}

function formatPrice(n) {
  const num = Number(n) || 0
  return num.toFixed(2)
}
</script>

<style scoped>
.orders-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem }
.btn-create { background:#06b6d4; color:white; border:none; padding:0.5rem 0.8rem; border-radius:8px }
.btn-create:hover { background:#0891b2 }
.orders-list { display:flex; flex-direction:column; gap:0.6rem }
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.9rem; background:#fff; border-radius:10px; box-shadow:0 8px 20px rgba(2,6,23,0.06) }
.order-meta strong { display:block; font-size:1rem }
.muted { color:#64748b; font-size:0.9rem }
.order-total { margin-top: 0.35rem; font-weight: 700; color: #1f2937 }
.order-actions { display:flex; gap:0.6rem }
.btn-ship { background:linear-gradient(90deg,#16a34a,#059669); color:white; padding:0.45rem 0.8rem; border-radius:8px; border:none }
.btn-ship:hover { filter:brightness(0.95) }
.empty { color:#64748b; padding:1rem; background:#f8fafc; border-radius:8px; text-align:center }
.msg { margin-top:0.6rem; color:#065f46; background:#ecfdf5; padding:0.6rem; border-radius:8px }
</style>
