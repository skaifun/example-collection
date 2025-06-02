# Build Logic 模块说明文档

## 概述

`build-logic` 模块用来管理项目构建中的约定和通用模块。

## 功能特性

### 1. Java 构建约定 (`conventions.java.gradle.kts`)

统一 Java 项目配置，使用 Java 21

### 2. Docker 构建约定 (`conventions.docker.gradle.kts`)

提供运行和停止 Docker 容器的功能。

#### 2.1 可配置参数

- `imageName`: Docker 镜像名称（默认: `openjdk:21-jre-slim`）
- `containerName`: 容器名称（默认使用项目名称）
- `ports`: 端口映射列表（默认: `["8080:8080"]`）
- `volumes`: 卷挂载配置
- `environment`: 环境变量设置
- `workingDir`: 容器工作目录
- `entrypoint`: 容器入口点
- `command`: 容器启动命令
- `network`: Docker 网络配置
- `removeOnExit`: 退出时自动删除容器（默认: `true`）
- `detached`: 后台运行模式（默认: `false`）
- `interactive`: 交互模式（默认: `false`）
- `tty`: 伪终端分配（默认: `false`）
- `extraArgs`: 额外的 Docker 运行参数

#### 2.2 使用方法

在需要使用的子模块的 `build.gradle.kts` 文件中：

```kotlin
// 应用 Docker 构建约定
plugins {
    id("conventions.docker")
}
```

然后配置 Docker 运行的参数

```kotlin
// 配置 Docker 运行参数
dockerRun {
    imageName = "openjdk:21-jre-slim"
    containerName = "my-app-container"
    ports = mutableListOf("8080:8080", "9090:9090")
    environment["SPRING_PROFILES_ACTIVE"] = "prod"
    volumes = mutableListOf("./logs:/app/logs")
    detached = true
}
```

#### 2.3 可用的 Docker 任务

| 任务名称            | 描述           | 用法                        |
|-----------------|--------------|---------------------------|
| `dockerRun`     | 运行 Docker 容器 | `./gradlew dockerRun`     |
| `dockerStop`    | 停止 Docker 容器 | `./gradlew dockerStop`    |
| `dockerRestart` | 重启 Docker 容器 | `./gradlew dockerRestart` |
