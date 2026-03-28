<template>
  <div class="middle-home">
    <div class="cards">
      <div class="card">
        <h3>订单处理</h3>
        <p>查看并手工接受 / 拒绝客户订单，或转发给供应商。</p>
        <router-link to="/middle/orders" class="btn-primary">进入订单列表</router-link>
      </div>

      <div class="card">
        <h3>新增供应商</h3>
        <p>中间商可新增供应商信息并生成供应商资料。</p>
        <router-link to="/middle/add-supplier" class="btn-primary">新增供应商</router-link>
      </div>

      <!-- <div class="card"> -->
        <!-- <h3>消息通知</h3> -->
        <!-- <p>使用邮件通知客户订单状态更新（mock 演示）。</p> -->
        <!-- <button class="btn-secondary" @click="notImplemented">发送示例邮件</button> -->
      <!-- </div> -->

      <div class="card">
        <h3>销售统计</h3>
        <p>查看中间商销售统计与汇总数据。</p>
        <router-link to="/middle/sales" class="btn-primary">查看统计</router-link>
      </div>
    </div>

    <div class="notes">
      <h4>说明</h4>
      <ul>
        <li>订单可手工处理：接受 / 拒绝 / 转发供应商。</li>
        <!-- <li>邮件发送为演示，实际使用请接入真实邮件服务。</li> -->
      </ul>
    </div>
  </div>
</template>

<script setup>
function notImplemented() { alert('演示：邮件发送功能暂未实现。') }
</script>

<style scoped>
.cards { display:grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap:1rem }
.card { background:white; padding:1rem; border-radius:10px; box-shadow:0 8px 24px rgba(2,6,23,0.04) }
.card h3 { margin:0 0 0.5rem 0 }
.btn-primary { display:inline-block; margin-top:0.75rem; padding:0.5rem 0.9rem; background:linear-gradient(90deg,#fb923c,#fb6b2a); color:white; border-radius:8px; text-decoration:none }
.btn-secondary { background:#06b6d4; color:white; padding:0.45rem 0.8rem; border-radius:8px; border:none }
.notes { margin-top:1rem }
.notes ul { margin:0; padding-left:1.1rem }
</style>
