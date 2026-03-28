// 简单 mock API；开发时替换为真实 axios 请求
const wait = (ms) => new Promise((r) => setTimeout(r, ms))
const IS_LOCAL_DEV = typeof window !== 'undefined' && (window.location.hostname === '127.0.0.1' || window.location.hostname === 'localhost')
// const API_BASE_URL = '/tinymall'
// const API_BASE_URL = ''
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''
const DISABLE_AUTH_HEADER = import.meta.env.VITE_DISABLE_AUTH_HEADER === 'true'

// Build a stable comment-related URL. Returns primary URL and a fallback.
function buildCommentUrl(path = '') {
  const base = (API_BASE_URL || '').replace(/\/$/, '')
  // ensure we have one /tinymall prefix — if API_BASE_URL already includes it, don't add
  const withPrefix = base ? (base.includes('/tinymall') ? base : base + '/tinymall') : '/tinymall'
  const primary = `${withPrefix}/comments${path.startsWith('/') ? path : '/' + path}`
  const fallback = `/tinymall/comments${path.startsWith('/') ? path : '/' + path}`
  return { primary, fallback }
}

// Helper: fetch with token header for all authenticated requests
function fetchWithAuth(url, options = {}) {
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  const headers = { ...options.headers }
  if (!DISABLE_AUTH_HEADER && token) {
    headers['token'] = token
  }
  return fetch(url, { ...options, headers })
}

// 商品数据遵循：GoodsID, GoodsName, StoreID, Text, Count, Price, Picture
const mockProducts = [
  { GoodsID: 1, GoodsName: '商品 A', StoreID: 'store-1', Text: '优质商品 A', Count: 50, Price: 99.0, Picture: '/assets/prod-a.jpg' },
  { GoodsID: 2, GoodsName: '商品 B', StoreID: 'store-2', Text: '热销商品 B', Count: 20, Price: 149.0, Picture: '/assets/prod-b.jpg' },
  { GoodsID: 3, GoodsName: '商品 C', StoreID: 'store-1', Text: '实惠商品 C', Count: 100, Price: 29.0, Picture: '/assets/prod-c.jpg' }
]

// 订单数据格式：OrderID, User, Items:[{ GoodsID, Count }], Status, SupplierID
let mockOrders = [
  { OrderID: 101, User: 'alice', Items: [{ GoodsID: 1, Count: 1 }], Status: '供货商已发货', SupplierID: 'supplier-1' },
  { OrderID: 102, User: 'bob', Items: [{ GoodsID: 2, Count: 2 }], Status: '订单创建', SupplierID: 'supplier-2' }
]

export default {
  _stateToStatus(n) {
    // Numeric state mapping: 0=订单创建,1=订单已支付,2=中间商接受订单,3=供货商发货, -1=拒绝订单
    if (n === 4) return '已收货'
    if (n === 3) return '供货商已发货'
    if (n === 2) return '中间商已接受'
    if (n === 1) return '已支付'
    if (n === -1) return '拒绝'
    return '订单创建'
  },
  async login({ email, password, role }) {
    // Call real backend: POST /tinymall/login/login (basic login without verify)
    try {
      const res = await fetch(`${API_BASE_URL}/login/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          userEmail: email, 
          userPwd: password
        })
      })
      if (res.ok) {
        const response = await res.json()
        console.log('[api.login] response:', response)
        const isSuccess = response && response.code === 1
        if (isSuccess) {
          const data = response.data || {}
            const headerToken = res.headers.get('token') || res.headers.get('Token') || res.headers.get('x-token') || res.headers.get('X-Token')
            const token = headerToken || data.token || data.accessToken || data.jwt
            if (!token) {
              return { success: false, error: 'token_missing_in_response' }
            }
          console.log('✓ 登录成功，Token:', (token || '').substring(0, 20) + '...')
          return {
            success: true,
            token: token,
            role: data.role || role || 'user',
            username: data.username || data.userName || data.nickName || email,
            email: data.userEmail || data.email || email,
            userId: data.userId || data.id,
            userPicture: data.userPicture || data.avatar || data.userAvatar || data.picture,
            address: data.address
          }
        } else {
          return { success: false, error: (response && response.msg) || 'login_failed' }
        }
      } else {
        const errText = await res.text()
        console.error('Login HTTP error:', res.status, errText)
        return { success: false, error: `http_${res.status}` }
      }
    } catch (e) {
      console.error('login failed:', e.message)
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },

  // Send verification code to email for logon flow
  async sendVerificationCode(email) {
    try {
      // Use plain fetch for verification code endpoint to avoid sending token header by default.
      const url = `${API_BASE_URL}/login/logon/verify/${encodeURIComponent(email)}`
      const res = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': '*/*',
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })

      if (res.ok) {
        const response = await res.json()
        console.log('[api.sendVerificationCode] response:', response)
        // Support multiple backend response formats: { code: 200 } or { code: 1 } or { success: true }
        const ok = response && (response.code === 200 || response.code === 1 || response.success === true)
        if (ok) {
          return { success: true, msg: response.msg || '验证码发送成功' }
        } else {
          return { success: false, error: response && (response.msg || response.error) || '验证码发送失败' }
        }
      } else {
        // Read response body for better debugging (500 responses often contain error details)
        let text = ''
        try { text = await res.text() } catch (e) { text = '' }
        console.error('Send verification code HTTP error:', res.status, text)
        return { success: false, error: `http_${res.status}`, details: text }
      }
    } catch (e) {
      console.error('sendVerificationCode failed:', e && e.message)
      return { success: false, error: '后端连接失败', details: e && e.message }
    }
  },

  // Reset password: try common backend endpoints and normalize response
  async resetPassword({ email, code, newPassword } = {}) {
    try {
      // Try backend's explicit updateSecret endpoint first (PUT /login/updateSecret)
      try {
        const updateUrl = `${API_BASE_URL}/login/updateSecret`
        const payload = { userEmail: email, userPwd: newPassword, velify: code }
        // try JSON PUT
        let res = await fetch(updateUrl, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (res && res.ok) {
          const response = await res.json()
          console.log('[api.resetPassword] try updateSecret (json) ', updateUrl, 'response:', response)
          const ok = response && (response.success === true || response.code === 200 || response.code === 1)
          if (ok) {
            const data = response.data || response
            const token = response.token || (data && (data.token || data.accessToken || data.jwt))
            return { success: true, token, role: data && (data.role || 'user'), username: data && (data.username || data.userName) }
          }
          if (response && (response.msg || response.error)) return { success: false, error: response.msg || response.error }
        }

        // try form-encoded PUT as some backends expect x-www-form-urlencoded
        const form = new URLSearchParams()
        form.append('userEmail', email || '')
        form.append('userPwd', newPassword || '')
        form.append('velify', code || '')
        const res2 = await fetch(updateUrl, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: form.toString()
        })
        if (res2 && res2.ok) {
          const response = await res2.json()
          console.log('[api.resetPassword] try updateSecret (form) ', updateUrl, 'response:', response)
          const ok = response && (response.success === true || response.code === 200 || response.code === 1)
          if (ok) {
            const data = response.data || response
            const token = response.token || (data && (data.token || data.accessToken || data.jwt))
            return { success: true, token, role: data && (data.role || 'user'), username: data && (data.username || data.userName) }
          }
          if (response && (response.msg || response.error)) return { success: false, error: response.msg || response.error }
        }
      } catch (e) {
        console.warn('[api.resetPassword] updateSecret attempts failed:', e && e.message)
      }

      // First, try the common `logon` endpoint since some backends reuse it for reset
      const logonUrl = `${API_BASE_URL}/login/logon`
      const payload = { userEmail: email, userPwd: newPassword, velify: code }
      console.log('[api.resetPassword] trying', logonUrl, 'payload:', payload)
      try {
        // try JSON first
        let res = await fetch(logonUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        if (res && res.ok) {
          const response = await res.json()
          console.log('[api.resetPassword] try', logonUrl, 'response:', response)
          const ok = response && (response.success === true || response.code === 200 || response.code === 1)
          if (ok) {
            const data = response.data || response
            const token = response.token || (data && (data.token || data.accessToken || data.jwt))
            return { success: true, token, role: data && (data.role || 'user'), username: data && (data.username || data.userName) }
          }
          if (response && (response.msg || response.error)) return { success: false, error: response.msg || response.error }
        } else if (res) {
          // if JSON attempt got non-OK, try form-encoded (some backends expect this)
          let text = ''
          try { text = await res.text() } catch (_) { text = '' }
          console.warn('[api.resetPassword] JSON POST non-ok:', res.status, text)
        }

        // try x-www-form-urlencoded as alternative
        const form = new URLSearchParams()
        form.append('userEmail', email || '')
        form.append('userPwd', newPassword || '')
        form.append('velify', code || '')
        const res2 = await fetch(logonUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: form.toString()
        })
        if (res2 && res2.ok) {
          const response = await res2.json()
          console.log('[api.resetPassword] try (form) ', logonUrl, 'response:', response)
          const ok = response && (response.success === true || response.code === 200 || response.code === 1)
          if (ok) {
            const data = response.data || response
            const token = response.token || (data && (data.token || data.accessToken || data.jwt))
            return { success: true, token, role: data && (data.role || 'user'), username: data && (data.username || data.userName) }
          }
          if (response && (response.msg || response.error)) return { success: false, error: response.msg || response.error }
        } else if (res2) {
          let text = ''
          try { text = await res2.text() } catch (_) { text = '' }
          console.warn('[api.resetPassword] form POST non-ok:', res2.status, text)
        }
      } catch (e) {
        console.warn('[api.resetPassword] logon attempts failed:', e && e.message)
      }

      // try other common endpoints as a fallback (no auth header)
      // include legacy / custom endpoints used by different backends (added updateSecret)
      const other = [`${API_BASE_URL}/login/updateSecret`, `${API_BASE_URL}/login/reset`, `${API_BASE_URL}/login/logon/reset`, `${API_BASE_URL}/login/resetPassword`, `${API_BASE_URL}/user/resetPassword`]
      for (const url of other) {
        try {
          const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userEmail: email, userPwd: newPassword, velify: code })
          })
          if (!res) continue
          if (res.ok) {
            const response = await res.json()
            console.log('[api.resetPassword] try', url, 'response:', response)
            const ok = response && (response.success === true || response.code === 200 || response.code === 1)
            if (ok) {
              const data = response.data || response
              const token = response.token || (data && (data.token || data.accessToken || data.jwt))
              return { success: true, token, role: data && (data.role || 'user'), username: data && (data.username || data.userName) }
            }
            if (response && (response.msg || response.error)) return { success: false, error: response.msg || response.error }
          }
        } catch (e) {
          // continue
        }
      }

      // Only return mock success in local dev to avoid production surprises
      if (IS_LOCAL_DEV) return { success: true, token: `mock-token-${email}`, role: 'user', username: (email || '').split('@')[0] }
      return { success: false, error: 'no_reset_endpoint_found' }
    } catch (e) {
      console.error('resetPassword failed:', e && e.message)
      return { success: false, error: '后端连接失败', details: e && e.message }
    }
  },

  // Logon with verification code (registration/login)
  async logon({ email, password, username, verifyCode }) {
    try {
      const res = await fetch(`${API_BASE_URL}/login/logon`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userEmail: email,
          userPwd: password,
          userName: username || email.split('@')[0],
          velify: verifyCode || ''
        })
      })
      if (res.ok) {
        const response = await res.json()
        console.log('[api.logon] response:', response)
        const isSuccess = response && response.code === 1
        if (isSuccess) {
          const data = response.data || {}
          const token = data.token || data.accessToken || data.jwt || `token-${data.userEmail || email}`
          console.log('✓ 注册成功，Token:', (token || '').substring(0, 20) + '...')
          return {
            success: true,
            code: response && response.code,
            token: token,
            role: data.role || 'user',
            username: data.username || data.userName || data.nickName || username || email,
            email: data.userEmail || data.email || email,
            userId: data.userId || data.id,
            userPicture: data.userPicture || data.avatar || data.userAvatar || data.picture,
            address: data.address
          }
        } else {
          return { success: false, code: response && response.code, msg: (response && response.msg) || 'logon_failed', error: (response && response.msg) || 'logon_failed' }
        }
      } else {
        const errText = await res.text()
        console.error('Logon HTTP error:', res.status, errText)
        return { success: false, error: `http_${res.status}` }
      }
    } catch (e) {
      console.error('logon failed:', e.message)
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },

  async register({ email, username, password, code }) {
    // Register using logon endpoint on real backend
    return await this.logon({ email, password, username, verifyCode: code })
  },

  async updateUserInfo({ userId, userName, userPwd, userEmail, userPicture, address } = {}) {
    try {
      const token = sessionStorage.getItem('token') || localStorage.getItem('token')
      const payloadUser = {
        userId: userId ?? null,
        userName: userName ?? '',
        userPwd: userPwd ?? '',
        userEmail: userEmail ?? '',
        userPicture: userPicture ?? '',
        address: address ?? ''
      }
      const res = await fetchWithAuth(`${API_BASE_URL}/user/update`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'token': token || ''
        },
        body: JSON.stringify(payloadUser)
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return {
          success: isSuccess,
          code: response && response.code,
          msg: response && response.msg,
          data: response && response.data
        }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },

  async uploadUserPicture(userId, file) {
    try {
      if (!userId || !file) return { success: false, error: 'missing_params' }
      const form = new FormData()
      form.append('file', file)
      const res = await fetchWithAuth(`${API_BASE_URL}/user/picture/${userId}`, {
        method: 'POST',
        body: form
      })
      if (res.ok) {
        const response = await res.json()
        // DEBUG: log upload response to help diagnose blob vs http URL issues
        try { console.log('[api.uploadUserPicture] response:', response) } catch (e) {}
        const isSuccess = response && (response.code === 0 || response.code === 1 || response.code === 200)
        return { success: isSuccess, data: response && response.data, msg: response && response.msg }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },

  async addCart({ goodsId, userId, count }) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/shoppingCart/addCart`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goodsId, userId, count })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async updateCart({ shoppingCartId, goodsCount, delete: isDelete }) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/shoppingCart/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ shoppingCartId, goodsCount, delete: isDelete })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async addOrderOnCart({ goodsId, count, userId, address }) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/shoppingCart/addOrderOnCart`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goodsId, count, userId, address })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async addOrder({ goodsId, count, userId, address }) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/order/addOrder`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ goodsId, count, userId, address })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async userDelOrder(orderId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/order/delOrder/${orderId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async payOrder({ orderId, paymentToken }) {
    try {
      const payload = {}
      if (orderId !== undefined && orderId !== null) payload.orderId = orderId
      if (paymentToken) payload.paymentToken = paymentToken
      const res = await fetchWithAuth(`${API_BASE_URL}/pay/pay`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (res.ok) {
        try {
          return await res.json()
        } catch (e) {
          return { success: true, code: res.status }
        }
      }
    } catch (e) {}
    return { success: false }
  },

  async checkOrder(orderId) {
    try {
      if (!orderId) return { success: false, error: 'missing_order_id' }
      const res = await fetchWithAuth(`${API_BASE_URL}/order/checkOrder/${orderId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },

  async fetchProducts() {
    // Try backend first
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/goods/list`)
      if (res.ok) {
        const payload = await res.json()
        let rows = []
        if (Array.isArray(payload)) rows = payload
        else if (payload && Array.isArray(payload.data)) rows = payload.data
        else if (payload && payload.data && Array.isArray(payload.data.list)) rows = payload.data.list

        return (rows || []).map(r => ({
          GoodsID: r.GoodsID ?? r.goodsID ?? r.goodsId ?? r.id,
          GoodsName: r.GoodsName ?? r.goodsName ?? r.name,
          StoreID: r.StoreID ?? r.storeID ?? r.storeId,
          StoreName: r.StoreName ?? r.storeName ?? r.supplierName ?? '',
          Text: r.Text ?? r.text ?? r.description,
          Count: r.Count ?? r.count ?? r.stock,
          Price: r.Price ?? r.price ?? r.unitPrice,
          Picture: r.Picture ?? r.picture ?? r.image
        }))
      }
    } catch (e) {
      // ignore and fallback
    }
    await wait(200)
    return mockProducts.map(p => ({ ...p }))
  },
  // Server-side paginated products query. Expects backend endpoint /goods/listInpage
  // params: pageNum, pageSize, listGoodsDto (object) -> flattened to query keys like listGoodsDto.field=value
  async fetchProductsInpage({ pageNum = 1, pageSize = 12, listGoodsDto = {} } = {}) {
    // Backend changed to: GET /tinymall/goods/listInpage/{pageNum}/{pageSize}
    // Other optional listGoodsDto fields will be appended as query parameters.
    try {
      const base = `${API_BASE_URL}/goods/listInpage/${encodeURIComponent(pageNum)}/${encodeURIComponent(pageSize)}`
      const params = new URLSearchParams()
      if (listGoodsDto && typeof listGoodsDto === 'object') {
        for (const k of Object.keys(listGoodsDto)) {
          const v = listGoodsDto[k]
          if (v === undefined || v === null) continue
          params.append(k, String(v))
        }
      }
      const url = params.toString() ? `${base}?${params.toString()}` : base
      const res = await fetchWithAuth(url, { method: 'GET', headers: { 'Accept': '*/*' } })
      console.debug('[api.fetchProductsInpage] GET url:', url, 'status:', res && res.status)
      if (!res || !res.ok) return { success: false }
      const payload = await res.json()
      const data = payload && payload.data ? payload.data : payload
      const rows = data && (data.list || data.records || data.items) ? (data.list || data.records || data.items) : (Array.isArray(data) ? data : [])
      const total = (data && (data.total || data.totalCount || data.count)) ? (data.total ?? data.totalCount ?? data.count) : rows.length
      return { success: true, rows, total, raw: payload }
    } catch (e) {
      console.error('[api.fetchProductsInpage] error', e)
      return { success: false }
    }
  },
  // 评论相关接口
  async fetchComments(goodsId) {
    try {
      const { primary, fallback } = buildCommentUrl(`/${encodeURIComponent(goodsId)}`)
      let res = await fetchWithAuth(primary, { method: 'GET', headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
      if ((!res || res.status === 404) && fallback) {
        res = await fetchWithAuth(fallback, { method: 'GET', headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
      }
      if (res && res.ok) {
        const payload = await res.json()
        return payload && payload.data ? payload.data : payload
      }
    } catch (e) {
      console.error('[api.fetchComments] error', e)
    }
    await wait(100)
    return []
  },

  async addComment(addComDto) {
    try {
      const { primary, fallback } = buildCommentUrl('/addCom')
      let res = await fetchWithAuth(primary, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(addComDto) })
      if ((!res || res.status === 404) && fallback) {
        res = await fetchWithAuth(fallback, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(addComDto) })
      }
      if (res && res.ok) return await res.json()
    } catch (e) {
      console.error('[api.addComment] error', e)
    }
    return { success: false }
  },

  async updateComment(updateComDto) {
    try {
      const { primary, fallback } = buildCommentUrl('/updateCom')
      let res = await fetchWithAuth(primary, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updateComDto) })
      if ((!res || res.status === 404) && fallback) {
        res = await fetchWithAuth(fallback, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updateComDto) })
      }
      if (res && res.ok) return await res.json()
      // 非 2xx 响应：尝试读取文本并打印详情，便于调试后端报错
      if (res) {
        try {
          const txt = await res.text()
          console.error('[api.updateComment] HTTP', res.status, 'responseText:', txt)
          // 尝试解析为 JSON for caller
          try { return JSON.parse(txt) } catch (e) { return { success: false, error: `http_${res.status}`, details: txt } }
        } catch (e) {
          console.error('[api.updateComment] read response failed', e)
          return { success: false, error: `http_${res.status}` }
        }
      }
    } catch (e) {
      console.error('[api.updateComment] error', e)
    }
    return { success: false }
  },

  async uploadCommentImage(orderId, file) {
    try {
      if (!file) return { success: false, error: 'missing_file' }
      const form = new FormData()
      form.append('file', file)
      const { primary, fallback } = buildCommentUrl(`/uploadImg/${encodeURIComponent(orderId)}`)
      const urls = [primary]
      if (fallback && fallback !== primary) urls.push(fallback)
      for (const url of urls) {
        try {
          const res = await fetchWithAuth(url, { method: 'POST', body: form })
          let parsed = null
          try { parsed = await res.clone().json() } catch (e) { parsed = null }
          if (res && res.ok) return parsed || { code: res.status, data: null }
          if (res && res.status === 404) continue
          return parsed || { success: false, error: `http_${res && res.status}` }
        } catch (e) {
          continue
        }
      }
    } catch (e) {
      console.error('[api.uploadCommentImage] error', e)
    }
    return { success: false }
  },
  // 删除评论图片（后端接口：DELETE /tinymall/comments/delImg 接受 JSON 或 form-urlencoded）
  async deleteCommentImage(imgUrl) {
    if (!imgUrl) return { success: false, error: 'missing_url' }
    const { primary, fallback } = buildCommentUrl('/delImg')
    const urls = [primary]
    if (fallback && fallback !== primary) urls.push(fallback)
    for (const url of urls) {
      try {
        // 优先尝试：发送纯文本字符串（多数后端简单实现期望一个字符串）
        try {
          const resTxt = await fetchWithAuth(url, { method: 'POST', headers: { 'Content-Type': 'text/plain' }, body: String(imgUrl) })
          if (resTxt && resTxt.ok) {
            try { return await resTxt.json() } catch (e) { return { success: true } }
          }
          if (resTxt && (resTxt.status === 404 || resTxt.status === 405 || resTxt.status === 400)) {
            const txt = await resTxt.text().catch(() => '')
            console.warn('[api.deleteCommentImage] POST text failed', resTxt.status, txt)
          }
        } catch (e) {
          console.warn('[api.deleteCommentImage] POST text threw', e && e.message)
        }

        // 次选：DELETE 只带纯文本 body
        try {
          const res2 = await fetchWithAuth(url, { method: 'DELETE', headers: { 'Content-Type': 'text/plain' }, body: String(imgUrl) })
          if (res2 && res2.ok) {
            try { return await res2.json() } catch (e) { return { success: true } }
          }
          if (res2 && (res2.status === 404 || res2.status === 405 || res2.status === 400)) {
            const txt = await res2.text().catch(() => '')
            console.warn('[api.deleteCommentImage] DELETE text failed', res2.status, txt)
          }
        } catch (e) {
          console.warn('[api.deleteCommentImage] DELETE text threw', e && e.message)
        }

        // 兼容：某些后端需要 JSON body
        try {
          const postRes = await fetchWithAuth(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ url: imgUrl }) })
          if (postRes && postRes.ok) return await postRes.json()
          const postText = await postRes.text().catch(() => '')
          console.warn('[api.deleteCommentImage] POST json failed', postRes && postRes.status, postText)
        } catch (e) {
          console.warn('[api.deleteCommentImage] POST json threw', e && e.message)
        }

        // 最后尝试 form-urlencoded
        try {
          const form = new URLSearchParams(); form.append('url', imgUrl)
          const formRes = await fetchWithAuth(url, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form.toString() })
          if (formRes && formRes.ok) {
            try { return await formRes.json() } catch (e) { return { success: true } }
          }
          const formText = await formRes.text().catch(() => '')
          console.warn('[api.deleteCommentImage] POST form failed', formRes && formRes.status, formText)
        } catch (e) {
          console.warn('[api.deleteCommentImage] POST form threw', e && e.message)
        }

      } catch (e) {
        console.warn('[api.deleteCommentImage] outer error', e && e.message)
        continue
      }
    }
    return { success: false, error: 'all_methods_failed' }
  },
  async deleteComment(commentId) {
    if (!commentId) return { success: false, error: 'missing_id' }
    const { primary, fallback } = buildCommentUrl(`/${encodeURIComponent(commentId)}`)
    const urls = [primary]
    if (fallback && fallback !== primary) urls.push(fallback)
    for (const url of urls) {
      try {
        // Try a plain DELETE without body (some servers reject DELETE bodies)
        let res = await fetchWithAuth(url, { method: 'DELETE' })
        if (res && res.ok) {
          try { const parsed = await res.json(); return parsed || { success: true } } catch (e) { return { success: true } }
        }
        // If server returns 405 (Method Not Allowed), try common POST delete fallback
        if (res && res.status === 405) {
          try {
            const postUrl = url.endsWith('/') ? `${url}delete` : `${url}/delete`
            const postRes = await fetchWithAuth(postUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id: commentId }) })
            if (postRes && postRes.ok) {
              try { return await postRes.json() } catch (e) { return { success: true } }
            }
            const postText = postRes ? await postRes.text() : null
            return { success: false, error: `http_${postRes && postRes.status}`, details: postText }
          } catch (e) {
            return { success: false, error: e.message }
          }
        }
        // If 404, try next fallback url
        if (res && res.status === 404) continue
        const parsed = await (async () => { try { return await res.clone().json() } catch (e) { return null } })()
        return parsed || { success: false, error: `http_${res && res.status}` }
      } catch (e) { continue }
    }
    return { success: false }
  },
  async fetchUserOrders(userIdOrUsername) {
    let uid = null
    if (userIdOrUsername !== undefined && userIdOrUsername !== null) {
      const n = Number(userIdOrUsername)
      if (!Number.isNaN(n) && n > 0) uid = n
    }
    if (!uid && typeof userIdOrUsername === 'string') {
      try {
        uid = await this.getUserId(userIdOrUsername)
      } catch (e) {}
    }

    // Try backend (/tinymall/order/list/{userId})
    if (uid) {
      try {
        const res = await fetchWithAuth(`${API_BASE_URL}/order/list/${uid}`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
        if (res.ok) {
          const payload = await res.json()
          const rows = Array.isArray(payload) ? payload : (payload && payload.data ? payload.data : [])
          const hasItemsArray = rows && rows.length && (rows[0].items || rows[0].Items || rows[0].orderItems || rows[0].detailList || rows[0].goodsList || rows[0].goods)
          if (!hasItemsArray && rows && rows.length && (rows[0].orderId || rows[0].OrderID || rows[0].goodsId || rows[0].GoodsID)) {
            const grouped = {}
            for (const r of rows) {
              const oid = r.orderId ?? r.OrderID ?? r.id
              if (!grouped[oid]) {
                const rawState = r.state ?? r.State ?? r.status
                const orderTotal = r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price
                grouped[oid] = {
                  id: oid,
                  user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
                  items: [],
                  status: (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建'),
                  state: rawState,
                  supplierId: r.supplierId ?? r.SupplierID ?? r.storeId ?? r.StoreID ?? null,
                  storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
                  time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
                  totalPrice: orderTotal,
                  address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
                }
              }
              grouped[oid].items.push({
                productId: r.goodsId ?? r.goodsID ?? r.GoodsID,
                qty: r.count ?? r.Count ?? r.goodsCount ?? 0,
                name: r.goodsName ?? r.GoodsName ?? '',
                goodsName: r.goodsName ?? r.GoodsName ?? '',
                price: r.price ?? r.Price ?? 0,
                storeId: r.storeId ?? r.StoreID ?? r.storeID ?? null,
                storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? ''
              })
            }
            const orders = Object.values(grouped)
            try {
              const needName = orders.some(o => (o.items || []).some(it => !(it.name || it.goodsName)))
              if (needName) {
                const prods = await this.fetchProducts()
                const map = {}
                ;(prods || []).forEach(p => { map[p.GoodsID] = p })
                for (const o of orders) {
                  for (const it of (o.items || [])) {
                    if (!it.name && !it.goodsName) {
                      const p = map[it.productId]
                      if (p) {
                        it.name = p.GoodsName || it.name
                        it.goodsName = p.GoodsName || it.goodsName
                      }
                    }
                  }
                }
              }
            } catch (e) {}
            return orders
          }
          const orders = (rows || []).map(r => {
            const rawItems = r.items || r.Items || r.orderItems || r.detailList || r.goodsList || r.goods || []
            const items = Array.isArray(rawItems) ? rawItems.map(it => ({
              productId: it.productId ?? it.GoodsID ?? it.goodsId ?? it.goodsID,
              qty: it.qty ?? it.Count ?? it.goodsCount ?? it.count ?? 0,
              name: it.name ?? it.GoodsName ?? it.goodsName ?? '',
              goodsName: it.goodsName ?? it.GoodsName ?? it.name ?? '',
              price: it.price ?? it.Price ?? it.goodsPrice ?? 0,
              storeId: it.storeId ?? it.StoreID ?? it.storeID ?? null,
              storeName: it.storeName ?? it.StoreName ?? it.supplierName ?? ''
            })) : (r.GoodsID ? [{ productId: r.GoodsID ?? r.goodsId, qty: r.Count ?? r.goodsCount ?? 0 }] : [])

            const rawState = r.state ?? r.State ?? r.status
            const status = (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建')
            return {
              id: r.id ?? r.OrderID ?? r.orderId,
              user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
              items,
              status: (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建'),
              state: rawState,
              supplierId: r.supplierId ?? r.SupplierID ?? (items && items[0] && items[0].storeId),
              storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
              time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
              totalPrice: r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price,
              address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
            }
          })
          try {
            const needName = orders.some(o => (o.items || []).some(it => !(it.name || it.goodsName)))
            if (needName) {
              const prods = await this.fetchProducts()
              const map = {}
              ;(prods || []).forEach(p => { map[p.GoodsID] = p })
              for (const o of orders) {
                for (const it of (o.items || [])) {
                  if (!it.name && !it.goodsName) {
                    const p = map[it.productId]
                    if (p) {
                      it.name = p.GoodsName || it.name
                      it.goodsName = p.GoodsName || it.goodsName
                    }
                  }
                }
              }
            }
          } catch (e) {}
          return orders
        }
      } catch (e) {}
    }

    await wait(200)
    if (typeof userIdOrUsername === 'string') {
      return mockOrders.filter(o => o.User === userIdOrUsername).map(o => ({ id: o.OrderID, user: o.User, items: o.Items ? o.Items.map(it => ({ productId: it.GoodsID, qty: it.Count })) : [], status: o.Status || o.status || '订单创建', supplierId: o.SupplierID }))
    }
    return mockOrders.map(o => ({ id: o.OrderID, user: o.User, items: o.Items ? o.Items.map(it => ({ productId: it.GoodsID, qty: it.Count })) : [], status: o.Status || o.status || '订单创建', supplierId: o.SupplierID }))
  },

  async fetchMiddleOrders() {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/mall/list`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const payload = await res.json()
        const rows = Array.isArray(payload) ? payload : (payload && payload.data ? payload.data : [])
        const hasItemsArray = rows && rows.length && (rows[0].items || rows[0].Items || rows[0].orderItems || rows[0].detailList || rows[0].goodsList || rows[0].goods)
        if (!hasItemsArray && rows && rows.length && (rows[0].orderId || rows[0].OrderID || rows[0].goodsId || rows[0].GoodsID)) {
          const grouped = {}
          for (const r of rows) {
            const oid = r.orderId ?? r.OrderID ?? r.id
            if (!grouped[oid]) {
              const rawState = r.state ?? r.State ?? r.status
              const orderTotal = r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price
              grouped[oid] = {
                id: oid,
                user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
                items: [],
                status: (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建'),
                state: rawState,
                supplierId: r.supplierId ?? r.SupplierID ?? r.storeId ?? r.StoreID ?? null,
                storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
                time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
                totalPrice: orderTotal,
                address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
              }
            }
            grouped[oid].items.push({
              productId: r.goodsId ?? r.goodsID ?? r.GoodsID,
              qty: r.count ?? r.Count ?? r.goodsCount ?? 0,
              name: r.goodsName ?? r.GoodsName ?? '',
              price: r.price ?? r.Price ?? 0,
              storeId: r.storeId ?? r.StoreID ?? r.storeID ?? null,
              storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? ''
            })
          }
          return Object.values(grouped)
        }
        return (rows || []).map(r => {
          const rawState = r.state ?? r.State ?? r.status
          const status = (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建')
          const items = r.items || r.Items || r.orderItems || r.detailList || r.goodsList || r.goods || []
          return {
            id: r.id ?? r.OrderID ?? r.orderId,
            user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
            items: Array.isArray(items) ? items.map(it => ({
              productId: it.productId ?? it.GoodsID ?? it.goodsId ?? it.goodsID,
              qty: it.qty ?? it.Count ?? it.goodsCount ?? it.count ?? 0,
              name: it.name ?? it.GoodsName ?? it.goodsName ?? '',
              price: it.price ?? it.Price ?? it.goodsPrice ?? 0,
              storeId: it.storeId ?? it.StoreID ?? it.storeID ?? null,
              storeName: it.storeName ?? it.StoreName ?? it.supplierName ?? ''
            })) : [],
            status,
            state: rawState,
            supplierId: r.supplierId ?? r.SupplierID ?? r.storeId ?? r.StoreID ?? (items && items[0] && (items[0].storeId || items[0].StoreID)),
            storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
            time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
            totalPrice: r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price,
            address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
          }
        })
      }
    } catch (e) {}
    await wait(200)
    return mockOrders.map(o => ({ id: o.OrderID, user: o.User, items: o.Items ? o.Items.map(it => ({ productId: it.GoodsID, qty: it.Count })) : [], status: o.Status || o.status || '订单创建', supplierId: o.SupplierID }))
  },
  async addStore({ storeName, storeEmail, picture } = {}) {
    try {
      const payload = {
        storeName: storeName || '',
        storeEmail: storeEmail || '',
        picture: picture || ''
      }
      const res = await fetchWithAuth(`${API_BASE_URL}/mall/addStore`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async manageOrder({ orderId, msg, refuse } = {}) {
    try {
      const payload = {
        orderId: orderId ?? null,
        msg: msg ?? '',
        refuse: !!refuse
      }
      const res = await fetchWithAuth(`${API_BASE_URL}/mall/manageOrder`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async urgeSupplier(orderId, storeId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/mall/urge/${encodeURIComponent(orderId)}/${encodeURIComponent(storeId)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async updateOrderStatus(orderId, status) {
    // Try backend first: accept numeric `state` or string `status` mapped server-side
    try {
      const statusMap = {
        '订单创建': 0,
        '已支付': 1,
        '中间商已接受': 2,
        '供货商已发货': 3,
        '拒绝': -1
      }
      const payload = (typeof status === 'number')
        ? { orderId, state: status }
        : (statusMap[status] !== undefined ? { orderId, state: statusMap[status] } : { orderId, status })
      const res = await fetchWithAuth(`${API_BASE_URL}/api/orders/state`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    // fallback mock
    await wait(200)
    const o = mockOrders.find(x => x.OrderID === orderId)
    if (o) o.Status = status
    return { success: !!o }
  },
  async shipOrder(orderId, supplierId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/orders/ship`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderId, supplierId })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    await wait(200)
    // fallback: update mockOrders if supplierId matches
    const o = mockOrders.find(x => x.OrderID === orderId && x.SupplierID == supplierId)
    if (o) o.Status = '供货商已发货'
    return { success: !!o }
  },
  async cancelOrder(orderId) {
    // cancel via backend delete endpoint
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/order/delOrder/${orderId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    await wait(100)
    const o = mockOrders.find(x => x.OrderID === orderId)
    if (o) o.Status = '拒绝'
    return { success: !!o }
  },
  async fetchInventory() {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/goods`)
      if (res.ok) return await res.json()
    } catch (e) {}
    await wait(200)
    return mockProducts.map(p => ({ ...p }))
  },
  async listGoods(storeId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/store/listGoods/${encodeURIComponent(storeId)}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const payload = await res.json()
        const rows = Array.isArray(payload) ? payload : (payload && payload.data ? payload.data : [])
        return (rows || []).map(r => ({
          GoodsID: r.GoodsID ?? r.goodsID ?? r.goodsId ?? r.id,
          GoodsName: r.GoodsName ?? r.goodsName ?? r.name,
          StoreID: r.StoreID ?? r.storeID ?? r.storeId ?? storeId,
          Text: r.Text ?? r.text ?? r.description,
          Count: r.Count ?? r.count ?? r.stock,
          Price: r.Price ?? r.price ?? r.unitPrice,
          Picture: r.Picture ?? r.picture ?? r.image
        }))
      }
    } catch (e) {}
    await wait(200)
    return mockProducts.filter(p => String(p.StoreID) === String(storeId)).map(p => ({ ...p }))
  },
  async addGoods({ file, goodsName, storeId, text, count, price } = {}) {
    try {
      const form = new FormData()
      if (file) form.append('file', file)
      if (goodsName !== undefined) form.append('goodsName', goodsName)
      if (storeId !== undefined && storeId !== null) form.append('storeID', storeId)
      if (text !== undefined) form.append('text', text)
      if (count !== undefined && count !== null) form.append('count', count)
      if (price !== undefined && price !== null) form.append('price', price)

      const res = await fetchWithAuth(`${API_BASE_URL}/store/add`, {
        method: 'POST',
        body: form
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async updateGoods({ goodsId, goodsName, text, price, count, add, file } = {}) {
    try {
      const hasCount = count !== undefined && count !== null
      const payloadCount = hasCount ? count : 0
      const payloadAdd = hasCount ? (add ?? null) : true
      let res = null
      if (file) {
        const form = new FormData()
        if (file) form.append('file', file)
        if (goodsId !== undefined && goodsId !== null) form.append('goodsId', goodsId)
        if (goodsName !== undefined) form.append('goodsName', goodsName)
        if (text !== undefined) form.append('text', text)
        if (price !== undefined && price !== null) form.append('price', price)
        form.append('count', payloadCount)
        if (payloadAdd !== undefined && payloadAdd !== null) form.append('add', payloadAdd)
        res = await fetchWithAuth(`${API_BASE_URL}/store/updateGoods`, {
          method: 'PUT',
          body: form
        })
      } else {
        const payload = {
          goodsId: goodsId ?? null,
          goodsName: goodsName ?? '',
          text: text ?? '',
          price: price ?? null,
          count: payloadCount,
          add: payloadAdd
        }
        res = await fetchWithAuth(`${API_BASE_URL}/store/updateGoods`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
      }

      if (res && res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = res ? await res.text() : ''
      return { success: false, error: res ? `http_${res.status}` : 'no_response', details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async updateGoodsPic(goodsId, file) {
    try {
      if (!goodsId || !file) return { success: false, error: 'missing_params' }
      const form = new FormData()
      form.append('file', file)
      const url = `${API_BASE_URL}/store/updateGoodsPic/${encodeURIComponent(goodsId)}`
      try { console.debug('[api.updateGoodsPic] request URL:', url, 'file:', file && file.name) } catch (e) {}
      const res = await fetchWithAuth(url, {
        method: 'POST',
        body: form
      })
      try { console.debug('[api.updateGoodsPic] fetch returned ok=', res && res.ok, 'status=', res && res.status) } catch (e) {}
      if (res.ok) {
        // Some backends respond with empty body (204 or 200 with no JSON).
        // Try reading text first and parse JSON only if non-empty to avoid json() throwing.
        const text = await res.text()
        if (!text || !text.trim()) {
          try { console.debug('[api.updateGoodsPic] empty response body, treating as success. status=', res.status) } catch (e) {}
          return { success: true, code: res.status, msg: 'empty_response', data: null }
        }
        let response = null
        try {
          response = JSON.parse(text)
        } catch (e) {
          try { console.warn('[api.updateGoodsPic] response not JSON:', text) } catch (err) {}
          return { success: false, error: 'invalid_json', details: text }
        }
        try { console.debug('[api.updateGoodsPic] response:', response) } catch (e) {}
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        if (!isSuccess) {
          try { console.warn('[api.updateGoodsPic] marked failure:', response) } catch (e) {}
        }
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      try { console.error(`[api.updateGoodsPic] HTTP ${res.status}:`, errText) } catch (e) {}
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      try { console.error('[api.updateGoodsPic] exception:', e && e.message) } catch (err) {}
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async deliveryOrder(orderId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/store/delivery/${encodeURIComponent(orderId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const response = await res.json()
        const isSuccess = response && (response.code === 0 || response.code === 200 || response.code === 1)
        return { success: isSuccess, code: response && response.code, msg: response && response.msg, data: response && response.data }
      }
      const errText = await res.text()
      return { success: false, error: `http_${res.status}`, details: errText }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e.message }
    }
  },
  async updateInventory(productId, delta) {
    await wait(200)
    const p = mockProducts.find(x => x.GoodsID === productId)
    if (p) p.Count += delta
    return { success: !!p }
  },
  async fetchSalesStats() {
    await wait(200)
    return { totalSales: 12345.6, orderCount: mockOrders.length }
  }
  ,
  async fetchMiddlemanSalesStats() {
    await wait(200)
    // 统计所有已发货订单总额和数量（可视为中间商完成的销售）
    const shipped = mockOrders.filter(o => o.Status === '供货商已发货')
    const totalSales = shipped.reduce((sum, o) => {
      const orderSum = (o.Items || []).reduce((s, it) => {
        const p = mockProducts.find(x => x.GoodsID === it.GoodsID)
        return s + (p ? p.Price * (it.Count || 0) : 0)
      }, 0)
      return sum + orderSum
    }, 0)
    return { totalSales, orderCount: shipped.length, avgOrder: shipped.length ? +(totalSales / shipped.length).toFixed(2) : 0 }
  }
  ,
  async sendOrderEmail(orderId) {
    await wait(200)
    // legacy: send basic notification
    const o = mockOrders.find(x => x.OrderID === orderId)
    return { success: !!o }
  }
  ,
  async sendOrderEmailWithMessage(orderId, message) {
    // Send notification to backend
    try {
      await fetchWithAuth(`${API_BASE_URL}/order/notify`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'notify', orderId, message })
      })
    } catch (e) {}
    await wait(100)
    return { success: true }
  },
  async sendToSupplier(orderId, supplierId) {
    // If supplierId provided, call legacy endpoint; otherwise call /api/orders/send to auto-dispatch
    try {
      if (!orderId) return { success: false }
      if (supplierId) {
        const res = await fetchWithAuth(`${API_BASE_URL}/api/orders/send`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId })
        })
        if (res.ok) return await res.json()
      } else {
        const res = await fetchWithAuth(`${API_BASE_URL}/api/orders/send`, {
          method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ orderId })
        })
        if (res.ok) return await res.json()
      }
    } catch (e) {}
    await wait(200)
    const o = mockOrders.find(x => x.OrderID === orderId)
    if (!o) return { success: false }
    o.Status = '中间商已接受'
    o.SupplierID = supplierId || o.SupplierID
    return { success: true }
  },
  async fetchSupplierOrders(supplierId) {
    // Try backend first: GET //store/listOrders/{storeId}
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/store/listOrders/${encodeURIComponent(supplierId)}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const payload = await res.json()
        const rows = Array.isArray(payload) ? payload : (payload && payload.data ? payload.data : [])
        const hasItemsArray = rows && rows.length && (rows[0].items || rows[0].Items || rows[0].orderItems || rows[0].detailList || rows[0].goodsList || rows[0].goods)
        if (!hasItemsArray && rows && rows.length && (rows[0].orderId || rows[0].OrderID || rows[0].goodsId || rows[0].GoodsID)) {
          const grouped = {}
          for (const r of rows) {
            const oid = r.orderId ?? r.OrderID ?? r.id
            if (!grouped[oid]) {
              const rawState = r.state ?? r.State ?? r.status
              const orderTotal = r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price
              grouped[oid] = {
                id: oid,
                user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
                userId: r.userId ?? r.UserID ?? r.userid ?? r.userID ?? r.buyerId ?? r.BuyerID ?? null,
                items: [],
                status: (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建'),
                state: rawState,
                supplierId: r.supplierId ?? r.SupplierID ?? r.storeId ?? r.StoreID ?? supplierId,
                storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
                time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
                totalPrice: orderTotal,
                address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
              }
            }
            grouped[oid].items.push({
              productId: r.goodsId ?? r.goodsID ?? r.GoodsID,
              qty: r.count ?? r.Count ?? r.goodsCount ?? 0,
              name: r.goodsName ?? r.GoodsName ?? '',
              price: r.price ?? r.Price ?? 0,
              storeId: r.storeId ?? r.StoreID ?? r.storeID ?? supplierId,
              storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? ''
            })
          }
          return Object.values(grouped)
        }
        return (rows || []).map(r => {
          const rawState = r.state ?? r.State ?? r.status
          const items = r.items || r.Items || r.orderItems || r.detailList || r.goodsList || r.goods || []
          return {
            id: r.id ?? r.OrderID ?? r.orderId,
            user: r.user ?? r.UserName ?? r.userName ?? r.username ?? '',
            userId: r.userId ?? r.UserID ?? r.userid ?? r.userID ?? r.buyerId ?? r.BuyerID ?? null,
            items: Array.isArray(items) ? items.map(it => ({
              productId: it.productId ?? it.GoodsID ?? it.goodsId ?? it.goodsID,
              qty: it.qty ?? it.Count ?? it.goodsCount ?? it.count ?? 0,
              name: it.name ?? it.GoodsName ?? it.goodsName ?? '',
              price: it.price ?? it.Price ?? it.goodsPrice ?? 0,
              storeId: it.storeId ?? it.StoreID ?? it.storeID ?? supplierId,
              storeName: it.storeName ?? it.StoreName ?? it.supplierName ?? ''
            })) : [],
            status: (this._stateToStatus && typeof rawState === 'number') ? this._stateToStatus(rawState) : (rawState || r.Status || '订单创建'),
            state: rawState,
            supplierId: r.supplierId ?? r.SupplierID ?? r.storeId ?? r.StoreID ?? supplierId,
            storeName: r.storeName ?? r.StoreName ?? r.supplierName ?? '',
            time: r.time ?? r.Time ?? r.createTime ?? r.createdTime ?? r.create_time ?? r.created_at ?? r.createAt ?? null,
            totalPrice: r.totalPrice ?? r.total ?? r.amount ?? r.TotalPrice ?? r.Total ?? r.Amount ?? r.orderTotal ?? r.total_price,
            address: r.address ?? r.Address ?? r.userAddress ?? r.UserAddress ?? r.addr ?? ''
          }
        })
      }
    } catch (e) {}
    await wait(200)
    return mockOrders.filter(o => o.SupplierID == supplierId && o.Status === '中间商已接受')
  }
  ,
  async createSupplierMockOrder(supplierId, opts = {}) {
    await wait(100)
    const maxId = mockOrders.reduce((m, o) => Math.max(m, o.OrderID), 100)
    const newOrder = {
      OrderID: maxId + 1,
      User: opts.user || 'test-buyer',
      Items: opts.items || [{ GoodsID: 1, Count: 1 }],
      Status: '中间商已接受',
      SupplierID: supplierId
    }
    mockOrders.push(newOrder)
    return { success: true, order: newOrder }
  }
  ,
  async createOrder(username, items) {
    // items: [{ productId, qty, storeId }]
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, items })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    // fallback: create mock order(s)
    await wait(200)
    const maxId = mockOrders.reduce((m, o) => Math.max(m, o.OrderID), 100)
    const newOrder = { OrderID: maxId + 1, User: username, Items: (items || []).map(i => ({ GoodsID: i.productId, Count: i.qty })), Status: '已支付', SupplierID: items && items[0] && items[0].storeId }
    mockOrders.push(newOrder)
    return { success: true, orders: [newOrder] }
  },
  async fetchShoppingCart(username) {
    // Try backend: lookup user then fetch shoppingcart by userid
    try {
      const usersRes = await fetchWithAuth(`${API_BASE_URL}/api/users`)
      if (usersRes.ok) {
        const users = await usersRes.json()
        const u = users.find(x => x.UserName === username || x.UserEmail === username)
        if (u) {
          const res = await fetchWithAuth(`${API_BASE_URL}/api/shoppingcart?userid=${u.UserID}`)
          if (res.ok) {
            const rows = await res.json()
            return rows.map(r => ({ id: r.ShoppingCartID, productId: r.GoodsID, name: r.GoodsName || '', price: r.Price || (r.TotalPrice && r.Count ? +(r.TotalPrice / r.Count).toFixed(2) : 0), qty: r.Count, storeId: r.StoreID || null }))
          }
        }
      }
    } catch (e) {}
    // fallback localStorage
    await wait(100)
    return JSON.parse(localStorage.getItem('cart') || '[]')
  },
  async getUserId(username) {
    if (!username) return null
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/users`)
      if (res.ok) {
        const users = await res.json()
        const u = users.find(x => x.UserName === username || x.UserEmail === username)
        if (u) return u.UserID
      }
    } catch (e) {}
    return null
  },
  async fetchShoppingCartByUserId(userId) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/shoppingCart/list/${userId}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res.ok) {
        const payload = await res.json()
        const rows = Array.isArray(payload) ? payload : (payload && payload.data ? payload.data : [])
        return (rows || []).map(r => ({
          id: r.ShoppingCartID ?? r.shoppingCartId ?? r.id,
          productId: r.GoodsID ?? r.goodsId ?? r.goodsID,
          name: r.GoodsName ?? r.goodsName ?? r.name ?? '',
          price: r.Price ?? r.price ?? (r.TotalPrice && r.Count ? +(r.TotalPrice / r.Count).toFixed(2) : 0),
          qty: r.Count ?? r.count ?? 0,
          storeId: r.StoreID ?? r.storeId ?? r.storeID ?? null,
          storeName: r.StoreName ?? r.storeName ?? r.supplierName ?? ''
        }))
      }
    } catch (e) {}
    return []
  },
  async saveShoppingCartByUserId(userId, cart) {
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/shoppingcart`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userid: userId, items: cart })
      })
      if (res.ok) return await res.json()
    } catch (e) {}
    return { success: false }
  },
  async fetchProduct(id) {
    if (!id) return null
    // try backend
    try {
      const res = await fetchWithAuth(`${API_BASE_URL}/api/goods/${id}`)
      if (res.ok) return await res.json()
    } catch (e) {}
    // fallback: search products
    try {
      const prods = await this.fetchProducts()
      return prods.find(p => p.GoodsID == id) || null
    } catch (e) { return null }
  },
  async getGoodsNameById(id) {
    const p = await this.fetchProduct(id)
    if (!p) return null
    return p.GoodsName || p.GoodsName || p.GoodsID || null
  },
  async saveShoppingCart(username, cart) {
    // Try backend: lookup user then POST cart items
    try {
      const usersRes = await fetchWithAuth(`${API_BASE_URL}/api/users`)
      if (usersRes.ok) {
        const users = await usersRes.json()
        const u = users.find(x => x.UserName === username || x.UserEmail === username)
        if (u) {
          const res = await fetchWithAuth(`${API_BASE_URL}/api/shoppingcart`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userid: u.UserID, items: cart })
          })
          if (res.ok) return await res.json()
        }
      }
    } catch (e) {}
    // fallback: save to localStorage
    localStorage.setItem('cart', JSON.stringify(cart))
    return { success: true }
  }
  ,
  // Stores (suppliers)
  _storeCache: {},
  async fetchStores() {
    if (this._storeCache && Object.keys(this._storeCache).length) return Object.values(this._storeCache)
    return []
  },
  async fetchStore(id) {
    if (!id) return null
    if (this._storeCache && this._storeCache[id]) return this._storeCache[id]
    const all = await this.fetchStores()
    return all.find(s => s.StoreID == id) || null
  }
  ,
  // 返回给定商品的 StoreID（优先使用本地缓存的商品列表）
  async findStoreIdByGoodsId(goodsId) {
    if (!goodsId) return null
    try {
      const prods = await this.fetchProducts()
      const p = prods.find(x => x.GoodsID == goodsId)
      if (p) return p.StoreID || null
    } catch (e) {}
    return null
  }
  ,
  // AI 聊天：同步请求（非流式）
  async aiChat({ sessionId = '', userId = 0, message = '' } = {}) {
    try {
      const payload = { sessionId, userId, message }
      const res = await fetchWithAuth(`http://localhost:8080/ai/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      if (res && res.ok) {
        try { return await res.json() } catch (e) { return { success: true, rawStatus: res.status } }
      }
      const txt = res ? await res.text() : ''
      return { success: false, error: `http_${res && res.status}`, details: txt }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e && e.message }
    }
  },

  // AI 聊天（流式）：使用 fetch 流式读取后端返回的 text/event-stream 或 chunked 文本
  // onChunk 接收每次解码后的字符串片段；onDone 在完成时调用；onError 接收错误
  async aiChatStream({ sessionId = '', userId = 0, message = '' } = {}, { onChunk, onDone, onError } = {}) {
    try {
      const payload = { sessionId, userId, message }
      const res = await fetchWithAuth(`http://www.ren-demo.asia/ai/chat/stream`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
        body: JSON.stringify(payload)
      })

      // If backend returns 404 (stream endpoint not present), fall back to non-stream aiChat
      if (res && res.status === 404) {
        try {
          const fallback = await this.aiChat({ sessionId, userId, message })
          const text = (fallback && (fallback.data || fallback.reply || fallback.result || fallback.message)) || (typeof fallback === 'string' ? fallback : JSON.stringify(fallback))
          onChunk && onChunk(String(text))
          onDone && onDone()
          return
        } catch (e) {
          onError && onError(e)
          return
        }
      }

      if (!res || !res.body) {
        const txt = res ? await res.text().catch(() => '') : ''
        onError && onError(new Error('no_stream_or_empty_response: ' + txt))
        return
      }
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let done = false
      let buffer = ''
      // Read stream and parse Server-Sent Events (SSE) style messages.
      while (!done) {
        const { value, done: streamDone } = await reader.read()
        done = !!streamDone
        if (value) {
          buffer += decoder.decode(value, { stream: !done })
          // process complete SSE event blocks separated by double newlines
          let idx
          while ((idx = buffer.indexOf('\n\n')) !== -1) {
            const rawEvent = buffer.slice(0, idx).trim()
            buffer = buffer.slice(idx + 2)
            if (!rawEvent) continue
            // parse lines
            const lines = rawEvent.split(/\r?\n/)
            let eventType = null
            const dataLines = []
            for (const line of lines) {
              if (line.startsWith('event:')) {
                eventType = line.replace(/^event:\s*/i, '').trim()
              } else if (line.startsWith('data:')) {
                dataLines.push(line.replace(/^data:\s*/i, ''))
              } else {
                // fallback: treat as data
                dataLines.push(line)
              }
            }
            const dataText = dataLines.join('\n')
            // handle control event
            if (eventType && eventType.toLowerCase() === 'done') {
              try { onChunk && onChunk(dataText) } catch (e) { console.warn('aiChatStream onChunk handler error', e) }
              onDone && onDone()
              return
            }
            try { onChunk && onChunk(dataText) } catch (e) { console.warn('aiChatStream onChunk handler error', e) }
          }
        }
      }
      // if stream ended but buffer has remaining data, flush it
      if (buffer && buffer.trim()) {
        try { onChunk && onChunk(buffer.trim()) } catch (e) { console.warn('aiChatStream onChunk handler error', e) }
      }
      onDone && onDone()
    } catch (e) {
      onError && onError(e)
    }
  }
  ,
  // 清除会话历史
  async aiClearSession(sessionId) {
    if (!sessionId) return { success: false, error: 'missing_sessionId' }
    try {
      const url = `http://www.ren-demo.asia/ai/session/${encodeURIComponent(sessionId)}`
      const res = await fetchWithAuth(url, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      if (res && (res.ok || res.status === 200)) {
        try { return await res.json() } catch (e) { return { success: true, code: res.status } }
      }
      const txt = res ? await res.text().catch(() => '') : ''
      return { success: false, error: `http_${res && res.status}`, details: txt }
    } catch (e) {
      return { success: false, error: 'backend_connection_failed', details: e && e.message }
    }
  }
}