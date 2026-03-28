<template>
  <div class="orders-section">
    <div class="section-header">
      <h2>订单历史</h2>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="orders.length === 0" class="empty-state">
      <div class="empty-state-icon">📭</div>
      <h3>暂无订单</h3>
      <p>您还没有任何订单</p>
      <router-link to="/user/products" class="btn">去购物</router-link>
    </div>

    <div v-else>
      <div class="orders-grid">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-id">
              <span class="label">订单号：</span>
              <span class="value">#{{ order.id }}</span>
            </div>
            <span :class="getStatusBadgeClass(order.status)" class="badge">
              {{ order.status === '拒绝' ? '已取消' : order.status }}
            </span>
          </div>

          <div class="order-body">
            <div class="order-info-row">
              <span class="info-label">👤 买家：</span>
              <span class="info-value">{{ order.user }}</span>
            </div>
            <div class="order-info-row">
              <span class="info-label">🕒 订单时间：</span>
              <span class="info-value">{{ formatDate(order.time || order.Time) || '暂无' }}</span>
            </div>
            <div class="order-info-row">
              <span class="info-label">📍 收货地址：</span>
              <span class="info-value">{{ order.address || '暂无' }}</span>
            </div>

            <div class="order-items">
              <p class="items-label">📦 商品：</p>
              <ul class="items-list">
                <li v-for="item in order.items" :key="item.productId" class="item">
                  <div class="item-main">
                    <div class="item-title">{{ item.name || item.goodsName || item.GoodsName || ('商品 #' + item.productId) }} × {{ item.qty }}</div>
                    <div class="item-meta">
                      <span class="item-price">单价：¥{{ formatPrice(item.price) }}</span>
                      <span class="item-subtotal">小计：¥{{ formatPrice((item.price || 0) * (item.qty || 0)) }}</span>
                    </div>
                  </div>
                  <div class="item-actions">
                    <button v-if="order.status === '已收货' && !order.hasComment" class="btn btn-sm" @click="openComment(order, item)">评价</button>
                    <span v-else-if="order.status === '已收货' && order.hasComment" class="text-muted">已评价</span>
                  </div>
                </li>
              </ul>
              <div class="order-total">订单总价：¥{{ formatPrice(getOrderTotal(order)) }}</div>
            </div>

            <div class="order-timeline">
              <div class="timeline-item">
                <div class="timeline-dot done"></div>
                <div class="timeline-label">
                  <span>订单创建</span>
                  <small>{{ formatDate(order.time || order.Time) }}</small>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-dot" :class="['已支付', '中间商已接受', '供货商已发货', '已收货'].includes(order.status) ? 'done' : ''"></div>
                <div class="timeline-label">
                  <span>已支付</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-dot" :class="['中间商已接受', '供货商已发货', '已收货'].includes(order.status) ? 'done' : ''"></div>
                <div class="timeline-label">
                  <span>中间商已接受</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-dot" :class="['供货商已发货', '已收货'].includes(order.status) ? 'done' : ''"></div>
                <div class="timeline-label">
                  <span>供货商已发货</span>
                </div>
              </div>
            </div>
          </div>
          <div class="order-footer">
            <button class="btn btn-sm btn-outline">查看详情</button>
            <button v-if="order.status === '供货商已发货'" class="btn btn-sm" @click="confirmReceive(order)">确认收货</button>
            <button v-if="order.status !== '供货商已发货' && order.status !== '中间商已接受' && order.status !== '拒绝' && order.status !== '已收货'" @click="cancel(order)" class="btn btn-sm btn-danger">取消订单</button>
          </div>
        </div>
      </div>
    </div>
    <CommentModal v-model="commentVisible" :goodsId="commentGoodsId" :orderId="commentOrderId" :existingComment="commentExisting" @submitted="onCommentSubmitted" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api'
import { useAuthStore } from '../../stores/auth'
import CommentModal from '../../components/CommentModal.vue'

// register component for template (script setup auto registers imported components)

const orders = ref([])
const loading = ref(true)
const auth = useAuthStore()
const commentVisible = ref(false)
const commentGoodsId = ref(null)
const commentOrderId = ref(null)
const commentExisting = ref(null)
const _commentsCache = {} // cache comments per productId to avoid duplicate requests

function openComment(order, item) {
  if (order.hasComment) { alert('该订单已评价，无法重复评价'); return }
  // if there is an existing comment for this order+product, pass it to the modal for editing
  const cms = _commentsCache[item.productId] || []
  const found = cms.find(c => String(c.orderId) === String(order.id) && String(c.userId) === String(auth.userId)) || null
  commentExisting.value = found
  commentGoodsId.value = item.productId
  commentOrderId.value = order.id
  commentVisible.value = true
}

function onCommentSubmitted(payload) {
  // payload = { success, res }
  if (payload && payload.success) {
    alert('评价提交成功')
    // mark the order as commented
    const oid = commentOrderId.value
    const od = orders.value.find(x => String(x.id) === String(oid))
    if (od) od.hasComment = true
    // clear existing comment state
    commentExisting.value = null
  } else {
    alert('评价提交结果：' + (payload && payload.res && payload.res.msg ? payload.res.msg : '未知'))
  }
}

onMounted(async () => {
  const all = await api.fetchUserOrders(auth.userId || auth.username)
  // order history shows shipped, cancelled, and received orders (received at bottom)
  orders.value = (all || []).filter(o => o.status === '供货商已发货' || o.status === '拒绝' || o.status === '已收货')
    .sort((a, b) => {
      const ap = a.status === '已收货' ? 1 : 0
      const bp = b.status === '已收货' ? 1 : 0
      if (ap !== bp) return ap - bp
      return 0
    })
  // build a set of unique productIds to fetch comments once per product
  const productIds = new Set()
  for (const o of orders.value) {
    for (const it of (o.items || [])) productIds.add(it.productId)
  }
  // fetch comments per product and cache
  for (const pid of Array.from(productIds)) {
    try {
      const cms = await api.fetchComments(pid)
      _commentsCache[pid] = Array.isArray(cms) ? cms : []
    } catch (e) {
      _commentsCache[pid] = []
    }
  }
  // mark orders that already have a comment by this user for any item in the order
  for (const o of orders.value) {
    let has = false
    for (const it of (o.items || [])) {
      const cms = _commentsCache[it.productId] || []
      if (cms.some(c => String(c.orderId) === String(o.id) && String(c.userId) === String(auth.userId))) {
        has = true
        break
      }
    }
    o.hasComment = has
  }
  loading.value = false
})

async function cancel(order) {
  if (order.status === '供货商已发货' || order.status === '中间商已接受') { alert('中间商已接受或供货商已发货订单无法取消'); return }
  if (!confirm('确定要取消此订单吗？')) return
  const res = await api.cancelOrder(order.id)
  if (res && res.success) {
    order.status = '拒绝'
  } else {
    alert('取消订单失败，请稍后重试')
  }
}

async function confirmReceive(order) {
  if (order.status !== '供货商已发货') {
    alert('只有已发货的订单才能确认收货')
    return
  }
  if (!confirm('确认已收到该订单商品？')) return
  const res = await api.checkOrder(order.id)
  if (res && (res.code === 1 || res.code === 200 || res.success)) {
    order.status = '已收货'
    alert('确认收货成功')
  } else {
    alert('确认收货失败，请稍后重试')
  }
}

function getStatusBadgeClass(status) {
  const statusMap = {
    '订单创建': 'badge-warning',
    '已支付': 'badge-primary',
    '中间商已接受': 'badge-primary',
    '供货商已发货': 'badge-success',
    '已收货': 'badge-success',
    '拒绝': 'badge-danger'
  }
  return statusMap[status] || 'badge-secondary'
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return String(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function calcTotal(items = []) {
  return items.reduce((s, it) => s + (Number(it.price) || 0) * (Number(it.qty) || 0), 0)
}

function getOrderTotal(order) {
  const total = Number(order?.totalPrice ?? order?.total ?? order?.amount ?? order?.orderTotal ?? order?.TotalPrice ?? order?.Total ?? order?.Amount)
  if (Number.isFinite(total)) return total
  return calcTotal(order?.items || [])
}

function formatPrice(n) {
  const num = Number(n) || 0
  return num.toFixed(2)
}
</script>


<style scoped>
.orders-section {
  width: 100%;
}

.section-header {
  margin-bottom: 2rem;
}

.section-header h2 {
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.orders-grid {
  display: grid;
  gap: 1.5rem;
}

.order-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.order-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #e9ecef;
}

.order-id {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.label {
  font-weight: 600;
  color: #6c757d;
}

.value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2c3e50;
}

.badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.order-body {
  padding: 1.5rem;
}

.order-info-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.info-label {
  font-weight: 600;
  color: #6c757d;
}

.info-value {
  color: #2c3e50;
}

.order-items {
  margin: 1.5rem 0;
}

.items-label {
  font-weight: 600;
  margin: 0 0 0.5rem 0;
}

.items-list {
  list-style: none;
  padding-left: 1rem;
}

.item {
  padding: 0.6rem 0.4rem; 
  color: #6c757d;
  position: relative;
  display:flex;
  justify-content:space-between;
  align-items:center;
}

.item::before {
  content: '▪';
  position: absolute;
  left: 0;
  color: #667eea;
}

.item-price,
.item-subtotal {
  margin-left: 0.6rem;
  color: #374151;
  font-size: 0.9rem;
}

.item-main { display:flex; flex-direction:column }
.item-actions { margin-left: 0.75rem }

.order-total {
  margin-top: 0.6rem;
  font-weight: 700;
  color: #1f2937;
}

.order-timeline {
  margin: 1.5rem 0;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  justify-content: space-around;
  position: relative;
}

.order-timeline::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 1rem;
  right: 1rem;
  height: 2px;
  background: #e9ecef;
  transform: translateY(-50%);
  z-index: 1;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  border: 3px solid #e9ecef;
  margin-bottom: 0.5rem;
  transition: all 0.3s ease;
}

.timeline-dot.done {
  background: #28a745;
  border-color: #28a745;
}

.timeline-label {
  text-align: center;
  font-size: 0.85rem;
}

.timeline-label span {
  font-weight: 600;
  color: #2c3e50;
  display: block;
}

.timeline-label small {
  color: #6c757d;
  display: block;
  margin-top: 0.25rem;
}

.order-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.order-footer button {
  flex: 1;
  max-width: 150px;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }

  .order-timeline {
    flex-direction: column;
  }

  .order-timeline::before {
    left: 50%;
    top: 0;
    bottom: 0;
    width: 2px;
    height: auto;
    transform: translateX(-50%);
  }

  .timeline-item {
    align-items: flex-start;
    padding-left: 2rem;
  }

  .timeline-dot {
    position: absolute;
    left: -11px;
    top: 0;
  }

  .timeline-label {
    text-align: left;
  }

  .order-footer {
    flex-direction: column;
  }

  .order-footer button {
    max-width: none;
  }
}
</style>