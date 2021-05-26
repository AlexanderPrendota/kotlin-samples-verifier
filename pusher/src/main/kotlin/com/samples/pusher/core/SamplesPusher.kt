package com.samples.pusher.core

import com.samples.pusher.core.model.PusherConfiguration
import com.samples.verifier.model.CollectionOfRepository

interface SamplesPusher {
	fun readConfigFromFile(filename: String): SamplesPusherImpl
	fun configure(fn: PusherConfiguration.() -> Unit): SamplesPusherImpl

	/**
	 * @return true if all is ok
	 */
	fun push(collection: CollectionOfRepository, isCreateIssue: Boolean = true): Boolean
	fun filterBadSnippets(res: CollectionSamples): List<Snippet>
	fun createCommentPR(
		id: Long,
		badSnippets: List<Snippet>,
		res: CollectionOfRepository,
		repositoryUrl: String
	)
}