<template>
  <section class="enterprise-dashboard">
    <div class="dashboard-header">
      <h1>企业管理系统</h1>
      <p class="subtitle">欢迎回来，{{ auth.username }}！</p>
    </div>

    <div class="quick-stats">
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingOrders }}</div>
          <div class="stat-label">已支付订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ completedOrders }}</div>
          <div class="stat-label">已完成订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ totalRevenue }}</div>
          <div class="stat-label">本周营收</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ growth }}%</div>
          <div class="stat-label">同比增长</div>
        </div>
      </div>
    </div>

    <div class="dashboard-actions">
      <router-link to="/enterprise/orders" class="action-card">
        <div class="action-icon">📋</div>
        <div class="action-title">订单处理</div>
        <p>管理和处理客户订单</p>
      </router-link>
      <router-link to="/enterprise/inventory" class="action-card">
        <div class="action-icon">📦</div>
        <div class="action-title">库存管理</div>
        <p>更新和监控产品库存</p>
      </router-link>
      <router-link to="/enterprise/sales" class="action-card">
        <div class="action-icon">📊</div>
        <div class="action-title">销售统计</div>
        <p>查看销售数据分析</p>
      </router-link>
    </div>

    <router-view />
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../services/api'

const auth = useAuthStore()
const pendingOrders = ref(0)
const completedOrders = ref(0)
const totalRevenue = ref('0.00')
const growth = ref(0)

onMounted(async () => {
  const orders = await api.fetchEnterpriseOrders()
  pendingOrders.value = orders.filter(o => o.status === '已支付').length
  completedOrders.value = orders.filter(o => o.status === '供货商已发货').length
  totalRevenue.value = (Math.random() * 50000 + 10000).toFixed(2)
  growth.value = Math.floor(Math.random() * 30 + 5)
})
</script>

<style scoped>
.enterprise-dashboard {
  width: 100%;
}

.dashboard-header {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.dashboard-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: white;
}

.subtitle {
  margin: 0;
  font-size: 1.1rem;
  opacity: 0.9;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 10rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 2px 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-top: 0.5rem;
}

.dashboard-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 2px solid transparent;
  text-decoration: none;
  color: inherit;
}

.action-card:hover {
  border-color: #28a745;
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.action-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.action-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.action-card p {
  color: #6c757d;
  margin: 0;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .dashboard-header {
    padding: 1.5rem;
  }

  .quick-stats,
  .dashboard-actions {
    grid-template-columns: 1fr;
  }
}
</style>