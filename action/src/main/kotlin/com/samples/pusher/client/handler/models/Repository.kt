package com.samples.pusher.client.handler.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Repository(
  var name: String = "",
  @SerialName("full_name") var fullName: String = "",
  @SerialName("html_url") var htmlUrl: String = "",
  @SerialName("git_url") var gitUrl: String = "",
  var url: String = ""
)