# Miniapp 后端错误码文档

## 1. 统一失败响应

后端失败时统一返回：

```json
{
  "code": 40100,
  "message": "Unauthorized",
  "data": null
}
```

说明：

- `code`：业务错误码
- `message`：错误说明
- `data`：失败时固定为 `null`

## 2. 错误码清单

| 业务错误码 | HTTP 状态码 | 常量名 | 含义 | miniapp 建议处理 |
| --- | --- | --- | --- | --- |
| `40000` | `400` | `BAD_REQUEST` | 请求参数合法但业务不允许，例如传了 `EXPIRED` 状态、恢复未删除任务、统计范围非法 | 提示用户当前操作不允许 |
| `40100` | `401` | `UNAUTHORIZED` | 未登录、token 缺失、token 无效、token 过期 | 清理本地 token，重新走登录 |
| `40300` | `403` | `FORBIDDEN` | 已认证但无权限 | 统一提示无权限，通常无需重试 |
| `40400` | `404` | `NOT_FOUND` | 资源不存在，例如任务或分类不存在 | 提示资源已不存在，并刷新列表 |
| `42200` | `422` | `VALIDATION_ERROR` | 参数校验失败，例如必填字段缺失 | 表单页直接展示校验信息 |
| `50010` | `500` | `WECHAT_LOGIN_FAILED` | 微信登录换 session 失败，或后端未配置微信参数 | 登录页提示重试，必要时联系后端检查配置 |
| `50000` | `500` | `SYSTEM_ERROR` | 未归类系统异常 | 提示系统繁忙，请稍后重试 |

## 3. 典型错误场景

### 3.1 未登录或 token 失效

HTTP 状态码：`401`

响应示例：

```json
{
  "code": 40100,
  "message": "Unauthorized",
  "data": null
}
```

前端建议：

- 统一拦截 `40100`
- 清空本地 `accessToken`
- 跳回登录流程

### 3.2 参数校验失败

HTTP 状态码：`422`

响应示例：

```json
{
  "code": 42200,
  "message": "title must not be blank, nickName must not be blank",
  "data": null
}
```

前端建议：

- 表单页直接 toast 或内联展示 `message`
- 不要重试同一请求，先修正输入

### 3.3 业务异常

HTTP 状态码取决于业务类型。

示例 1：任务不存在

```json
{
  "code": 40400,
  "message": "Task not found",
  "data": null
}
```

示例 2：状态不允许前端设置为过期

```json
{
  "code": 40000,
  "message": "Status EXPIRED is system controlled",
  "data": null
}
```

前端建议：

- `40400`：刷新当前列表或退出详情页
- `40000`：提示用户当前操作无效

### 3.4 微信登录失败

HTTP 状态码：`500`

响应示例：

```json
{
  "code": 50010,
  "message": "WeChat login failed",
  "data": null
}
```

也可能返回更具体消息，例如：

```json
{
  "code": 50010,
  "message": "WeChat appId/appSecret is not configured",
  "data": null
}
```

前端建议：

- 提示“登录失败，请稍后重试”
- 如果持续失败，通知后端检查微信配置

## 4. miniapp 错误处理约定

- 请求成功但 `code != 0`，也按失败处理
- `40100`：统一回登录
- `42200`：优先把 `message` 直接反馈给用户
- `40400`：详情页建议返回上一级并刷新
- `50010`、`50000`：统一提示“服务异常，请稍后重试”
