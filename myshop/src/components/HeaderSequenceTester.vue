<template>
  <div class="header-tester">
    <h3>Header Sequence Tester</h3>
    <div style="display:flex; gap:8px; margin-bottom:8px">
      <button @click="doSequential" :disabled="running">顺序请求</button>
      <button @click="doParallel" :disabled="running">并行请求</button>
      <button @click="clear">清除</button>
    </div>

    <div v-if="running">运行中...</div>

    <div v-if="results.length">
      <div v-for="r in results" :key="r.step" style="border:1px solid #eee; padding:8px; margin-bottom:6px">
        <strong>{{ r.step }}</strong>
        <pre style="white-space:pre-wrap; font-size:12px">{{ pretty(r.result) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { runSequential, runParallel } from '../services/headerSequence'

const running = ref(false)
const results = ref([])

function pretty(v) { try { return JSON.stringify(v, null, 2) } catch (e) { return String(v) } }

async function doSequential() {
  running.value = true
  results.value = []
  try {
    const r = await runSequential()
    results.value = r
  } catch (e) {
    results.value = [{ step: 'error', result: String(e) }]
  } finally { running.value = false }
}

async function doParallel() {
  running.value = true
  results.value = []
  try {
    const r = await runParallel()
    results.value = r
  } catch (e) {
    results.value = [{ step: 'error', result: String(e) }]
  } finally { running.value = false }
}

function clear() { results.value = [] }
</script>

<style scoped>
.header-tester { background: #fff; padding:12px; border-radius:8px; box-shadow:0 6px 18px rgba(0,0,0,0.06) }
button { padding:6px 10px; border-radius:6px; border:1px solid #ddd; background:#f8fafc }
</style>
