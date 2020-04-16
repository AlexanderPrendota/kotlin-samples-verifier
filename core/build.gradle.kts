import org.gradle.jvm.tasks.Jar
import java.lang.Thread.sleep

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.palantir.docker") version "0.25.0"
    id("com.palantir.docker-run") version "0.25.0"
    id("org.jetbrains.dokka") version "0.10.0"
    `maven-publish`
    signing
}

group = "io.github.AlexanderPrendota"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.rjeschke:txtmark:0.13")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.6.1.202002131546-r")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")

    implementation("com.squareup.retrofit2:retrofit:2.7.2")
    implementation("com.squareup.retrofit2:converter-jaxb:2.8.1")
    implementation("com.squareup.retrofit2:converter-jackson:2.7.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.squareup.okhttp3:okhttp:4.5.0")

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


val MAVEN_UPLOAD_USER: String by project
val MAVEN_UPLOAD_PWD: String by project

publishing {
    repositories {
        maven {
            name = "MavenCentral"
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = MAVEN_UPLOAD_USER
                password = MAVEN_UPLOAD_PWD
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(dokkaJar)
            artifact(sourcesJar)

            pom {
                name.set("Kotlin Samples Verifier")
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


signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}