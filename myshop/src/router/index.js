import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import UserDashboard from '../views/user/UserDashboard.vue'
import ProductList from '../views/user/ProductList.vue'
import Cart from '../views/user/Cart.vue'
import OrderHistory from '../views/user/OrderHistory.vue'
import MiddleDashboard from '../views/middle/MiddleDashboard.vue'
import MiddleHome from '../views/middle/MiddleHome.vue'
// enterprise routes removed (no enterprise unit)
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/user/products' },
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/user',
    component: UserDashboard,
    meta: { requiresAuth: true, allowedRoles: ['user'] },
    children: [
      { path: 'products', component: ProductList, name: 'Products' },
      { path: 'cart', component: Cart, name: 'Cart' },
      { path: 'my-orders', component: () => import('../views/user/MyOrders.vue'), name: 'MyOrders' },
      { path: 'orders', component: OrderHistory, name: 'OrderHistory' }
    ]
  },
  // enterprise routes removed
  // 中间商入口（单独 URL，公开访问）
  {
    path: '/middle',
    component: MiddleDashboard,
    children: [
      { path: '', component: MiddleHome },
      { path: 'orders', component: () => import('../views/middle/MiddleOrders.vue'), name: 'MiddleOrders' },
      { path: 'shipping', component: () => import('../views/middle/MiddleShipping.vue'), name: 'MiddleShipping' },
      { path: 'history', component: () => import('../views/middle/MiddleHistory.vue'), name: 'MiddleHistory' },
      { path: 'sales', component: () => import('../views/middle/MiddleSales.vue'), name: 'MiddleSales' },
      { path: 'add-supplier', component: () => import('../views/middle/MiddleAddSupplier.vue'), name: 'MiddleAddSupplier' }
    ]
  },
  // 个人信息（所有已登录角色可访问）
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true, allowedRoles: ['user', 'supplier'] }
  },
  // 桌宠页面（已回滚）
  // 供应商通过 URL + id 直接进入各自的管理台（无需手动登录）
  {
    path: '/supplier/:supplierId',
    component: () => import('../views/supplier/SupplierDashboard.vue'),
    meta: { supplierPublic: true },
    children: [
      { path: '', component: () => import('../views/supplier/SupplierInventory.vue'), name: 'SupplierInventory' },
      { path: 'orders', component: () => import('../views/supplier/SupplierOrders.vue'), name: 'SupplierOrders' },
      { path: 'history', component: () => import('../views/supplier/SupplierHistory.vue'), name: 'SupplierHistory' }
    ]
  },
  // fallback: 未匹配路径默认进入中间商入口
  { path: '/:pathMatch(.*)*', redirect: '/middle' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})



// NOTE: do not remove existing localStorage entries here — supplier sessions are handled in-session

// 全局守卫：检查登录与角色
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  // 调试信息：打印目标路径与当前认证状态
  console.debug('[router] beforeEach to=', to.fullPath, 'isLoggedIn=', auth.isLoggedIn, 'role=', auth.role)

  // 中间商入口为公开页面，允许任意访问（不做自动重定向）
  if (to.path.startsWith('/middle')) {
    return next()
  }

  // 允许未登录用户访问商品列表页（公开浏览商品）
  if (to.path === '/user/products' || to.name === 'Products') {
    return next()
  }

  // 明确处理供应商公开入口：若是普通用户已登录则阻止；否则在会话级设置 supplier 身份并放行
  if (to.meta && to.meta.supplierPublic) {
    const id = to.params && to.params.supplierId
    if (id) {
      // 如果当前以普通用户登录，先清除该用户的持久会话（登出用户），再设置供应商会话
      if (auth.isLoggedIn && auth.role === 'user') {
        auth.token = ''
        auth.role = ''
        auth.username = ''
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('username')
      }
      // 在会话级别设置供应商身份（不会持久化到 localStorage）
      auth.token = 'mock-supplier-token-' + id
      auth.role = 'supplier'
      auth.username = 'supplier-' + id
      sessionStorage.setItem('token', auth.token)
      sessionStorage.setItem('role', auth.role)
      sessionStorage.setItem('username', auth.username)
    }
    return next()
  }

  if (to.meta.requiresAuth) {
    if (!auth.isLoggedIn) {
      console.debug('[router] requiresAuth but not logged in, redirect to Login', { to: to.fullPath })
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }
    const allowed = to.meta.allowedRoles || []
    if (allowed.length && !allowed.includes(auth.role)) {
      console.debug('[router] requiresAuth but role not allowed, redirect to Login', { role: auth.role, allowed })
      return next({ name: 'Login' })
    }
  }

  next()
})

export default router