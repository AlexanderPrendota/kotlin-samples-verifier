New samples

New samples in a repository ${src.url}
Files:
<#list src.snippets as code, value>
File: ${value.fileName}
    ```${code}``` 
</#list>