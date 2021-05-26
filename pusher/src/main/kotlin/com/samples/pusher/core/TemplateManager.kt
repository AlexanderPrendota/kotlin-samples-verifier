package com.samples.pusher.core

import com.samples.pusher.core.utils.isHttpUrl
import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.StringWriter
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class TemplateManager {
  data class Template(val head: String, val body: String)

  private val cfgTemplates: Configuration = Configuration(Configuration.VERSION_2_3_31)

  fun configureTemplate(path: String) {
    if (path.isHttpUrl()) {
      cfgTemplates.templateLoader = object : URLTemplateLoader() {
        override fun getURL(name: String) = runCatching { URL("$path/$name") }.getOrNull()
      }
    } else {
      cfgTemplates.setDirectoryForTemplateLoading(File(path))
    }
    cfgTemplates.unsetLocale()
    cfgTemplates.defaultEncoding = "UTF-8"
    cfgTemplates.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    cfgTemplates.logTemplateExceptions = false
    cfgTemplates.wrapUncheckedExceptions = true
    cfgTemplates.fallbackOnNullLoopVariable = false
  }

  fun getTemplate(name: String, model: HashMap<String, Any>): Template {
    val temp = cfgTemplates.getTemplate(name)

    val out = StringWriter()
    temp.process(model, out)

    val strs = out.toString()
    val head = strs.substringBefore("\n\n")
    var body = strs.substringAfter("\n\n")
    if (body.isEmpty())
      throw Exception("Template has to contain body")
    if (body.length > MAX_BODY_LENGTH)
      body = body.substring(0, MAX_BODY_LENGTH - TOO_LONG.length) + TOO_LONG
    return Template(head, body)
  }

  fun getBranchName(): String {
    val sdf = SimpleDateFormat("ddMMyyhhmmss")
    val currentDate = sdf.format(Date())
    return "new-samples-$currentDate"
  }

  companion object {
    const val MAX_BODY_LENGTH = 65536
    const val TOO_LONG = " [TOO LONG]"
  }
}