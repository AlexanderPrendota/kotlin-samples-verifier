plugins {
  application
}

version = "unspecified"

application {
  getMainClass().set("com.samples.verifier.client.Client")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.github.spullara.cli-parser:cli-parser:1.1.6")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

  implementation(project(":kotlin-samples-verifier"))
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