// // Test DB connection script
// // Usage (set env vars then run):
// // MYSQL_HOST=127.0.0.1 MYSQL_USER=root MYSQL_PASSWORD=pass MYSQL_DATABASE=dianshang2 node backend/test-connection.js

// import { initDB, query } from './db.js'

// async function test() {
//   try {
//     await initDB()
//     console.log('Connected to MySQL.')
//     // check key tables from your dump
//     const tables = ['goods','`order`','shoppingcart','store','user']
//     for (const t of tables) {
//       try {
//         const r = await query(`SELECT COUNT(*) as cnt FROM ${t}`)
//         console.log(`${t}:`, r[0].cnt)
//       } catch (e) {
//         console.warn(`table ${t} not accessible or missing:`, e.message)
//       }
//     }
//     process.exit(0)
//   } catch (e) {
//     console.error('DB connection failed:', e.message)
//     process.exit(1)
//   }
// }

// test()
