package com.samples.pusher.client.handler

import com.samples.pusher.client.CheckOptions
import com.samples.pusher.client.handler.models.PullRequestEvent
import com.samples.pusher.client.handler.models.PushEvent
import com.samples.pusher.core.SamplesPusher
import com.samples.verifier.SamplesVerifier
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class EventType {
  PUSH, PULL_REQUEST, SCHEDULE
}

class GitEventHandler(
  private val verifier: SamplesVerifier,
  private val pusher: SamplesPusher,
  private val options: CheckOptions
) {
  private val format = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  fun process(eventType: EventType, eventContent: String): Boolean {
    return when (eventType) {
      EventType.PUSH -> process(format.decodeFromString<PushEvent>(eventContent))
      EventType.PULL_REQUEST -> process(format.decodeFromString<PullRequestEvent>(eventContent))
      EventType.SCHEDULE -> processSchedule()
    }
  }

  fun processSchedule(): Boolean {
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
      && pusher.filterBadSnippets(collection.snippets).isEmpty()
  }

  fun process(event: PullRequestEvent): Boolean {
    /* Anyway check samples

    if (event.action != "opened" &&
      event.action != "synchronize"
    )
      return true*/

    val collection = verifier.collect(
      event.pullRequest.base.repo.htmlUrl,
      event.pullRequest.base.ref,
      event.pullRequest.head.repo.htmlUrl,
      event.pullRequest.head.ref,
      options.fileType
    )
    val badSamples = pusher.filterBadSnippets(collection.snippets)
    if (badSamples.isEmpty()) return true
    pusher.createBadSamplesCommentPR(event.number, badSamples, collection, event.repository.htmlUrl)
    return false
  }
}