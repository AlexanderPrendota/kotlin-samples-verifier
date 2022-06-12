plugins {
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
  kotlin("jvm") version "1.6.20" apply false
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
    implementation("org.slf4j:slf4j-api:2.0.0-alpha6")
    implementation("org.slf4j:slf4j-log4j12:2.0.0-alpha7")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.2.0.202206071550-r")
  }
}