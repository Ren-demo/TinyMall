<template>
  <div class="middle-orders">
    <div class="orders-head">
      <h3>待确认订单</h3>
    </div>

    <div v-if="loading" class="empty">加载中...</div>
    <div v-else-if="orders.length===0" class="empty">暂无待确认订单</div>

    <div v-else class="orders-list">
      <div v-for="o in orders" :key="o.id" class="order-item">
        <div class="order-meta">
          <strong>订单 #{{ o.id }}</strong>
          <div class="muted">买家：{{ o.user }} • 商品数：{{ o.items.length }}</div>
          <div class="muted">下单时间：{{ o.time || o.Time || '暂无' }}</div>
          <div class="muted">收货地址：{{ o.address || '暂无' }}</div>
          <div class="muted">状态：{{ getStatusLabel(o) }}</div>
          <ul class="items-list">
            <li v-for="it in o.items" :key="it.productId" class="item">
              {{ it.name || it.goodsName || ('商品 #' + it.productId) }} × {{ it.qty }}
              <span class="item-price">单价：¥{{ getItemUnitPrice(it) }}</span>
              <span class="item-store">供应商：{{ it.storeName || o.storeName || it.storeId || '未知' }}</span>
            </li>
          </ul>
          <div class="order-total">订单总价：¥{{ getOrderTotal(o) }}</div>
        </div>
        <div class="order-actions">
          <button @click="acceptAndSend(o)" class="btn-accept" :disabled="o._sending">接受订单</button>
          <button @click="rejectOrder(o)" class="btn-reject" :disabled="o._sending">拒绝并通知</button>
        </div>
      </div>
    </div>

    <div v-if="msg" class="msg">{{ msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api'

const orders = ref([])
const loading = ref(true)
const msg = ref('')

onMounted(async () => {
  const res = await api.fetchMiddleOrders()
  // 待确认：已支付(1)
  orders.value = (res || []).filter(o => o.state === 1 || o.status === '已支付')
  loading.value = false
})

function calcTotal(items = []) {
  const sum = (items || []).reduce((s, it) => s + getItemTotal(it), 0)
  return sum.toFixed(2)
}

function getOrderTotal(o) {
  const total = Number(o?.totalPrice ?? o?.total ?? o?.amount ?? o?.orderTotal ?? o?.TotalPrice ?? o?.Total ?? o?.Amount)
  if (Number.isFinite(total)) return total.toFixed(2)
  return calcTotal(o?.items || [])
}

function getItemTotal(it) {
  const total = Number(it.totalPrice ?? it.total ?? it.amount ?? it.subtotal ?? it.priceTotal)
  if (Number.isFinite(total) && total > 0) return total
  const qty = Number(it.qty) || 0
  const price = Number(it.price) || 0
  if (price && qty > 1 && !it.unitPrice && !it.goodsPrice) return price
  return price * qty
}

function getItemUnitPrice(it) {
  const qty = Number(it.qty) || 0
  const total = getItemTotal(it)
  if (qty > 0) return (total / qty).toFixed(2)
  const price = Number(it.price) || 0
  return price.toFixed(2)
}

function getStatusLabel(o) {
  if (o.status) return o.status
  if (o.state === 1) return '已支付未接受'
  return '订单创建'
}

async function acceptAndSend(o) {
  // Optimistic update: mark as sending and set state=2 immediately so button turns green
  if (o._sending) return
  o._sending = true
  const prevState = o.state
  o.state = 2
  try {
    let r = await api.manageOrder({ orderId: o.id, msg: '接受订单', refuse: false })
    if (!(r && (r.success || r.code === 1 || r.code === 200))) {
      r = await api.updateOrderStatus(o.id, 2)
    }
    if (r && (r.success || r.code === 1 || r.code === 200)) {
      o.status = '中间商已接受'
      msg.value = `订单 #${o.id} 已接受`
      orders.value = orders.value.filter(x => x.id !== o.id)
    } else {
      o.state = prevState
      msg.value = `订单 #${o.id} 接受失败`
    }
  } catch (e) {
    o.state = prevState
    msg.value = `订单 #${o.id} 接受失败`
  } finally {
    o._sending = false
    setTimeout(()=> msg.value = '', 3000)
  }
}

async function rejectOrder(o) {
  if (o._sending) return
  o._sending = true
  const prevState = o.state
  // optimistic: mark as rejected so UI updates immediately
  o.state = -1
  o.status = '拒绝'
  try {
    let res = await api.manageOrder({ orderId: o.id, msg: '拒绝订单', refuse: true })
    if (!(res && (res.success || res.code === 1 || res.code === 200))) {
      res = await api.updateOrderStatus(o.id, '拒绝')
    }
    await api.sendOrderEmailWithMessage(o.id, '订单被拒绝，金额已退还')
    msg.value = `订单 #${o.id} 已拒绝并通知客户`
  } catch (e) {
    // revert
    o.state = prevState
    o.status = (prevState === 1 ? '已支付' : (prevState === 2 ? '中间商已接受' : (prevState === 3 ? '供货商已发货' : '订单创建')))
    msg.value = `订单 #${o.id} 拒绝失败`
  } finally {
    o._sending = false
    setTimeout(()=> msg.value = '', 3000)
  }
}
</script>

<style scoped>
.orders-head { margin-bottom:0.75rem }
.orders-list { display:flex; flex-direction:column; gap:0.6rem }
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.9rem; background:#fff; border-radius:10px; box-shadow:0 8px 20px rgba(2,6,23,0.06) }
.order-meta strong { display:block }
.muted { color:#64748b; font-size:0.9rem }
.items-list { list-style: none; padding-left: 1rem; margin: 0.35rem 0 0 0 }
.item { padding: 0.25rem 0; color: #475569; position: relative; padding-left: 1rem }
.item::before { content: '▪'; position: absolute; left: 0; color: #94a3b8 }
.item-price, .item-store { margin-left: 0.5rem; color: #64748b; font-size: 0.85rem }
.order-total { margin-top: 0.4rem; font-weight: 700; color: #1f2937 }
.order-actions { display:flex; gap:0.6rem }
.btn-accept { background:linear-gradient(90deg,#f59e0b,#f97316); color:white; padding:0.45rem 0.8rem; border-radius:8px; border:none }
.btn-sent { background:linear-gradient(90deg,#16a34a,#059669) !important }
.btn-reject { background:linear-gradient(90deg,#ef4444,#dc2626); color:white; padding:0.45rem 0.8rem; border-radius:8px; border:none }
.empty { color:#64748b; padding:1rem; background:#f8fafc; border-radius:8px; text-align:center }
.msg { margin-top:0.6rem; color:#78350f; background:#fff7ed; padding:0.6rem; border-radius:8px }
</style>
