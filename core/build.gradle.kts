import org.gradle.jvm.tasks.Jar
import java.lang.Thread.sleep

plugins {
  kotlin("jvm") version "1.3.70"
  id("com.palantir.docker") version "0.26.0"
  id("com.palantir.docker-run") version "0.25.0"
  id("org.jetbrains.dokka") version "0.10.1"
  id("com.jfrog.bintray") version "1.8.5"
  `maven-publish`
}

group = "com.kotlin.samples.verifier"
version = "1.1.0"

repositories {
  mavenCentral()
}

dependencies {
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")

  implementation(kotlin("stdlib-jdk8"))
  implementation("com.vladsch.flexmark:flexmark-all:0.62.2")
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
  implementation("org.apache.directory.studio:org.apache.commons.io:2.4")

  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-jaxb:2.9.0")
  implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
  implementation("com.squareup.okhttp3:okhttp:4.9.1")

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

tasks.dokka {
  outputFormat = "html"
  outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  archiveClassifier.set("javadoc")
  from(tasks.dokka)
}

val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(dokkaJar)
      artifact(sourcesJar)

      pom {
        name.set("kotlin-samples-verifier")
        url.set("https://github.com/AlexanderPrendota/kotlin-samples-verifier")
        developers {
          developer {
            id.set("myannyax")
            name.set("Mariia Filipanova")
            email.set("myannyax@gmail.com")
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

bintray {
  val bintrayUser: String? by project
  val bintrayApiKey: String? by project
  user = bintrayUser
  key = bintrayApiKey
  setPublications("mavenJava")
  pkg.apply {
    repo = "kotlin-samples-verifier"
    name = "kotlin-samples-verifier"
    setLicenses("Apache-2.0")
    vcsUrl = "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git"
  }
}
