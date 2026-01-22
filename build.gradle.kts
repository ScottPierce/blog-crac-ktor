plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.1.1"
    application
}

group = "dev.scottpierce"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("dev.scottpierce.MainKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.1"
val logbackVersion = "1.5.16"
val cracVersion = "1.5.0"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.crac:crac:$cracVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-core:$ktorVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("blog-crac-ktor")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}