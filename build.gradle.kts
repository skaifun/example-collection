description = "Examples"

subprojects {
    plugins.withId("java") {
        dependencies {
            add("implementation", platform(project(":example-bom")))
            add("testImplementation", platform(project(":example-bom")))
            add("annotationProcessor", platform(project(":example-bom")))
        }
    }
}
