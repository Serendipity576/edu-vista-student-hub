@echo off
chcp 65001 >nul
echo ========================================
echo   服务状态检查
echo ========================================
echo.

echo [Redis状态]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr "redis"
if %errorlevel% equ 0 (
    echo ✅ Redis容器正在运行
    docker exec -it student-hub-redis redis-cli ping 2>nul
    if %errorlevel% equ 0 (
        echo ✅ Redis连接正常
    )
) else (
    echo ❌ Redis容器未运行
)
echo.

echo [MySQL状态]
echo 检查本地MySQL服务...
sc query MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    sc query MySQL80 | findstr "RUNNING"
    if %errorlevel% equ 0 (
        echo ✅ 本地MySQL服务正在运行 (端口3306)
        echo.
        echo 你的应用已配置为使用本地MySQL:
        echo   数据库: student_hub
        echo   用户: kongshan
        echo   端口: 3306
        echo.
        echo 请确保已创建数据库:
        echo   CREATE DATABASE IF NOT EXISTS student_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ) else (
        echo ⚠️  本地MySQL服务未运行
    )
) else (
    echo ⚠️  未检测到本地MySQL服务
)
echo.

echo [Docker容器状态]
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr -v "CONTAINER"
echo.

echo ========================================
echo 总结:
echo - Redis: ✅ 已启动 (Docker容器)
echo - MySQL: 使用本地MySQL服务或Docker容器
echo ========================================
pause

