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
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")

  implementation(project(":kotlin-samples-verifier"))
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "11"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
  }
}