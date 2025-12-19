# Redis 缓存工作流梳理

## 总览
- 缓存类型：Spring Cache（统一 cacheNames=students）、手动 Redis 哈希、操作计数。
- 主要文件：
  - `backend/src/main/java/com/eduvista/service/StudentService.java`
  - `backend/src/main/java/com/eduvista/service/RedisCacheService.java`
  - `backend/src/main/java/com/eduvista/config/RedisConfig.java`
  - `backend/src/main/java/com/eduvista/aspect/OperationLogAspect.java`
  - `backend/src/main/java/com/eduvista/controller/DashboardController.java`
  - `backend/src/main/java/com/eduvista/config/DataInitializer.java`
  - 配置：`backend/src/main/resources/application.yml`

## 配置与序列化
- `RedisConfig`
  - `@EnableCaching` 开启 Spring Cache。
  - `CacheManager`：默认 TTL 30 分钟，键用 `StringRedisSerializer`，值用 `GenericJackson2JsonRedisSerializer`（支持 PageImpl/JavaTime/类型信息）。
  - `RedisTemplate`：同样使用 JSON 序列化，键/hashKey 为字符串，值/hashValue 为 JSON。
  - `RedissonClient`：单机模式，地址 `redis://{spring.data.redis.host}:{spring.data.redis.port}`。
- `application.yml`：
  - MySQL 连接已带 `allowPublicKeyRetrieval=true`。
  - Redis：`spring.data.redis.host/port/password/database`；Session 使用 Redis。

## 数据流与缓存逻辑
### 1) 学生查询（Spring Cache，DTO）
- 入口：`StudentController`
  - `/student/list` 调 `studentService.findAll(pageable)` 或 `search(keyword, pageable)`
  - `/student/{id}` 调 `findById(id)`
- Service：`StudentService`
  - `@CacheConfig(cacheNames = "students")`
  - `@Cacheable` 键规范：
    - 分页：`page:{page}:size:{size}`
    - 单条：`id:{id}`
    - 搜索：`search:{keyword}:page:{page}:size:{size}`
  - `@CacheEvict(allEntries = true)`：`save`、`deleteById` 全量清理 students 缓存。
  - DTO <-> Entity 转换；班级关联通过 `classId/className` 避免循环引用和懒加载问题。
- 底层存储：通过 `CacheManager` 写入 Redis（值为 JSON 序列化的 DTO 或 PageResponse<DTO>）。

### 2) 学生哈希缓存（手动，DTO）
- 场景：分布式锁保护下的精细化缓存。
- 实现：`RedisCacheService`
  - 键前缀：`student:hash:{id}`
  - TTL：30 分钟
  - 字段：id、studentNo、name、gender、phone、email、classId、className、avatar（空值写空串防 NPE）。
  - 方法：
    - `cacheStudentHash(StudentDTO)` 写入 Hash
    - `getStudentFromHash(id)` 读取 Hash
    - `getStudentWithLock(id, studentService)`：Redisson 分布式锁 -> 先查 Hash，不存在则调用 `studentService.findById`（走 Spring Cache/DB）并回填 Hash。
- 触发：
  - `DataInitializer.warmupCache()`：启动预热时对所有学生调用 `cacheStudentHash`。
  - `getStudentWithLock` 内部。

### 3) 操作计数（手动，字符串键）
- 计数键：`operation:count:{operation}`，TTL 1 天。
- 写入：`OperationLogAspect.logOperation()` 每次 controller 调用后执行 `incrementOperationCount(operationName)`。
- 读取：`DashboardController.getOperationStats()` 汇总 `getStudentById/getStudentList/createStudent/updateStudent/deleteStudent/login` 六类计数。

## 启动与依赖
- 确保 Redis 在本地或指定地址可用：
  - 提供了 `start-redis.sh` / `start-redis.bat`；或使用 `docker-compose up redis`（若已在 compose 中定义）。
- Spring Cache 启用条件：`spring.data.redis.host` 存在（`RedisConfig` 上的 ConditionalOnProperty）。
- 若需要关闭缓存：可移除/注释 `spring.data.redis.*` 配置或关闭 `@EnableCaching`（不推荐）。

## 日志与排障
- 连接失败常见错误：`Unable to connect to Redis server: localhost:6379` -> 检查 Redis 启动/host/port/password。
- 公钥检索错误（MySQL）：已通过 `allowPublicKeyRetrieval=true` 解决。
- 序列化问题：使用统一的 JSON 序列化器，已处理 PageImpl/JavaTime；如遇类型信息问题，可查看 `RedisConfig.createJacksonSerializer`。

## 扩展建议
- 若对 `Class` / `Course` 增加缓存，建议同样使用 DTO + `@CacheConfig` + 统一键命名，避免实体循环引用。
- 缓存粒度：大列表走 Spring Cache，热点单体可结合 Hash + 分布式锁；更新路径统一清空相关命名空间。
- TTL 调整：在 `RedisConfig`（CacheManager 默认 TTL）与 `RedisCacheService`（Hash TTL/计数 TTL）可分别调整。
