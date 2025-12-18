# 使用本地MySQL说明

## ✅ 当前状态

- **Redis**: ✅ 已启动（Docker容器，端口6379）
- **MySQL**: ✅ 本地MySQL服务正在运行（端口3306）

你的应用已经配置为使用本地MySQL，配置如下：
- 数据库: `student_hub`
- 用户名: `kongshan`
- 密码: `zmt20041204`
- 端口: `3306`

## 📝 需要做的

只需要确保数据库 `student_hub` 存在即可。

### 方法1：使用MySQL命令行

```bash
# 登录MySQL（使用你的密码）
mysql -u kongshan -p

# 在MySQL中执行
CREATE DATABASE IF NOT EXISTS student_hub 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

# 查看数据库
SHOW DATABASES;

# 退出
EXIT;
```

### 方法2：使用MySQL Workbench或其他工具

1. 连接到本地MySQL（用户：kongshan）
2. 执行以下SQL：
```sql
CREATE DATABASE IF NOT EXISTS student_hub 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

或者直接运行项目提供的 `创建数据库.sql` 文件。

### 方法3：直接启动应用（会自动创建）

Spring Boot配置了 `ddl-auto: update`，如果数据库不存在，应用会尝试创建（如果用户有权限）。更安全的做法是手动创建数据库。

## 🚀 启动应用

数据库准备好后，直接启动应用：

```bash
cd backend
mvn spring-boot:run
```

应用启动时会：
1. 自动创建表结构（因为配置了 `ddl-auto: update`）
2. 初始化默认数据（管理员账户、角色等）
3. 连接Redis进行缓存

## ✨ 优势

使用本地MySQL的好处：
- ✅ 性能更好（无Docker网络开销）
- ✅ 数据持久化在本地，易于备份
- ✅ 可以使用熟悉的MySQL工具管理数据

## 🔄 如果需要使用Docker MySQL

如果你想改用Docker的MySQL容器：

1. **停止本地MySQL服务**（如果需要释放3306端口）：
   ```bash
   net stop MySQL80
   # 或在服务管理器中停止MySQL服务
   ```

2. **启动Docker MySQL**：
   ```bash
   docker run -d --name student-hub-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root123456 -e MYSQL_DATABASE=student_hub mysql:8.0
   ```

3. **修改 application.yml**，使用root用户：
   ```yaml
   username: root
   password: root123456
   ```

## 📋 验证

运行 `检查服务状态.bat` 可以查看所有服务的状态。

