#!/bin/bash

echo "正在启动Redis容器..."

# 检查是否已有Redis容器
if docker ps -a | grep -q student-hub-redis; then
    echo "发现现有Redis容器，正在启动..."
    docker start student-hub-redis
else
    echo "创建新的Redis容器..."
    docker run -d --name student-hub-redis -p 6379:6379 redis:7-alpine
fi

if [ $? -eq 0 ]; then
    echo "Redis启动成功！"
    echo "等待Redis就绪..."
    sleep 3
    docker ps | grep redis
    echo ""
    echo "可以使用以下命令查看Redis日志:"
    echo "docker logs student-hub-redis"
    echo ""
    echo "停止Redis使用:"
    echo "docker stop student-hub-redis"
else
    echo ""
    echo "Redis启动失败！请检查:"
    echo "1. Docker是否已安装并运行"
    echo "2. 端口6379是否被占用"
fi

