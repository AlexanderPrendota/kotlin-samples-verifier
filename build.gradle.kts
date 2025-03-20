plugins {
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  kotlin("jvm") version "2.1.20" apply false
}

subprojects {
  version = "1.0"
}

nexusPublishing {
  repositories {
    sonatype {
      packageGroup.set("io.github.alexanderprendota")
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}

subprojects {
  apply {
    plugin("org.jetbrains.kotlin.jvm")
  }

  dependencies {
    val implementation by configurations
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-log4j12:2.0.17")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
  }
}