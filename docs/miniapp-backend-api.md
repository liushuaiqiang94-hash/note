# Miniapp 后端接口文档

## 1. 文档说明

- 文档事实来源：`apps/server` 当前已实现代码
- 接口前缀：`/api/app/v1`
- 默认本地基地址：`http://localhost:8080`
- 除登录接口外，全部接口都需要请求头：`Authorization: Bearer <accessToken>`
- 统一成功响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

- 分页响应 `data` 结构：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "total": 2,
  "list": []
}
```

## 2. 认证与用户

### 2.1 微信登录

- 用途：小程序用 `wx.login` 获取 `code` 后换取后端 `accessToken`
- 方法：`POST`
- 路径：`/api/app/v1/auth/wechat/login`
- 是否鉴权：否
- 请求头：`Content-Type: application/json`

请求体：

```json
{
  "code": "wx-login-code",
  "nickName": "张三",
  "avatarUrl": "https://example.com/avatar.png"
}
```

字段说明：

- `code`：必填，微信登录 code
- `nickName`：必填，用户昵称
- `avatarUrl`：选填，头像地址

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "accessToken": "jwt-token",
    "expireInSeconds": 604800,
    "userId": 1,
    "nickName": "张三",
    "avatarUrl": "https://example.com/avatar.png"
  }
}
```

关键规则：

- 首次登录会自动创建用户
- 重复登录会更新昵称、头像和最近登录时间
- 登录成功后 miniapp 需要持久化 `accessToken`

### 2.2 获取当前用户

- 用途：获取当前登录用户基础资料
- 方法：`GET`
- 路径：`/api/app/v1/me`
- 是否鉴权：是

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "userId": 1,
    "nickName": "张三",
    "avatarUrl": "https://example.com/avatar.png"
  }
}
```

## 3. 分类接口

### 3.1 获取分类列表

- 方法：`GET`
- 路径：`/api/app/v1/categories`
- 是否鉴权：是

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "name": "工作",
      "color": "#1677ff",
      "sortNo": 1,
      "isDefault": false
    }
  ]
}
```

关键规则：

- 返回当前用户自己的未删除分类
- 排序规则：`sortNo` 升序，再按 `id` 升序

### 3.2 新增分类

- 方法：`POST`
- 路径：`/api/app/v1/categories`
- 是否鉴权：是

请求体：

```json
{
  "name": "生活",
  "color": "#52c41a",
  "sortNo": 2
}
```

字段说明：

- `name`：必填
- `color`：选填，最大长度 16
- `sortNo`：选填，默认 `0`

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "id": 2,
    "name": "生活",
    "color": "#52c41a",
    "sortNo": 2,
    "isDefault": false
  }
}
```

### 3.3 修改分类

- 方法：`PUT`
- 路径：`/api/app/v1/categories/{categoryId}`
- 是否鉴权：是

请求体：

```json
{
  "name": "学习",
  "color": "#faad14",
  "sortNo": 3
}
```

成功响应示例与新增分类一致。

关键规则：

- 只能修改自己的未删除分类
- 分类不存在时返回 `40400`

### 3.4 删除分类

- 方法：`DELETE`
- 路径：`/api/app/v1/categories/{categoryId}`
- 是否鉴权：是

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": null
}
```

关键规则：

- 分类删除为软删除
- 该分类下当前用户任务的 `categoryId` 会被置空

## 4. 任务接口

### 4.1 获取任务列表

- 方法：`GET`
- 路径：`/api/app/v1/tasks`
- 是否鉴权：是

查询参数：

- `status`：选填，任务状态
- `categoryId`：选填，分类 ID
- `keyword`：选填，搜索标题或描述
- `date`：选填，格式 `yyyy-MM-dd`
- `pageNum`：选填，默认 `1`
- `pageSize`：选填，默认 `10`

请求示例：

```text
GET /api/app/v1/tasks?status=TODO&categoryId=1&keyword=日报&date=2030-01-01&pageNum=1&pageSize=10
```

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 1,
    "list": [
      {
        "id": 10,
        "categoryId": 1,
        "title": "完成日报",
        "description": "下班前提交",
        "priority": "HIGH",
        "status": "TODO",
        "dueAt": "2030-01-01T10:00:00",
        "remindAt": "2030-01-01T09:00:00",
        "repeatType": "NONE",
        "completedAt": null,
        "createdAt": "2026-03-23T09:18:05.467",
        "updatedAt": "2026-03-23T09:18:05.467"
      }
    ]
  }
}
```

关键规则：

- 仅返回当前用户未删除任务
- 默认排序：`createdAt` 倒序
- `keyword` 同时匹配 `title` 和 `description`
- `date` 按 `dueAt` 所在自然日过滤

### 4.2 新增任务

- 方法：`POST`
- 路径：`/api/app/v1/tasks`
- 是否鉴权：是

请求体：

```json
{
  "categoryId": 1,
  "title": "完成日报",
  "description": "下班前提交",
  "priority": "HIGH",
  "dueAt": "2030-01-01T10:00:00",
  "remindAt": "2030-01-01T09:00:00",
  "repeatType": "NONE"
}
```

字段说明：

- `categoryId`：选填，必须是当前用户自己的分类
- `title`：必填
- `description`：选填
- `priority`：选填，默认 `MEDIUM`
- `dueAt`：选填
- `remindAt`：选填
- `repeatType`：选填，默认 `NONE`

成功响应示例：返回单个任务对象，字段同任务列表项。

关键规则：

- `dueAt` 早于当前时间时，任务会直接创建为 `EXPIRED`
- `remindAt` 有值时会自动创建提醒记录

### 4.3 获取任务详情

- 方法：`GET`
- 路径：`/api/app/v1/tasks/{taskId}`
- 是否鉴权：是

成功响应示例：返回单个任务对象，字段同任务列表项。

关键规则：

- 只能获取自己的未删除任务
- 任务不存在时返回 `40400`

### 4.4 修改任务

- 方法：`PUT`
- 路径：`/api/app/v1/tasks/{taskId}`
- 是否鉴权：是

请求体与新增任务一致。

关键规则：

- 若任务当前不是 `DONE`，更新后会重新按 `dueAt` 计算状态：
  - 未过期：`TODO`
  - 已过期：`EXPIRED`
- 若任务当前是 `DONE`，更新内容不会自动改回未完成
- 更新 `remindAt` 后会重建提醒记录

### 4.5 修改任务状态

- 方法：`PATCH`
- 路径：`/api/app/v1/tasks/{taskId}/status`
- 是否鉴权：是

请求体：

```json
{
  "status": "DONE"
}
```

可传值：

- `DONE`
- `TODO`
- `EXPIRED` 不能由前端主动传，传了会返回 `40000`

成功响应示例：返回更新后的任务对象。

关键规则：

- 传 `DONE`：任务标记完成，写入 `completedAt`，并删除提醒记录
- 传 `TODO`：表示恢复为未完成
- 如果恢复时 `dueAt` 已经过期，响应中的实际状态会是 `EXPIRED`

### 4.6 删除任务

- 方法：`DELETE`
- 路径：`/api/app/v1/tasks/{taskId}`
- 是否鉴权：是

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": null
}
```

关键规则：

- 删除为软删除，写入回收站
- 删除后对应提醒记录会被删除

### 4.7 今日任务

- 方法：`GET`
- 路径：`/api/app/v1/tasks/today`
- 是否鉴权：是

成功响应：返回任务数组，元素结构同任务列表项。

关键规则：

- 仅返回 `dueAt` 在今天范围内的未删除任务
- 排序规则：`dueAt` 升序，再按优先级高到低

### 4.8 回收站列表

- 方法：`GET`
- 路径：`/api/app/v1/tasks/recycle-bin`
- 是否鉴权：是

查询参数：

- `pageNum`：选填，默认 `1`
- `pageSize`：选填，默认 `10`

成功响应：分页结构，`list` 中元素结构同任务列表项。

关键规则：

- 仅返回当前用户已删除任务
- 排序规则：`deletedAt` 倒序

### 4.9 恢复任务

- 方法：`POST`
- 路径：`/api/app/v1/tasks/{taskId}/restore`
- 是否鉴权：是

成功响应：返回恢复后的任务对象。

关键规则：

- 若任务未删除，返回 `40000`
- 恢复时如果任务原本已完成且 `completedAt` 存在，则保持 `DONE`
- 否则按 `dueAt` 恢复为 `TODO` 或 `EXPIRED`

## 5. 统计接口

### 5.1 获取统计概览

- 方法：`GET`
- 路径：`/api/app/v1/stats/overview`
- 是否鉴权：是

查询参数：

- `range`：必填，可选 `today`、`week`、`month`

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "completedCount": 1,
    "totalCount": 3,
    "completionRate": 0.3333333333333333,
    "consecutiveCompletedDays": 1
  }
}
```

关键规则：

- 统计对象是当前用户未删除任务
- 统计范围判断口径：
  - 优先取 `dueAt`
  - 若 `dueAt` 为空，则取 `createdAt`
- `completionRate` 是 `0-1` 小数，不是百分数字符串
- `consecutiveCompletedDays` 按 `completedAt` 倒推连续自然日

### 5.2 获取趋势统计

- 方法：`GET`
- 路径：`/api/app/v1/stats/trend`
- 是否鉴权：是

查询参数：

- `range`：必填，可选 `week`、`month`

成功响应示例：

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "date": "2026-03-17",
      "count": 0
    },
    {
      "date": "2026-03-18",
      "count": 1
    }
  ]
}
```

关键规则：

- 仅统计已完成任务数量
- 返回按天补齐的数组，不会漏天
- `week` 返回最近 7 天
- `month` 返回最近 30 天

## 6. 枚举值速查

### 6.1 任务状态 `TaskStatus`

- `TODO`
- `DONE`
- `EXPIRED`

### 6.2 任务优先级 `TaskPriority`

- `LOW`
- `MEDIUM`
- `HIGH`

### 6.3 重复类型 `RepeatType`

- `NONE`
- `DAILY`
- `WEEKLY`
- `MONTHLY`
