<template>
  <section class="user-dashboard">
    <div class="dashboard-header">
      <h1>用户中心</h1>
      <p class="subtitle">欢迎回来，{{auth.username==='' ? '游客' : auth.username }}！</p>
    </div>

    <div class="quick-stats">
      <div class="stat-card">
        <div class="stat-icon">🛒</div>
        <div class="stat-content">
          <div class="stat-value">{{ cartCount }}</div>
          <div class="stat-label">购物车商品</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <div class="stat-value">{{ orderCount }}</div>
          <div class="stat-label">我的订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ totalSpent }}</div>
          <div class="stat-label">总消费</div>
        </div>
      </div>
    </div>

    <router-view />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../services/api'
import { useCartStore } from '../../stores/cart'

const auth = useAuthStore()
const cartStore = useCartStore()
const cartCount = computed(() => cartStore.totalItems())
const orderCount = ref(0)
const totalSpent = ref('0.00')

async function loadStats() {
  // ensure cart store is initialized and loaded
  try {
    cartStore.init()
  } catch (e) {}

  // fetch orders and compute stats
  try {
    const orders = await api.fetchUserOrders(auth.userId || auth.username)
    orderCount.value = (orders || []).length
    // compute total spent from orders items if available (exclude cancelled/unpaid)
    const sum = (orders || []).filter(o => o.status !== '拒绝' && o.status !== '已取消' && o.status !== '订单创建').reduce((s, o) => {
      const items = o.items || []
      return s + items.reduce((ss, it) => ss + ((it.price || 0) * (it.qty || it.Count || 0)), 0)
    }, 0)
    totalSpent.value = (+sum).toFixed(2)
  } catch (e) {
    totalSpent.value = '0.00'
  }
}

onMounted(loadStats)
</script>

<style scoped>
.user-dashboard {
  width: 100%;
  padding: 0 0.75rem;
  box-sizing: border-box;
}

.dashboard-header {
  /* background: radial-gradient(#fff42133 1px, transparent 1px);
  background-size: 20px 20px;
  background-color: #111; */
  background: linear-gradient(135deg, #ff7ebe, #00bfff);
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    width: 200%;
    height: 200%;
    background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23ffffff33' d='M0,192L48,208C96,224,192,256,288,245.3C384,235,480,181,576,160C672,139,768,149,864,154.7C960,160,1056,160,1152,133.3C1248,107,1344,53,1392,26.7L1440,0L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E");
    animation: wave 10s linear infinite;
  }

  @keyframes wave {
    0% { transform: translateX(0); }
    100% { transform: translateX(-50%); }
  }
  /* background: linear-gradient(135deg, #f093fbdd 0%, #4f46e5dd 50%, #1a1a1a 100%); */
  /* background: linear-gradient(135deg, #fff421d9 0%, #ff7e00 60%, #000 120%); */
  /* background: radial-gradient(circle, #1c46ffdd 0%, #000 70%); */
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

/* 与商品列表对齐：限制宽度并居中 */
.user-dashboard > .dashboard-header,
.user-dashboard > .quick-stats {
  max-width: 1200px;
  margin: 0 auto;
}


.stat-card {
  background: rgb(194, 239, 202);
  border-radius: 12px;
  margin-top: 0.8rem;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
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

@media (max-width: 768px) {
  .dashboard-header {
    padding: 1.5rem;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }
}
</style>