import java.lang.Thread.sleep

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.palantir.docker") version "0.25.0"
    id("com.palantir.docker-run") version "0.25.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.rjeschke:txtmark:0.13")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.6.1.202002131546-r")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")

    implementation("com.squareup.retrofit2:retrofit:2.7.2")
    implementation("com.squareup.retrofit2:converter-jaxb:2.7.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.7.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.squareup.okhttp3:okhttp:4.4.1")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("org.slf4j:slf4j-log4j12:2.0.0-alpha1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

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
        sleep(10 * 1000)
    }
}

tasks.test {
    dependsOn("dockerRun")
    useJUnitPlatform()
    finalizedBy("dockerStop", "dockerRemoveContainer")
}