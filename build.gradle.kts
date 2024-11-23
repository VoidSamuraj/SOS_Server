
val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project
val exposed_version: String by project
val h2_version: String by project
val postgreSQL_version: String by project

plugins {
    kotlin("jvm") version "2.0.10"
    id("io.ktor.plugin") version "2.3.12"
    kotlin("plugin.serialization") version "2.0.10"
}

group = ""
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-hsts:$ktor_version")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.jetbrains.exposed:exposed-core:0.41.1")
    testImplementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    testImplementation("com.h2database:h2:$h2_version")
    testImplementation("junit:junit:4.13.2")

    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposed_version")

    implementation("org.postgresql:postgresql:$postgreSQL_version")
    implementation("com.h2database:h2:$h2_version")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("org.jsoup:jsoup:1.18.1")

    implementation("org.simplejavamail:simple-java-mail:8.11.3")
}

val env = System.getenv("ENV") ?: "dev"

tasks.withType<Test> {
    systemProperty("ktor.environment", "test")
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
        attributes["Env"] = env
    }
    if (project.hasProperty("test")) {
        isZip64 = true
    }
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
        attributes["Env"] = env
    }
    isZip64 = true
}
/*
tasks.register<Exec>("buildReact") {
    workingDir = file("src/main/resources/react")
    commandLine("npm", "run", "webpackbuild")
}
// before processResources run buildReact
tasks.named("processResources") {
    dependsOn("buildReact")
}
*/
