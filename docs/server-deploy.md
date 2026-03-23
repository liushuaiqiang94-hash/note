# 后端服务器部署说明

本文档说明如何将 `apps/server` 构建出的镜像部署到服务器，并通过 `Nginx + Docker Compose + MySQL` 提供可供小程序访问的 HTTPS 接口。

## 1. 部署结构

- `mysql`：持久化业务数据
- `server`：Spring Boot 后端容器
- `nginx`：对外暴露 80/443，转发请求到后端

推荐拓扑：

```text
微信小程序 -> HTTPS 域名 -> Nginx -> task-list-server -> MySQL
```

## 2. 前置条件

部署前需要准备：

- 一台已安装 Docker 和 Docker Compose 的 Linux 服务器
- 一个可解析到服务器公网 IP 的域名，例如 `api.example.com`
- 域名对应的 SSL 证书
- 微信小程序正式 `AppID` 和 `AppSecret`
- GitHub Actions 已成功将镜像推送到 GHCR

当前镜像默认地址：

```text
ghcr.io/liushuaiqiang94-hash/note/server:latest
```

## 3. 准备部署文件

项目根目录已提供：

- [docker-compose.yml](/C:/Users/Lll/Desktop/t_file/code/encoding/demo222/docker-compose.yml)
- [.env.example](/C:/Users/Lll/Desktop/t_file/code/encoding/demo222/.env.example)
- [nginx.conf](/C:/Users/Lll/Desktop/t_file/code/encoding/demo222/deploy/nginx.conf)

实际部署时建议复制 `.env.example` 为 `.env`：

```bash
cp .env.example .env
```

然后修改 `.env` 中的以下内容：

- `SERVER_IMAGE`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `APP_JWT_SECRET`
- `WECHAT_APP_ID`
- `WECHAT_APP_SECRET`

## 4. 修改域名配置

默认 Nginx 配置文件中的域名是：

```nginx
server_name api.example.com;
```

部署前需要把 [nginx.conf](/C:/Users/Lll/Desktop/t_file/code/encoding/demo222/deploy/nginx.conf) 中的 `api.example.com` 改成你的真实域名。

## 5. 准备证书

当前配置默认读取：

```text
deploy/certs/fullchain.pem
deploy/certs/privkey.pem
```

你可以：

- 手动放入现成证书
- 或先用 Certbot 申请证书后复制到该目录

如果证书文件不存在，Nginx 容器会启动失败。

## 6. 启动服务

在项目根目录执行：

```bash
docker compose pull
docker compose up -d
```

查看运行状态：

```bash
docker compose ps
```

查看后端日志：

```bash
docker compose logs -f server
```

查看 Nginx 日志：

```bash
docker compose logs -f nginx
```

## 7. 微信小程序侧配置

服务启动后，还需要在微信公众平台配置：

- 服务器域名改为你的 HTTPS 域名
- 域名必须和 Nginx 对外访问域名一致

注意：

- 小程序正式环境不能使用 `localhost`
- 只能使用 HTTPS 域名

## 8. 升级镜像

当 GitHub Actions 构建出新镜像后，在服务器执行：

```bash
docker compose pull server
docker compose up -d server
```

如果想完整刷新：

```bash
docker compose pull
docker compose up -d
```

## 9. 常见问题

### 9.1 小程序请求失败

检查项：

- 域名是否已加入微信后台服务器域名
- HTTPS 证书是否有效
- Nginx 是否正常启动
- 服务器安全组是否放通 80/443

### 9.2 微信登录失败

检查项：

- `WECHAT_APP_ID` 是否正确
- `WECHAT_APP_SECRET` 是否正确
- 小程序实际使用的 `AppID` 是否与后端配置一致

### 9.3 数据库连接失败

检查项：

- `MYSQL_USER` / `MYSQL_PASSWORD` 是否与容器环境一致
- `SPRING_DATASOURCE_URL` 是否仍指向 `mysql:3306`
- MySQL 容器是否已通过健康检查

## 10. 生产建议

上线前建议再补这几项：

- 定期备份 MySQL 数据卷
- 将 `.env` 放到服务器，不提交到 Git
- 为 GHCR 拉取配置稳定的凭据
- 给 Nginx 增加访问日志和限流配置
- 给后端增加监控、告警和健康检查接口
