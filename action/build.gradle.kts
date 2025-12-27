plugins {
    kotlin("plugin.serialization") version "2.1.20"
    application
}

group = "com.kotlin.samples.pusher"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.samples.pusher.client.Client")
}

repositories {
    mavenCentral()
}

dependencies {
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    implementation("com.github.spullara.cli-parser:cli-parser:1.1.6")
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")
    implementation("org.apache.commons:commons-configuration2:2.12.0")
    implementation("commons-beanutils:commons-beanutils:1.10.1")
    implementation("org.freemarker:freemarker:2.3.34")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation(project(":kotlin-samples-verifier"))//implementation("io.github.alexanderprendota:kotlin-samples-verifier:1.1.4")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
