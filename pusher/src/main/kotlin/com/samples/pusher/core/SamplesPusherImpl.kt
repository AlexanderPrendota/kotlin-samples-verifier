package com.samples.pusher.core


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.samples.pusher.core.utils.cloneRepository
import com.samples.verifier.Code
import com.samples.verifier.GitException
import com.samples.verifier.model.ExecutionResult
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random.Default.nextInt

import org.kohsuke.github.GHIssueBuilder

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

            git.checkout().setCreateBranch(true).setName("new-branch-${nextInt()}").call();

            var i = 0
            res.forEach { File(dirSamples, "${++i}.kt").writeText(it.key) }

            logger.debug(".kt files are  writed ")


            git.add().addFilepattern(".").call();
            git.commit().setAll(true).setAllowEmpty(true).setMessage("New samples").setCommitter("pusher", "bot@samples.kotlin.com").call();

            val credentialsProvider: CredentialsProvider = UsernamePasswordCredentialsProvider(username, password)
            git.push().setRemote(url).setCredentialsProvider(credentialsProvider).call()

            logger.debug(".kt are pushed")



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