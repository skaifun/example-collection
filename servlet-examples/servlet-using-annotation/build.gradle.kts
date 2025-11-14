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
}

tasks.withType<Test> {
    useJUnitPlatform()
}
