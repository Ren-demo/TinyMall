README — 前端答辩说明（仅前端负责人使用）

版本：1.0
作者：前端答辩人

概述
-	目的：为答辩中前端负责人准备一份专业、可演示且面向评委的 README，重点覆盖“用户界面（User）”、“中间商界面（Middle）”以及前端与真实后端的通信细节。
-	范围：仅说明前端部分的实现、运行、演示与对外 API 协议；忽略项目内的模拟后端（mock-server），因为答辩使用真实后端。

关键文件与位置
-	前端入口：`src/main.js`
-	路由：`src/router/index.js`（按角色隔离路由）
-	状态管理：`src/stores/auth.js`、`src/stores/cart.js`（使用 Pinia）
-	API 层：`src/services/api.js`（包含 `fetchWithAuth` 与 `API_BASE_URL`）
-	页面/组件：
  - 用户相关：`src/views/user/*`、`src/components/Cart.vue` 等
  - 中间商相关：`src/views/middle/*`、`src/components/MiddleNav.vue` 等

答辩重点（对评委要说的专业点）
-	职责范围：我负责前端展示层与业务流的实现，包括路由控制、状态管理、与后端 API 的交互、UI 交互/可用性与演示脚本。
-	架构简介：前端为单页应用（SPA），使用 Vue 3 + Vite；组件化设计分离视图（views）与可复用组件（components）；数据通过服务层（`src/services/api.js`）与后端通信，应用级状态由 Pinia 管理。
-	安全与鉴权：前端通过 `Authorization` 头携带 token（`sessionStorage`/`localStorage`），受限路由在 `router` 中通过 `meta.requiresAuth` 校验并在 `src/stores/auth.js` 中保存用户角色信息。

用户界面（User）说明 — 演示要点
-	目标：展示用户从浏览商品到下单的完整体验（浏览、加入购物车、提交订单、查看订单状态）。
-	关键组件/页面：`ProductList.vue`（商品列表）、`Cart.vue`（购物车）、`OrderCheckout`（结算页）、`MyOrders.vue`（订单列表）。
-	交互流程（高层次）：
  1. 商品列表通过 GET `/api/products` 获取数据并渲染。
  2. 加入购物车在本地状态（Pinia）更新，并可选择同步到后端 POST `/api/cart`（如需求）。
  3. 提交订单向 POST `/api/orders` 发送订单信息（用户ID、商品ID、数量、地址等），等待后端返回订单号与状态。
  4. 查询订单使用 GET `/api/orders/:orderId` 或 GET `/api/orders?userId=...` 获取最新状态并更新界面。
-	可展示的专业术语：组件复用、虚拟 DOM 渲染优化、乐观更新（optimistic UI）与回滚策略（如用在加入购物车时）。

中间商界面（Middle）说明 — 演示要点（重点）
-	目标：展示中间商如何管理/分配订单、添加供应商并跟踪发货状态。
-	关键组件/页面：`MiddleDashboard.vue`、`MiddleOrders.vue`、`MiddleAddSupplier.vue`。
-	典型业务流：
  1. 中间商查询待处理订单：GET `/api/middle/orders?status=pending`。
  2. 查看订单详情并选择供应商：GET `/api/suppliers` 获取候选供应商列表，然后 POST `/api/middle/orders/:orderId/assign` 提交分配请求。
  3. 分配成功后，后端返回更新后的订单对象，前端更新对应订单状态并通知界面（状态：`assigned` -> `shipped` -> `completed`）。
-	关键实现要点（专业表达）：
  - 使用幂等 API 设计避免重复分配（接口返回明确的状态码与幂等 token）。
  - 前端在分配操作上采用事务感知（确认弹窗 + loading 状态 + 错误回退）。
  - 对长时间操作使用后端异步处理反馈（返回任务 ID 并轮询或通过 WebSocket 推送）。

前端 — 与后端的数据交互（专业/API 合约示例）
- 总原则：前端作为 API 客户端，仅关注契约（contract）；契约通过 HTTP + JSON 明确字段与状态码。

示例接口（示意，答辩时须与后端确认最终契约）：
1) 获取商品列表
  - 请求：`GET /api/products?page=1&pageSize=20`
  - 返回（200）：
```json
{
  "total": 123,
  "page": 1,
  "pageSize": 20,
  "items": [ { "id": 1, "name": "商品A", "price": 99.9, "stock": 20, "picture": "..." } ]
}
```
2) 创建订单
  - 请求：`POST /api/orders`
  - 请求体（JSON）：
```json
{
  "userId": 1001,
  "items": [ { "productId": 1, "quantity": 2 } ],
  "address": "...",
  "paymentMethod": "online"
}
```
  - 返回（201）：
```json
{ "orderId": 98765, "status": "created", "createdAt": "2026-03-25T..." }
```
3) 中间商分配订单
  - 请求：`POST /api/middle/orders/98765/assign`
  - 请求体：`{ "supplierId": 3002 }`
  - 返回（200）：返回更新后的订单对象，包含 `assignedTo` 与 `status` 字段。

前端实现细节（实现者可提及的专业点）
-	错误处理：对 4xx、5xx 返回做统一处理，常见做法是 `api.js` 返回标准错误对象并在 UI 层以通知/弹窗显示。
-	重试与幂等：对可能超时的写操作（如分配）在客户端实现指数退避重试或提醒人工重试；服务端应支持幂等。
-	并发控制：订单状态变化可能并发发生，前端在更新前应读取最新版本号（或 ETag）并在冲突时提示用户。
-	性能：列表型接口使用分页与后端筛选，前端避免一次性加载全部数据。

运行与演示准备（仅前端）
-	安装依赖：`npm install`
-	运行开发服务器：`npm run dev`
-	环境变量：请将真实后端地址写入 `.env`（或 `VITE_API_BASE_URL`）并在 `src/services/api.js` 中确认 `API_BASE_URL` 指向真实后端。示例：
```
VITE_API_BASE_URL=https://api.your-backend.example.com
```
-	忽略 mock：答辩时请确保 `VITE_DISABLE_AUTH_HEADER`（或项目中对应的开关）正常配置，且未启用 `mock-server`。说明：本 README 假设真实后端可用并已对接。

演示检查清单（答辩前 10 项快速检查）
-	确认三类演示账号：用户 / 中间商 / 供应商（账号密码在答辩前准备）
-	确认 `VITE_API_BASE_URL` 指向真实后端并能连通
-	启动 `npm run dev` 并验证首页可正常加载数据
-	演示顺序准备好（先用户下单，再中间商分配）
-	准备备份截图与 2 分钟录屏（万一现场网络不可用）
-	在浏览器调试工具中准备 Network 面板以便必要时展示请求/响应
-	关闭无关通知与标签页，保证演示流畅

答辩可能被问到的技术问题（及建议回答要点）
-	“前端如何保证界面与后端状态一致？” → 使用接口确认、乐观更新配合后端回滚、定期/按需拉取最新状态；关键操作等待后端返回后再进入下一步。
-	“如何处理高并发导致的库存超卖？” → 后端为主负责库存一致性（事务或分布式锁），前端可通过库存校验与快速反馈减少并发冲突。
-	“为何将状态放在 Pinia，而不是组件内？” → 因为订单、购物车、鉴权等数据是跨页面共享的，Pinia 能提供集中式状态管理与一致的读写接口，便于维护与调试。

补充说明（给评委的专业声明）
-	本次答辩仅负责前端交付：UI/UX、路由、状态管理、API 对接、演示脚本与容错处理。后端逻辑（数据库、业务规则、通知、队列等）由后端组负责，答辩现场假设后端接口契约已就绪。

后续（我可以为你生成）
-	PPT 文案（逐页标题+要点+台词）
-	演示脚本精简版（按时间轴）
-	把本 README 中的 API 示例替换为后端团队的真实接口契约（需要后端文档）

漂浮装饰图（实现原理与要点）
--------------------------------
- 目的：在演示页面两侧加入可拖拽、自动漂浮且带惯性与边界反弹的装饰元素，提高界面趣味性且不影响主要交互。
- 主要实现点（前端可在答辩中简短说明）：
  - 布局：两个固定定位元素（fixed）分别放在页面左右，通过 CSS `transform: translate(x,y)` 控制位置，避免影响文档流。
  - 自动漂浮动画：内部容器使用 CSS keyframes（示例名 `floatY`），在不改变外层 `translate` 的前提下做上下微幅位移动画，呈现“悬浮”效果。
  - 拖拽：使用 Pointer Events（`pointerdown`/`pointermove`/`pointerup`）来实现拖拽，拖拽时停止内部动画的惯性，直接更新外层 translate 值以提高性能。
  - 惯性（抛掷效果）：在拖拽过程中采样最近若干个指针位置（如 6 个样本、时间窗约 200ms），在释放时用位移 / 时间计算像素/秒的初速度（vx, vy），用 requestAnimationFrame 根据指数衰减（v *= exp(-k*dt)）不断积分位置，使元素缓慢减速移动。
  - 边界与反弹：计算可允许的 x/y 范围（基于视口宽高与元素尺寸），在运动过程中检测越界并反转相应速度分量同时乘以阻尼系数（如 0.5~0.7），形成弹性反弹效果。
  - 大小与裁切：将图片尺寸限制在容器大小（如 88x88），并用 `object-fit: cover` 与圆角来保持外观一致，避免超出被裁切或遮挡页面要素。
  - 首次位置与自适应：在组件挂载时基于视口计算初始坐标（靠近左右下方并留内边距），并在窗口 `resize` 时用同一边界函数 clamp 位置，保证不同分辨率下显示合理；拖拽后的用户位置会被保留，不会被初始化重置。
  - 资产加载：为了演示稳定性，建议将装饰图片放在本地 `public/assets/` 下（项目已提供下载脚本），避免外网资源不可用影响答辩。

演示要点（现场讲解建议）
- 简短说明："这是一个演示性UI元素，用 CSS 动画提供微漂浮感，支持拖拽与惯性，内部保持与页面主要交互隔离，不会阻塞用户操作。"
- 如演示惯性，可在右侧拖拽并快速释放，展示抛掷与反弹效果，同时打开浏览器 Network/Console 以示运行在前端（无额外请求）。
- 如讲技术细节，可提及使用 Pointer Events、requestAnimationFrame、指数衰减模型与阻尼反弹的实现要点。


