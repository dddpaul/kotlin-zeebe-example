import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "com.github.dddpaul"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven(url = "https://artifacts.camunda.com/artifactory/camunda-identity-snapshots/")
    maven(url = "https://binary.alfabank.ru/artifactory/releases-virtual")
    maven(url = "https://binary.alfabank.ru/artifactory/plugins-release")
}

dependencies {
    implementation("ru.alfabank.mobile.starter:coworker-spring-boot-starter")
    implementation("ru.alfabank.mobile.starter:zeebe-exceptions")
    implementation("me.tongfei:progressbar:0.10.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.camunda:zeebe-process-test-extension:8.4.2")
}

dependencyManagement {
    dependencies {
        dependencySet("ru.alfabank.mobile.starter:1.13.0") {
            entry("coworker-spring-boot-starter")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
