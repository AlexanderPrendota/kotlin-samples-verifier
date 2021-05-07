package com.samples.pusher.client

import com.samples.pusher.core.SamplesPusher
import com.samples.verifier.SamplesVerifier
import kotlinx.serialization.json.Json

enum class EventType {
    push, pull_request
}
class GitEventHandler(val verifier: SamplesVerifier, val pusher: SamplesPusher) {
    val format = Json { ignoreUnknownKeys = true }
    fun process(eventType: EventType, eventContent: String) {

    }


}