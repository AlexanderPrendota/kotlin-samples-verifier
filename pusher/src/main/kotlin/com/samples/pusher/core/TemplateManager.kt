package com.samples.pusher.core

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

class TemplateManager {
  data class Template(val head: String, val body: String)

  private val cfgTemplates: Configuration = Configuration(Configuration.VERSION_2_3_31)

  init {
    configureTemplate()
  }

  fun configureTemplate() {
    cfgTemplates.setDirectoryForTemplateLoading(File("templates"))
    cfgTemplates.setDefaultEncoding("UTF-8")
    cfgTemplates.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
    cfgTemplates.setLogTemplateExceptions(false)
    cfgTemplates.setWrapUncheckedExceptions(true)
    cfgTemplates.setFallbackOnNullLoopVariable(false)
  }

  fun getTemplate(name: String, model: HashMap<String, Any>): Template {
    val temp = cfgTemplates.getTemplate(name)

    val out = StringWriter()
    temp.process(model, out)

    val strs = out.toString()
    val head = strs.substringBefore("\n\n")
    val body = strs.substringAfter("\n\n")
    if (body.isEmpty())
      throw Exception("Template has to contain header and body")
    return Template(head, body)
  }

  fun getBranchName(): String {
    val sdf = SimpleDateFormat("dd.M.yy-hh:mm:ss")
    val currentDate = sdf.format(Date())
    return "new-samples-$currentDate"
  }

}