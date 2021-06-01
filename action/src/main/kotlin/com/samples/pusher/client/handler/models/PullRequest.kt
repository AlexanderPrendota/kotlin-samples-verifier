package com.samples.pusher.client.handler.models

import kotlinx.serialization.Serializable

@Serializable
data class PullRequest(
  var url: String = "",
  var id: Long = 0,
  var user: User = User(),
  var body: String = "",
  var title: String = "",
  var base: PullRequestMarker = PullRequestMarker(),
  var head: PullRequestMarker = PullRequestMarker()
)

@Serializable
data class PullRequestMarker(
  var repo: Repository = Repository(),
  var label: String = "",
  var ref: String = "",
  var sha: String = "",
  var user: User = User()
)