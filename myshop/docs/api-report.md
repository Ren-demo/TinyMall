# 后端接口数据要求（基于 backend/）

> 以 my-shop/backend 实际实现为准；mock 以 MySQL 表 user/goods/order/store/shoppingcart 为数据源。状态数值惯例：0 待处理，1 已接受/已确认，2 已发货，-1 拒绝。

---

## 1. 登录

### 1.1 用户登录

**接口描述：** 用户登录

**请求方式：** POST

**请求URL：** `/api/login`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**接口描述：** 验证用户名/邮箱和密码，返回 token

**请求示例：**

```json
{
  "username": "alice",
  "password": "123456"
}
```

或

```json
{
  "email": "alice@example.com",
  "password": "123456"
}

**请求参数：**


### 7.8 供应商修改商品信息

| username | 用户名（与 email 二选一） | body | false | string |

| email | 邮箱（与 username 二选一） | body | false | string |

| password | 密码 | body | true | string |



**响应状态：**



| 状态码 | 说明 |
|---------|---------|---------|---------|---------|--------|
| updateGoodsDto | UpdateGoodsDto | body | true |  | UpdateGoodsDto |
| goodsId |  |  | false | integer(int32) |  |
| goodsName |  |  | false | string |  |
| text |  |  | false | string |  |
| price |  |  | false | number(float) |  |
| count |  |  | false | integer(int32) |  |
| add |  |  | false | boolean |  |
| file |  | query | false | file |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---
**接口描述：** 查询所有余量大于 0 的商品
**请求URL：** `/tinymall/goods/list`
**请求数据类型：** `application/x-www-form-urlencoded`
**响应数据类型：** `*/*`

| code | 状态码 | integer(int32) |
| msg | 提示信息 | string |
| data | 数据对象（商品数组） | object |

**data 中商品字段：**
| GoodsID | 商品ID | integer |
| GoodsName | 商品名称 | string |
| StoreID | 门店ID | string |
| Text | 商品描述 | string |
| Count | 库存数量 | integer |
| Price | 价格 | number |
| Picture | 图片路径 | string |

**响应示例：**
```json
{
  "code": 0,
  "msg": "",
  "data": [
    {
      "GoodsID": 1,
      "StoreID": "store-1",
      "Text": "优质商品 A",
      "Count": 50,
      "Price": 99.0,
      "Picture": "/assets/prod-a.jpg"
    },
    {
      "GoodsID": 2,
      "GoodsName": "商品 B",
      "StoreID": "store-2",
      "Text": "热销商品 B",
      "Count": 20,
      "Price": 149.0,
      "Picture": "/assets/prod-b.jpg"
    }
  ]
}
```
  "error": "invalid_credentials"
}
```

**备注：** 
- 优先查询 user 表匹配 UserName/UserEmail + UserPwd
- DB 异常时降级为 mock 模式，直接返回成功（开发环境）

---

## 2. 用户信息修改

### 2.1 updateUserInfo

**接口描述：** 用户修改个人信息（不需要输入原密码）

**请求方式：** PUT

**请求URL：** `/tinymall/user/update`

**请求数据类型：** `application/json`

**响应数据类型：** `*/*`

**请求示例：**

```json
{
  "user": {
    "userId": 1,
    "userName": "alice",
    "userPwd": "newpass",
    "userEmail": "alice@example.com",
    "userPicture": "/avatars/alice.jpg"
  }
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| user | User | body | true |  | User |
| userId |  |  | false | integer(int32) |  |
| userName |  |  | false | string |  |
| userPwd |  |  | false | string |  |
| userEmail |  |  | false | string |  |
| userPicture |  |  | false | string |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

**响应示例：**

```json
{
  "code": 0,
  "msg": "",
  "data": {}
}
```

---

## 3. 商品

### 3.1 查询订单

**接口描述：** 查询订单列表（支持按用户、供应商筛选）

**请求方式：** GET

**请求URL：** `/api/orders`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| username | 用户名（按用户查询） | query | false | string |
| supplierId | 供应商ID（按供应商查询，仅返回 State=1 的订单） | query | false | string |

**响应数据类型：** `application/json`

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

订单对象数组，每个订单包含：

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| id | 订单ID | integer |
| user | 用户名/邮箱 | string |
| supplierId | 供应商/门店ID | string |
| supplierName | 供应商/门店名称 | string |
| time | 下单时间 | string(datetime) |
| state | 订单状态（0=待处理 1=已接受 2=已发货 -1=拒绝） | integer |
| items | 订单商品列表 | array |
**items 数组元素：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| productId | 商品ID | integer |
| name | 商品名称 | string |
| qty | 数量 | integer |
| price | 单价 | number |
| picture | 商品图片 | string |
| storeId | 门店ID | string |
| storeName | 门店名称 | string |

**响应示例：**

```json
[
  {
    "id": 101,
    "user": "alice@example.com",
    "supplierId": "store-1",
    "supplierName": "门店A",
    "time": "2026-01-21T10:30:00.000Z",
    "state": 1,
    "items": [
      {
        "productId": 1,
        "name": "商品 A",
        "qty": 2,
        "price": 99.0,
        "storeId": "store-1",
        "storeName": "门店A"
      }
    ]
  }
]
```

### 3.2 创建订单

**接口描述：** 创建新订单

**请求方式：** POST

**请求URL：** `/api/orders`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**请求示例：**

```json
{
  "username": "alice",
  "items": [
    {
      "productId": 1,
      "qty": 2,
      "storeId": "store-1",
      "price": 99.0
    },
    {
      "productId": 2,
      "qty": 1,
      "price": 149.0
    }
  ]
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| username | 用户名 | body | true | string |
| user | 用户名（与 username 同义） | body | false | string |
| items | 订单商品列表 | body | true | array |

**items 数组元素：**

| 参数名称 | 参数说明 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|
| productId | 商品ID | true | integer |
| GoodsID | 商品ID（与 productId 同义） | false | integer |
| qty | 数量 | true | integer |
| Count | 数量（与 qty 同义） | false | integer |
| storeId | 门店ID（可选，未提供则从 goods 表查询） | false | string |
| price | 单价（可选，未提供则从 goods 表查询） | false | number |

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| success | 是否成功 | boolean |
| orders | 创建的订单行记录 | array |

**响应示例：**

```json
{
  "success": true,
  "orders": [
    {
      "OrderID": 101,
      "UserID": 5,
      "StoreID": "store-1",
      "GoodsID": 1
    },
    {
      "OrderID": 102,
      "UserID": 5,
      "StoreID": "store-2",
      "GoodsID": 2
    }
  ]
}
```

**备注：**
- 每个 item 插入一行到 order 表，State=0（待处理），Time=NOW()
- OrderID 为当前最大值+1
- StoreID 若未提供则从 goods 表查询

### 3.3 更新订单状态

**接口描述：** 更新订单状态

**请求方式：** POST

**请求URL：** `/api/orders/state`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**请求示例：**

```json
{
  "orderId": 101,
  "status": "已接受"
}
```

或

```json
{
  "orderId": 101,
  "state": 1
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| orderId | 订单ID | body | true | integer |
| OrderID | 订单ID（与 orderId 同义） | body | false | integer |
| state | 状态数值（0/1/2/-1） | body | false | integer |
| status | 状态字符串（已发货/已接受/拒绝/待处理） | body | false | string |

**状态映射：**

| status 字符串 | state 数值 | 含义 |
|--------------|-----------|------|
| 已发货 | 2 | 已发货 |
| 已确认 | 1 | 已接受 |
| 已接受 | 1 | 已接受 |
| 拒绝 | -1 | 拒绝/取消 |
| 待处理 | 0 | 待处理 |
| 已提交 | 0 | 待处理 |

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| success | 是否成功 | boolean |
| error | 错误信息（失败时） | string |

**响应示例：**

```json
{
  "success": true
}
```

### 3.4 发送订单到供应商

**接口描述：** 中间商发送订单到供应商，自动扣减库存

**请求方式：** POST

**请求URL：** `/api/orders/send`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**请求示例：**

```json
{
  "orderId": 101
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| orderId | 订单ID | body | true | integer |

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| success | 是否成功 | boolean |
| suppliers | 涉及的供应商ID列表 | array |
| inventoryChanges | 库存变更记录 | array |

**inventoryChanges 数组元素：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| storeId | 门店ID | string |
| goodsId | 商品ID | integer |
| deducted | 扣减数量 | integer |

**响应示例：**

```json
{
  "success": true,
  "suppliers": ["store-1", "store-2"],
  "inventoryChanges": [
    {
      "storeId": "store-1",
      "goodsId": 1,
      "deducted": 2
    }
  ]
}
```

**备注：**
- 仅处理 State=0（待处理）的订单行
- 按 StoreID 分组，扣减对应商品库存（goods.Count）
- 将订单行 State 更新为 1（已接受）

### 3.5 供应商发货

**接口描述：** 供应商标记订单为已发货

**请求方式：** POST

**请求URL：** `/api/orders/ship`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**请求示例：**

```json
{
  "orderId": 101,
  "supplierId": "store-1"
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| orderId | 订单ID | body | true | integer |
| OrderID | 订单ID（与 orderId 同义） | body | false | integer |
| supplierId | 供应商ID | body | true | string |
| storeId | 门店ID（与 supplierId 同义） | body | false | string |

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| success | 是否成功 | boolean |
| changed | 更新的行数 | integer |

**响应示例：**

```json
{
  "success": true,
  "changed": 1
}
```

**备注：** 将匹配 OrderID + StoreID 的订单行 State 更新为 2（已发货）

---

## 4. 购物车

### 4.1 addCart

**接口描述：** 商品添加购物车

**请求方式：** POST

**请求URL：** `/tinymall/goods/addCart`

**请求数据类型：** `application/json`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| addCartDto | AddCartDto | body | true |  | AddCartDto |
| goodsId |  |  | false | integer(int32) |  |
| userId |  |  | false | integer(int32) |  |
| count |  |  | false | integer(int32) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

### 4.2 查询购物车

**接口描述：** 获取用户购物车列表

**请求方式：** GET

**请求URL：** `/api/shoppingcart`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| userid | 用户ID（可选，不提供则返回所有） | query | false | integer |

**响应数据类型：** `application/json`

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| ShoppingCartID | 购物车条目ID | integer |
| UserID | 用户ID | integer |
| StoreID | 门店ID | string |
| GoodsID | 商品ID | integer |
| Count | 数量 | integer |
| TotalPrice | 总价 | number |
| GoodsName | 商品名称 | string |
| Price | 单价 | number |

**响应示例：**

```json
[
  {
    "ShoppingCartID": 1,
    "UserID": 5,
    "StoreID": "store-1",
    "GoodsID": 1,
    "Count": 2,
    "TotalPrice": 198.0,
    "GoodsName": "商品 A",
    "Price": 99.0
  },
  {
    "ShoppingCartID": 2,
    "UserID": 5,
    "StoreID": "store-2",
    "GoodsID": 2,
    "Count": 1,
    "TotalPrice": 149.0,
    "GoodsName": "商品 B",
    "Price": 149.0
  }
]
```

### 4.3 保存购物车

**接口描述：** 更新用户购物车（支持批量 upsert）

**请求方式：** POST

**请求URL：** `/api/shoppingcart`

**请求数据类型：** `application/json`

**响应数据类型：** `application/json`

**请求示例：**

```json
{
  "userid": 5,
  "items": [
    {
      "productId": 1,
      "qty": 2,
      "price": 99.0,
      "storeId": "store-1"
    },
    {
      "id": 2,
      "productId": 2,
      "qty": 3,
      "price": 149.0
    }
  ]
}
```

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| userid | 用户ID | body | false | integer |
| username | 用户名（与 userid 二选一，会自动反查 UserID） | body | false | string |
| items | 购物车商品列表 | body | true | array |

**items 数组元素：**

| 参数名称 | 参数说明 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|
| id | 购物车条目ID（更新时提供） | false | integer |
| productId | 商品ID | true | integer |
| GoodsID | 商品ID（与 productId 同义） | false | integer |
| qty | 数量 | true | integer |
| Count | 数量（与 qty 同义） | false | integer |
| price | 单价 | false | number |
| name | 商品名称 | false | string |
| storeId | 门店ID | false | string |

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| success | 是否成功 | boolean |
| deleted | 删除的行数（清理不在列表中的商品） | object |
| remaining | 剩余购物车记录（可选） | array |

**响应示例：**

```json
{
  "success": true,
  "deleted": {
    "affectedRows": 1
  },
  "remaining": [
    {
      "ShoppingCartID": 1,
      "UserID": 5,
      "StoreID": "store-1",
      "GoodsID": 1,
      "Count": 2,
      "TotalPrice": 198.0
    }
  ]
}
```

**备注：**
- 如果 item 包含 `id`，则按 ShoppingCartID 更新
- 否则按 UserID + GoodsID 查找已有记录，存在则更新，不存在则插入
- 最后删除该用户购物车中不在本次 items 列表中的商品
- 若 items 为空数组，则清空该用户购物车

---

## 5. 门店

### 5.1 获取门店列表

**接口描述：** 获取所有门店列表

**请求方式：** GET

**请求URL：** `/api/store`

**响应数据类型：** `application/json`

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

返回 store 表所有字段（包含 StoreID, StoreName, StoreEmail 等）

**响应示例：**

```json
[
  {
    "StoreID": "store-1",
    "StoreName": "门店A",
    "StoreEmail": "storea@example.com"
  },
  {
    "StoreID": "store-2",
    "StoreName": "门店B",
    "StoreEmail": "storeb@example.com"
  }
]
```

### 5.2 获取单个门店详情

**接口描述：** 根据门店ID获取门店详情

**请求方式：** GET

**请求URL：** `/api/store/{id}`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 |
|---------|---------|---------|---------|---------|
| id | 门店ID | path | true | string |

**响应数据类型：** `application/json`

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应示例：**

```json
{
  "StoreID": "store-1",
  "StoreName": "门店A",
  "StoreEmail": "storea@example.com"
}
```

---

## 6. 用户

### 6.1 获取用户列表

**接口描述：** 获取所有用户信息

**请求方式：** GET

**请求URL：** `/api/users`

**响应数据类型：** `application/json`

**响应状态：**

| 状态码 | 说明 |
|-------|------|
| 200 | OK |

**响应参数：**

| 参数名称 | 参数说明 | 类型 |
|---------|---------|------|
| UserID | 用户ID | integer |
| UserName | 用户名 | string |
| UserEmail | 用户邮箱 | string |
| UserPicture | 用户头像 | string |

**响应示例：**

```json
[
  {
    "UserID": 1,
    "UserName": "alice",
    "UserEmail": "alice@example.com",
    "UserPicture": "/avatars/alice.jpg"
  },
  {
    "UserID": 2,
    "UserName": "bob",
    "UserEmail": "bob@example.com",
    "UserPicture": "/avatars/bob.jpg"
  }
]
```

---

## 7. 诊断与健康检查

### 7.1 回显请求头

**接口描述：** 调试用，返回客户端发送的请求头

**请求方式：** GET

**请求URL：** `/echo-headers`

**响应示例：**

```json
{
  "receivedHeaders": {
    "host": "localhost:4000",
    "user-agent": "Mozilla/5.0...",
    "accept": "*/*"
  }
}
```

### 7.2 回显请求

**接口描述：** 调试用，返回请求的完整信息

**请求方式：** POST / GET

**请求URL：** `/echo`

**响应示例：**

```json
{
  "method": "POST",
  "url": "/echo",
  "receivedHeaders": {
    "content-type": "application/json"
  },
  "body": {
    "test": "data"
  }
}
```

### 7.3 健康检查

**接口描述：** 服务健康检查

**请求方式：** GET

**请求URL：** `/health`

**响应示例：**

```json
{
  "ok": true,
  "time": "2026-01-21T10:30:00.000Z"
}
```

---

### 7.4 新增供应商（中间商）

**接口描述：** 新增供应商

**请求方式：** POST

**请求URL：** `/tinymall/mall/addStore`

**请求数据类型：** `application/json`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| addStoreDto | AddStoreDto | body | true |  | AddStoreDto |
| storeName |  |  | false | string |  |
| storeEmail |  |  | false | string |  |
| picture |  |  | false | string(binary) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.5 商店管理订单（接受/拒绝）

**接口描述：** 商店管理订单（接受/拒绝）

**请求方式：** PUT

**请求URL：** `/tinymall/mall/manageOrder`

**请求数据类型：** `application/json`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| manageOrderDto | ManageOrderDto | body | true |  | ManageOrderDto |
| orderId |  |  | false | integer(int32) |  |
| msg |  |  | false | string |  |
| refuse |  |  | false | boolean |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.6 新增商品（供货商）

**接口描述：** 新增商品

**请求方式：** POST

**请求URL：** `/tinymall/store/add`

**请求数据类型：** `multipart/form-data`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| file |  | query | true | file |  |
| addGoodsDto | AddGoodsDto | body | true |  | AddGoodsDto |
| goodsName |  |  | false | string |  |
| storeId |  |  | false | integer(int32) |  |
| text |  |  | false | string |  |
| count |  |  | false | integer(int32) |  |
| price |  |  | false | number(float) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.7 供应商查看自己的订单

**接口描述：** 供应商查看自己的订单

**请求方式：** GET

**请求URL：** `/tinymall/store/listOrders/{storeId}`

**请求数据类型：** `application/x-www-form-urlencoded`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| storeId |  | path | true | integer(int32) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.8 供应商修改商品信息

**接口描述：** 供应商修改商品信息

**请求方式：** PUT

**请求URL：** `/tinymall/store/updateGoods`

**请求数据类型：** `application/json`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| updateGoodsDto | UpdateGoodsDto | body | true |  | UpdateGoodsDto |
| goodsId |  |  | false | integer(int32) |  |
| goodsName |  |  | false | string |  |
| text |  |  | false | string |  |
| price |  |  | false | number(float) |  |
| count |  |  | false | integer(int32) |  |
| add |  |  | false | boolean |  |
| file |  | query | false | file |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.9 供应商修改商品图片

**接口描述：** 供应商修改商品图片

**请求方式：** POST

**请求URL：** `/tinymall/store/updateGoodsPic/{goodsId}`

**请求数据类型：** `multipart/form-data`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| goodsId |  | path | true | integer(int32) |  |
| file |  | query | true | file |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.10 供应商发货

**接口描述：** 供应商发货

**请求方式：** PUT

**请求URL：** `/tinymall/store/delivery/{orderId}`

**请求数据类型：** `application/x-www-form-urlencoded`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| orderId |  | path | true | integer(int32) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

### 7.11 商店催促供应商发货

**接口描述：** 商店催促供应商发货

**请求方式：** POST

**请求URL：** `/tinymall/mall/urge/{orderId}/{storeId}`

**请求数据类型：** `application/x-www-form-urlencoded`

**响应数据类型：** `*/*`

**请求参数：**

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | schema |
|---------|---------|---------|---------|---------|--------|
| orderId |  | path | true | integer(int32) |  |
| storeId |  | path | true | integer(int32) |  |

**响应状态：**

| 状态码 | 说明 | schema |
|-------|------|--------|
| 200 | OK | Result |

**响应参数：**

| 参数名称 | 参数说明 | 类型 | schema |
|---------|---------|------|--------|
| code |  | integer(int32) | integer(int32) |
| msg |  | string |  |
| data |  | object |  |

---

## 8. 公共约定

### 8.1 CORS 配置

- 允许的请求方法：GET, POST, OPTIONS
- 允许的请求头：Content-Type, Authorization, X-Custom-Header, X-Client-Action
- 允许的来源：*（所有来源）

### 8.2 订单状态说明

| state 数值 | 中文状态 | 英文说明 |
|-----------|---------|---------|
| 0 | 待处理 | Pending |
| 1 | 已接受/已确认 | Accepted |
| 2 | 已发货 | Shipped |
| -1 | 拒绝/取消 | Rejected |

**备注：**
- 数据库使用数值 `State` 字段存储
- 前端通过 `_stateToStatus` 函数映射为中文字符串
- 时间字段 `time` 来自 `order.Time`（MySQL DATETIME），格式可被 JavaScript `new Date()` 解析

### 8.3 字段别名说明

为兼容不同命名习惯，部分接口支持字段别名：

| 标准字段 | 别名字段 |
|---------|---------|
| username | email, user |
| orderId | OrderID |
| productId | GoodsID |
| qty | Count |
| supplierId | storeId |
| userid | username（需反查） |
