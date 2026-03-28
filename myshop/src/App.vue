<template>
  <div id="app">
    <NavBar />
    <main class="container">
      <router-view />
    </main>

    <!-- 左右漂浮装饰图（可拖拽） -->
    <div
      class="floating floating-left"
      :style="{ transform: `translate(${leftX}px, ${leftY}px)` }"
      @pointerdown.prevent="startDrag('left', $event)"
    >
      <div class="floating-inner">
        <img class="floating-img" src="/assets/left-float.gif" alt="左侧装饰" />
      </div>
    </div>

    <div
      class="floating floating-right"
      :style="{ transform: `translate(${rightX}px, ${rightY}px)` }"
      @pointerdown.prevent="startDrag('right', $event)"
    >
      <div class="floating-inner">
        <img class="floating-img" src="/assets/right-float.gif" alt="右侧装饰" />
      </div>
    </div>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'

// 可拖拽漂浮图位置状态
const leftX = ref(90)
const leftY = ref(700)
const rightX = ref(-90)
const rightY = ref(700)

// 拖拽与惯性状态
let dragging = null // { side, startX, startY, origX, origY }
const velocityX = ref(0)
const velocityY = ref(0)
let lastMoves = [] // recent pointer samples [{x,y,t}]
let inertiaRAF = null

// Compute allowed translate bounds for left/right floating elements
function getBounds(side, elemW = 88, elemH = 88) {
  const vw = window.innerWidth || document.documentElement.clientWidth
  const vh = window.innerHeight || document.documentElement.clientHeight
  const minY = 0
  const maxY = Math.max(0, vh - elemH)
  if (side === 'left') {
    const minX = 0
    const maxX = Math.max(0, vw - elemW)
    return { minX, maxX, minY, maxY }
  }
  // right: translateX range such that absolute left = vw - elemW + rightX stays within [0, vw-elemW]
  // so rightX in [elemW - vw, 0]
  const minX = Math.min(0, elemW - vw)
  const maxX = 0
  return { minX, maxX, minY, maxY }
}

function stopInertia() {
  if (inertiaRAF) {
    cancelAnimationFrame(inertiaRAF)
    inertiaRAF = null
  }
  velocityX.value = 0
  velocityY.value = 0
}

function startDrag(side, e) {
  const p = (e instanceof PointerEvent) ? e : window.event
  // stop any running inertia
  stopInertia()
  try { p.target.setPointerCapture && p.target.setPointerCapture(p.pointerId) } catch (e) {}
  dragging = {
    side,
    startX: p.clientX,
    startY: p.clientY,
    origX: side === 'left' ? leftX.value : rightX.value,
    origY: side === 'left' ? leftY.value : rightY.value
  }
  lastMoves = [{ x: p.clientX, y: p.clientY, t: Date.now() }]
}

function onPointerMove(e) {
  if (!dragging) return
  const now = Date.now()
  const x = e.clientX
  const y = e.clientY
  // update position
  const dx = x - dragging.startX
  const dy = y - dragging.startY
  if (dragging.side === 'left') {
    leftX.value = dragging.origX + dx
    leftY.value = dragging.origY + dy
    const b = getBounds('left')
    // clamp while dragging to avoid going off-screen
    leftX.value = Math.min(Math.max(leftX.value, b.minX), b.maxX)
    leftY.value = Math.min(Math.max(leftY.value, b.minY), b.maxY)
  } else {
    rightX.value = dragging.origX + dx
    rightY.value = dragging.origY + dy
    const b = getBounds('right')
    rightX.value = Math.min(Math.max(rightX.value, b.minX), b.maxX)
    rightY.value = Math.min(Math.max(rightY.value, b.minY), b.maxY)
  }
  // record sample (keep last ~6 samples within 200ms)
  lastMoves.push({ x, y, t: now })
  while (lastMoves.length > 6 || (lastMoves.length > 1 && now - lastMoves[0].t > 200)) lastMoves.shift()
}

function applyInertia(side, vx, vy) {
  // vx,vy in px/sec
  const friction = 4.5 // per second exponential-ish decay
  let lastTime = null
  const bounceDamping = 0.58 // invert velocity and multiply by this on bounce
  const elemW = 88
  const elemH = 88

  function step(ts) {
    if (!lastTime) lastTime = ts
    const dt = Math.max(0, (ts - lastTime) / 1000) // seconds
    lastTime = ts
    // apply exponential decay: v *= exp(-friction * dt)
    const decay = Math.exp(-friction * dt)
    vx *= decay
    vy *= decay
    // integrate
    let dx = vx * dt
    let dy = vy * dt
    if (side === 'left') {
      let nx = leftX.value + dx
      let ny = leftY.value + dy
      const b = getBounds('left', elemW, elemH)
      // X bounds
      if (nx < b.minX) {
        nx = b.minX
        vx = -vx * bounceDamping
      } else if (nx > b.maxX) {
        nx = b.maxX
        vx = -vx * bounceDamping
      }
      // Y bounds
      if (ny < b.minY) {
        ny = b.minY
        vy = -vy * bounceDamping
      } else if (ny > b.maxY) {
        ny = b.maxY
        vy = -vy * bounceDamping
      }
      leftX.value = nx
      leftY.value = ny
    } else {
      let nx = rightX.value + dx
      let ny = rightY.value + dy
      const b = getBounds('right', elemW, elemH)
      if (nx < b.minX) {
        nx = b.minX
        vx = -vx * bounceDamping
      } else if (nx > b.maxX) {
        nx = b.maxX
        vx = -vx * bounceDamping
      }
      if (ny < b.minY) {
        ny = b.minY
        vy = -vy * bounceDamping
      } else if (ny > b.maxY) {
        ny = b.maxY
        vy = -vy * bounceDamping
      }
      rightX.value = nx
      rightY.value = ny
    }
    const speed = Math.sqrt(vx * vx + vy * vy)
    // stop when very slow (<12 px/sec)
    if (speed < 12) {
      inertiaRAF = null
      return
    }
    inertiaRAF = requestAnimationFrame(step)
  }
  inertiaRAF = requestAnimationFrame(step)
}

function endDrag(e) {
  if (!dragging) return
  // compute velocity from lastMoves
  const now = Date.now()
  if (lastMoves.length >= 2) {
    const first = lastMoves[0]
    const last = lastMoves[lastMoves.length - 1]
    const dt = Math.max(1, last.t - first.t) / 1000 // seconds
    const vx = (last.x - first.x) / dt // px/sec
    const vy = (last.y - first.y) / dt
    // start inertia if speed significant
    const speed = Math.sqrt(vx * vx + vy * vy)
    if (speed > 40) {
      applyInertia(dragging.side, vx, vy)
    }
  }
  // clear dragging state
  dragging = null
  lastMoves = []
}

onMounted(() => {
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', endDrag)
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerup', endDrag)
  window.removeEventListener('resize', onResize)
  stopInertia()
})

function onResize() {
  // clamp positions within new bounds
  const bl = getBounds('left')
  leftX.value = Math.min(Math.max(leftX.value, bl.minX), bl.maxX)
  leftY.value = Math.min(Math.max(leftY.value, bl.minY), bl.maxY)
  const br = getBounds('right')
  rightX.value = Math.min(Math.max(rightX.value, br.minX), br.maxX)
  rightY.value = Math.min(Math.max(rightY.value, br.minY), br.maxY)
}

</script>
 
<style>
.container {
  padding: 1rem;
}

/* 漂浮装饰样式 */
.floating {
  position: fixed;
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
  transition: box-shadow 160ms ease;
  z-index: 1200;
}
.floating:active { cursor: grabbing }
.floating-left {
  left: 0;
  top: 0;
  transform-origin: 0 0;
}
.floating-right {
  right: 0;
  top: 0;
  transform-origin: 100% 0;
}

/* 内层动画容器：实现上下漂浮，不影响外层拖拽的 translate */
.floating-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  will-change: transform;
  animation-name: floatY;
  animation-iteration-count: infinite;
  animation-timing-function: ease-in-out;
}
.floating-left .floating-inner { animation-duration: 4.2s; animation-delay: 0s }
.floating-right .floating-inner { animation-duration: 5.1s; animation-delay: 0.6s }

@keyframes floatY {
  0% { transform: translateY(0px) }
  50% { transform: translateY(-12px) }
  100% { transform: translateY(0px) }
}

.floating-img { width: 300%; height: 300%; object-fit: cover; border-radius: 50%; display:block }

/* 小屏幕隐藏以避免遮挡 */
@media (max-width: 900px) {
  .floating { display: none }
}
</style>