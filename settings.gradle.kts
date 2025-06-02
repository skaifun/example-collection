@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}
rootProject.name = "Example Collection"
includeBuild("build-logic")

include("servlet-examples:servlet-using-xml")
include("servlet-examples:servlet-using-annotation")
include("servlet-examples:servlet-using-xml-with-springmvc")
include("servlet-examples:servlet-using-java-with-springmvc")
