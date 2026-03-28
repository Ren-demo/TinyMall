<template>
  <div class="supplier-history">
    <div class="history-head"><h3>历史已发货订单</h3></div>
    <div v-if="loading" class="empty">加载中...</div>
    <div v-else-if="orders.length===0" class="empty">暂无历史订单</div>
    <div v-else class="orders-list">
      <div v-for="o in orders" :key="o.id" class="order-item">
        <div class="order-meta">
          <strong>订单 #{{ o.id }}</strong>
          <div class="muted">买家ID：{{ o.userId || '未知' }} • 商品数：{{ o.items.length }}</div>
          <div class="muted">店铺：{{ o.storeName || '未知' }}</div>
          <div class="muted">下单时间：{{ o.time || o.Time || '暂无' }}</div>
          <div class="muted">收货地址：{{ o.address || '暂无' }}</div>
          <ul class="items-list">
            <li v-for="it in o.items" :key="it.productId" class="item">
              {{ it.name || it.goodsName || ('商品 #' + it.productId) }} × {{ it.qty }}
              <span class="item-price">单价：¥{{ formatPrice(it.price) }}</span>
            </li>
          </ul>
        </div>
        <div class="order-status">{{ getStatusLabel(o) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../services/api'

const route = useRoute()
const supplierId = route.params.supplierId
const orders = ref([])
const loading = ref(true)

onMounted(async () => {
  const all = await api.fetchSupplierOrders(supplierId)
  orders.value = (all || []).filter(o => o.status === '供货商已发货' || o.status === '已收货' || o.state === 3 || o.state === 4)
    .sort((a, b) => {
      const ap = a.status === '已收货' ? 1 : 0
      const bp = b.status === '已收货' ? 1 : 0
      if (ap !== bp) return ap - bp
      return 0
    })
  loading.value = false
})

function getStatusLabel(o) {
  if (o.status) return o.status
  if (o.state === 4) return '已收货'
  if (o.state === 3) return '供货商已发货'
  return '订单创建'
}

function formatPrice(n) {
  const num = Number(n) || 0
  return num.toFixed(2)
}
</script>

<style scoped>
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.8rem; background:white; border-radius:8px; margin-bottom:0.6rem }
.order-item { display:flex; justify-content:space-between; align-items:center; padding:0.9rem; background:white; border-radius:8px; margin-bottom:0.6rem; box-shadow:0 6px 18px rgba(2,6,23,0.04) }
.muted { color:#64748b; font-size:0.9rem }
.items-list { list-style: none; padding-left: 1rem; margin: 0.35rem 0 0 0 }
.item { padding: 0.2rem 0; color: #475569; position: relative; padding-left: 1rem }
.item::before { content: '▪'; position: absolute; left: 0; color: #94a3b8 }
.item-price { margin-left: 0.5rem; color: #64748b; font-size: 0.85rem }
.empty { color:#64748b; padding:1rem; background:#f8fafc; border-radius:8px; text-align:center }
.history-head { margin-bottom:0.75rem }
.order-status { color:#065f46; font-weight:600 }
</style>
