New samples<#if src.diff?has_content> from ${src.url[src.url?last_index_of("/")+1..]}</#if>

New samples in a repository ${src.url}
<#if src.diff?has_content>Commit: ${src.url}/commit/${src.diff.endRef}</#if>
Files:
<#if src.diff?has_content>
    <#list changedFiles as it>
    [${it}](${src.url}/blob/${src.diff.endRef}/${it})
    </#list>
<#else>
    <#list changedFiles as it>
    [${it}](${src.url}/blob/${src.branch[src.branch?last_index_of("/")+1..]}/${it})
    </#list>
</#if>

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