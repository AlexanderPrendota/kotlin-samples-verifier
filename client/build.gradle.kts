plugins {
  application
}

version = "unspecified"

application {
  mainClassName = "com.samples.verifier.client.Client"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.github.spullara.cli-parser:cli-parser:1.1.5")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

  implementation(project(":kotlin-samples-verifier"))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}