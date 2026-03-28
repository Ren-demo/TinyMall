<template>
  <div class="middle-history">
    <div class="history-head"><h3>已发货历史</h3></div>

    <div v-if="loading" class="empty">加载中...</div>
    <div v-else-if="orders.length===0" class="empty">暂无历史订单</div>

    <div v-else class="orders-list">
      <div v-for="o in orders" :key="o.id" class="order-item">
        <div class="order-meta">
          <strong>订单 #{{ o.id }}</strong>
          <div class="muted">买家：{{ o.user }} • 商品数：{{ o.items.length }}</div>
          <div class="muted">状态：{{ o.status }}</div>
          <div class="muted">下单时间：{{ o.time || o.Time || '暂无' }}</div>
          <div class="muted">收货地址：{{ o.address || '暂无' }}</div>
          <ul class="items-list">
            <li v-for="it in o.items" :key="it.productId" class="item">
              {{ it.name || it.goodsName || ('商品 #' + it.productId) }} × {{ it.qty }}
              <span class="item-price">单价：¥{{ getItemUnitPrice(it) }}</span>
              <span class="item-subtotal">小计：¥{{ getItemTotal(it).toFixed(2) }}</span>
            </li>
          </ul>
          <div class="order-total">订单总价：¥{{ getOrderTotal(o) }}</div>
        </div>
        <div class="order-extra">供应商：{{ o.storeName || '未分配' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api'

const orders = ref([])
const loading = ref(true)

onMounted(async () => {
  const res = await api.fetchMiddleOrders()
  // 历史订单：已发货(3) + 已收货(4)，已收货置底
  orders.value = (res || []).filter(o => o.state === 3 || o.state === 4 || o.status === '供货商已发货' || o.status === '已收货')
    .sort((a, b) => {
      const ap = a.status === '已收货' ? 1 : 0
      const bp = b.status === '已收货' ? 1 : 0
      if (ap !== bp) return ap - bp
      return 0
    })
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
</script>

<style scoped>
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.8rem; background:white; border-radius:8px; margin-bottom:0.6rem }
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.9rem; background:white; border-radius:8px; margin-bottom:0.6rem; box-shadow:0 6px 18px rgba(2,6,23,0.04) }
.muted { color:#64748b; font-size:0.9rem }
.items-list { list-style: none; padding-left: 1rem; margin: 0.35rem 0 0 0 }
.item { padding: 0.25rem 0; color: #475569; position: relative; padding-left: 1rem }
.item::before { content: '▪'; position: absolute; left: 0; color: #94a3b8 }
.item-price, .item-store { margin-left: 0.5rem; color: #64748b; font-size: 0.85rem }
.item-subtotal { margin-left: 0.5rem; color: #64748b; font-size: 0.85rem }
.order-total { margin-top: 0.4rem; font-weight: 700; color: #1f2937 }
.empty { color:#64748b; padding:1rem; background:#f8fafc; border-radius:8px; text-align:center }
.history-head { margin-bottom:0.75rem }
.order-extra { color:#334155; font-weight:600 }
</style>
