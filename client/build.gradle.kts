plugins {
    kotlin("jvm") version "1.3.70"
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.spullara.cli-parser:cli-parser:1.1.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")

    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("org.slf4j:slf4j-log4j12:2.0.0-alpha1")

    implementation(project(":core"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}