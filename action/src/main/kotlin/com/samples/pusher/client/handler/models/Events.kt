package com.samples.pusher.client.handler.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Event

@Serializable
data class PullRequestEvent(
  var action: String = "",
  var number: Long,
  var repository: Repository = Repository(),
  var sender: User = User(),
  @SerialName("pull_request") var pullRequest: PullRequest = PullRequest()
) : Event

@Serializable
data class Pusher(
  var name: String = "",
  var email: String = ""
)

@Serializable
data class PushEvent(
  var ref: String = "",
  var after: String = "",
  var before: String = "",
  var repository: Repository = Repository(),
  var pusher: Pusher = Pusher(),
  var sender: User = User()
) : Event