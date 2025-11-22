# SpringBoot Integration Tests

## 项目简介

本项目演示如何在一个基于 **Spring Boot + Spring Security + Spring Data JPA + PostgreSQL** 的应用中进行集成测试。

## 🎯 功能说明

本项目实现了一个用户账号（`UserAccount`）管理系统，提供以下 Restful API：

- `POST /api/accounts` - 创建用户账号
- `GET /api/accounts/{id}` - 根据 ID 查询用户
- `GET /api/accounts?keyword={keyword}` - 根据关键字搜索用户，如果没指定则返回全部用户
- `DELETE /api/accounts/{id}` - 删除用户账号

## 技术栈

### 应用层
- **Spring Boot** - 应用框架
- **Spring Security** - 安全认证
- **Spring Data JPA** - 数据访问层
- **PostgreSQL** - 关系型数据库
- **Liquibase** - 数据库版本管理
- **Lombok** - 简化 Java 代码

### 测试层
- **Spring Boot Test** - 集成测试框架
- **Testcontainers** - 容器化测试环境
- **MockMvc** - HTTP 请求模拟
- **JUnit 5** - 测试框架
- **AssertJ** - 流式断言库
- **Spring Security Test** - 安全测试支持

## 测试

### 前提条件

1. **Java 21**
2. **Docker Desktop**（Testcontainers 需要）
3. **Gradle**（使用项目自带的 Gradle Wrapper）

### 运行测试

```bash
# 启动 Docker Desktop（必须）
./gradlew :springboot-integration-tests:test
```

### 查看测试报告

```bash
open springboot-integration-tests/build/reports/tests/test/index.html
```

## 相关链接

- [Spring Boot Testing 官方文档](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Testcontainers 官方文档](https://www.testcontainers.org/)
- [AssertJ 文档](https://assertj.github.io/doc/)
