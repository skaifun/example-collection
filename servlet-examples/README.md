# Servlet examples

## 简介

本示例主要展示在 Java Web 开发中注册和配置 Servlet 的不同方法，从传统的 XML 到现代的注解方式，以及在引入 Spring WebMVC 框架之后又如何配置。

```
servlet-examples/
├── servlet-using-xml/                      # 使用传统的 web.xml 的方式注册一个 Servlet 和一个 Filter
├── servlet-using-annotation/               # 使用注解（@WebServlet, @WebFilter）的方式注册一个 Servlet 和一个 Filter
├── servlet-using-xml-with-springmvc/       # 引入 Spring WebMVC 后，如何在 web.xml 中配置
└── servlet-using-java-with-springmvc/      # 引入 Spring WebMVC 后，不使用 web.xml 如何配置
```

## 功能

四个模块需要实现以下两个功能：

1. 处理路径 `/hello` 的 `GET` 请求，接受一个参数 `name`，如果 `name` 为空，则默认 `name` 为 `world`。
2. 实现一个 `Filter`，记录每次请求的 URL、方法、参数和处理时间，并打印到日志中。

其中 `servlet-using-xml` 和 `servlet-using-annotation` 使用传统的 `HttpServlet` 子类的方式处理请求，而
`servlet-using-xml-with-springmvc` 和 `servlet-using-java-with-springmvc` 使用 Spring WebMVC 框架中的 `@Controller` 注解处理。

## 运行

四个模块均使用 Docker 运行一个 Tomcat 11 版本的容器作为部署环境，可以直接通过 Gradle 运行。例如：

```bash
# 在项目的根目录
./gradlew servlet-examples:servlet-using-java-with-springmvc:dockerRun
```

## 注意：

1. 请确保已安装 Docker，且在命令行可用。
2. 运行之后可以通过 <kbd>Ctrl+C</kbd> 的方式停止，停止后容器将自动销毁。
3. 由于 macOS 系统的限制，无法直接通过 IDEA 提供的一些快捷方式运行 Gradle（详见[这里][1]），建议直接通过命令行运行。

[1]: https://youtrack.jetbrains.com/articles/SUPPORT-A-923/How-to-fix-Gradle-issue-Cause-error2-No-such-file-or-directory-on-macOS
