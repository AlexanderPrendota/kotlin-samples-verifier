import org.gradle.jvm.tasks.Jar
import java.lang.Thread.sleep

plugins {
  id("com.palantir.docker") version "0.36.0"
  id("com.palantir.docker-run") version "0.36.0"
  id("org.jetbrains.dokka") version "1.9.20"
  `maven-publish`
  signing
}

group = "io.github.alexanderprendota"
version = "1.1.0"

repositories {
  mavenCentral()
}

dependencies {
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.1")

  implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
  implementation("org.jsoup:jsoup:1.19.1")
  implementation("org.apache.directory.studio:org.apache.commons.io:2.4")

  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-jaxb:2.11.0")
  implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "17"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "17"
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

tasks.dokkaHtml {
  outputDirectory.set(file("$buildDir/javadoc"))
}

val dokkaJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  archiveClassifier.set("javadoc")
  from(tasks.dokkaHtml)
}

val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

signing {
  sign(publishing.publications)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(dokkaJar)
      artifact(sourcesJar)
      artifactId = "kotlin-samples-verifier"

      pom {
        name.set("kotlin-samples-verifier")
        url.set("https://github.com/AlexanderPrendota/kotlin-samples-verifier")
        description.set("A library that extracts embedded code snippets from HTML and Markdown files from a " +
          "remote git repository, runs them and collects results.")
        developers {
          developer {
            id.set("myannyax")
            name.set("Mariia Filipanova")
            email.set("myannyax@gmail.com")
          }
          developer {
            id.set("vmishenev")
            name.set("Vadim Mishenev")
            email.set("vad-mishenev-@yandex.ru")
          }
        }
        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        scm {
          connection.set("scm:git:https://github.com/AlexanderPrendota/kotlin-samples-verifier.git")
          developerConnection.set("scm:git:https://github.com/AlexanderPrendota/kotlin-samples-verifier.git")
          url.set("https://github.com/AlexanderPrendota/kotlin-samples-verifier")
        }
      }
    }
  }
}