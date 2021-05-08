package com.samples.pusher.client.handler.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
  var login: String = "",
  var id: Long = 0,
  var url: String = "",
  @SerialName("html_url") var htmlUrl: String = "",
  @SerialName("avatar_url") var avatarUrl: String = ""
)