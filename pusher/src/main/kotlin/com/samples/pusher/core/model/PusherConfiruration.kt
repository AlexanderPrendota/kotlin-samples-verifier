package com.samples.pusher.core.model

import kotlin.jvm.javaClass
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.ex.ConfigurationException

class PusherConfiruration {

    /**
     *committer's  name and email
     */
    var committerName = "bot"
    var committerEmail= "bot@samples.kotlin.com"

    /**
     *     what branch will the pull request be created for
     */
    var baseBranchPR= "master"


    fun readFromFile(filename: String) {
        val params =  Parameters()
        val builder =
            FileBasedConfigurationBuilder<FileBasedConfiguration>( PropertiesConfiguration::class.java )
                .configure(params.properties().setFileName(filename))

        val config = builder.getConfiguration()
        committerName =  config.getString("committerName",  committerName)
        committerEmail = config.getString("committerEmail", committerEmail)
        baseBranchPR =   config.getString("baseBranchPR",   baseBranchPR)

    }

}