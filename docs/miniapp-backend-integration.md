# Miniapp 后端联调说明

## 1. 基础信息

- 服务名称：任务清单后端
- 本地默认地址：`http://localhost:8080`
- 接口统一前缀：`/api/app/v1`
- 文档依据：当前 `apps/server` 已实现代码

## 2. 鉴权约定

### 2.1 哪些接口需要 token

- 不需要 token：`POST /api/app/v1/auth/wechat/login`
- 需要 token：其余全部 miniapp 接口

### 2.2 token 传递方式

请求头：

```text
Authorization: Bearer <accessToken>
```

建议：

- miniapp 在请求封装层统一注入 token
- 服务端返回 `40100` 时，统一清理 token 并重新登录

## 3. 推荐登录流程

### 3.1 登录链路

1. 小程序调用 `wx.login()` 获取 `code`
2. 调用 `POST /api/app/v1/auth/wechat/login`
3. 后端返回 `accessToken`
4. miniapp 将 `accessToken` 持久化到本地
5. 后续请求自动携带 `Authorization` 头

### 3.2 登录接口请求示例

```json
{
  "code": "wx-login-code",
  "nickName": "张三",
  "avatarUrl": "https://example.com/avatar.png"
}
```

### 3.3 登录成功后建议缓存的数据

- `accessToken`
- `expireInSeconds`
- `userId`
- `nickName`
- `avatarUrl`

## 4. 时间与字段格式

### 4.1 日期时间字段

当前后端返回的 `LocalDateTime` 实际表现为 ISO 风格字符串，例如：

```text
2030-01-01T10:00:00
2026-03-23T09:18:05.467
```

说明：

- 返回值中带 `T`
- 可能带秒，也可能带毫秒或更高精度小数位
- miniapp 不要依赖固定长度截取时间字符串

### 4.2 日期查询参数

任务列表的 `date` 查询参数格式固定为：

```text
yyyy-MM-dd
```

示例：

```text
GET /api/app/v1/tasks?date=2030-01-01
```

### 4.3 字段命名风格

- 全部请求体和响应体字段使用 `camelCase`
- 文档和代码保持一致，不使用下划线命名

## 5. 枚举值约定

### 5.1 任务状态

- `TODO`
- `DONE`
- `EXPIRED`

说明：

- 前端可以传 `DONE` 或 `TODO` 到状态更新接口
- `EXPIRED` 由系统控制，前端不能主动设置

### 5.2 任务优先级

- `LOW`
- `MEDIUM`
- `HIGH`

默认值：

- 新增和修改任务时未传 `priority`，后端按 `MEDIUM` 处理

### 5.3 重复类型

- `NONE`
- `DAILY`
- `WEEKLY`
- `MONTHLY`

默认值：

- 未传 `repeatType`，后端按 `NONE` 处理

## 6. 关键业务口径

### 6.1 任务列表

- 普通任务列表：当前用户未删除任务
- 默认排序：`createdAt` 倒序
- 支持状态、分类、关键词、日期筛选
- 关键词同时匹配 `title` 和 `description`

### 6.2 今日任务

- 口径：`dueAt` 落在今天自然日范围内
- 排序：`dueAt` 升序，再按优先级高到低

### 6.3 任务状态流转

- 创建任务时：
  - `dueAt` 未来或为空：`TODO`
  - `dueAt` 已过期：`EXPIRED`
- 标记完成：状态变 `DONE`
- 标记未完成：
  - `dueAt` 未过期：变 `TODO`
  - `dueAt` 已过期：变 `EXPIRED`

### 6.4 分类删除

- 分类删除后，关联任务不会删除
- 这些任务的 `categoryId` 会被置空

### 6.5 回收站

- 任务删除后进入回收站
- 普通列表不会返回回收站任务
- 回收站任务保留 7 天后由定时任务物理清理

### 6.6 统计口径

`overview(range=today|week|month)`：

- 统计当前用户未删除任务
- 范围判断优先取 `dueAt`
- 没有 `dueAt` 时取 `createdAt`
- 返回 `completedCount`、`totalCount`、`completionRate`、`consecutiveCompletedDays`

`trend(range=week|month)`：

- 只统计已完成任务数量
- 按天返回完整数组
- 缺失日期补 `0`

`consecutiveCompletedDays`：

- 按 `completedAt` 的自然日向前连续计算

## 7. miniapp 请求封装建议

- 在请求层统一拼接 `baseUrl`
- 在请求拦截器统一注入 `Authorization`
- 在响应层统一判断：
  - HTTP 不是 `2xx` 视为失败
  - `code !== 0` 视为失败
- 建议统一处理：
  - `40100`：重新登录
  - `42200`：toast 具体校验信息
  - 其他错误：toast 通用错误

## 8. 常见联调问题

### 8.1 登录接口返回微信配置错误

现象：

- `50010`
- `message` 可能是 `WeChat appId/appSecret is not configured`

处理：

- 检查服务端 `application-local.yml` 中的微信配置

### 8.2 接口返回未登录

现象：

- `40100 Unauthorized`

处理：

- 检查是否已携带 `Authorization: Bearer <token>`
- 检查 token 是否已被覆盖或清空

### 8.3 表单提交失败

现象：

- `42200`

处理：

- 检查是否漏传必填字段，如 `title`、`nickName`

### 8.4 状态恢复后返回 `EXPIRED`

现象：

- 前端传了 `TODO`
- 响应状态变成 `EXPIRED`

原因：

- 任务截止时间已过，后端按规则自动恢复为过期
