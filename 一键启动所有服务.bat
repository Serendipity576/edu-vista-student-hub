@echo off
chcp 65001 >nul
echo ========================================
echo   学生信息管理系统 - 一键启动服务
echo ========================================
echo.

echo [1/3] 检查Docker状态...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未运行！请先启动Docker Desktop
    echo.
    echo 请执行以下步骤：
    echo 1. 打开Docker Desktop
    echo 2. 等待Docker完全启动（系统托盘图标变绿）
    echo 3. 重新运行此脚本
    pause
    exit /b 1
)
echo ✅ Docker正在运行
echo.

echo [2/3] 检查并启动服务...

REM 检查Redis
docker ps | findstr "student-hub-redis" >nul 2>&1
if %errorlevel% neq 0 (
    echo 启动Redis容器...
    docker run -d --name student-hub-redis -p 6379:6379 redis:7-alpine 2>nul
    if %errorlevel% equ 0 (
        echo ✅ Redis已启动
    ) else (
        echo ❌ Redis启动失败
    )
) else (
    echo ✅ Redis已在运行
)

REM 检查MySQL端口
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 检测到MySQL服务（端口3306已被占用，使用本地MySQL）
    echo    你的应用配置为使用本地MySQL，无需Docker容器
) else (
    echo 启动MySQL容器...
    docker-compose up -d mysql 2>nul
    if %errorlevel% equ 0 (
        echo ✅ MySQL容器已启动
    ) else (
        echo ⚠️  MySQL容器启动失败（可能端口被占用）
        echo    如需使用Docker MySQL，请先停止本地MySQL服务
    )
)
echo.

echo [3/3] 等待服务就绪...
timeout /t 5 /nobreak >nul
echo.

echo ========================================
echo ✅ 基础服务启动完成！
echo ========================================
echo.
echo 服务状态：
echo [Redis]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr "redis"
echo [MySQL]
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 本地MySQL服务正在运行 (端口3306)
    echo    请确保已创建数据库: student_hub
    echo    执行: mysql -u kongshan -p ^< 创建数据库.sql
) else (
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr "mysql"
)
echo.
echo 接下来可以：
echo 1. 确保数据库存在: 查看 创建数据库.sql
echo 2. 启动后端: cd backend ^&^& mvn spring-boot:run
echo 3. 启动前端: cd frontend ^&^& npm run dev
echo.
echo 查看详细说明: 使用本地MySQL说明.md
echo ========================================
echo.
pause

