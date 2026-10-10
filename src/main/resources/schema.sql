-- 数据库名按照作业原文写为 DateTest。
CREATE DATABASE IF NOT EXISTS DateTest CHARACTER SET utf8mb4;
USE DateTest;

-- 字符串主键由 Java 程序赋值，与 getOneById(String id, ...) 一致。
CREATE TABLE IF NOT EXISTS student (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    major VARCHAR(100),
    age INT,
    enrollment_date DATE,
    graduated BOOLEAN,
    tuition DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS college (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20)
);
