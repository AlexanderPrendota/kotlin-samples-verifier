# Kotlin samples verifier

![Java CI with Gradle](https://github.com/AlexanderPrendota/kotlin-samples-verifier/workflows/Java%20CI%20with%20Gradle/badge.svg)

Kotlin documentation testing tool.

A library that extracts embedded code snippets from HTML and Markdown files from a remote git repository, runs them and collects results.

# Setup

## Execution
Check and Collect send calls to [kotlin-compiler-server](https://github.com/AlexanderPrendota/kotlin-compiler-server) to execute code,
URL is passed as an argument to the factory create method and set to `http://localhost:8080/` by default.

Gradle tasks to run server using docker:

* Pull server docker image and start it as a docker container:

```shell
./gradlew dockerRun
```

* Stop and remove docker container:

```shell
./gradlew dockerStop dockerRemoveContainer
```

## Gradle

```groovy
repositories {
    maven {
        url = uri("https://dl.bintray.com/myannyax/kotlin-samples-verifier")
    }
}
```
```
dependencies {
    ...
    implementation("com.kotlin.samples.verifier:core:1.0.0")
}
```

# Console Client

* Download Kotlin dependencies and build project:

```shell
./gradlew build
```

### Collect options:
  | Name (alias) | Format | Description | Default |
  | ------------- |:-------------:| :-----:|:-------------:|
  |-attributes (-a) | [String[,]] | Attributes for code snippets, separated by "," like so: "attr1,attr2" | n/a |
  |-compiler-url | [String] | Kotlin compiler URL | `http://localhost:8080/` |
  |-file-type | [FileType] | MD or HTML (type of files to be processed) | MD |
  |-kotlin-env | [KotlinEnv] | JS or JVM | JVM |
  |-out (-o) | [String] | Filename to store results | n/a |
  |-repository (-r) | [String] | Git repository URL with samples to execute | n/a |
  
### Check options:
  | Name (alias) | Format | Description | Default |
  | ------------- |:-------------:| :-----:|:-------------:|
  |-attributes (-a) | [String[,]] | Attributes for code snippets, separated by "," like so: "attr1,attr2"|  n/a |
  |-compiler-url | [String] | Kotlin compiler URL | `http://localhost:8080/` |
  |-file-type | [FileType] | MD or HTML (type of files to be processed) | MD |
  |-kotlin-env | [KotlinEnv] | JS or JVM | JVM |
  |-repository (-r) | [String] | Git repository URL with samples to execute| n/a |
  
  
### Example:

```shell
./gradlew run --args="check -r https://github.com/AlexanderPrendota/kotlin-samples-verifier.git -a run-kotlin"
```

# Usage Example

```kotlin
val samplesVerifier = SamplesVerifierFactory.create()

val repositoryURL = "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git"
val attributes = listOf("run-kotlin")

// log execution results

samplesVerifier.check(repositoryURL, attributes, FileType.MD)

// get results as Map<ExecutionResult, Code>

val results = samplesVerifier.collect(repositoryURL, attributes, FileType.MD)
```
