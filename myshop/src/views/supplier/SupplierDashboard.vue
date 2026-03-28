<template>
  <section class="supplier-dashboard">
    <aside class="sidebar">
      <div class="sidebar-head">
        <h2>供应商控制台</h2>
        <div class="supplier-id">ID: {{ supplierId }}</div>
      </div>
      <nav class="supplier-nav">
        <router-link :to="`/supplier/${supplierId}`" class="nav-item">库存</router-link>
        <router-link :to="`/supplier/${supplierId}/orders`" class="nav-item">待发货</router-link>
        <router-link :to="`/supplier/${supplierId}/history`" class="nav-item">历史订单</router-link>
      </nav>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const supplierId = route.params.supplierId
const auth = useAuthStore()

onMounted(() => {
  if (supplierId) {
    // 在会话级设置供应商身份（不覆盖 localStorage 的持久登录）
    auth.token = 'mock-supplier-token-' + supplierId
    auth.role = 'supplier'
    auth.username = 'supplier-' + supplierId
    sessionStorage.setItem('token', auth.token)
    sessionStorage.setItem('role', auth.role)
    sessionStorage.setItem('username', auth.username)
  }
})
</script>

<style scoped>
.supplier-dashboard { display:flex; gap:1.25rem; align-items:flex-start }
.sidebar { width:230px; background: linear-gradient(180deg,#0ea5a4,#0891b2); color:white; border-radius:10px; padding:1rem }
.sidebar-head h2 { margin:0 0 0.25rem 0; font-size:1.1rem }
.supplier-id { font-size:0.9rem; opacity:0.9 }
.supplier-nav { display:flex; flex-direction:column; gap:0.5rem; margin-top:1rem }
.nav-item { padding:0.6rem 0.8rem; background:transparent; border-radius:8px; color:white; text-decoration:none; display:block }
.nav-item.router-link-active { background: rgba(255,255,255,0.12); box-shadow: inset 0 0 0 1px rgba(255,255,255,0.04) }
.content { flex:1 }

@media (max-width: 900px) {
  .supplier-dashboard { flex-direction:column }
  .sidebar { width:100% }
}
</style>
