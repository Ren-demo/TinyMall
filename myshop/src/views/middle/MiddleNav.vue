<template>
  <nav class="middle-nav-vertical">
    <router-link to="/middle" class="nav-item">首页</router-link>
    <router-link to="/middle/orders" class="nav-item">待确认订单</router-link>
    <router-link to="/middle/shipping" class="nav-item">正在发货</router-link>
    <router-link to="/middle/history" class="nav-item">历史订单</router-link>
    <router-link to="/middle/sales" class="nav-item">销售统计</router-link>
  </nav>
</template>

<script setup>
</script>

<style scoped>
.middle-nav-vertical { display:flex; flex-direction:column; gap:0.6rem }
.nav-item { padding:0.6rem 0.8rem; border-radius:8px; color:white; text-decoration:none }
.nav-item.router-link-active { background: rgba(255,255,255,0.12) }
</style>
