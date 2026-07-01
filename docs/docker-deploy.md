# Docker 一键部署指南

## 一、前置条件

### 1.1 安装要求

| 软件           | 最低版本  | 说明                                |
| -------------- | --------- | ----------------------------------- |
| Docker Desktop | 4.15+     | 包含 Docker Engine 和 Docker Compose |
| Docker Engine  | 20.10+    | 容器运行时                          |
| Docker Compose | V2        | 多容器编排                          |

### 1.2 资源建议

| 资源   | 开发模式 | 生产模式 |
| ------ | -------- | -------- |
| 内存   | 4GB+     | 2GB+     |
| CPU    | 2核+     | 2核+     |
| 磁盘   | 10GB+    | 5GB+     |

### 1.3 验证安装

```powershell
docker --version      # Docker version 20.10+
docker compose version  # Docker Compose version v2.x.x
```

---

## 二、快速开始

```powershell
# 1. 复制环境变量模板
cp .env.example .env

# 2. 启动所有服务（首次会自动构建镜像）
docker-compose up -d --build

# 3. 访问
# 前端: http://localhost:5173
# 后端 API: http://localhost:9090
# Swagger 文档: http://localhost:9090/doc.html
```

启动过程约 3-5 分钟（首次构建），后续启动约 30 秒。

---

## 三、开发模式

### 3.1 启动命令

```powershell
docker-compose up -d --build
```

### 3.2 端口映射

| 服务   | 端口  | 访问地址                      |
| ------ | ----- | ----------------------------- |
| 前端   | 5173  | http://localhost:5173         |
| 后端   | 9090  | http://localhost:9090         |
| MySQL  | 3306  | localhost:3306                |
| Redis  | 6379  | localhost:6379                |

### 3.3 前端热更新

前端容器挂载了 `src/`、`public/`、`index.html`，修改代码后浏览器自动刷新。

```powershell
# 查看前端日志
docker-compose logs -f frontend
```

### 3.4 后端代码修改

后端修改代码后需要重新构建镜像：

```powershell
docker-compose up -d --build backend
```

构建时间约 1-3 分钟，利用 Docker 层缓存可加速到 30 秒。

### 3.5 查看日志

```powershell
# 查看所有服务日志
docker-compose logs -f

# 查看单个服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
docker-compose logs -f redis
```

---

## 四、生产模式

### 4.1 启动命令

```powershell
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

### 4.2 端口映射

| 服务 | 端口 | 访问地址              |
| ---- | ---- | --------------------- |
| Nginx | 80  | http://localhost      |

### 4.3 与开发模式的区别

| 维度       | 开发模式           | 生产模式               |
| ---------- | ------------------ | ---------------------- |
| 前端       | Vite dev server    | Nginx + 静态文件       |
| 热更新     | 支持               | 不支持                 |
| Swagger    | 开启               | 关闭                   |
| 日志级别   | debug              | info                   |
| 访问入口   | :5173（前端）      | :80（统一入口）        |
| API 代理   | Vite proxy         | Nginx proxy            |

### 4.4 生产模式访问方式

所有请求通过 Nginx 统一入口：

- 前端页面：`http://localhost`
- 后端 API：`http://localhost/api/...`
- WebSocket：`ws://localhost/ws`

---

## 五、常用操作

### 5.1 服务管理

```powershell
# 启动
docker-compose up -d

# 停止
docker-compose down

# 重启
docker-compose restart

# 重启单个服务
docker-compose restart backend

# 查看状态
docker-compose ps
```

### 5.2 数据管理

```powershell
# 停止并清除所有数据（数据库、缓存）
docker-compose down -v

# 手动清除数据目录
Remove-Item -Recurse -Force docker/data/mysql, docker/data/redis

# 重新初始化
docker-compose up -d --build
```

### 5.3 更新数据库

修改 `database/migration/` 下的 SQL 文件后：

```powershell
# 1. 重新生成 init.sql
.\scripts\build-init-sql.ps1

# 2. 清除旧数据并重启 MySQL
docker-compose down -v
docker-compose up -d --build mysql
```

### 5.4 进入容器调试

```powershell
# 进入后端容器
docker-compose exec backend sh

# 进入 MySQL 容器
docker-compose exec mysql mysql -u root -pdabashou123 dabashou

# 进入 Redis 容器
docker-compose exec redis redis-cli

# 进入前端容器（开发模式）
docker-compose exec frontend sh
```

### 5.5 重建单个服务

```powershell
# 只重建后端（代码修改后）
docker-compose up -d --build backend

# 只重建前端
docker-compose up -d --build frontend

# 重建所有服务
docker-compose up -d --build
```

---

## 六、环境切换指南

### 6.1 换电脑开发

```powershell
# 1. 克隆代码
git clone <repo-url>
cd DaBaShou

# 2. 复制环境变量
cp .env.example .env

# 3. 启动
docker-compose up -d --build
```

### 6.2 端口冲突

如果默认端口被占用，修改 `.env` 文件：

```bash
# 修改前端端口（默认 5173）
FRONTEND_PORT=8080

# 修改后端端口（默认 9090）
BACKEND_PORT=9091

# 修改 MySQL 端口（默认 3306）
MYSQL_PORT=3307

# 修改 Redis 端口（默认 6379）
REDIS_PORT=6380
```

修改后重启：

```powershell
docker-compose up -d
```

### 6.3 数据迁移

```powershell
# 方式 1：保留数据目录
# 将 docker/data/ 目录一起拷贝到新环境

# 方式 2：导出数据库
docker-compose exec mysql mysqldump -u root -pdabashou123 dabashou > backup.sql

# 在新环境导入
docker-compose exec -T mysql mysql -u root -pdabashou123 dabashou < backup.sql
```

### 6.4 切换开发/生产模式

```powershell
# 停止当前服务
docker-compose down

# 启动生产模式
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

# 切回开发模式
docker-compose up -d --build
```

---

## 七、环境变量说明

在 `.env` 文件中配置，`.env.example` 提供了默认值模板。

| 变量                | 默认值                                    | 说明                          |
| ------------------- | ----------------------------------------- | ----------------------------- |
| `HTTP_PORT`           | `80`                                        | 生产模式 Nginx 端口           |
| `FRONTEND_PORT`       | `5173`                                      | 开发模式前端端口              |
| `BACKEND_PORT`        | `9090`                                      | 后端 API 端口                 |
| `MYSQL_PORT`          | `3306`                                      | MySQL 端口                    |
| `REDIS_PORT`          | `6379`                                      | Redis 端口                    |
| `MYSQL_ROOT_PASSWORD` | `dabashou123`                               | MySQL root 密码               |
| `MYSQL_DATABASE`      | `dabashou`                                  | 数据库名称                    |
| `REDIS_PASSWORD`      | （空）                                    | Redis 密码，留空表示无密码    |
| `JWT_SECRET`          | `your-256-bit-secret-key-here-change-in-production` | JWT 签名密钥（生产环境必改） |
| `SPRING_PROFILES_ACTIVE` | `dev`                                     | Spring Profile（dev/prod）    |

### 安全建议

生产环境部署时，请修改以下变量：

```bash
MYSQL_ROOT_PASSWORD=<强密码>
JWT_SECRET=<随机 256-bit 字符串>
```

---

## 八、文件结构说明

```
DaBaShou/
├── docker-compose.yml              # 开发模式编排（默认）
├── docker-compose.prod.yml         # 生产模式覆盖配置
├── .env.example                    # 环境变量模板
├── docker/
│   ├── backend/
│   │   └── Dockerfile              # 后端多阶段构建（Maven + JRE 21）
│   ├── frontend/
│   │   ├── Dockerfile.dev          # 前端开发（Vite dev server + 热更新）
│   │   ├── Dockerfile.prod         # 前端生产（Nginx + 静态文件）
│   │   └── nginx.conf              # Nginx 配置（反向代理 + SPA）
│   ├── mysql/
│   │   └── init.sql                # 自动生成的数据库初始化脚本
│   └── data/                       # 数据持久化目录（gitignore）
│       ├── mysql/                  # MySQL 数据文件
│       └── redis/                  # Redis 数据文件
└── scripts/
    └── build-init-sql.ps1          # 合并 migration 脚本的工具
```

### 文件作用

| 文件                       | 作用                                             |
| -------------------------- | ------------------------------------------------ |
| `docker-compose.yml`         | 开发模式：前端 Vite + 后端 JAR + MySQL + Redis   |
| `docker-compose.prod.yml`    | 生产模式：覆盖前端为 Nginx，关闭 Swagger         |
| `docker/backend/Dockerfile`  | 后端构建：Maven 编译 → JRE 21 运行时             |
| `docker/frontend/Dockerfile.dev` | 前端开发：Node.js + Vite dev server            |
| `docker/frontend/Dockerfile.prod` | 前端生产：Node.js 构建 → Nginx 托管          |
| `docker/frontend/nginx.conf` | Nginx 配置：静态文件 + `/api` `/ws` 反向代理      |
| `docker/mysql/init.sql`      | 数据库初始化脚本（由 build-init-sql.ps1 生成）   |
| `scripts/build-init-sql.ps1` | 将 database/migration/ 下的 SQL 合并为 init.sql |
| `.env.example`               | 环境变量模板，复制为 .env 后使用                 |

---

## 九、故障排查

### 9.1 端口被占用

```
Error: Bind for 0.0.0.0:3306 failed: port is already allocated
```

**解决**：修改 `.env` 中的端口配置，或停止占用端口的程序：

```powershell
# 查找占用端口的进程
netstat -ano | findstr :3306

# 停止进程
Stop-Process -Id <PID> -Force
```

### 9.2 镜像构建失败

```
ERROR: failed to solve: process "/bin/sh -c mvn package" did not complete
```

**解决**：

```powershell
# 清理 Docker 缓存
docker builder prune -a

# 重新构建
docker-compose up -d --build --no-cache backend
```

### 9.3 数据库连接失败

```
Communications link failure
```

**检查步骤**：

```powershell
# 1. 确认 MySQL 容器运行中
docker-compose ps mysql

# 2. 查看 MySQL 日志
docker-compose logs mysql

# 3. 确认 MySQL 就绪
docker-compose exec mysql mysqladmin ping -u root -pdabashou123

# 4. 确认网络连通
docker-compose exec backend ping mysql
```

### 9.4 init.sql 未执行

MySQL 容器首次启动才会执行 `docker-entrypoint-initdb.d/` 下的脚本。

**解决**：

```powershell
# 清除 MySQL 数据，重新初始化
docker-compose down -v
Remove-Item -Recurse -Force docker/data/mysql
docker-compose up -d --build mysql
```

### 9.5 前端页面空白

**检查步骤**：

```powershell
# 1. 查看前端容器日志
docker-compose logs frontend

# 2. 确认后端 API 可访问
curl http://localhost:9090/doc.html

# 3. 检查浏览器控制台是否有 API 请求错误
```

### 9.6 后端启动慢

后端首次启动需要等待 MySQL 和 Redis 就绪，以及 Spring 初始化，约 30-60 秒。

```powershell
# 查看后端启动日志
docker-compose logs -f backend

# 等待健康检查通过
docker-compose ps
```

---

## 十、常用命令速查

| 场景                 | 命令                                                                |
| -------------------- | ------------------------------------------------------------------- |
| 首次部署             | `cp .env.example .env && docker-compose up -d --build`                |
| 开发模式启动         | `docker-compose up -d`                                                |
| 生产模式启动         | `docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build` |
| 查看状态             | `docker-compose ps`                                                   |
| 查看日志             | `docker-compose logs -f`                                              |
| 重启后端             | `docker-compose up -d --build backend`                                |
| 重启前端             | `docker-compose up -d --build frontend`                               |
| 进入 MySQL           | `docker-compose exec mysql mysql -u root -pdabashou123 dabashou`      |
| 进入 Redis           | `docker-compose exec redis redis-cli`                                 |
| 停止服务             | `docker-compose down`                                                 |
| 清除数据重来         | `docker-compose down -v && docker-compose up -d --build`              |
| 更新数据库           | `.\scripts\build-init-sql.ps1 && docker-compose down -v && docker-compose up -d` |
| 导出数据库           | `docker-compose exec mysql mysqldump -u root -pdabashou123 dabashou > backup.sql` |
| 导入数据库           | `docker-compose exec -T mysql mysql -u root -pdabashou123 dabashou < backup.sql` |
