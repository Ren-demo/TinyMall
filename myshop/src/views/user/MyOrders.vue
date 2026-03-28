<template>
  <div class="my-orders">
    <div class="section-header">
      <h2>我的订单</h2>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="orders.length===0" class="empty-state">暂无订单</div>

    <div v-else class="orders-grid">
      <div v-for="o in orders" :key="o.id" class="order-card">
        <div class="order-header">
          <div class="order-id">订单号：#{{ o.id }}</div>
          <span :class="getStatusBadgeClass(o.status)" class="badge">{{ o.status === '拒绝' ? '已取消' : o.status }}</span>
        </div>
        <div class="order-body">
          <div class="order-info-row">
            <span class="info-label">👤 买家：</span>
            <span class="info-value">{{ o.user || auth.username }}</span>
          </div>
          <div class="order-info-row">
            <span class="info-label">🕒 订单时间：</span>
            <span class="info-value">{{ formatDate(o.time || o.Time) || '暂无' }}</span>
          </div>
          <div class="order-info-row">
            <span class="info-label">📍 收货地址：</span>
            <span class="info-value">{{ o.address || '暂无' }}</span>
          </div>

          <div class="order-items">
            <p class="items-label">📦 商品：</p>
            <ul class="items-list">
              <li v-for="item in o.items" :key="item.productId" class="item">
                {{ item.name || item.goodsName || item.GoodsName || ('商品 #' + item.productId) }} × {{ item.qty }}
                <span class="item-price">单价：¥{{ formatPrice(item.price) }}</span>
                <span class="item-subtotal">小计：¥{{ formatPrice((item.price || 0) * (item.qty || 0)) }}</span>
              </li>
            </ul>
            <div class="order-total">订单总价：¥{{ formatPrice(getOrderTotal(o)) }}</div>
          </div>

          <div class="order-timeline">
            <div class="timeline-item">
              <div class="timeline-dot done"></div>
              <div class="timeline-label">
                <span>订单创建</span>
                <small>{{ formatDate(o.time || o.Time) }}</small>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot" :class="['已支付', '中间商已接受', '供货商已发货'].includes(o.status) ? 'done' : ''"></div>
              <div class="timeline-label">
                <span>已支付</span>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot" :class="['中间商已接受', '供货商已发货'].includes(o.status) ? 'done' : ''"></div>
              <div class="timeline-label">
                <span>中间商已接受</span>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot" :class="o.status === '供货商已发货' ? 'done' : ''"></div>
              <div class="timeline-label">
                <span>供货商已发货</span>
              </div>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <button v-if="o.status === '订单创建'" @click="openPay(o)" class="btn btn-sm btn-pay">支付</button>
          <button @click="removeOrder(o)" class="btn btn-sm btn-outline">删除订单</button>
          <button class="btn btn-sm" @click="viewDetails(o)">查看详情</button>
        </div>
      </div>
    </div>

    <div v-if="showPayModal" class="pay-modal-mask">
      <div class="pay-modal">
        <div class="pay-modal-header">
          <h3>订单支付</h3>
          <button class="btn-close" @click="closePay" :disabled="paying">×</button>
        </div>
        <div class="pay-modal-body">
          <p>订单号：#{{ payOrder?.id }}</p>
          <p>应付金额：¥{{ formatPrice(getOrderTotal(payOrder)) }}</p>
          <div id="card-element" class="pay-card"></div>
          <label class="pay-test">
            <input type="checkbox" v-model="useTestToken" /> 使用测试令牌 tok_visa
          </label>
          <p v-if="paying" class="pay-loading">支付中，请稍候...</p>
          <p v-if="paySuccess" class="pay-success">支付成功</p>
          <p v-if="payError" class="pay-error">{{ payError }}</p>
          <div v-if="payResult" class="pay-result">
            <p v-if="payResult.id">交易ID：{{ payResult.id }}</p>
            <p v-else-if="payResult.paymentId">交易ID：{{ payResult.paymentId }}</p>
            <p v-if="payResult.status">支付状态：{{ payResult.status }}</p>
            <p v-if="payResult.amount">金额：¥{{ (payResult.amount/100).toFixed(2) }}</p>
            <p v-if="payResult.receipt_url"><a :href="payResult.receipt_url" target="_blank" rel="noopener">查看收据</a></p>
            <p v-if="payResult.card">卡：{{ payResult.card.brand }} ****{{ payResult.card.last4 }}</p>
            <pre v-if="payResult.error" class="pay-error">{{ payResult.error }}</pre>
          </div>
          <p class="pay-hint">卡信息由 Stripe 处理，不会经过本系统</p>
        </div>
        <div class="pay-modal-footer">
          <button class="btn btn-outline" @click="closePay" :disabled="paying">{{ paySuccess ? '关闭' : '取消' }}</button>
          <button class="btn btn-sm btn-pay" v-if="!paySuccess" @click="confirmPay" :disabled="paying">{{ paying ? '支付中...' : '支付' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import api from '../../services/api'
import { useAuthStore } from '../../stores/auth'
import { useRoute } from 'vue-router'

const auth = useAuthStore()
const route = useRoute()
const orders = ref([])
const loading = ref(true)
const showPayModal = ref(false)
const payOrder = ref(null)
const paying = ref(false)
const payError = ref('')
const useTestToken = ref(false)
const paySuccess = ref(false)
const stripe = ref(null)
const elements = ref(null)
const cardElement = ref(null)
const payResult = ref(null)

onMounted(async () => {
  const all = await api.fetchUserOrders(auth.userId || auth.username)
  // show orders that are not yet shipped (新订单及处理中) and exclude cancelled
  const list = (all || []).filter(o => (o.status !== '供货商已发货' && o.status !== '拒绝'))
  orders.value = list.sort((a, b) => {
    const ap = a.status === '订单创建' ? 0 : 1
    const bp = b.status === '订单创建' ? 0 : 1
    if (ap !== bp) return ap - bp
    return 0
  })
  loading.value = false
  const payOrderId = route.query && route.query.payOrderId
  const payGoodsId = route.query && route.query.payGoodsId
  if (payOrderId || payGoodsId) {
    await nextTick()
    let found = null
    if (payOrderId) {
      found = orders.value.find(o => String(o.id) === String(payOrderId))
    }
    if (!found && payGoodsId) {
      found = orders.value.find(o => o.status === '订单创建' && (o.items || []).some(it => String(it.productId) === String(payGoodsId)))
    }
    if (found) openPay(found)
  }
})

async function removeOrder(order) {
  if (!confirm('确定要删除此订单吗？')) return
  const res = await api.userDelOrder(order.id)
  if (res && (res.code === 1 || res.code === 200 || res.success)) {
    const idx = orders.value.findIndex(x => x.id === order.id)
    if (idx >= 0) orders.value.splice(idx, 1)
  } else {
    alert('删除订单失败，请稍后重试')
  }
}

function viewDetails(o) {
  alert(`订单 #${o.id}\n状态：${o.status}\n商品数：${o.items.length}`)
}

async function openPay(order) {
  payOrder.value = order
  showPayModal.value = true
  payError.value = ''
  paySuccess.value = false
  await nextTick()
  initStripe()
}

function closePay() {
  showPayModal.value = false
  payOrder.value = null
  payError.value = ''
  useTestToken.value = false
  paySuccess.value = false
  payResult.value = null
  if (cardElement.value) {
    try { cardElement.value.unmount() } catch (e) {}
  }
  cardElement.value = null
  elements.value = null
}

function getOrderTotal(order) {
  const total = Number(order?.totalPrice ?? order?.total ?? order?.amount ?? order?.orderTotal ?? order?.TotalPrice ?? order?.Total ?? order?.Amount)
  if (Number.isFinite(total)) return total
  return calcTotal(order?.items || [])
}

async function confirmPay() {
  if (!payOrder.value) return
  payError.value = ''
  paying.value = true
  try {
    let paymentToken = ''
    if (useTestToken.value) {
      paymentToken = 'pm_card_visa'
    } else {
      if (!stripe.value || !cardElement.value) {
        const ok = await initStripe()
        if (!ok || !stripe.value || !cardElement.value) {
          payError.value = payError.value || '支付组件初始化失败'
          paying.value = false
          return
        }
      }
      const { token, error } = await stripe.value.createToken(cardElement.value)
      if (error) {
        payError.value = error.message || '支付失败'
        paying.value = false
        return
      }
      paymentToken = token && token.id
    }
    const result = await api.payOrder({
      orderId: payOrder.value.id,
      paymentToken
    })
    payResult.value = result || null
    const ok = result && (result.code === 1 || result.code === 200 || result.code === 0 || result.success)
    if (!ok) {
      payError.value = (result && result.msg) || '支付失败'
      paying.value = false
      return
    }
    const idx = orders.value.findIndex(o => o.id === payOrder.value.id)
    if (idx >= 0) orders.value[idx].status = '已支付'
    paySuccess.value = true
  } catch (e) {
    payError.value = '支付失败，请重试'
    payResult.value = { error: e && e.message ? e.message : String(e) }
  } finally {
    paying.value = false
  }
}

async function initStripe() {
  if (cardElement.value) return true
  const key = import.meta.env.VITE_STRIPE_PUBLIC_KEY
  if (!key) {
    payError.value = '未配置 Stripe 公钥，请重启开发服务'
    return false
  }
  if (!window.Stripe) {
    await new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://js.stripe.com/v3/'
      script.onload = resolve
      script.onerror = reject
      document.head.appendChild(script)
    }).catch(() => {
      payError.value = 'Stripe 加载失败'
    })
  }
  if (!window.Stripe) return false
  stripe.value = stripe.value || window.Stripe(key)
  elements.value = stripe.value.elements()
  const container = document.getElementById('card-element')
  if (!container) {
    payError.value = '支付组件容器不存在'
    return false
  }
  cardElement.value = elements.value.create('card', {
    style: {
      base: {
        color: '#1f2937',
        fontSize: '16px',
        '::placeholder': { color: '#9ca3af' }
      }
    }
  })
  cardElement.value.on('change', (event) => {
    payError.value = event.error ? event.error.message : ''
  })
  cardElement.value.mount(container)
  setTimeout(() => {
    try { cardElement.value && cardElement.value.focus() } catch (e) {}
  }, 0)
  return true
}

function getStatusBadgeClass(status) {
  const statusMap = {
    '订单创建': 'badge-warning',
    '已支付': 'badge-primary',
    '中间商已接受': 'badge-primary',
    '供货商已发货': 'badge-success',
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

function formatPrice(n) {
  const num = Number(n) || 0
  return num.toFixed(2)
}
</script>

<style scoped>
.orders-grid {
  display: grid;
  gap: 1.5rem;
}

.order-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.order-card:hover { transform: translateY(-4px); box-shadow: 0 12px 30px rgba(0,0,0,0.12); }

.order-header {
  background: linear-gradient(135deg, #f8f9fa 0%, #eef2f6 100%);
  padding: 1rem 1.25rem;
  display:flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eef2f6;
}
.order-id { font-weight: 700; color: #2c3e50 }

.badge {
  padding: 0.45rem 0.9rem;
  border-radius: 18px;
  font-weight: 700;
  font-size: 0.9rem;
}
.badge-warning { background:#fff3cd; color:#856404 }
.badge-primary { background:#cfe2ff; color:#084298 }
.badge-success { background:#d1e7dd; color:#0f5132 }
.badge-danger { background:#f8d7da; color:#842029 }
.badge-secondary { background:#e9ecef; color:#495057 }

.order-body { padding: 1rem 1.25rem }
.order-info-row { display:flex; gap:0.75rem; margin-bottom:0.75rem }
.info-label { font-weight:600; color:#495057 }
.info-value { color:#2c3e50 }

.order-items { margin: 0.8rem 0 }
.items-label { font-weight:600; margin:0 0 0.5rem 0 }
.items-list { list-style: none; padding-left: 1rem; margin:0 }
.item { padding: 0.35rem 0; color:#6c757d; position: relative; padding-left: 1.25rem }
.item::before { content: '▪'; position: absolute; left: 0; color: #5b8def }

.item-price,
.item-subtotal {
  margin-left: 0.6rem;
  color: #374151;
  font-size: 0.9rem;
}

.order-total {
  margin-top: 0.6rem;
  font-weight: 700;
  color: #1f2937;
}

.order-timeline {
  margin-top: 1rem;
  padding: 0.8rem;
  background: #f8f9fb;
  border-radius: 8px;
  display:flex;
  justify-content: space-around;
  position: relative;
}
.order-timeline::before { content: ''; position: absolute; top: 50%; left: 1rem; right: 1rem; height: 2px; background:#e9edf3; transform: translateY(-50%); z-index:1 }
.timeline-item { display:flex; flex-direction:column; align-items:center; z-index:2; flex:1 }
.timeline-dot { width:18px; height:18px; border-radius:50%; background:white; border:3px solid #e9edf3; margin-bottom:0.4rem; transition:all 0.18s }
.timeline-dot.done { background: #28a745; border-color:#28a745 }
.timeline-label { text-align:center }
.timeline-label span { font-weight:600; color:#2c3e50; display:block }
.timeline-label small { color:#6c757d; display:block; margin-top:0.25rem }

.order-footer { padding: 0.9rem 1.25rem; border-top: 1px solid #eef2f6; display:flex; gap:0.75rem; justify-content:flex-end }
.order-footer .btn { padding:0.5rem 1rem; border-radius:8px }
.btn-sm { font-size:0.92rem }
.btn-danger { background: #ff6b6b; color:white; border:none }
.btn-pay { background: #22c55e; color:white; border:none }
.btn-pay:hover { filter:brightness(0.95) }
.btn-outline { background:transparent; border:1px solid #2b6cb0; color:#2b6cb0 }

.pay-modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.pay-modal {
  width: 440px;
  max-width: 92vw;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(0,0,0,0.18);
  overflow: hidden;
}

.pay-modal-header {
  padding: 0.8rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
}

.pay-modal-body {
  padding: 1rem;
}

.pay-card {
  min-height: 56px;
  border: 1px solid #e3e7f0;
  border-radius: 10px;
  padding: 12px 14px;
  background: #fff;
  margin: 12px 0;
}

.pay-error {
  color: #e64646;
  font-size: 0.85rem;
  margin: 4px 0 6px;
}

.pay-success {
  color: #16a34a;
  font-size: 0.9rem;
  margin-top: 6px;
  font-weight: 600;
}

.pay-test {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  color: #475569;
  margin-top: 4px;
}

.pay-loading {
  color: #2563eb;
  font-size: 0.85rem;
  margin-top: 6px;
}

.pay-qrcode {
  margin: 0.8rem 0;
  height: 160px;
  border-radius: 8px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-weight: 600;
}

.pay-hint {
  color: #6b7280;
  font-size: 0.9rem;
}

.pay-modal-footer {
  padding: 0.8rem 1rem 1rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
}

@media (max-width: 768px) {
  .order-header { flex-direction: column; align-items:flex-start; gap:0.5rem }
  .order-timeline { flex-direction: column }
  .order-timeline::before { left:50%; top:0; bottom:0; width:2px; height:auto; transform: translateX(-50%) }
  .timeline-item { align-items:flex-start; padding-left:2rem }
  .timeline-dot { position:absolute; left:-11px; top:0 }
  .order-footer { flex-direction: column }
}
</style>
