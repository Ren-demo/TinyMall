<template>
  <div class="orders-management">
    <div class="section-header">
      <h2>订单处理</h2>
      <div class="filter-bar">
        <select v-model="filterStatus" class="filter-select">
          <option value="">全部订单</option>
          <option value="订单创建">订单创建</option>
          <option value="已支付">已支付</option>
          <option value="中间商已接受">中间商已接受</option>
          <option value="供货商已发货">供货商已发货</option>
          <option value="拒绝">已拒绝</option>
        </select>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredOrders.length === 0" class="empty-state">
      <div class="empty-state-icon">📭</div>
      <h3>暂无订单</h3>
      <p>没有找到相应的订单</p>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in filteredOrders" :key="order.id" class="order-item">
        <div class="order-info">
          <div class="order-basic">
            <h3 class="order-id">订单 #{{ order.id }}</h3>
            <span :class="getStatusBadgeClass(order.status)" class="badge">
              {{ order.status }}
            </span>
          </div>
          <div class="order-details">
            <div class="detail-row">
              <span class="detail-label">👤 买家：</span>
              <span class="detail-value">{{ order.user }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">📦 商品数：</span>
              <span class="detail-value">{{ order.items.length }} 件</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">📋 商品列表：</span>
              <span class="detail-value">
                <ul class="items-list">
                  <li v-for="item in order.items" :key="item.productId">
                    产品 #{{ item.productId }} × {{ item.qty }}
                  </li>
                </ul>
              </span>
            </div>
          </div>
        </div>

        <div class="order-actions">
          <div class="status-selector">
            <label>更新状态：</label>
            <select v-model="statusUpdates[order.id]" class="status-select">
              <option value="订单创建">订单创建</option>
              <option value="已支付">已支付</option>
              <option value="中间商已接受">中间商已接受</option>
              <option value="供货商已发货">供货商已发货</option>
              <option value="拒绝">拒绝</option>
            </select>
          </div>
          <div class="action-buttons">
            <button @click="updateStatus(order)" class="btn btn-sm btn-success">
              💾 保存
            </button>
            <button v-if="isMiddle" @click="sendEmail(order)" class="btn btn-sm btn-primary">
              ✉️ 发送邮件
            </button>
            <button v-if="isMiddle" @click="confirmAndSend(order)" class="btn btn-sm btn-warning">
              ✅ 确认并发送给供应商
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="successMsg" class="alert alert-success">
      ✓ {{ successMsg }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../services/api'

const orders = ref([])
const loading = ref(true)
const filterStatus = ref('')
const statusUpdates = ref({})
const successMsg = ref('')

const route = useRoute()
const isMiddle = computed(() => route.path && route.path.startsWith('/middle'))

const filteredOrders = computed(() => {
  return orders.value.filter(o => {
    if (!filterStatus.value) return true
    return o.status === filterStatus.value
  })
})

onMounted(async () => {
  const res = await api.fetchEnterpriseOrders()
  orders.value = res
  res.forEach(o => {
    statusUpdates.value[o.id] = o.status
  })
  loading.value = false
})

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

async function updateStatus(order) {
  const newStatus = statusUpdates.value[order.id]
  await api.updateOrderStatus(order.id, newStatus)
  order.status = newStatus
  successMsg.value = `订单 #${order.id} 状态已更新为：${newStatus}`
  setTimeout(() => {
    successMsg.value = ''
  }, 3000)
}

async function sendEmail(order) {
  const res = await api.sendOrderEmail(order.id)
  if (res && res.success) {
    successMsg.value = `订单 #${order.id} 邮件已发送` 
    setTimeout(() => { successMsg.value = '' }, 3000)
  } else {
    successMsg.value = `订单 #${order.id} 邮件发送失败` 
    setTimeout(() => { successMsg.value = '' }, 3000)
  }
}

async function confirmAndSend(order) {
  const supplierId = prompt('请输入要转发到的供应商 ID（例如：123）:')
  if (!supplierId) return
  const res = await api.sendToSupplier(order.id, supplierId)
  if (res && res.success) {
    order.status = '中间商已接受'
    order.supplierId = supplierId
    successMsg.value = `订单 #${order.id} 已接受并发送给供应商 ${supplierId}`
    setTimeout(() => { successMsg.value = '' }, 3000)
  } else {
    successMsg.value = `订单 #${order.id} 转发失败`
    setTimeout(() => { successMsg.value = '' }, 3000)
  }
}
</script>

<style scoped>
.orders-management {
  width: 100%;
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

.filter-bar {
  flex: 0 0 250px;
}

.filter-select,
.status-select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.filter-select:focus,
.status-select:focus {
  border-color: #28a745;
  box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.1);
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
}

.orders-list {
  display: grid;
  gap: 1.5rem;
}

.order-item {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 2rem;
  align-items: start;
  transition: all 0.3s ease;
}

.order-item:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.order-basic {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.order-id {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.order-details {
  display: grid;
  gap: 0.75rem;
}

.detail-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 0.5rem;
  align-items: start;
}

.detail-label {
  font-weight: 600;
  color: #6c757d;
}

.detail-value {
  color: #2c3e50;
}

.items-list {
  list-style: none;
  padding-left: 1rem;
  margin: 0;
}

.items-list li {
  color: #6c757d;
  font-size: 0.9rem;
  padding: 0.25rem 0;
  position: relative;
  padding-left: 1rem;
}

.items-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #28a745;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  min-width: 250px;
}

.status-selector {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.status-selector label {
  font-weight: 600;
  color: #2c3e50;
}

.order-actions button {
  width: 100%;
}

.alert {
  margin-top: 1rem;
}

@media (max-width: 968px) {
  .order-item {
    grid-template-columns: 1fr;
  }

  .order-actions {
    min-width: auto;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar {
    flex: 1;
    width: 100%;
  }

  .detail-row {
    grid-template-columns: 100px 1fr;
  }
}
</style>