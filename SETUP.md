# 项目启动指南

## 前置准备

### 1. 安装必要软件
- JDK 17+
- Node.js 18+
- Maven 3.8+
- Docker Desktop (可选，用于快速启动基础服务)

### 2. 数据库准备

#### 方式一：使用Docker（推荐）
```bash
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root123456 \
  -e MYSQL_DATABASE=student_hub \
  mysql:8.0

docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7-alpine

docker run -d \
  --name zookeeper \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:latest \
  -e ZOOKEEPER_CLIENT_PORT=2181

docker run -d \
  --name kafka \
  -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:latest
```

#### 方式二：本地安装
- MySQL 8.0: 创建数据库 `student_hub`
- Redis 7: 默认配置启动
- Kafka: 需要Zookeeper，配置参考官方文档

## 后端启动

### 1. 进入后端目录
```bash
cd backend
```

### 2. 修改配置（如需要）
编辑 `src/main/resources/application.yml`，确认数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/student_hub?...
    username: root
    password: root123456
```

### 3. 编译打包
```bash
mvn clean install
```

### 4. 运行应用
```bash
mvn spring-boot:run
```

或者运行打包后的jar：
```bash
java -jar target/student-hub-backend-1.0.0.jar
```

### 5. 验证启动
访问: http://localhost:8080/api/auth/login

## 前端启动

### 1. 进入前端目录
```bash
cd frontend
```

### 2. 安装依赖
```bash
npm install
```

### 3. 启动开发服务器
```bash
npm run dev
```

### 4. 验证启动
访问: http://localhost:5173

## Docker Compose 一键启动（推荐）

### 启动所有服务
```bash
docker-compose up -d
```

### 查看日志
```bash
docker-compose logs -f
```

### 停止服务
```bash
docker-compose down
```

## 默认账户

- **管理员**: `admin` / `admin123`
- **权限**: ADMIN (可访问所有功能)

## 常见问题

### 1. 后端启动失败
- 检查MySQL、Redis、Kafka是否正常运行
- 检查端口是否被占用（8080, 3306, 6379, 9092）
- 查看日志文件定位错误

### 2. 前端无法连接后端
- 确认后端服务已启动（http://localhost:8080）
- 检查 `vite.config.js` 中的代理配置
- 检查浏览器的网络请求（F12开发者工具）

### 3. 文件上传失败
- 检查 `application.yml` 中的 `file.upload.path` 配置
- 确保上传目录有写权限
- Windows系统路径使用正斜杠 `/` 或双反斜杠 `\\`

### 4. Kafka消息发送失败
- 确认Zookeeper和Kafka都已启动
- 检查Kafka配置中的bootstrap-servers
- 查看Kafka日志确认Topic是否创建成功

### 5. Redis连接失败
- 确认Redis服务已启动
- 检查Redis密码配置（默认无密码）
- 确认Redis端口6379未被占用

## 开发建议

1. **IDE配置**
   - 后端: IntelliJ IDEA / Eclipse (安装Lombok插件)
   - 前端: VS Code / WebStorm (安装Vue插件)

2. **代码格式化**
   - 后端: 使用Google Java Style Guide
   - 前端: 使用Prettier + ESLint

3. **调试技巧**
   - 后端: 使用Spring Boot DevTools热重载
   - 前端: 使用Vue DevTools浏览器插件
   - 使用Postman测试API接口

4. **性能优化**
   - 生产环境关闭SQL日志：`show-sql: false`
   - 调整Redis缓存TTL根据实际需求
   - 前端生产构建：`npm run build`

## 下一步

1. 查看 README.md 了解项目详细功能
2. 查看 API文档了解接口规范
3. 根据需要修改配置和业务逻辑
4. 添加单元测试和集成测试

