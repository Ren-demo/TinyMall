import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../services/api'
import { useAuthStore } from './auth'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const auth = useAuthStore()
  let userId = null

  async function resolveUserId() {
    if (!auth.isLoggedIn) { userId = null; return null }
    if (auth.userId) {
      const uid = Number(auth.userId)
      if (!Number.isNaN(uid) && uid > 0) {
        userId = uid
        return uid
      }
    }
    if (userId) return userId
    try {
      userId = await api.getUserId(auth.username)
      return userId
    } catch (e) { return null }
  }

  async function load() {
    if (auth.isLoggedIn) {
      try {
        const uid = await resolveUserId()
        if (uid) {
          const rows = await api.fetchShoppingCartByUserId(uid)
          items.value = rows || []
          return
        }
      } catch (e) {
        // fallback to localStorage
      }
    }
    try {
      items.value = JSON.parse(localStorage.getItem('cart') || '[]')
    } catch (e) {
      items.value = []
    }
  }

  async function save() {
    if (auth.isLoggedIn) {
      return
    }
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function find(productId) {
    return items.value.find(i => i.productId === productId)
  }

  async function addItem(item) {
    const existing = find(item.productId)
    if (existing) {
      existing.qty += item.qty || 1
    } else {
      items.value.push({ productId: item.productId, name: item.name || '', price: item.price || 0, qty: item.qty || 1, storeId: item.storeId || null })
    }
    if (auth.isLoggedIn) {
      try {
        const uid = await resolveUserId()
        if (uid) {
          await api.addCart({ goodsId: item.productId, userId: uid, count: item.qty || 1 })
          await load()
        }
      } catch (e) {
        // ignore and fallback to local save
      }
    }
    await save()
  }

  async function increase(productId) {
    const e = find(productId)
    if (e) {
      e.qty++
      if (auth.isLoggedIn && e.id) {
        try {
          await api.updateCart({ shoppingCartId: e.id, goodsCount: e.qty, delete: false })
        } catch (err) {
          // ignore and fallback to local save
        }
      }
      await save()
    }
  }

  async function decrease(productId) {
    const e = find(productId)
    if (e && e.qty > 1) {
      e.qty--
      if (auth.isLoggedIn && e.id) {
        try {
          await api.updateCart({ shoppingCartId: e.id, goodsCount: e.qty, delete: false })
        } catch (err) {
          // ignore and fallback to local save
        }
      }
      await save()
    }
  }

  async function remove(productId) {
    const idx = items.value.findIndex(i => i.productId === productId)
    if (idx >= 0) {
      const e = items.value[idx]
      items.value.splice(idx, 1)
      if (auth.isLoggedIn && e && e.id) {
        try {
          await api.updateCart({ shoppingCartId: e.id, goodsCount: 0, delete: true })
        } catch (err) {
          // ignore and fallback to local save
        }
      }
      await save()
    }
  }

  async function clear() {
    const toDelete = items.value.slice()
    items.value = []
    if (auth.isLoggedIn) {
      for (const it of toDelete) {
        if (it && it.id) {
          try {
            await api.updateCart({ shoppingCartId: it.id, goodsCount: 0, delete: true })
          } catch (e) {
            // ignore and fallback to local save
          }
        }
      }
    }
    await save()
  }

  function totalItems() {
    return items.value.reduce((s, it) => s + (it.qty || 0), 0)
  }

  function subtotal() {
    return items.value.reduce((s, it) => s + (it.qty || 0) * (it.price || 0), 0)
  }

  // init: load once and listen to storage events for cross-tab sync
  function init() {
    load()
    window.addEventListener('storage', (e) => {
      if (e.key === 'cart') load()
    })
  }

  return { items, load, save, addItem, increase, decrease, remove, clear, totalItems, subtotal, init }
})
