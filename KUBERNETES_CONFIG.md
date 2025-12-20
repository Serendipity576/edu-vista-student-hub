# Kubernetes 环境配置指南

## Redis 和 Kafka 服务地址配置

在 Kubernetes 环境中，服务之间通过 Service 名称进行通信，而不是 `localhost`。

### 环境变量配置

在 Kubernetes Deployment 中，需要设置以下环境变量：

```yaml
env:
  - name: SPRING_DATA_REDIS_HOST
    value: "redis-service"  # 或 "redis-service.student-hub.svc.cluster.local"
  - name: SPRING_DATA_REDIS_PORT
    value: "6379"
  - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
    value: "kafka-service:9092"  # 或 "kafka-service.student-hub.svc.cluster.local:9092"
```

### 完整的后端 Deployment 示例

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: student-hub
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: your-backend-image:latest
          ports:
            - containerPort: 8080
          env:
            # MySQL 配置
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:mysql://mysql-service:3306/student_hub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
            - name: SPRING_DATASOURCE_USERNAME
              value: "your-username"
            - name: SPRING_DATASOURCE_PASSWORD
              value: "your-password"
            
            # Redis 配置
            - name: SPRING_DATA_REDIS_HOST
              value: "redis-service"
            - name: SPRING_DATA_REDIS_PORT
              value: "6379"
            
            # Kafka 配置
            - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
              value: "kafka-service:9092"
          resources:
            limits:
              cpu: "1000m"
              memory: "1Gi"
```

### 服务发现说明

在 Kubernetes 中：
- **同一 Namespace**：可以直接使用服务名，如 `redis-service`
- **不同 Namespace**：需要使用完整域名，如 `redis-service.student-hub.svc.cluster.local`
- **端口**：如果 Service 的端口是 6379，直接使用服务名即可，不需要指定端口（除非 Service 定义了不同的端口）

### 故障排查

如果应用启动时仍然报 Redis 连接错误：

1. **检查 Redis Service 是否存在**：
   ```bash
   kubectl get svc -n student-hub | grep redis
   ```

2. **检查 Redis Pod 是否运行**：
   ```bash
   kubectl get pods -n student-hub | grep redis
   ```

3. **测试服务连通性**（在同一个 Namespace 的 Pod 中）：
   ```bash
   kubectl run -it --rm debug --image=busybox --restart=Never -n student-hub -- sh
   # 在容器中执行
   telnet redis-service 6379
   ```

4. **查看后端 Pod 日志**：
   ```bash
   kubectl logs -f <backend-pod-name> -n student-hub
   ```

5. **验证环境变量**：
   ```bash
   kubectl exec <backend-pod-name> -n student-hub -- env | grep SPRING
   ```

### 本地开发 vs Kubernetes

- **本地开发**：使用 `localhost:6379` 和 `localhost:9092`
- **Kubernetes**：使用服务名，如 `redis-service:6379` 和 `kafka-service:9092`

`application.yml` 已配置为支持环境变量覆盖，所以无需修改代码，只需在 Kubernetes Deployment 中设置正确的环境变量即可。
