# 学生信息管理系统

基于Vue3 + Spring Boot的全栈学生信息管理系统，满足现代化企业级开发实践要求。

## 技术栈

### 前端
- Vue 3 (Composition API)
- Pinia (状态管理)
- Vue Router (路由管理)
- Element Plus (UI组件库)
- ECharts (数据可视化)
- Vite (构建工具)
- SCSS (样式预处理)

### 后端
- Spring Boot 3.2.0
- Spring Security (安全框架)
- Spring Data JPA (持久层)
- Spring Data Redis (缓存)
- Spring Session (会话管理)
- Spring Kafka (消息队列)
- JWT (身份认证)
- Redisson (分布式锁)
- MySQL 8.0
- Kafka

## 功能特性

### 核心功能
- ✅ 用户认证与授权（JWT + Spring Security）
- ✅ 学生信息CRUD操作
- ✅ 头像上传与预览
- ✅ 班级相册墙（瀑布流布局）
- ✅ 数据仪表盘（实时操作统计）
- ✅ 操作日志记录（AOP切面）
- ✅ 权限控制（基于角色）
- ✅ 主题切换（亮色/暗色模式）
- ✅ 响应式布局（移动端适配）

### 技术亮点
- ✅ Redis缓存策略（30分钟TTL）
- ✅ 防击穿机制（Redisson分布式锁）
- ✅ Kafka消息队列（异步处理）
- ✅ AOP操作日志（实时统计）
- ✅ ECharts数据可视化
- ✅ 路由守卫（访问控制）
- ✅ 动态路由加载

## 项目结构

```
edu-vista-student-hub/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/eduvista/
│   │   │   │   ├── aspect/        # AOP切面
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── controller/    # 控制器
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── kafka/         # Kafka消息处理
│   │   │   │   ├── repository/    # 数据访问层
│   │   │   │   ├── security/      # 安全组件
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   └── util/          # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── pom.xml
│   └── Dockerfile
├── frontend/               # 前端项目
│   ├── src/
│   │   ├── assets/        # 静态资源
│   │   ├── layouts/       # 布局组件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia状态管理
│   │   ├── styles/        # 样式文件
│   │   ├── utils/         # 工具函数
│   │   ├── views/         # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── docker-compose.yml     # Docker编排
└── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0
- Redis 7+
- Kafka 3.6+

### 本地开发

#### 1. 启动基础服务
```bash
# 使用Docker启动MySQL、Redis、Kafka
docker-compose up -d mysql redis zookeeper kafka
```

#### 2. 启动后端
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务启动在: http://localhost:8080

#### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

前端服务启动在: http://localhost:5173

### Docker部署

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 默认账户

- 管理员: `admin` / `admin123`
- 角色: ADMIN

## API文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 学生管理
- `GET /api/student/list` - 获取学生列表（分页）
- `GET /api/student/{id}` - 获取学生详情
- `POST /api/student` - 创建学生（需ADMIN权限）
- `PUT /api/student/{id}` - 更新学生（需ADMIN权限）
- `DELETE /api/student/{id}` - 删除学生（需ADMIN权限）
- `POST /api/student/{id}/avatar` - 上传头像

### 仪表盘
- `GET /api/dashboard/operation-stats` - 获取操作统计（需ADMIN权限）

## 核心实现

### JWT认证流程
1. 用户登录 → 验证用户名密码
2. 生成JWT Token → 包含用户信息和角色
3. Token存储Redis → 24小时有效期
4. 前端存储Token → localStorage
5. 请求携带Token → Authorization Header

### Redis缓存策略
- 学生查询：`@Cacheable`，30分钟TTL
- 操作计数：实时更新，1天TTL
- 学生Hash：手动缓存，30分钟TTL
- 防击穿：Redisson分布式锁

### AOP操作日志
- 拦截所有Controller方法
- 记录：用户名、操作、URL、IP、参数、执行时间
- 存储：MySQL + Redis实时计数

### Kafka消息处理
- Topic: `student-register`
- Producer: 学生注册后发送消息
- Consumer: 异步处理欢迎邮件、积分变更
- 可靠性：手动ACK + 异常重试

## 开发规范

### 代码规范
- 后端：遵循Spring Boot最佳实践
- 前端：遵循Vue3 Composition API规范
- 命名：驼峰命名法，语义化

### Git提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建/工具相关

## 许可证

MIT License

## 作者

学生信息管理系统开发团队

