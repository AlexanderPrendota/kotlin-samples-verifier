package com.samples.pusher.core.utils

import com.samples.verifier.GitException
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.eclipse.jgit.api.Git
import java.io.File

internal fun cloneRepository(dir: File, repositoryURL: String, branch: String): Git {
    val level = Logger.getRootLogger().level
    Logger.getRootLogger().level = Level.ERROR
    try {
        dir.mkdirs()
        return Git.cloneRepository()
            .setDirectory(dir)
            .setURI(repositoryURL)
            .setBranch(branch)
            .call()

    } catch (e: Exception) {
        throw GitException(e)
    } finally {
        Logger.getRootLogger().level = level
    }
}