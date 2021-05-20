New samples<#if src.diff?has_content> from commit</#if>

New samples in a repository ${src.url}
<#if src.diff?has_content>Commit: ${src.url}/commit/${src.diff.endRef}</#if>
Files:
<#list src.snippets as code, value>
 ${value.fileName}
    <#--  ```${code}```  -->
</#list>
<#if badSnippets?has_content>
Bad samples:
    <#list badSnippets as item>
      File: ${item.res.fileName}
    
    ```kotlin
    ${item.code}
    ```
    
    <#list item.res.errors as err>
    (${err.interval.start.line}:${err.interval.start.ch}, ${err.interval.end.line}:${err.interval.end.ch}) **${err.severity}** ${err.message}
    </#list>
        
    ------
    
    </#list>
</#if>