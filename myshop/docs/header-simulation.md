# 模拟请求头接收器（Mock Backend）

这是一个轻量级的无依赖 Node 后端，用于接收并回显请求头与请求体，方便在前端进行完整的请求头发送/接收测试。

## 运行 mock 服务

在项目根目录运行：

```bash
node backend/mock-server.js
```

服务监听端口 `4000`，可用接口：

- GET http://localhost:4000/health  —— 健康检查
- GET http://localhost:4000/echo-headers  —— 回显请求头
- POST http://localhost:4000/echo  —— 回显请求头与请求体

（注：已设置 CORS，允许来自任意来源的测试请求）

## 在浏览器中测试

打开开发者控制台，导入并调用示例（项目中已提供 `src/services/headerClient.js`）：

```js
import { echoHeaders, postEcho } from '/src/services/headerClient.js'

// 回显头
echoHeaders().then(r => console.log('echoHeaders response', r))

// POST 并回显
postEcho({ foo: 'bar' }).then(r => console.log('postEcho response', r))
```

### 顺序与并行请求示例

项目中提供了 `src/services/headerSequence.js` 与组件 `src/components/HeaderSequenceTester.vue`，用于演示一次性运行多请求（顺序或并行）：

浏览器控制台运行：

```js
import { runSequential, runParallel } from '/src/services/headerSequence.js'
runSequential().then(r => console.log('sequential', r))
runParallel().then(r => console.log('parallel', r))
```

或者在页面中引入组件：

1. 在任意页面/组件中导入 `HeaderSequenceTester.vue` 并注册
2. 点击 “顺序请求” / “并行请求” 按钮查看回显


你也可以使用 `fetch` 或 `curl` 测试：

```bash
curl -v -H "X-Custom-Header: test" http://localhost:4000/echo-headers

curl -v -X POST -H "Content-Type: application/json" -H "X-Custom-Header: test" -d '{"a":1}' http://localhost:4000/echo
```

## Node 环境下的示例（无需额外依赖）

```js
const http = require('http')

const data = JSON.stringify({ a: 1 })

const options = {
  hostname: 'localhost',
  port: 4000,
  path: '/echo',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(data),
    'X-Custom-Header': 'node-client'
  }
}

const req = http.request(options, (res) => {
  let body = ''
  res.on('data', (chunk) => body += chunk)
  res.on('end', () => console.log('response:', body))
})
req.write(data)
req.end()
```

## 说明
- 服务尽量保持无依赖，便于快速运行与调试。
- 前端示例使用 `fetch` 发送自定义头部（如 `Authorization`、`X-Custom-Header`），服务会在返回的 JSON 中回显 `receivedHeaders` 字段。

如果你希望我：
- 添加一个更完整的 mock 登录/注册流程并校验特定请求头（比如验证 `Authorization`），或
- 将 mock 服务改为使用 `express`（需要安装依赖并修改启动脚本），
请告诉我你的偏好，我会继续实现。

## 可选：把数据存入 MySQL（本机）

已提供 `backend/db.js` 与迁移脚本 `backend/migrate.js`，可将 mock 数据迁移到你本地的 MySQL。步骤：

1. 安装依赖：

```bash
npm install mysql2
```

2. 在你的环境中设置 MySQL 连接信息（示例）：

Windows PowerShell 示例：
```powershell
$env:MYSQL_HOST = '127.0.0.1'
$env:MYSQL_PORT = '3306'
$env:MYSQL_USER = 'root'
$env:MYSQL_PASSWORD = 'yourpassword'
$env:MYSQL_DATABASE = 'my_shop'
node backend/migrate.js
```

或者 Linux/macOS：
```bash
MYSQL_HOST=127.0.0.1 MYSQL_USER=root MYSQL_PASSWORD=yourpassword MYSQL_DATABASE=my_shop node backend/migrate.js
```

3. 迁移并填充数据后，启动 mock 服务（它会尝试把收到的验证码与注册写入 MySQL）：

```bash
node backend/mock-server.js
```

注意：如果不希望立即安装 MySQL，脚本仍会在没有 DB 的情况下回退到内存/回显行为。
