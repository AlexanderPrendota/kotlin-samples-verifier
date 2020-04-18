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

### Collect options:
  -attributes (-a) [String[,]] Attributes for code snippets, separated by "," like so: "attr1,attr2"
  
  -compiler-url [String] Kotlin compiler URL (http://localhost:8080/)
  
  -file-type [FileType] MD or HTML (type of files to be processed) (MD)
   
  -kotlin-env [KotlinEnv] JS or JVM (JVM)
   
  -out (-o) [String] Filename to store results
  
  -repository (-r) [String] Git repository URL with samples to execute
  
### Check options:
  -attributes (-a) [String[,]] Attributes for code snippets, separated by "," like so: "attr1,attr2"
  
  -compiler-url [String] Kotlin compiler URL (http://localhost:8080/)
  
  -file-type [FileType] MD or HTML (type of files to be processed) (MD)
    
  -kotlin-env [KotlinEnv] JS or JVM (JVM)
    
  -repository (-r) [String] Git repository URL with samples to execute
  
  
### Example:

./gradlew run --args="check -r https://github.com/AlexanderPrendota/kotlin-samples-verifier.git -a run-kotlin"


# Configure Gradle Dependencies

```
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

