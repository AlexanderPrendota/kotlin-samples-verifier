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

Also, the tool can collect snippets from the changes between two commits or two branches.

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
    implementation("com.kotlin.samples.verifier:core:1.1.0")
}
```

# Console Client

* Download Kotlin dependencies and build project:

```shell
./gradlew build -x test
./gradlew installDist
```
  
### Options:
  | Name (alias) | Format | Description | Default |
  | ------------- |:-------------:| :-----:|:-------------:|
  |-snippet-flags (-f) | [String[,]] | Flags for code snippets, separated by \",\" like so: \"attr1,attr2\"|  n/a |
  |-ignore-attributes | [String[,]] | Attributes (name and value separated by \\\":\\\" (name:value)) for code snippets to ignore, separated by \\\",\\\" like so: \\\"attr1,attr2\\\" | null |
  |-parse-directory | [String] | Regexp for directories to be processed | null |
  |-ignore-directory | [String] | Regexp for directories to be ignored | null |
  |-parse-tags | [String[,]] | Html tags to be accepted as code snippets, works for both html and md. Default (code) for MD so only fencedCodeBlocks are accepted as code snippets | null |
  |-compiler-url | [String] | Kotlin compiler URL | `http://localhost:8080/` |
  |-commits | [String [,String]] | Considering only the changed files between two arbitrary commits \"commit1,commit2\" or starting from \"commit1\" if \"commit2\" on one side is omitted | null |
  |-file-type | [FileType] | MD or HTML (type of files to be processed) | MD |
  |-kotlin-env | [KotlinEnv] | JS or JVM | JVM |
  |-repository (-r) | [String] | Git repository URL with samples to execute| n/a |
  |-tag-filter | [String] | User filter for tag containing snippet  like so: (#tag=\"name\" & attr1=\"val\"). It also supports !, &, / operations | "" |
  |-ignore-tag-filter | [String] | User filter for ignoring of tag including inners tags | "" |
  
#### Only for collect:
| Name (alias) | Format | Description | Default |
| ------------- |:-------------:| :-----:|:-------------:|
|-out (-o) | [String] | Filename to store results | n/a |

  
  
### Example:

```shell
./client/build/install/client/bin/client check -r https://github.com/AlexanderPrendota/kotlin-samples-verifier.git -f run-kotlin
```

# Usage Example

```kotlin
val samplesVerifier = SamplesVerifierFactory.create().configure {
  snippetFlags = hashSetOf("run-kotlin")
  ignoreAttributes = hashSetOf(Attribute("data-highlight-only", ""))
  parseTags = hashSetOf("code", "div")
}

val repositoryURL = "https://github.com/AlexanderPrendota/kotlin-samples-verifier.git"

// log execution results

samplesVerifier.check(repositoryURL, "master", FileType.MD)

// get results as Map<ExecutionResult, Code>

val results = samplesVerifier.collect(repositoryURL, "master", FileType.MD)

// process files from list

val filenames = listOf<String>(...)

val results2 = samplesVerifier.collect(filenames, FileType.MD)
```
