/**
 * Docker运行配置扩展类
 * 用于配置Docker容器的运行参数
 */
open class DockerRunExtension {
    /** Docker镜像名称 */
    var imageName: String = "openjdk:21-jre-slim"

    /** 容器名称，如果为空则使用项目名称 */
    var containerName: String = ""

    /** 端口映射列表，格式为"主机端口:容器端口" */
    var ports: MutableList<String> = mutableListOf("8080:8080")

    /** 卷挂载列表，格式为"主机路径:容器路径[:权限]" */
    var volumes: MutableList<String> = mutableListOf()

    /** 环境变量映射 */
    var environment: MutableMap<String, String> = mutableMapOf()

    /** 容器工作目录 */
    var workingDir: String = ""

    /** 容器入口点 */
    var entrypoint: String = ""

    /** 容器启动命令 */
    var command: MutableList<String> = mutableListOf()

    /** Docker网络名称 */
    var network: String = ""

    /** 容器退出时是否自动删除 */
    var removeOnExit: Boolean = true

    /** 是否在后台运行容器 */
    var detached: Boolean = false

    /** 是否以交互模式运行容器 */
    var interactive: Boolean = false

    /** 是否分配一个伪终端 */
    var tty: Boolean = false

    /** 额外的Docker运行参数 */
    var extraArgs: MutableList<String> = mutableListOf()
}

afterEvaluate {
    if (dockerRun.containerName.isEmpty()) {
        dockerRun.containerName = project.name
    }
}

// 创建扩展
val dockerRun = extensions.create<DockerRunExtension>("dockerRun")

// 构建docker run命令的辅助函数
fun buildDockerRunCommand(): List<String> {
    val command = mutableListOf("docker", "run")

    // 基础选项
    if (dockerRun.detached) command.add("-d")
    if (dockerRun.interactive) command.add("-i")
    if (dockerRun.tty) command.add("-t")
    if (dockerRun.removeOnExit) command.add("--rm")

    // 容器名称，默认为项目名称
    command.addAll(listOf("--name", dockerRun.containerName))

    // 端口映射
    dockerRun.ports.forEach { port ->
        command.addAll(listOf("-p", port))
    }

    // 卷挂载
    dockerRun.volumes.forEach { volume ->
        command.addAll(listOf("-v", volume))
    }

    // 环境变量
    dockerRun.environment.forEach { (key, value) ->
        command.addAll(listOf("-e", "$key=$value"))
    }

    // 工作目录
    if (dockerRun.workingDir.isNotEmpty()) {
        command.addAll(listOf("-w", dockerRun.workingDir))
    }

    // 网络
    if (dockerRun.network.isNotEmpty()) {
        command.addAll(listOf("--network", dockerRun.network))
    }

    // 入口点
    if (dockerRun.entrypoint.isNotEmpty()) {
        command.addAll(listOf("--entrypoint", dockerRun.entrypoint))
    }

    // 额外参数
    command.addAll(dockerRun.extraArgs)

    // 镜像名称
    command.add(dockerRun.imageName)

    // 命令参数
    command.addAll(dockerRun.command)

    return command
}

// 辅助接口用于注入ExecOperations
interface DockerExecOperations {
    @get:Inject val execOperations: ExecOperations
}

// 运行Docker容器任务
tasks.register("dockerRun") {
    group = "docker"
    description = "运行Docker容器"

    doLast {
        val operations = project.objects.newInstance<DockerExecOperations>().execOperations
        val command = buildDockerRunCommand()
        logger.info("执行命令: ${command.joinToString(" ")}")

        operations.exec {
            commandLine(command)
        }

        if (dockerRun.detached) {
            logger.info("Docker容器已启动: ${dockerRun.containerName}")
            if (dockerRun.ports.isNotEmpty()) {
                logger.info("端口映射: ${dockerRun.ports.joinToString(", ")}")
            }
        }
    }
}

// 停止Docker容器任务
tasks.register("dockerStop") {
    group = "docker"
    description = "停止Docker容器"

    doLast {
        val operations = project.objects.newInstance<DockerExecOperations>().execOperations
        try {
            operations.exec {
                commandLine("docker", "stop", dockerRun.containerName)
            }
            logger.info("Docker容器已停止: ${dockerRun.containerName}")
        } catch (e: Exception) {
            logger.error("容器 ${dockerRun.containerName} 未在运行或不存在", e)
        }
    }
}

// 重启Docker容器任务
tasks.register("dockerRestart") {
    group = "docker"
    description = "重启Docker容器"

    dependsOn("dockerStop")
    finalizedBy("dockerRun")

    doLast {
        // 等待一秒确保容器停止
        Thread.sleep(1000)
    }
}
