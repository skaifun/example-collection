plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.7"))

    constraints {
        api("jakarta.servlet:jakarta.servlet-api:6.1.0")
    }
}