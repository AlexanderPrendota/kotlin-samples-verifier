package com.samples.pusher.core.model

import com.samples.verifier.model.ProjectSeveriry
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.net.URL

class PusherConfiruration {

  var severity: ProjectSeveriry = ProjectSeveriry.ERROR

  /**
   *  committer's  name and email
   */
  var committerName = "bot"
  var committerEmail = "bot@samples.kotlin.com"


  /**
   *  commit message
   */
  var commitMsg = "New samples"

  /**
   *     what branch will the pull request be created for
   */
  var baseBranchPR = "master"


  fun readFromFile(filename: String) {
    val props = Parameters().properties()
    if (filename.indexOf("http://", 0, true) == 0 ||
      filename.indexOf("https://", 0, true) == 0
    ) {
      props.setURL(URL(filename))
    } else {
      props.setFileName(filename)
    }
    val builder =
      FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration::class.java)
        .configure(props)

    val config = builder.getConfiguration()
    committerName = config.getString("committerName", committerName)
    committerEmail = config.getString("committerEmail", committerEmail)
    commitMsg = config.getString("commitMsg", commitMsg)
    baseBranchPR = config.getString("baseBranchPR", baseBranchPR)

  }

}