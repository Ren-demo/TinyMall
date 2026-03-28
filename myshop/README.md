# TinyMall 前端 — 项目说明（面向答辩的完整文档）

版本与作者
- 项目名称：TinyMall（演示用电商前端）
- 语言/框架：Vue 3（Composition API） + Vite + Pinia + Vue Router
- 版本：代码库内 package.json 所示（请以 commit 为准）

目的与功能定位
- 本项目为教学/演示用途，展示一个多角色电商前端的实现：普通用户、平台中间商、供应商。
- 支持的主要功能：商品浏览、商品详情、购物车、下单、订单管理（用户/中间商/供应商）、库存管理、商品图片上传与编辑。

一、运行环境与先决条件
- 推荐 Node.js 版本：16 及以上（建议使用 Node 18+）。
- 依赖管理：npm（项目使用 npm 脚本）。
- 端口说明：开发服务器默认使用 3000；Mock 后端默认使用 4000（如启用）。

二、快速启动（本地开发）
1. 克隆仓库并进入目录：
```bash
git clone <repo> && cd my-shop
```
2. 安装依赖：
```bash
npm install
```
3. 启动开发模式（Vite dev server）：
```bash
npm run dev
```
4. 可选：启动本地 Mock 后端（便于无后端环境下开发）：
```bash
node backend/mock-server.js
```
Mock 服务通常监听 http://localhost:4000，支持简单的 REST 路由与 DB 回退（查看 [backend/mock-server.js](backend/mock-server.js)）。

三、环境变量
- 前端通过 Vite 环境变量读取后端基地址：`VITE_API_BASE_URL`（推荐在生产环境设置）。
- 在项目根创建 `.env.production` 或 `.env`，示例：
```
VITE_API_BASE_URL=http://localhost:4000
VITE_DISABLE_AUTH_HEADER=false
```
- 注意：Vite env 以 `VITE_` 前缀暴露给浏览器。

四、构建与预览（生产包）
- 构建命令：
```bash
npm run build
```
- 预览构建产物：
```bash
npm run preview
```
- 构建产物位于 `dist/`，可直接部署到静态服务器（Nginx、GitHub Pages、Netlify、Vercel 等）。

五、项目架构与关键模块说明
- 入口与启动：
	- `src/main.js`：应用入口，注册 Pinia 与 Router，挂载到 `#app`。
- 路由与权限：
	- `src/router/index.js`：路由定义与全局守卫，负责权限判断和供应商公开入口（`/supplier/:supplierId`）。
		- 关键点：中间商入口 `/middle` 为公开入口；供应商入口通过会话级 token 模拟供应商登录。
- 认证状态：
	- `src/stores/auth.js`：使用 Pinia 管理认证状态，优先读取 `sessionStorage`（用于临时会话），并同步到 `localStorage` 用于持久登录。
		- `isLoggedIn` 的判断基于 token 是否存在。
- API 层：
	- `src/services/api.js`：统一封装所有后台请求，包含：
		- `fetchWithAuth(url, options)`：会自动从 `sessionStorage`/`localStorage` 注入 `token` 请求头（除非 `VITE_DISABLE_AUTH_HEADER=true`）。
		- 针对商品、订单、购物车、评论等功能提供了封装函数（例如 `fetchProducts`, `updateGoods`, `updateGoodsPic` 等）。
		- 实现了“优先调用真实后端，失败时回退到内置 mock 数据”的策略，便于离线开发。
- 视图层：
	- `src/views/user/ProductList.vue`：商品浏览与详情弹窗（Detail Modal 使用 teleport，注意样式作用域）。
	- `src/views/enterprise/Inventory.vue`：企业/供应商库存管理（CRUD 操作，调用 `api.addGoods`/`api.updateGoods`/`api.updateGoodsPic`）。

六、技术构成与模块职责（详解）

以下按“模块 → 关键文件 → 作用”格式描述，便于答辩时逐一讲解实现细节。

- 入口与运行时
	- `src/main.js`：应用入口，创建 Vue 应用、挂载 Pinia 与 Router，并在 `router.isReady()` 后挂载到 DOM，便于调试初始重定向问题。
	- `index.html`：应用根模板，挂载点 `#app`。

- 路由与导航控制
	- `src/router/index.js`：路由表与全局守卫实现。
		- 负责：路由定义、权限（`meta.requiresAuth` / `meta.allowedRoles`）检查、供应商公开入口（`meta.supplierPublic`）的会话级处理、以及对 `/middle` 的公开访问策略。
		- 答辩点：说明如何通过守卫区分公开页面、受限页面与供应商的临时登录流程。

- 认证与会话管理
	- `src/stores/auth.js`（Pinia store）：
		- 管理 token、role、username、userId、userPicture 等状态。
		- 同步策略：优先读取 `sessionStorage`（临时会话），并写入 `localStorage` 作为持久登录；提供 `login()` 与 `logout()` 方法。
		- 答辩点：解释为何采用 session 优先（供应商临时会话隔离）以及对持久登录的处理细节。

- API 层（核心）
	- `src/services/api.js`：本项目最关键的适配层，职责详列：
		- `API_BASE_URL`：通过 `import.meta.env.VITE_API_BASE_URL` 配置，可为空（相对路径）或指向后端地址。
		- `fetchWithAuth(url, options)`：统一注入 `token`（从 session/local 读取）到请求头；封装 fetch，便于统一拦截与调试。
		- Mock 回退策略：在真实后端不可用时返回内置 `mockProducts` / `mockOrders`，支持离线开发。
		- 主要函数与作用：
			- `fetchProducts()` / `fetchProductsInpage()`：商品列表（支持分页、兼容后端多种返回形态）。
			- `addGoods()` / `updateGoods()`：商品新增与更新（支持 FormData 提交图片）。
			- `updateGoodsPic(goodsId, file)`：专门的图片上传接口，含空响应兼容与日志记录。
			- `listGoods(storeId)`：按门店/供应商列出库存商品。
			- 其他：购物车、订单、评论、统计等高层接口。
		- 答辩点：展示如何兼容不同后端响应、为何要在客户端做容错（空 body、非 JSON、HTTP code 处理）。

- 视图组件与交互边界
	- `src/views/*`：按角色划分的页面集合（user / middle / supplier / enterprise），每个视图负责 UI 渲染与局部交互。
	- `src/components/*`：可复用组件（Header、CommentSection、NavBar 等），职责：分离 UI 与业务逻辑，便于测试与复用。
	- 答辩点：以 `ProductList.vue` 为示例说明数据流（props / emits / api 调用）与样式隔离（scoped vs 全局）。

- 后端（mock）
	- `backend/mock-server.js`：开发期的轻量后端实现。
		- 支持：商品、订单、购物车、store、用户等基础 REST 路由；优先尝试读写 MySQL（`backend/db.js`），失败时回退为内存/回显逻辑。
		- CORS：允许自定义 header（含 `token`），方便前端测试带鉴权的请求。
		- 答辩点：说明 mock-server 如何帮助离线联调、为何需要记录并展示请求头/请求体以便排查。

- 构建与环境
	- `package.json`：包含 `dev`、`build`、`preview` 脚本（Vite）；`serverScripts` 提示可运行的 mock-server。
	- `.env` / `.env.production`：通过 `VITE_API_BASE_URL` 等变量区分开发/生产后端地址；说明 Vite 只暴露 `VITE_` 前缀的 env 给客户端。

- 日志、错误处理与调试策略
	- 前端：在关键 API（如 `updateGoodsPic`）增加 `console.debug` / `console.error`，并在 UI 层显示 `details` 字段以便复现问题。
	- 后端 mock：通过标准化日志输出（`writeLog`）记录入参与解析结果，方便在演示中复现请求序列。
	- 答辩点：展示一次典型的故障排查流程（例如图片上传显示 `backend_connection_failed`，如何从 Console → Network → mock-server 日志逐步定位）。

- 测试与验收
	- 建议：使用 `Vitest` 或 `Jest` 做单元/集成测试；E2E 建议使用 `Cypress` 检查关键路径（下单、上传、供应商流程）。
	- 在答辩中准备 8–12 条验收用例并现场执行以证明功能完整性。

- 安全与性能注意事项
	- Token 暴露：避免在 UI 中显示完整 token；演示时可使用 mock token。  
	- 文件上传：生产建议使用 CDN 与后端签名上传，避免在前端直接传到后端并承受带宽压力。  
	- 性能：图片懒加载、分页后端分页（`fetchProductsInpage`）以减少首屏负载。

---

六、Mock 后端说明
- 文件：`backend/mock-server.js`。
	- 提供简单的 REST API 用于开发与接口联调，如 `/api/goods`, `/api/orders`, `/api/shoppingcart` 等。
	- 尝试访问本地 MySQL（见 `backend/db.js`）以提供真实数据，否则降级为内存 mock 返回。
	- 启动：`node backend/mock-server.js`。

七、重要实现细节与设计决策（答辩要点）
- 会话与供应商入口：供应商可以通过 `GET /supplier/:supplierId` 直接进入管理台，路由守卫会在 `sessionStorage` 中写入临时 token 与 role，避免持久化污染用户登录。
- API 兼容性：`src/services/api.js` 通过对返回数据的多种字段名做兼容（例如 GoodsID/GoodsId/id），提高与不同后端实现对接的鲁棒性。
- 上传与容错：图片上传函数如 `updateGoodsPic` 已实现：
	- 在后台返回空响应（204 或空 body）时，前端将视为空响应为成功（避免 json parse 抛错）。
	- 在 Network/Console 中记录详细日志以便排查（`console.debug`/`console.error`）。
- Teleport 与样式问题：商品详情 modal 使用 `<teleport to="body">`，因此样式应放在非 scoped 或全局样式，或在组件中同时提供 scoped + unscoped 样式。

八、开发流程建议（团队/答辩演示流程）
1. 环境准备：确保 Node 与 Mock 后端（或真实后端）可用，设置 `VITE_API_BASE_URL` 指向后端。  
2. 本地开发：使用 `npm run dev`，在浏览器打开 `http://localhost:3000` 进行交互式调试。  
3. 联调后端：优先在浏览器 Network 面板观察请求 URL、Headers、Response。后端若为本地 mock，可通过 `backend/mock-server.js` 查看日志。  
4. 代码提交：保持每次更改小范围、提供清晰 commit message，必要时在 PR 中附上复现步骤与截图。

九、构建与部署（示例：使用 Nginx 部署）
1. 执行构建：`npm run build`，生成 `dist/` 目录。  
2. 将 `dist/` 上传至服务器并配置 Nginx：

示例 Nginx 配置片段：
```
server {
	listen 80;
	server_name your.domain.com;
	root /var/www/tinymall/dist;

	index index.html;

	location / {
		try_files $uri $uri/ /index.html;
	}

	# 若后端同域部署，反向代理示例：
	location /api/ {
		proxy_pass http://127.0.0.1:4000/;
		proxy_set_header Host $host;
	}
}
```

十、测试与验收要点（答辩关注）
- 功能测试用例（示例）：
	- 未登录用户访问 `/user/products` 并能看到商品列表。  
	- 登录为 `user` 后能下单、查看订单。  
	- 以 `supplier` 身份通过 `/supplier/:id` 进入供应商管理台并能更新商品（含图片上传）。
	- 检查商品详情 modal 样式与图片覆盖是否正常（teleport 情形）。
- 性能与可靠性测试建议：页面首屏渲染时间、图片加载失败回退、API 超时与重试策略。

十一、常见问题与排查指南
- 后端连接失败：
	- 检查 `VITE_API_BASE_URL` 是否设置正确；若为空，前端将使用相对路径。  
	- 如果使用 mock-server，运行 `node backend/mock-server.js` 并观察控制台日志（mock-server 会打印收到的请求）。
- 图片上传显示“backend_connection_failed”但后端有反应：
	- 原因常为后端返回空 body 或非 JSON，前端直接调用 `res.json()` 会抛出解析异常；查看 Console 中 `[api.updateGoodsPic]` 日志（已在代码中加入日志）。
	- 解决方法：查看 Network → Response 原始内容，或让后端在上传成功时返回标准 JSON（例如 `{ "code":0, "msg":"ok" }`）。
- 路由重定向/登录问题：
	- 全局守卫实现于 `src/router/index.js`，可在 `router.isReady()` 后观察初始路由（`src/main.js` 已记录）。

十二、已知限制与后续改进建议
- 当前前端对后端的字段名有较多兼容逻辑，长期应统一后端契约以减少前端适配代码。  
- 上传与大文件支持：目前未实现断点续传或分片上传，生产环境建议使用 CDN + 后端签名上传策略。  
- 权限模型：目前以简单 role 判断为主（user/supplier），如需支持更细粒度权限应引入 RBAC 并在后端强校验。

附录：快速文件索引
- 入口：[src/main.js](src/main.js#L1)  
- 路由：[src/router/index.js](src/router/index.js#L1)  
- 认证状态：[src/stores/auth.js](src/stores/auth.js#L1)  
- API 封装：[src/services/api.js](src/services/api.js#L1)  
- Mock 后端：[backend/mock-server.js](backend/mock-server.js#L1)  

如果你同意，我会把这份内容覆盖 `README.md`（已准备好），并可以同时生成一份答辩用的 PPT 大纲或一页式讲稿。请确认是否覆盖并是否需要 PPT/讲稿。 