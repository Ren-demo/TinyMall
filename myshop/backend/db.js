// // Simple MySQL helper using mysql2/promise
// // Configure via env vars: MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE

// import mysql from 'mysql2/promise'

// let pool = null

// export async function initDB() {
//   if (pool) return pool
//   const host = process.env.MYSQL_HOST || '127.0.0.1'
//   const port = process.env.MYSQL_PORT || 3306
//   const user = process.env.MYSQL_USER || 'root'
//   const password = process.env.MYSQL_PASSWORD || ''
//   const database = process.env.MYSQL_DATABASE || 'my_shop'

//   pool = mysql.createPool({ host, port, user, password, database, waitForConnections: true, connectionLimit: 10, queueLimit: 0 })
//   return pool
// }

// export async function query(sql, params = []) {
//   if (!pool) await initDB()
//   const [rows] = await pool.execute(sql, params)
//   return rows
// }

// export default { initDB, query }
