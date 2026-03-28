<template>
  <nav class="navbar">
    <div class="navbar-container">
      <div class="navbar-brand">
        <router-link to="/user/products" class="brand-logo">🛍️ MyShop</router-link>
      </div>
      
      <div class="navbar-menu">
        <!-- 若当前是中间商入口，则优先展示中间商的菜单（不以登录角色判断） -->
        <template v-if="route.path && route.path.startsWith('/middle')">
          <div class="nav-section">
            <router-link to="/middle" class="nav-link">中间首页</router-link>
            <router-link to="/middle/orders" class="nav-link">中间订单</router-link>
            <router-link to="/middle/history" class="nav-link">交易记录</router-link>
            <router-link to="/middle/sales" class="nav-link">销售</router-link>
          </div>
        </template>

        <!-- 否则按登录角色展示对应导航 -->
        <template v-else-if="auth.isLoggedIn">
          <div class="nav-section">
            <template v-if="auth.role === 'user'">
              <router-link to="/user/products" class="nav-link">商品列表</router-link>
              <router-link to="/user/cart" class="nav-link">购物车</router-link>
              <router-link to="/user/my-orders" class="nav-link">我的订单</router-link>
              <router-link to="/user/orders" class="nav-link">订单历史</router-link>
            </template>
            <!-- enterprise role removed -->
            <template v-else-if="auth.role === 'supplier'">
              <router-link :to="`/supplier/${(auth.username||'').split('-').pop()}`" class="nav-link">库存管理（供应商）</router-link>
            </template>
          </div>
        </template>
      </div>

      <div class="navbar-right">
        <!-- 中间商入口显示专属标识，不显示当前登录用户信息 -->
        <template v-if="route.path && route.path.startsWith('/middle')">
          <div class="user-info">
            <span class="badge badge-secondary">中间商入口</span>
          </div>
        </template>
        <template v-else-if="auth.isLoggedIn">
          <div class="user-info">
            <div class="avatar" :class="{ empty: !auth.userPicture }">
              <img v-if="auth.userPicture" :src="auth.userPicture" alt="avatar" />
              <span v-else>{{ (auth.username || 'U').slice(0, 1).toUpperCase() }}</span>
            </div>
            <span class="badge badge-primary">{{ auth.role === 'user' ? '用户' : '🔧 供应商' }}</span>
            <strong class="username">{{ auth.username }}</strong>
            <router-link v-if="auth.role !== 'supplier'" to="/profile" class="nav-link nav-link-ghost">个人信息</router-link>
            <template v-if="auth.role === 'user'">
              <router-link to="/user/cart" class="nav-link">
                购物车
                <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
              </router-link>
            </template>
          </div>
          <button @click="auth.logout()" class="btn btn-sm btn-danger">
            退出登录
          </button>
        </template>
        <template v-else>
          <router-link :to="{ name: 'Login', query: { redirect: route.fullPath } }" class="btn btn-sm">登录</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { computed } from 'vue'
const auth = useAuthStore()
const route = useRoute()
const cart = useCartStore()
const cartCount = computed(() => cart.totalItems())
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #0066cc 0%, #0052a3 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.navbar-brand {
  flex-shrink: 0;
}

.brand-logo {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.brand-logo:hover {
  text-decoration: none;
  transform: scale(1.05);
}

.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 2rem;
}

.nav-section {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: white;
  font-weight: 500;
  padding: 0.5rem 0;
  position: relative;
  transition: all 0.3s ease;
}

.nav-link-ghost {
  padding: 0.35rem 0.6rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 999px;
  font-size: 0.85rem;
}

.nav-link:hover,
.nav-link.router-link-active {
  text-decoration: none;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: white;
  transition: width 0.3s ease;
}

.nav-link:hover::after,
.nav-link.router-link-active::after {
  width: 100%;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: white;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-weight: 700;
  color: #fff;
  font-size: 0.85rem;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  font-size: 0.95rem;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .navbar-container {
    flex-wrap: wrap;
    height: auto;
    padding: 0.5rem 1rem;
  }

  .navbar-brand {
    order: 1;
  }

  .navbar-menu {
    order: 3;
    width: 100%;
    margin: 0.5rem 0 0 0;
    flex-direction: column;
  }

  .nav-section {
    flex-direction: column;
    gap: 0;
  }

  .navbar-right {
    order: 2;
  }
}
</style>