import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.eightbitbazar"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // OAuth2 Authorization Server
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")

    // AWS S3 SDK (MinIO compatible)
    implementation("software.amazon.awssdk:s3:2.39.1")

    // RabbitMQ
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Elasticsearch
    implementation("org.springframework.data:spring-data-elasticsearch")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Utilities
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.21.3")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.testcontainers:mysql:1.21.3")
    testImplementation("org.testcontainers:rabbitmq:1.21.3")
    testImplementation("org.testcontainers:elasticsearch:1.21.3")
    testImplementation("org.testcontainers:minio:1.21.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 1
    configureLocalVariablesPodman()
}

fun Test.configureLocalVariablesPodman() {
    val activeProfile = System.getProperty("spring.profiles.active") ?: "default"

    if (activeProfile == "local-podman") {
        val os = DefaultNativePlatform.getCurrentOperatingSystem()
        if (os.isLinux) {
            val uid = ProcessBuilder("id", "-u").start().inputStream.bufferedReader().use { it.readText().trim() }
            environment("DOCKER_HOST", "unix:///run/user/$uid/podman/podman.sock")
        } else if (os.isMacOsX) {
            environment("DOCKER_HOST", "unix:///tmp/podman.sock")
        }
        environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    }
}
