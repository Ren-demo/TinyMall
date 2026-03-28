<template>
  <div class="middle-sales">
    <h3>销售统计</h3>
    <div v-if="loading" class="empty">加载中...</div>
    <div v-else>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="label">已接受 + 已发货订单总额</div>
          <div class="value">{{ formatCurrency(stats.totalSales) }}</div>
        </div>
        <div class="stat-card">
          <div class="label">已接受 + 已发货订单数</div>
          <div class="value">{{ stats.orderCount }}</div>
        </div>
        <div class="stat-card">
          <div class="label">已接受 + 已发货订单均价</div>
          <div class="value">{{ formatCurrency(stats.avgOrder) }}</div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-title">每日流水折线图</div>
        <div v-if="dailySeries.length === 0" class="empty">暂无流水数据</div>
        <div v-else class="line-chart">
          <svg viewBox="0 0 600 220" preserveAspectRatio="none">
            <g class="grid">
              <line v-for="(t, i) in yTicks" :key="i" :x1="36" :x2="580" :y1="yPos(t)" :y2="yPos(t)" />
            </g>
            <polyline class="line" :points="linePoints" />
            <g class="dots">
              <circle v-for="(p, i) in lineDotPoints" :key="i" :cx="p.x" :cy="p.y" r="3" />
            </g>
          </svg>
          <div class="y-labels">
            <div v-for="(t, i) in yTicks" :key="i" class="y-label" :style="{ top: yLabelPos(t) + 'px' }">
              {{ formatCurrency(t) }}
            </div>
          </div>
          <div class="x-labels">
            <span v-for="(d, i) in dailySeries" :key="i">{{ d.label }}</span>
          </div>
        </div>
      </div>

      <div class="hot-card">
        <div class="chart-title">最近热销商品 TOP 3</div>
        <div v-if="topProducts.length === 0" class="empty">暂无热销数据</div>
        <ul v-else class="hot-list">
          <li v-for="p in topProducts" :key="p.productId" class="hot-item">
            <div class="hot-name">{{ p.name || ('商品 #' + p.productId) }}</div>
            <div class="hot-meta">销量：{{ p.qty }} 件</div>
            <div class="hot-meta">销售额：{{ formatCurrency(p.sales) }}</div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api'

const stats = ref({ totalSales: 0, orderCount: 0, avgOrder: 0 })
const topProducts = ref([])
const chartMax = ref(1)
const dailySeries = ref([])
const yTicks = ref([0, 0.5, 1])
const linePoints = ref('')
const lineDotPoints = ref([])
const loading = ref(true)

onMounted(async () => {
  const res = await api.fetchMiddleOrders()
  const acceptedOrShipped = (res || []).filter(o => o.state === 2 || o.state === 3 || o.status === '中间商已接受' || o.status === '供货商已发货')
  const totalSales = acceptedOrShipped.reduce((sum, o) => sum + calcOrderTotal(o.items || []), 0)
  const orderCount = acceptedOrShipped.length
  const avgOrder = orderCount ? +(totalSales / orderCount).toFixed(2) : 0
  stats.value = { totalSales, orderCount, avgOrder }
  chartMax.value = Math.max(1, totalSales, orderCount, avgOrder)
  topProducts.value = buildTopProducts(acceptedOrShipped)
  dailySeries.value = buildDailySeries(acceptedOrShipped)
  const maxDaily = Math.max(1, ...dailySeries.value.map(x => x.value))
  yTicks.value = [maxDaily, maxDaily / 2, 0]
  linePoints.value = buildLinePoints(dailySeries.value, maxDaily)
  lineDotPoints.value = buildLineDots(dailySeries.value, maxDaily)
  loading.value = false
})

function calcOrderTotal(items = []) {
  return (items || []).reduce((s, it) => s + getItemTotal(it), 0)
}

function getItemTotal(it) {
  const total = Number(it.totalPrice ?? it.total ?? it.amount ?? it.subtotal ?? it.priceTotal)
  if (Number.isFinite(total) && total > 0) return total
  const qty = Number(it.qty) || 0
  const price = Number(it.price) || 0
  if (price && qty > 1 && !it.unitPrice && !it.goodsPrice) return price
  return price * qty
}

function buildDailySeries(orders = []) {
  const map = new Map()
  let maxDate = null
  for (const o of orders) {
    const raw = o.time || o.Time || o.createTime || o.createdTime || o.create_time || o.created_at || o.createAt
    const d = new Date(raw)
    if (isNaN(d.getTime())) continue
    const key = formatDateKey(d)
    const total = calcOrderTotal(o.items || [])
    map.set(key, (map.get(key) || 0) + total)
    if (!maxDate || d > maxDate) maxDate = d
  }
  const base = maxDate || new Date()
  const days = 7
  const series = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(base)
    d.setDate(base.getDate() - i)
    const key = formatDateKey(d)
    series.push({ key, label: formatDateLabel(d), value: +(map.get(key) || 0).toFixed(2) })
  }
  return series
}

function formatDateKey(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatDateLabel(d) {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}-${day}`
}

function buildLinePoints(series, maxVal) {
  const w = 600
  const h = 220
  const padX = 36
  const padY = 24
  const n = series.length
  if (!n) return ''
  const step = n === 1 ? 0 : (w - padX - 20) / (n - 1)
  return series.map((p, i) => {
    const x = padX + (n === 1 ? (w - padX - 20) / 2 : i * step)
    const y = h - padY - (p.value / (maxVal || 1)) * (h - padY * 2)
    return `${x},${y}`
  }).join(' ')
}

function buildLineDots(series, maxVal) {
  const w = 600
  const h = 220
  const padX = 36
  const padY = 24
  const n = series.length
  if (!n) return []
  const step = n === 1 ? 0 : (w - padX - 20) / (n - 1)
  return series.map((p, i) => {
    const x = padX + (n === 1 ? (w - padX - 20) / 2 : i * step)
    const y = h - padY - (p.value / (maxVal || 1)) * (h - padY * 2)
    return { x, y }
  })
}

function yPos(t) {
  const maxVal = Math.max(1, ...yTicks.value)
  const h = 220
  const padY = 24
  return h - padY - (t / maxVal) * (h - padY * 2)
}

function yLabelPos(t) {
  const maxVal = Math.max(1, ...yTicks.value)
  const h = 220
  const padY = 24
  return (h - padY - (t / maxVal) * (h - padY * 2)) - 8
}

function buildTopProducts(orders = []) {
  const map = new Map()
  for (const o of orders) {
    for (const it of (o.items || [])) {
      const id = it.productId ?? it.GoodsID ?? it.goodsId
      if (!id) continue
      const qty = Number(it.qty) || 0
      const sales = getItemTotal(it)
      const prev = map.get(id) || { productId: id, name: it.name || it.goodsName || it.GoodsName || '', qty: 0, sales: 0 }
      prev.qty += qty
      prev.sales += sales
      if (!prev.name) prev.name = it.name || it.goodsName || it.GoodsName || ''
      map.set(id, prev)
    }
  }
  return Array.from(map.values()).sort((a, b) => b.sales - a.sales).slice(0, 3)
}

function formatCurrency(v) {
  return '¥' + (v || 0).toFixed(2)
}
</script>

<style scoped>
.stat { margin: 0.6rem 0; background: white; padding: 0.8rem; border-radius: 8px }
.stats-grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap:1rem }
.stat-card { background:white; padding:1rem; border-radius:10px; box-shadow:0 8px 24px rgba(2,6,23,0.04) }
.label { color:#64748b; font-size:0.9rem }
.value { font-size:1.2rem; font-weight:700; margin-top:0.5rem }
.empty { color:#64748b; padding:1rem; background:#f8fafc; border-radius:8px; text-align:center }

.chart-card {
  margin-top: 1rem;
  background: white;
  padding: 1rem;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(2,6,23,0.04);
}

.chart-title {
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.75rem;
}

.line-chart {
  position: relative;
  height: 260px;
}

.line-chart svg {
  width: 100%;
  height: 220px;
}

.line {
  fill: none;
  stroke: #2563eb;
  stroke-width: 2.5;
}

.dots circle {
  fill: #2563eb;
}

.grid line {
  stroke: #e5e7eb;
  stroke-dasharray: 4 4;
}

.y-labels {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 220px;
  pointer-events: none;
}

.y-label {
  position: absolute;
  left: 0;
  transform: translateY(-50%);
  font-size: 0.8rem;
  color: #64748b;
}

.x-labels {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-top: 6px;
  font-size: 0.8rem;
  color: #64748b;
}

.hot-card {
  margin-top: 1rem;
  background: white;
  padding: 1rem;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(2,6,23,0.04);
}

.hot-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.6rem;
}

.hot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
  padding: 0.6rem 0.8rem;
  border: 1px solid #eef2f7;
  border-radius: 8px;
}

.hot-name {
  font-weight: 600;
  color: #0f172a;
}

.hot-meta {
  color: #64748b;
  font-size: 0.85rem;
}
</style>
