

Bad samples
Snippets:
<#list snippets as item>
File: ${item.res.fileName}

```kotlin
${item.code}
```

<#list item.res.errors as err>
(${err.interval.start.line}:${err.interval.start.ch}, ${err.interval.end.line}:${err.interval.end.ch}) **${err.severity}** ${err.message}
</#list>
    
------

</#list>
