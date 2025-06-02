plugins {
    war
    id("conventions.java")
    id("conventions.docker")
}

dependencies {
    compileOnly(libs.jakarta.servlet)
}

val warTask = tasks.named<War>("war")
val warFileLocation: String = warTask.get().archiveFile.get().asFile.absolutePath

dockerRun {
    imageName = "tomcat:11"
    ports = mutableListOf("8080:8080")
    environment = mutableMapOf(
        "TZ" to "Asia/Shanghai"
    )
    volumes = mutableListOf(
        "$warFileLocation:/usr/local/tomcat/webapps/ROOT.war"
    )
}

tasks.named("dockerRun") {
    dependsOn("war")
}
