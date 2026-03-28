<template>
  <section class="supplier-inventory">
    <EnterpriseInventory :storeId="supplierId" />
  </section>
</template>

<script setup>
import { useRoute } from 'vue-router'
import EnterpriseInventory from '../enterprise/Inventory.vue'

const route = useRoute()
const supplierId = route.params.supplierId
</script>

<style scoped>
.supplier-inventory { width: 100% }
</style>