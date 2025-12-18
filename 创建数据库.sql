-- 创建学生信息管理系统数据库
-- 执行此SQL脚本来创建所需的数据库

CREATE DATABASE IF NOT EXISTS student_hub 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 如果需要，可以切换到该数据库
USE student_hub;

-- 显示数据库创建结果
SHOW DATABASES LIKE 'student_hub';

