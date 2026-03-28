// // Utilities to run a series of requests to the mock header server
// // Usage examples at bottom; import and call from console or components
//
// async function fetchJson(url, opts = {}) {
//   const res = await fetch(url, opts)
//   const text = await res.text()
//   try { return JSON.parse(text) } catch (e) { return text }
// }
//
// export async function runSequential() {
//   const base = 'http://localhost:4000'
//   const results = []
//
//   // 1. health check
//   results.push({ step: 'health', ok: true, result: await fetchJson(`${base}/health`) })
//
//   // 2. echo headers (GET)
//   results.push({ step: 'echo-headers', result: await fetchJson(`${base}/echo-headers`, {
//     method: 'GET',
//     headers: {
//       'X-Custom-Header': 'seq-value',
//       'Authorization': 'Bearer seq-token'
//     }
//   })})
//
//   // 3. post echo (POST)
//   results.push({ step: 'post-echo', result: await fetchJson(`${base}/echo`, {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//       'X-Custom-Header': 'seq-post',
//       'Authorization': 'Bearer seq-token'
//     },
//     body: JSON.stringify({ ts: Date.now(), note: 'sequential test' })
//   })})
//
//   return results
// }
//
// export async function runParallel() {
//   const base = 'http://localhost:4000'
//   const p1 = fetchJson(`${base}/echo-headers`, { method: 'GET', headers: { 'X-Custom-Header': 'par-1' } })
//   const p2 = fetchJson(`${base}/echo`, { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-Custom-Header': 'par-2' }, body: JSON.stringify({ p: 2 }) })
//   const p3 = fetchJson(`${base}/health`)
//   const [r1, r2, r3] = await Promise.all([p1, p2, p3])
//   return [{ step: 'echo-headers', result: r1 }, { step: 'post-echo', result: r2 }, { step: 'health', result: r3 }]
// }
//
// // Simple retry helper for flaky endpoints
// export async function runWithRetry(fn, attempts = 3, delayMs = 300) {
//   let lastErr
//   for (let i = 0; i < attempts; i++) {
//     try {
//       return await fn()
//     } catch (e) {
//       lastErr = e
//       await new Promise(r => setTimeout(r, delayMs))
//     }
//   }
//   throw lastErr
// }
//
// // Example usage (run in browser console or component):
// // import { runSequential, runParallel } from '/src/services/headerSequence.js'
// // runSequential().then(r => console.log('sequential', r))
// // runParallel().then(r => console.log('parallel', r))
//
// export default { runSequential, runParallel, runWithRetry }
