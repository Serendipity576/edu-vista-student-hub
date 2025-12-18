# Redis 启动指南

## 问题说明
应用启动时需要连接Redis（端口6379），如果Redis未运行会导致启动失败。

## 解决方案

### 方案一：使用Docker启动Redis（推荐）

如果你已经安装了Docker，可以使用以下命令启动Redis：

```bash
# 启动Redis容器
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 验证Redis是否运行
docker ps | grep redis

# 查看Redis日志
docker logs redis
```

### 方案二：安装Redis for Windows

#### 1. 使用WSL（Windows Subsystem for Linux）
如果安装了WSL，可以在WSL中安装Redis：

```bash
# 在WSL中执行
sudo apt update
sudo apt install redis-server
redis-server
```

#### 2. 使用Memurai（Redis的Windows替代品）
下载地址：https://www.memurai.com/

#### 3. 使用Windows版Redis（不推荐，已停止维护）
可以从GitHub下载：https://github.com/microsoftarchive/redis/releases

### 方案三：修改配置跳过Redis（临时方案）

如果需要临时启动应用而不使用Redis，可以修改配置：

1. 编辑 `application.yml`，注释掉Redis相关配置
2. 移除 `@EnableCaching` 注解
3. 注意：这会导致缓存、会话管理等功能不可用

**不推荐此方案**，因为会影响应用功能。

## 验证Redis连接

启动Redis后，可以使用以下命令测试：

```bash
# 使用Docker
docker exec -it redis redis-cli ping
# 应该返回: PONG

# 或者使用redis-cli（如果已安装）
redis-cli -h localhost -p 6379 ping
```

## 启动应用

确保Redis运行后，重新启动Spring Boot应用：

```bash
cd backend
mvn spring-boot:run
```

## 常见问题

### 端口6379已被占用
如果端口6379已被占用，可以：
1. 修改Redis端口
2. 或者修改 `application.yml` 中的Redis端口配置

### Redis启动后立即停止
检查Redis日志查看错误信息：
```bash
docker logs redis
```

