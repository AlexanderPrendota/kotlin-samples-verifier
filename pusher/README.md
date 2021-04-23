# Kotlin Samples Pushes

It aims to handle snippets collection for their consistency in repository.

![Architecture](doc/images/SnippetsVerifier.png)


### Options:
  | Name (alias) | Format | Description | Default |
  | ------------- |:-------------:| :-----:|:-------------:|
  |-in | [String[,]] | Filename to read results (**required**)|  n/a |
  |-username| [String] | Username or access token for push to a target repository (**required**) | n/a |
  |-passw | [String] | User's password for push  to a target repository | empty |
  |-path (p) | [String] | Path relatively a target repository | empty |
  |-repository (-r) | [String] | Destination git repository to push (**required**)| n/a |
  