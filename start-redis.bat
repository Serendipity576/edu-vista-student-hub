@echo off
echo 正在启动Redis容器...
docker run -d --name student-hub-redis -p 6379:6379 redis:7-alpine

if %errorlevel% == 0 (
    echo Redis启动成功！
    echo 等待Redis就绪...
    timeout /t 3 /nobreak >nul
    docker ps | findstr redis
    echo.
    echo 可以使用以下命令查看Redis日志:
    echo docker logs student-hub-redis
    echo.
    echo 停止Redis使用:
    echo docker stop student-hub-redis
    echo docker rm student-hub-redis
) else (
    echo.
    echo Redis启动失败！请检查:
    echo 1. Docker Desktop是否已启动
    echo 2. 端口6379是否被占用
    echo.
    echo 如果端口被占用，请先停止现有的Redis容器:
    echo docker stop student-hub-redis
    echo docker rm student-hub-redis
)

pause

