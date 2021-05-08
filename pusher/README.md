# Kotlin Samples Pusher

It aims to handle snippets collection for their consistency in the repository.

![Architecture](doc/images/SnippetsVerifier.png)

### Functionality

This tool can run as GitHub Action.

It can run in the two work modes:

1. The action handles GitHub events such as ```pull request``` and ```push```.
2. The action runs a verification of repository.

### Options:
  | Name (alias) | Format | Description | Default |
  | ------------- |:-------------:| :-----:|:-------------:|
  |-push-repository (-pr) | [String] | Destination git repository to push (**required**)| n/a |
  |-username| [String] | Username or access token for push to a target repository (**required**) | n/a |
  |-passw | [String] | User's password for push  to a target repository | empty |
  |-push-path (p) | [String] | Path relatively a push repository | empty |
  |-config-path | [String] | Url or file path for loading config | config.properties | 
  |-template-path | [String] | Url or file path for loading templates | templates | 
  |-severity | [String] | Create issue and do not push if the snippet has errors equals or greater the severity | ERROR | 

Other options is for the verifier. 
  See https://github.com/AlexanderPrendota/kotlin-samples-verifier
  
### Templates:
It is possible to customize the title, body of issue or pull request.
 Templates of issue or pull request are stored in ```templates/issue.md``` and ```templates/pr.md``` resp.
 
 See https://freemarker.apache.org/docs/index.html