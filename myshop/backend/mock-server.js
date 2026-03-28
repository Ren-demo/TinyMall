// // Simple mock backend (ES module) to receive and echo request headers and body
// // Run: node backend/mock-server.js

// import http from 'http'
// import { URL } from 'url'
// import { initDB, query } from './db.js'

// const PORT = 4000

// function sendJSON(res, obj) {
//   const s = JSON.stringify(obj, null, 2)
//   res.setHeader('Content-Type', 'application/json')
//   res.setHeader('Content-Length', Buffer.byteLength(s))
//   res.end(s)
// }

// // Robust logger: stringify objects and write to stdout to avoid environment
// // issues when logging complex values.
// function writeLog(label, obj) {
//   try {
//     if (obj === undefined) {
//       process.stdout.write(String(label) + '\n')
//     } else {
//       let s
//       try { s = typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2) } catch (e) { s = String(obj) }
//       process.stdout.write(String(label) + ' ' + s + '\n')
//     }
//   } catch (e) {
//     // best-effort: fallback to console.error
//     try { console.error(label, obj) } catch (_) {}
//   }
// }

// const server = http.createServer((req, res) => {
//   // CORS
//   res.setHeader('Access-Control-Allow-Origin', '*')
//   res.setHeader('Access-Control-Allow-Methods', 'GET,POST,OPTIONS')
//   // allow custom client headers used by frontend (e.g. X-Client-Action)
//   res.setHeader('Access-Control-Allow-Headers', 'Content-Type,Authorization,token,X-Token,X-Custom-Header,X-Client-Action')

//   if (req.method === 'OPTIONS') {
//     res.statusCode = 204
//     return res.end()
//   }

//   // Parse URL using WHATWG URL
//   const host = req.headers.host || `localhost:${PORT}`
//   const fullUrl = new URL(req.url, `http://${host}`)
//   const pathname = fullUrl.pathname

//   // Collect body if any
//   let body = ''
//   req.on('data', (chunk) => { body += chunk })
//   req.on('end', async () => {
//     writeLog(`[mock-server] incoming: ${req.method} ${req.url}`)
//     let parsedBody = null
//     const contentType = (req.headers['content-type'] || '').split(';')[0]
//     if (body) {
//       try {
//         if (contentType === 'application/json') parsedBody = JSON.parse(body)
//         else parsedBody = body
//       } catch (e) { parsedBody = body }
//     }

//     // Routes
//     // Login: 验证用户名/邮箱 + 密码（尝试使用 DB）
//     if (req.method === 'POST' && pathname === '/api/login') {
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         const username = body.username || body.email || ''
//         const password = body.password || ''
//         const rows = await query('SELECT UserID,UserName,UserPwd,UserEmail FROM user WHERE UserName = ? OR UserEmail = ? LIMIT 1', [username, username])
//         if (rows && rows.length) {
//           const u = rows[0]
//           if ((u.UserPwd || '') === password) {
//             const token = 'db-token-user-' + u.UserID
//             res.statusCode = 200
//             return sendJSON(res, { success: true, token, role: 'user', username: u.UserName })
//           } else {
//             res.statusCode = 200
//             return sendJSON(res, { success: false, error: 'invalid_credentials' })
//           }
//         } else {
//           // user not found
//           res.statusCode = 200
//           return sendJSON(res, { success: false, error: 'user_not_found' })
//         }
//       } catch (e) {
//         console.warn('/api/login DB check failed, falling back to mock:', e.message)
//         // fallback: accept any username/password for development
//         const body = parsedBody || {}
//         const id = body.username || body.email || 'guest'
//         return sendJSON(res, { success: true, token: 'mock-token-user-' + id, role: body.role || 'user', username: body.username || body.email })
//       }
//     }
//     // Simple REST endpoints to expose DB data to frontend
//     if (req.method === 'GET' && pathname === '/api/goods') {
//       try {
//         await initDB()
//         const rows = await query('SELECT GoodsID,GoodsName,StoreID,Text,Count,Price,Picture FROM goods')
//         res.statusCode = 200
//         return sendJSON(res, rows)
//       } catch (e) {
//         console.warn('DB /api/goods failed, falling back to echo:', e.message)
//       }
//     }
//     // GET /api/goods/:id
//     if (req.method === 'GET' && pathname.startsWith('/api/goods')) {
//       try {
//         await initDB()
//         const parts = pathname.split('/')
//         const id = parts[3]
//         if (id) {
//           const rows = await query('SELECT GoodsID,GoodsName,StoreID,Text,Count,Price,Picture FROM goods WHERE GoodsID = ? LIMIT 1', [id])
//           res.statusCode = 200
//           return sendJSON(res, rows[0] || {})
//         }
//       } catch (e) {
//         console.warn('DB /api/goods/:id failed:', e && e.message)
//       }
//     }

//     if (req.method === 'GET' && pathname === '/api/orders') {
//       // Return grouped orders with items and related info (goods, store, user)
//       try {
//         await initDB()
//         const username = fullUrl.searchParams.get('username')
//         let rows
//         const supplierIdQ = fullUrl.searchParams.get('supplierId')
//         if (username) {
//           const u = await query('SELECT UserID,UserName,UserEmail FROM user WHERE UserName = ? OR UserEmail = ? LIMIT 1', [username, username])
//           if (!u || !u.length) return sendJSON(res, [])
//           const uid = u[0].UserID
//           rows = await query(`SELECT o.OrderID,o.UserID,o.StoreID,o.GoodsID,o.Count,o.Price,o.Time,o.State,
//                                     g.GoodsName,g.Picture as GoodsPicture,
//                                     s.StoreName,s.StoreEmail,
//                                     u.UserName,u.UserEmail
//                              FROM \`order\` o
//                              LEFT JOIN goods g ON o.GoodsID = g.GoodsID
//                              LEFT JOIN store s ON o.StoreID = s.StoreID
//                              LEFT JOIN user u ON o.UserID = u.UserID
//                              WHERE o.UserID = ?
//                              ORDER BY o.Time DESC`, [uid])
//         } else if (supplierIdQ) {
//           // supplier-specific view
//           rows = await query(`SELECT o.OrderID,o.UserID,o.StoreID,o.GoodsID,o.Count,o.Price,o.Time,o.State,
//                                     g.GoodsName,g.Picture as GoodsPicture,
//                                     s.StoreName,s.StoreEmail,
//                                     u.UserName,u.UserEmail
//                              FROM \`order\` o
//                              LEFT JOIN goods g ON o.GoodsID = g.GoodsID
//                              LEFT JOIN store s ON o.StoreID = s.StoreID
//                              LEFT JOIN user u ON o.UserID = u.UserID
//                              WHERE o.StoreID = ? AND o.State = 1
//                              ORDER BY o.Time DESC`, [supplierIdQ])
//         } else {
//           rows = await query(`SELECT o.OrderID,o.UserID,o.StoreID,o.GoodsID,o.Count,o.Price,o.Time,o.State,
//                                     g.GoodsName,g.Picture as GoodsPicture,
//                                     s.StoreName,s.StoreEmail,
//                                     u.UserName,u.UserEmail
//                              FROM \`order\` o
//                              LEFT JOIN goods g ON o.GoodsID = g.GoodsID
//                              LEFT JOIN store s ON o.StoreID = s.StoreID
//                              LEFT JOIN user u ON o.UserID = u.UserID
//                              ORDER BY o.Time DESC`)
//         }

//         // group by OrderID
//         const groups = {}
//         for (const r of rows) {
//           const oid = r.OrderID
//           if (!groups[oid]) {
//             groups[oid] = {
//               id: oid,
//               user: r.UserEmail || r.UserName || null,
//               supplierId: r.StoreID,
//               supplierName: r.StoreName || null,
//               time: r.Time,
//               state: r.State,
//               items: []
//             }
//           }
//           groups[oid].items.push({ productId: r.GoodsID, name: r.GoodsName, qty: r.Count, price: r.Price, picture: r.GoodsPicture, storeId: r.StoreID, storeName: r.StoreName })
//         }

//         const orders = Object.values(groups)
//         res.statusCode = 200
//         return sendJSON(res, orders)
//       } catch (e) {
//         console.warn('DB /api/orders failed, falling back to echo:', e && e.message)
//       }
//     }

//     if (req.method === 'POST' && pathname === '/api/orders') {
//       // body: { username: 'alice', items: [ { productId, qty, storeId? } ] }
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         const username = body.username || body.user || ''
//         const items = Array.isArray(body.items) ? body.items : []
//         if (!username) return sendJSON(res, { success: false, error: 'missing_username' })

//         // find user id if possible
//         let uid = null
//         try {
//           const uu = await query('SELECT UserID FROM user WHERE UserName = ? OR UserEmail = ? LIMIT 1', [username, username])
//           if (uu && uu.length) uid = uu[0].UserID
//         } catch (e) {}

//         const created = []
//         for (const it of items) {
//           const goodsId = it.productId || it.GoodsID || null
//           let storeId = it.storeId || null
//           if (!storeId && goodsId) {
//             try {
//               const g = await query('SELECT StoreID, Price FROM goods WHERE GoodsID = ? LIMIT 1', [goodsId])
//               if (g && g.length) {
//                 storeId = g[0].StoreID || null
//                 // prefer goods price if available
//                 if (!it.price) it.price = g[0].Price
//               }
//             } catch (e) {}
//           }
//           // generate next OrderID based on existing `order` table
//           const r = await query('SELECT COALESCE(MAX(OrderID), 100) as m FROM `order`')
//           const nextId = (r && r[0] && r[0].m ? r[0].m : 100) + 1

//           const count = it.qty || it.Count || 0
//           const price = it.price || 0
//           // insert into `order` table (one row per item in this schema)
//           await query('INSERT INTO `order` (OrderID, UserID, StoreID, GoodsID, Count, Price, Time, State) VALUES (?,?,?,?,?,?,NOW(),?)', [nextId, uid, storeId, goodsId, count, price, 0])
//           created.push({ OrderID: nextId, UserID: uid, StoreID: storeId, GoodsID: goodsId })
//         }

//         res.statusCode = 200
//         return sendJSON(res, { success: true, orders: created })
//       } catch (e) {
//         console.warn('DB POST /api/orders failed:', e && e.message)
//         return sendJSON(res, { success: false, error: e && e.message })
//       }
//     }

//     if (req.method === 'GET' && pathname === '/api/shoppingcart') {
//       try {
//         await initDB()
//         const userId = fullUrl.searchParams.get('userid')
//         if (userId) {
//           const rows = await query('SELECT sc.ShoppingCartID, sc.UserID, sc.StoreID, sc.GoodsID, sc.Count, sc.TotalPrice, g.GoodsName, g.Price FROM shoppingcart sc LEFT JOIN goods g ON sc.GoodsID = g.GoodsID WHERE sc.UserID = ?', [userId])
//           res.statusCode = 200
//           return sendJSON(res, rows)
//         }
//         const rows = await query('SELECT sc.ShoppingCartID, sc.UserID, sc.StoreID, sc.GoodsID, sc.Count, sc.TotalPrice, g.GoodsName, g.Price FROM shoppingcart sc LEFT JOIN goods g ON sc.GoodsID = g.GoodsID')
//         res.statusCode = 200
//         return sendJSON(res, rows)
//       } catch (e) {
//         console.warn('DB /api/shoppingcart failed, falling back to echo:', e.message)
//       }
//     }

//     if (req.method === 'POST' && pathname === '/api/shoppingcart') {
//       // body: { userid: number, items: [ { id?, productId, name, price, qty, storeId? } ] }
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         writeLog('[mock-server] POST /api/shoppingcart body:', body)
//         let uid = body.userid || null
//         const items = Array.isArray(body.items) ? body.items : []
//         // fallback: accept username in body and resolve to userid
//         if (!uid && body.username) {
//           try {
//             const uu = await query('SELECT UserID FROM user WHERE UserName = ? OR UserEmail = ? LIMIT 1', [body.username, body.username])
//             if (uu && uu.length) uid = uu[0].UserID
//           } catch (e) { writeLog('[mock-server] failed to resolve username to userid', { username: body.username, err: e && e.message }) }
//         }
//         if (!uid) return sendJSON(res, { success: false, error: 'missing_userid' })

//         // Upsert each item: if `id` provided update by ShoppingCartID; otherwise try to update by UserID+GoodsID; if not found insert.
//         for (const it of items) {
//           const storeId = it.storeId || 0
//           const goodsId = it.productId || it.GoodsID || 0
//           const count = it.qty || it.Count || 0
//           const total = (it.price || 0) * count

//           if (it.id) {
//             // update by ShoppingCartID
//             await query('UPDATE shoppingcart SET StoreID = ?, GoodsID = ?, Count = ?, TotalPrice = ? WHERE ShoppingCartID = ?', [storeId, goodsId, count, total, it.id])
//             continue
//           }

//           // try find existing row by user+goods
//           const existing = await query('SELECT ShoppingCartID FROM shoppingcart WHERE UserID = ? AND GoodsID = ? LIMIT 1', [uid, goodsId])
//           if (existing && existing.length) {
//             await query('UPDATE shoppingcart SET Count = ?, TotalPrice = ? WHERE ShoppingCartID = ?', [count, total, existing[0].ShoppingCartID])
//           } else {
//             await query('INSERT INTO shoppingcart (UserID, StoreID, GoodsID, Count, TotalPrice) VALUES (?,?,?,?,?)', [uid, storeId, goodsId, count, total])
//           }
//         }

//         // Optionally remove rows not in new list: compute goods list and delete others
//         const newGoods = items.map(it => it.productId || it.GoodsID).filter(Boolean)
//         let delResult = null
//         if (newGoods.length) {
//           const placeholders = newGoods.map(() => '?').join(',')
//           delResult = await query(`DELETE FROM shoppingcart WHERE UserID = ? AND GoodsID NOT IN (${placeholders})`, [uid, ...newGoods])
//           writeLog('[mock-server] DELETE shoppingcart not in newGoods result:', { uid, newGoods, delResult })
//         } else {
//           // If client provided an empty list, remove all shoppingcart rows for this user
//           try {
//             delResult = await query('DELETE FROM shoppingcart WHERE UserID = ?', [uid])
//             writeLog(`[mock-server] cleared shoppingcart for user ${uid}`, delResult)
//           } catch (e) {
//             writeLog('[mock-server] failed to clear shoppingcart for user', { uid, err: e && e.message })
//           }
//         }

//         // return remaining rows for debugging
//         try {
//           const remaining = await query('SELECT * FROM shoppingcart WHERE UserID = ?', [uid])
//           return sendJSON(res, { success: true, deleted: delResult, remaining })
//         } catch (e) {
//           return sendJSON(res, { success: true, deleted: delResult })
//         }
//       } catch (e) {
//         console.warn('DB POST /api/shoppingcart failed:', e.message)
//         return sendJSON(res, { success: false, error: e.message })
//       }
//     }

//     if (req.method === 'GET' && pathname.startsWith('/api/store')) {
//       try {
//         await initDB()
//         const parts = pathname.split('/')
//         const id = parts[3]
//         if (id) {
//           const rows = await query('SELECT * FROM store WHERE StoreID = ? LIMIT 1', [id])
//           res.statusCode = 200
//           return sendJSON(res, rows[0] || {})
//         }
//         const rows = await query('SELECT * FROM store')
//         res.statusCode = 200
//         return sendJSON(res, rows)
//       } catch (e) {
//         console.warn('DB /api/store failed, falling back to echo:', e.message)
//       }
//     }

//     if (req.method === 'GET' && pathname === '/api/users') {
//       try {
//         await initDB()
//         const rows = await query('SELECT UserID,UserName,UserEmail,UserPicture FROM user')
//         res.statusCode = 200
//         return sendJSON(res, rows)
//       } catch (e) {
//         console.warn('DB /api/users failed, falling back to echo:', e.message)
//       }
//     }

//     if (req.method === 'GET' && pathname === '/echo-headers') {
//       // Echo back request headers
//       const resp = { receivedHeaders: req.headers }
//       writeLog('[mock-server] GET /echo-headers headers:', req.headers)
//       res.statusCode = 200
//       return sendJSON(res, resp)
//     }

//     if ((req.method === 'POST' || req.method === 'GET') && pathname === '/echo') {
//       const resp = {
//         method: req.method,
//         url: req.url,
//         receivedHeaders: req.headers,
//         body: parsedBody
//       }
//       writeLog('[mock-server] /echo', resp)

//       // If DB available and action indicates, persist verification code or user
//       (async () => {
//         try {
//           await initDB()
//           if (parsedBody && parsedBody.action === 'sendVerificationCode' && parsedBody.email && parsedBody.code) {
//             await query('INSERT INTO verification_codes (email, code) VALUES (?, ?)', [parsedBody.email, parsedBody.code])
//             writeLog('[mock-server] stored verification code in DB for', parsedBody.email)
//           }
//           if (parsedBody && parsedBody.action === 'register' && parsedBody.email && parsedBody.username) {
//             await query('INSERT INTO users (email, username, role) VALUES (?, ?, ?)', [parsedBody.email, parsedBody.username, 'user'])
//             writeLog('[mock-server] stored user in DB', parsedBody.email)
//           }
//         } catch (e) {
//           // it's ok if DB isn't configured; keep echo behavior
//         }
//       })()

//       res.statusCode = 200
//       return sendJSON(res, resp)
//     }

//     // simple health
//     if (req.method === 'GET' && pathname === '/health') {
//       res.statusCode = 200
//       return sendJSON(res, { ok: true, time: new Date().toISOString() })
//     }

//     // Update order state
//     if (req.method === 'POST' && pathname === '/api/orders/state') {
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         const orderId = body.orderId || body.OrderID || null
//         let state = body.state
//         const status = body.status || body.action || null
//         if (!orderId) return sendJSON(res, { success: false, error: 'missing_orderId' })

//         // map common Chinese status strings to numeric state
//         if (state === undefined || state === null) {
//           if (typeof status === 'string') {
//             const map = { '已发货': 2, '已确认': 1, '已接受': 1, '拒绝': -1, '待处理': 0, '已提交': 0 }
//             state = map[status] !== undefined ? map[status] : null
//           }
//         }
//         if (state === null || state === undefined) return sendJSON(res, { success: false, error: 'missing_state' })
//         await query('UPDATE `order` SET State = ? WHERE OrderID = ?', [state, orderId])
//         return sendJSON(res, { success: true })
//       } catch (e) {
//         console.warn('DB POST /api/orders/state failed:', e && e.message)
//         return sendJSON(res, { success: false, error: e && e.message })
//       }
//     }

//     // POST /api/orders/send  -> middleman sends order to suppliers determined by goods' StoreID
//     if (req.method === 'POST' && pathname === '/api/orders/send') {
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         const orderId = body.orderId || body.OrderID || null
//         if (!orderId) return sendJSON(res, { success: false, error: 'missing_orderId' })

//         // find all rows for this OrderID
//         const rows = await query('SELECT OrderID, StoreID, GoodsID, Count, State FROM `order` WHERE OrderID = ?', [orderId])
//         if (!rows || !rows.length) return sendJSON(res, { success: false, error: 'order_not_found' })

//         const supplierMap = new Map()
//         for (const r of rows) {
//           const sid = r.StoreID || null
//           if (!sid) continue
//           if (!supplierMap.has(sid)) supplierMap.set(sid, [])
//           supplierMap.get(sid).push(r)
//         }

//         const suppliers = []
//         const inventoryChanges = []

//         // For each supplier, only process rows whose State is currently 0 (unprocessed)
//         for (const [sid, items] of supplierMap.entries()) {
//           // collect only items that are still in State=0 to avoid double-decrement
//           const toProcess = items.filter(it => it.State === 0 || it.State === '0')
//           if (!toProcess.length) continue

//           // decrement inventory for each goods row
//           for (const it of toProcess) {
//             try {
//               const dec = Math.max(0, Number(it.Count) || 0)
//               if (dec > 0) {
//                 await query('UPDATE goods SET Count = GREATEST(0, Count - ?) WHERE GoodsID = ?', [dec, it.GoodsID])
//                 inventoryChanges.push({ storeId: sid, goodsId: it.GoodsID, deducted: dec })
//               }
//             } catch (e) {
//               // log and continue
//               writeLog('[mock-server] inventory update failed for', { orderId, storeId: sid, goodsId: it.GoodsID, err: e && e.message })
//             }
//           }

//           // mark these order rows as accepted (1)
//           await query('UPDATE `order` SET State = ? WHERE OrderID = ? AND StoreID = ? AND State = ?', [1, orderId, sid, 0])
//           suppliers.push(sid)
//         }

//         // log inventory changes for debugging
//         writeLog('[mock-server] send order inventoryChanges:', { orderId, suppliers, inventoryChanges })
//         res.statusCode = 200
//         return sendJSON(res, { success: true, suppliers, inventoryChanges })
//       } catch (e) {
//         console.warn('DB POST /api/orders/send failed:', e && e.message)
//         return sendJSON(res, { success: false, error: e && e.message })
//       }
//     }

//     // POST /api/orders/ship -> supplier marks shipped for their StoreID
//     if (req.method === 'POST' && pathname === '/api/orders/ship') {
//       try {
//         await initDB()
//         const body = parsedBody || {}
//         const orderId = body.orderId || body.OrderID || null
//         const supplierId = body.supplierId || body.storeId || null
//         if (!orderId || !supplierId) return sendJSON(res, { success: false, error: 'missing_orderId_or_supplierId' })

//         const result = await query('UPDATE `order` SET State = ? WHERE OrderID = ? AND StoreID = ?', [2, orderId, supplierId])
//         return sendJSON(res, { success: true, changed: result && result.affectedRows ? result.affectedRows : 0 })
//       } catch (e) {
//         console.warn('DB POST /api/orders/ship failed:', e && e.message)
//         return sendJSON(res, { success: false, error: e && e.message })
//       }
//     }

//     // not found
//     res.statusCode = 404
//     sendJSON(res, { error: 'Not found' })
//   })
// })

//   server.listen(PORT, () => {
//   writeLog(`Mock header server listening on http://localhost:${PORT}`)
//   writeLog('Endpoints: GET /echo-headers  POST /echo  GET /health')
// })

// server.on('error', (err) => {
//   console.error('[mock-server] server error', err)
// })

// process.on('uncaughtException', (err) => {
//   console.error('[mock-server] uncaughtException', err)
// })

// process.on('unhandledRejection', (reason) => {
//   console.error('[mock-server] unhandledRejection', reason)
// })
