package com.samples.pusher.client

import com.samples.pusher.client.handler.models.PullRequestEvent
import com.samples.pusher.client.handler.models.PushEvent
import com.samples.pusher.core.SamplesPusher
import com.samples.verifier.SamplesVerifier
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class EventType {
  push, pull_request, schedule
}

class GitEventHandler(val verifier: SamplesVerifier, val pusher: SamplesPusher, val options: CheckOptions) {
  private val format = Json { ignoreUnknownKeys = true }

  fun process(eventType: EventType, eventContent: String): Boolean {
    return when (eventType) {
      EventType.push -> process(format.decodeFromString<PushEvent>(eventContent))
      EventType.pull_request -> process(format.decodeFromString<PullRequestEvent>(eventContent))
      EventType.schedule -> processSchedule()
    }

  }

  private fun processSchedule(): Boolean {
    val collection = verifier.collect(
      options.repositoryUrl,
      options.branch,
      options.fileType
    )
    return pusher.push(collection)
  }

  fun process(event: PushEvent): Boolean {
    val collection = verifier.collect(
      event.repository.htmlUrl,
      event.ref,
      options.fileType,
      event.before,
      event.after
    )
    return pusher.push(collection)
  }

  fun process(event: PullRequestEvent): Boolean {
    if (event.action != "opened" ||
      event.action != "synchronize"
    )
      return true

    val collection = verifier.collect(
      event.pullRequest.base.repo.gitUrl,
      event.pullRequest.base.ref,
      event.pullRequest.head.repo.gitUrl,
      event.pullRequest.head.ref,
      options.fileType
    )
    val badSamples = pusher.filterBadSnippets(collection.snippets)
    if (badSamples.isEmpty()) return true
    pusher.createCommentPR(event.number, badSamples, collection, event.repository.htmlUrl)
    return false
  }
}