package com.samples.pusher.core.model

import com.samples.pusher.core.utils.isHttpUrl
import com.samples.verifier.model.ProjectSeverity
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.net.URL

class PusherConfiguration {

  var severity: ProjectSeverity = ProjectSeverity.ERROR

  var committerEmail = "bot@samples.kotlin.com"
  var committerName = "kotlin-samples-pusher-bot"
  var baseBranchPR = "master"
  var commitMessage = "test(samples): add new samples"


  fun readFromFile(filename: String) {
    val props = Parameters().properties()
    if (filename.isHttpUrl()) {
      props.setURL(URL(filename))
    } else {
      props.setFileName(filename)
    }
    val builder =
      FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration::class.java)
        .configure(props)

    val config = builder.configuration
    committerName = config.getString("committerName", committerName)
    committerEmail = config.getString("committerEmail", committerEmail)
    commitMessage = config.getString("commitMsg", commitMessage)
    baseBranchPR = config.getString("baseBranchPR", baseBranchPR)

  }

}