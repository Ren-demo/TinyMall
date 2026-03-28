<template>
  <section class="middle-dashboard">
    <aside class="sidebar">
      <div class="sidebar-head">
        <h2>中间商控制台</h2>
        <div class="subtitle">管理入口</div>
      </div>
      <nav class="middle-nav">
        <router-link to="/middle" class="nav-item">首页</router-link>
        <router-link to="/middle/orders" class="nav-item">待确认订单</router-link>
        <router-link to="/middle/shipping" class="nav-item">正在发货</router-link>
        <router-link to="/middle/history" class="nav-item">历史订单</router-link>
        <router-link to="/middle/sales" class="nav-item">销售统计</router-link>
        <router-link to="/middle/add-supplier" class="nav-item">新增供应商</router-link>
      </nav>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
</script>

<style scoped>
.middle-dashboard { display:flex; gap:1rem; align-items:flex-start }
.sidebar { width:230px; background: linear-gradient(180deg,#fb923c,#fb6b2a); color:white; border-radius:10px; padding:1rem }
.sidebar-head h2 { margin:0; font-size:1.05rem }
.subtitle { font-size:0.85rem; opacity:0.95 }
.middle-nav { display:flex; flex-direction:column; gap:0.5rem; margin-top:1rem }
.nav-item { padding:0.6rem 0.8rem; color:white; text-decoration:none; border-radius:8px }
.nav-item.router-link-active { background: rgba(255,255,255,0.12) }
.content { flex:1 }

@media (max-width:900px) { .middle-dashboard { flex-direction:column } .sidebar { width:100% } }
</style>