plugins {
    id("conventions.java")
    id("conventions.test")
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.websocket)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform)
}
