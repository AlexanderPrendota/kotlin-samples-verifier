import java.lang.Thread.sleep

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("com.palantir.docker") version "0.26.0"
    id("com.palantir.docker-run") version "0.25.0"
    application
}

group = "com.kotlin.samples.pusher"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.samples.pusher.client.Client")
}

repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/groups/staging")
    }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")

    implementation("com.github.spullara.cli-parser:cli-parser:1.1.5")
    //implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("org.slf4j:slf4j-log4j12:2.0.0-alpha1")

    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")
    implementation("org.apache.commons:commons-configuration2:2.7")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
    implementation("io.github.alexanderprendota:kotlin-samples-verifier:1.1.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

//Docker tasks
val dockerImageName = "prendota/kotlin-compiler-server:latest"

docker {
    name = dockerImageName
    pull(true)
}

dockerRun {
    name = "kotlin-compiler-server"
    image = dockerImageName
    arguments("--network=host")
    clean = false
}

tasks.dockerRun {
    doLast {
        Thread.sleep(10 * 1000)
    }
}