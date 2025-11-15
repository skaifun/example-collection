@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}
rootProject.name = "examples"

include("example-bom")
include("servlet-examples:servlet-using-xml")
include("servlet-examples:servlet-using-annotation")
include("servlet-examples:servlet-using-xml-with-springmvc")
include("servlet-examples:servlet-using-java-with-springmvc")

include("springboot-with-websocket-using-handler")

rootProject.children.forEach { child ->
    child.buildFileName = "${child.name}.gradle.kts"
    child.children.forEach { sub ->
        sub.buildFileName = "${sub.name}.gradle.kts"
    }
}