# Kotlin samples verifier

![Java CI with Gradle](https://github.com/AlexanderPrendota/kotlin-samples-verifier/workflows/Java%20CI%20with%20Gradle/badge.svg)

Kotlin documentation testing tool.

A library that extracts embedded code snippets from HTML and Markdown files from a remote git repository, runs them and collects results.

# Setup
The tool sends calls to [kotlin-compiler-server](https://github.com/AlexanderPrendota/kotlin-compiler-server),
URL is passed as an argument to the factory create method and set to "http://localhost:8080/" by default.

* Pull server docker image and start it as a docker container:

`./gradlew dockerRun`

* Stop and remove docker container:

`./gradlew dockerStop dockerRemoveContainer`

# Console Client

* Download Kotlin dependencies and build project:

`./gradlew build`

* Start Kotlin application, main class: Client

provides command-line interface

# Configure Gradle Dependencies

```
repositories {
    mavenCentral()
    maven {
        url = uri("http://oss.sonatype.org/content/repositories/snapshots")
    }
}
```
```
dependencies {
    ...
    implementation("io.github.AlexanderPrendota:core:1.0-SNAPSHOT")
}
```




# Usage Example

```
val samplesVerifier = SamplesVerifierFactory.create()

val repositoryURL = "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git"
val attributes = listOf("run-kotlin")

// log execution results

samplesVerifier.check(repositoryURL, attributes, FileType.MD)

// get results as Map<ExecutionResult, Code>

val results = samplesVerifier.collect(repositoryURL, attributes, FileType.MD)
```

