package com.samples.pusher.core


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.pusher.core.utils.cloneRepository
import com.samples.verifier.Code
import com.samples.verifier.GitException
import com.samples.verifier.model.ExecutionResult
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random.Default.nextInt


internal class SamplesPusher(val url: String, val path: String,
                             val username: String, val password: String = "",
                             val branch: String = "master") {
    private val logger = LoggerFactory.getLogger("Samples Pusher")

    fun push(inputFile: String) {
        val mapper = jacksonObjectMapper()
        val res = mapper.readValue(File(inputFile), object : TypeReference<Map<Code, ExecutionResult>>() {})
        val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))

        try {

            val git = cloneRepository(dir, url, branch)
            val dirSamples = File(dir.path + File.pathSeparator + path)
            if (!dirSamples.exists())
                if (!dirSamples.mkdirs()) {
                    logger.error("Can't directory by path ${dirSamples.path}")
                    return
                }


            logger.debug("Created the path ${dirSamples.path}")
            val branchName = "new-branch-${nextInt()}"
            git.checkout().setCreateBranch(true).setName(branchName).call()

            var i = 0
            res.forEach { File(dirSamples, "${++i}.kt").writeText(it.key) }

            logger.debug(".kt files are  writed ")


            git.add().addFilepattern(".").call()
            git.commit().setAll(true).setAllowEmpty(true).setMessage("New samples").setCommitter("pusher", "bot@samples.kotlin.com").call()

            val credentialsProvider: CredentialsProvider = UsernamePasswordCredentialsProvider(username, password)
            git.push().setRemote(url).setCredentialsProvider(credentialsProvider).call()

            logger.debug(".kt are pushed")

            val client = GitHubClient()

            if(password.isEmpty())
                client.setOAuth2Token(username)
            else
                client.setCredentials(username, password)

            val prServise = org.eclipse.egit.github.core.service.PullRequestService(client)
            var pr = PullRequest()
            pr.setTitle("New samples ")
            pr.setBody("New files")
            pr.setBase(PullRequestMarker().setLabel(branchName))
            pr.setHead(PullRequestMarker().setLabel("master"))
            logger.info(RepositoryId.createFromUrl(url).generateId())
            pr = prServise.createPullRequest(RepositoryId.createFromUrl(url), pr)
            logger.debug("push request  is created ${pr.url}")

        } catch (e: GitException) {
            logger.error("${e.message}")
        } finally {
            if (dir.isDirectory) {
                dir.deleteRecursively()
            } else {
                dir.delete()
            }
        }
    }

}