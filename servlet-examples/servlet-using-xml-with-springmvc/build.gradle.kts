plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly(libs.jakarta.servlet)
    implementation(libs.spring.webmvc)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
