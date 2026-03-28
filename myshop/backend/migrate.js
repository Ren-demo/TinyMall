// // Run: MYSQL_HOST=... MYSQL_USER=... MYSQL_PASSWORD=... MYSQL_DATABASE=... node backend/migrate.js
// import { initDB, query } from './db.js'

// async function migrate() {
//   await initDB()
//   console.log('Connected to MySQL, creating tables...')

//   // Create users
//   await query(`
//     CREATE TABLE IF NOT EXISTS users (
//       id INT AUTO_INCREMENT PRIMARY KEY,
//       email VARCHAR(255) UNIQUE,
//       username VARCHAR(255),
//       password_hash VARCHAR(255),
//       role VARCHAR(50),
//       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//   `)

//   // verification codes
//   await query(`
//     CREATE TABLE IF NOT EXISTS verification_codes (
//       id INT AUTO_INCREMENT PRIMARY KEY,
//       email VARCHAR(255),
//       code VARCHAR(32),
//       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//       INDEX(email)
//     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//   `)

//   // products
//   await query(`
//     CREATE TABLE IF NOT EXISTS products (
//       GoodsID INT PRIMARY KEY,
//       GoodsName VARCHAR(255),
//       StoreID VARCHAR(255),
//       Text TEXT,
//       Count INT,
//       Price DECIMAL(10,2),
//       Picture VARCHAR(255)
//     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//   `)

//   // orders + items
//   await query(`
//     CREATE TABLE IF NOT EXISTS orders (
//       OrderID INT AUTO_INCREMENT PRIMARY KEY,
//       UserName VARCHAR(255),
//       State INT DEFAULT 0,
//       SupplierID VARCHAR(255),
//       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
//     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//   `)
//   await query(`
//     CREATE TABLE IF NOT EXISTS order_items (
//       id INT AUTO_INCREMENT PRIMARY KEY,
//       OrderID INT,
//       GoodsID INT,
//       Count INT,
//       FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
//     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
//   `)

//   console.log('Tables created. Seeding sample data...')

//   // Seed products (if empty)
//   const existing = await query('SELECT COUNT(*) as cnt FROM products')
//   if (existing[0].cnt === 0) {
//     await query('INSERT INTO products (GoodsID,GoodsName,StoreID,Text,Count,Price,Picture) VALUES (?,?,?,?,?,?,?)', [1,'商品 A','store-1','优质商品 A',50,99.00,'/assets/prod-a.jpg'])
//     await query('INSERT INTO products (GoodsID,GoodsName,StoreID,Text,Count,Price,Picture) VALUES (?,?,?,?,?,?,?)', [2,'商品 B','store-2','热销商品 B',20,149.00,'/assets/prod-b.jpg'])
//     await query('INSERT INTO products (GoodsID,GoodsName,StoreID,Text,Count,Price,Picture) VALUES (?,?,?,?,?,?,?)', [3,'商品 C','store-1','实惠商品 C',100,29.00,'/assets/prod-c.jpg'])
//     console.log('Seeded products')
//   }

//   const orderCount = await query('SELECT COUNT(*) as cnt FROM orders')
//   if (orderCount[0].cnt === 0) {
//     await query('INSERT INTO orders (OrderID,UserName,State,SupplierID) VALUES (?,?,?,?)', [101,'alice',2,'supplier-1'])
//     await query('INSERT INTO orders (OrderID,UserName,State,SupplierID) VALUES (?,?,?,?)', [102,'bob',0,'supplier-2'])
//     await query('INSERT INTO order_items (OrderID,GoodsID,Count) VALUES (?,?,?)', [101,1,1])
//     await query('INSERT INTO order_items (OrderID,GoodsID,Count) VALUES (?,?,?)', [102,2,2])
//     console.log('Seeded orders')
//   }

//   console.log('Migration complete.')
//   process.exit(0)
// }

// migrate().catch(err => { console.error(err); process.exit(1) })
