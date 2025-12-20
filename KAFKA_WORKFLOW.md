# Kafka 消息队列工作流程文档

## 📋 目录

1. [概述](#概述)
2. [架构设计](#架构设计)
3. [工作流程](#工作流程)
4. [代码实现详解](#代码实现详解)
5. [配置说明](#配置说明)
6. [消息格式](#消息格式)
7. [错误处理](#错误处理)
8. [测试与验证](#测试与验证)

---

## 概述

本项目使用 **Apache Kafka** 作为消息队列中间件，实现学生注册事件的异步处理。当管理员创建新学生时，系统会通过 Kafka 发送消息，触发后续的业务处理流程（如发送欢迎邮件、更新积分等）。

### 核心特性

- ✅ **异步处理**：学生创建操作与后续业务处理解耦
- ✅ **可靠性**：使用手动确认机制，确保消息不丢失
- ✅ **可扩展性**：支持多个消费者处理不同类型的消息
- ✅ **容错性**：Kafka 不可用时，应用仍可正常运行
- ✅ **条件启用**：通过配置控制 Kafka 功能的启用/禁用

---

## 架构设计

### 组件关系图

```
┌─────────────────┐
│  前端 (Vue)     │
│  创建学生请求   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ StudentController│
│  POST /student   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ StudentService  │
│  保存学生数据   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐         ┌──────────────────┐
│ KafkaProducer   │────────▶│  Kafka Broker    │
│  发送消息       │         │  (消息队列)      │
└─────────────────┘         └────────┬─────────┘
                                    │
                                    ▼
                            ┌──────────────────┐
                            │  KafkaConsumer   │
                            │  消费消息        │
                            └────────┬─────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                ▼
            ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
            │发送欢迎邮件  │  │更新学生积分  │  │记录日志     │
            └─────────────┘  └─────────────┘  └─────────────┘
```

### 消息主题 (Topics)

| 主题名称 | 用途 | 消息类型 |
|---------|------|---------|
| `student-register` | 学生注册事件 | 注册消息 |
| `student-welcome` | 欢迎消息 | 欢迎消息 |

### 消费者组

- **Group ID**: `student-hub-group`
- **消费模式**: 手动确认 (Manual Acknowledgment)
- **偏移量策略**: `earliest` (从最早的消息开始消费)

---

## 工作流程

### 完整流程图

```
1. 用户操作
   │
   ├─▶ 前端：管理员创建新学生
   │
2. 后端处理
   │
   ├─▶ StudentController.createStudent()
   │   └─▶ 接收学生数据
   │
   ├─▶ StudentService.save()
   │   ├─▶ 保存学生到数据库
   │   └─▶ 判断是否为新增学生 (id == null)
   │
3. Kafka 消息发送
   │
   ├─▶ KafkaProducer.sendStudentRegisterMessage()
   │   └─▶ 发送到 "student-register" 主题
   │       └─▶ 消息内容：{studentNo, name, email, timestamp, action}
   │
   ├─▶ KafkaProducer.sendWelcomeMessage()
   │   └─▶ 发送到 "student-welcome" 主题
   │       └─▶ 消息内容：{studentNo, name, message, timestamp}
   │
4. Kafka 消息消费
   │
   ├─▶ KafkaConsumer.consumeStudentRegister()
   │   ├─▶ 接收 "student-register" 主题消息
   │   ├─▶ 调用 sendWelcomeEmail()
   │   ├─▶ 调用 updateStudentPoints()
   │   └─▶ 手动确认消息 (ack.acknowledge())
   │
   └─▶ KafkaConsumer.consumeWelcomeMessage()
       ├─▶ 接收 "student-welcome" 主题消息
       ├─▶ 记录欢迎日志
       └─▶ 手动确认消息 (ack.acknowledge())
```

### 详细步骤说明

#### 步骤 1: 学生创建触发

当管理员在前端创建新学生时：

```java
// StudentController.java
@PostMapping
public CommonResponse<Map<String, Object>> createStudent(@RequestBody StudentDTO student) {
    StudentDTO saved = studentService.save(student);
    // ... 返回响应，包含 Kafka 消息状态
}
```

#### 步骤 2: 业务逻辑处理

在 `StudentService.save()` 方法中：

1. **保存学生数据**到数据库
2. **判断是否为新增学生** (`dto.getId() == null`)
3. **发送 Kafka 消息**（如果是新增）

```java
// StudentService.java
if (dto.getId() == null) {
    // 发送 Kafka 消息
    kafkaProducer.sendStudentRegisterMessage(...);
    kafkaProducer.sendWelcomeMessage(...);
}
```

#### 步骤 3: 消息发送

**消息 1: 学生注册消息**

```java
// KafkaProducer.java
public void sendStudentRegisterMessage(String studentNo, String name, String email) {
    Map<String, Object> message = new HashMap<>();
    message.put("studentNo", studentNo);
    message.put("name", name);
    message.put("email", email);
    message.put("timestamp", System.currentTimeMillis());
    message.put("action", "register");
    
    kafkaTemplate.send("student-register", studentNo, message);
}
```

**消息 2: 欢迎消息**

```java
// KafkaProducer.java
public void sendWelcomeMessage(String studentNo, String name) {
    Map<String, Object> message = new HashMap<>();
    message.put("studentNo", studentNo);
    message.put("name", name);
    message.put("message", "欢迎加入学生信息管理系统！");
    message.put("timestamp", System.currentTimeMillis());
    
    kafkaTemplate.send("student-welcome", studentNo, message);
}
```

#### 步骤 4: 消息消费

**消费者 1: 处理注册消息**

```java
// KafkaConsumer.java
@KafkaListener(topics = "student-register", groupId = "student-hub-group")
public void consumeStudentRegister(Map<String, Object> message, Acknowledgment ack) {
    // 1. 解析消息
    String studentNo = (String) message.get("studentNo");
    String name = (String) message.get("name");
    String email = (String) message.get("email");
    
    // 2. 执行业务逻辑
    sendWelcomeEmail(name, email);
    updateStudentPoints(studentNo, 100);
    
    // 3. 手动确认消息
    ack.acknowledge();
}
```

**消费者 2: 处理欢迎消息**

```java
// KafkaConsumer.java
@KafkaListener(topics = "student-welcome", groupId = "student-hub-group")
public void consumeWelcomeMessage(Map<String, Object> message, Acknowledgment ack) {
    String name = (String) message.get("name");
    log.info("欢迎 {} 加入系统！", name);
    
    ack.acknowledge();
}
```

---

## 代码实现详解

### 1. 配置类

#### KafkaConfig.java

负责配置 Kafka 的 Producer 和 Consumer。

**Producer 配置**：

```java
@Bean
public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");  // 等待所有副本确认
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);    // 重试 3 次
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // 幂等性
    return new DefaultKafkaProducerFactory<>(configProps);
}
```

**Consumer 配置**：

```java
@Bean
public ConsumerFactory<String, Object> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "student-hub-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // 手动提交
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // 从最早开始
    return new DefaultKafkaConsumerFactory<>(props);
}
```

**Listener 容器工厂**：

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
}
```

#### KafkaEnableConfig.java

启用 Kafka 监听器功能：

```java
@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@EnableKafka
public class KafkaEnableConfig {
    // 只在配置了 spring.kafka.bootstrap-servers 时启用
}
```

### 2. 生产者 (Producer)

#### KafkaProducer.java

**类注解**：

```java
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
}
```

**关键点**：
- `@ConditionalOnProperty`: 只在配置了 Kafka 服务器地址时才创建 Bean
- `KafkaTemplate`: Spring Kafka 提供的消息发送模板

**发送注册消息**：

```java
public void sendStudentRegisterMessage(String studentNo, String name, String email) {
    Map<String, Object> message = new HashMap<>();
    message.put("studentNo", studentNo);
    message.put("name", name);
    message.put("email", email);
    message.put("timestamp", System.currentTimeMillis());
    message.put("action", "register");
    
    try {
        kafkaTemplate.send(STUDENT_REGISTER_TOPIC, studentNo, message);
        log.info("发送学生注册消息成功: {}", studentNo);
    } catch (Exception e) {
        log.error("发送学生注册消息失败: {}", e.getMessage(), e);
    }
}
```

**参数说明**：
- `STUDENT_REGISTER_TOPIC`: 主题名称 (`"student-register"`)
- `studentNo`: 消息的 Key（用于分区路由）
- `message`: 消息内容（Map 会被序列化为 JSON）

### 3. 消费者 (Consumer)

#### KafkaConsumer.java

**监听注册消息**：

```java
@KafkaListener(topics = "student-register", groupId = "student-hub-group")
public void consumeStudentRegister(Map<String, Object> message, Acknowledgment ack) {
    try {
        log.info("收到学生注册消息: {}", message);
        
        // 解析消息
        String studentNo = (String) message.get("studentNo");
        String name = (String) message.get("name");
        String email = (String) message.get("email");
        
        // 执行业务逻辑
        sendWelcomeEmail(name, email);
        updateStudentPoints(studentNo, 100);
        
        // 手动确认
        ack.acknowledge();
    } catch (Exception e) {
        log.error("处理学生注册消息失败: {}", e.getMessage(), e);
        throw e;  // 抛出异常，消息会重新消费
    }
}
```

**关键点**：
- `@KafkaListener`: 标记方法为 Kafka 消息监听器
- `Acknowledgment`: 手动确认对象，必须调用 `acknowledge()` 才能确认消息
- 异常处理：如果抛出异常，消息不会被确认，Kafka 会重新投递

### 4. 业务集成

#### StudentService.java

在学生保存方法中集成 Kafka：

```java
@Transactional
public StudentDTO save(StudentDTO dto) {
    // 1. 保存学生数据
    Student saved = studentRepository.save(student);
    StudentDTO savedDTO = convertToDTO(saved);
    
    // 2. 如果是新增学生，发送 Kafka 消息
    if (dto.getId() == null) {
        boolean kafkaSuccess = false;
        if (kafkaProducer != null) {
            try {
                kafkaProducer.sendStudentRegisterMessage(
                    savedDTO.getStudentNo(),
                    savedDTO.getName(),
                    savedDTO.getEmail()
                );
                kafkaProducer.sendWelcomeMessage(
                    savedDTO.getStudentNo(),
                    savedDTO.getName()
                );
                kafkaSuccess = true;
            } catch (Exception e) {
                kafkaSuccess = false;
            }
        }
        savedDTO.setKafkaMessageSent(kafkaSuccess);
    }
    
    return savedDTO;
}
```

**关键点**：
- `@Autowired(required = false)`: KafkaProducer 可能为 null（如果 Kafka 未启用）
- 异常处理：Kafka 发送失败不影响主流程
- 状态记录：记录 Kafka 消息发送状态，返回给前端

#### StudentController.java

返回 Kafka 消息状态给前端：

```java
@PostMapping
public CommonResponse<Map<String, Object>> createStudent(@RequestBody StudentDTO student) {
    StudentDTO saved = studentService.save(student);
    
    Map<String, Object> response = new HashMap<>();
    response.put("student", saved);
    
    // 如果是新增学生，返回 Kafka 消息状态
    if (student.getId() == null) {
        Boolean kafkaSent = saved.getKafkaMessageSent();
        if (kafkaSent == null) {
            kafkaSent = false;
        }
        response.put("kafkaMessageSent", kafkaSent);
        if (kafkaSent) {
            response.put("kafkaMessage", "Kafka 消息已成功发送...");
        } else {
            response.put("kafkaMessage", "Kafka 消息发送失败...");
        }
    }
    
    return CommonResponse.success(response);
}
```

---

## 配置说明

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all              # 等待所有副本确认
      retries: 3             # 重试 3 次
      enable-idempotence: true  # 启用幂等性
    consumer:
      group-id: student-hub-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      enable-auto-commit: false  # 手动提交
      auto-offset-reset: earliest  # 从最早的消息开始
      properties:
        spring.json.trusted.packages: "*"  # 允许反序列化所有包
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `bootstrap-servers` | Kafka 服务器地址 | `localhost:9092` |
| `acks` | 生产者确认级别 | `all` (最可靠) |
| `retries` | 重试次数 | `3` |
| `enable-idempotence` | 启用幂等性 | `true` |
| `enable-auto-commit` | 自动提交偏移量 | `false` (手动提交) |
| `auto-offset-reset` | 偏移量重置策略 | `earliest` |

---

## 消息格式

### 学生注册消息 (student-register)

```json
{
  "studentNo": "2024001",
  "name": "张三",
  "email": "zhangsan@example.com",
  "timestamp": 1703123456789,
  "action": "register"
}
```

### 欢迎消息 (student-welcome)

```json
{
  "studentNo": "2024001",
  "name": "张三",
  "message": "欢迎加入学生信息管理系统！",
  "timestamp": 1703123456790
}
```

---

## 错误处理

### 1. Kafka 服务不可用

**场景**：Kafka 服务器未启动或连接失败

**处理方式**：
- Producer 发送失败时捕获异常，不影响主流程
- 返回 `kafkaMessageSent: false` 给前端
- 前端显示警告消息

**代码**：

```java
try {
    kafkaTemplate.send(topic, key, message);
} catch (Exception e) {
    log.error("发送消息失败: {}", e.getMessage(), e);
    // 不影响主流程
}
```

### 2. 消息消费失败

**场景**：消费者处理消息时抛出异常

**处理方式**：
- 不调用 `ack.acknowledge()`
- Kafka 会重新投递消息
- 记录错误日志

**代码**：

```java
@KafkaListener(...)
public void consumeMessage(Map<String, Object> message, Acknowledgment ack) {
    try {
        // 处理消息
        processMessage(message);
        ack.acknowledge();  // 成功才确认
    } catch (Exception e) {
        log.error("处理消息失败", e);
        throw e;  // 抛出异常，消息会重新消费
    }
}
```

### 3. 条件启用

**场景**：Kafka 未配置或未启用

**处理方式**：
- 使用 `@ConditionalOnProperty` 注解
- Kafka 相关 Bean 不会创建
- 应用仍可正常运行

**代码**：

```java
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@Service
public class KafkaProducer {
    // 只在配置了 bootstrap-servers 时才创建
}
```

---

## 测试与验证

### 1. 启动 Kafka

**使用 Docker**：

```bash
docker run -d --name kafka -p 9092:9092 apache/kafka:latest
```

**验证 Kafka 运行**：

```bash
docker ps | grep kafka
```

### 2. 测试消息发送

1. **启动应用**
2. **创建新学生**（通过前端或 API）
3. **查看后端日志**：

```
发送学生注册消息成功: 2024001
发送欢迎消息成功: 2024001
收到学生注册消息: {studentNo=2024001, name=张三, ...}
发送欢迎邮件到: zhangsan@example.com, 收件人: 张三
为学生 2024001 增加积分: 100
收到欢迎消息: {studentNo=2024001, name=张三, ...}
欢迎 张三 加入系统！
```

### 3. 查看 Kafka 消息

**使用 Kafka 命令行工具**：

```bash
# 查看 student-register 主题的消息
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic student-register \
  --from-beginning

# 查看 student-welcome 主题的消息
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic student-welcome \
  --from-beginning
```

### 4. 前端验证

创建新学生后，前端会显示：

- ✅ **成功**：`Kafka 消息已成功发送：学生注册消息和欢迎消息已发送到消息队列`
- ⚠️ **失败**：`Kafka 消息发送失败：请检查 Kafka 服务是否正常运行`

---

## 总结

### 优势

1. **解耦**：学生创建与后续业务处理解耦
2. **异步**：提高响应速度，用户体验更好
3. **可靠**：使用手动确认，确保消息不丢失
4. **可扩展**：易于添加新的消费者处理不同类型的消息
5. **容错**：Kafka 不可用时，应用仍可正常运行

### 适用场景

- ✅ 需要异步处理的业务操作
- ✅ 需要解耦的业务流程
- ✅ 需要可靠消息传递的场景
- ✅ 需要支持高并发的系统

### 注意事项

1. **Kafka 服务必须运行**：否则消息无法发送
2. **手动确认**：消费者必须调用 `ack.acknowledge()`，否则消息会重复消费
3. **异常处理**：消费者抛出异常时，消息会重新投递，需要确保幂等性
4. **配置检查**：确保 `application.yml` 中的 Kafka 配置正确

---

## 相关文件

- `backend/src/main/java/com/eduvista/kafka/KafkaProducer.java` - 消息生产者
- `backend/src/main/java/com/eduvista/kafka/KafkaConsumer.java` - 消息消费者
- `backend/src/main/java/com/eduvista/config/KafkaConfig.java` - Kafka 配置
- `backend/src/main/java/com/eduvista/config/KafkaEnableConfig.java` - Kafka 启用配置
- `backend/src/main/java/com/eduvista/service/StudentService.java` - 业务服务（集成 Kafka）
- `backend/src/main/java/com/eduvista/controller/StudentController.java` - 控制器（返回 Kafka 状态）
- `backend/src/main/resources/application.yml` - 配置文件

---

**文档版本**: 1.0  
**最后更新**: 2024-12-20

