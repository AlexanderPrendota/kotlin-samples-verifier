plugins {
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
  kotlin("jvm") version "1.8.10" apply false
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
    implementation("org.slf4j:slf4j-api:2.0.4")
    implementation("org.slf4j:slf4j-log4j12:2.0.6")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")
  }
}