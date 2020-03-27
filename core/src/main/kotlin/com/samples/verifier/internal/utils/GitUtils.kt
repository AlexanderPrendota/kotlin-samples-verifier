package com.samples.verifier.internal.utils

import com.samples.verifier.GitException
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.eclipse.jgit.api.Git
import java.io.File

internal fun cloneRepository(dir: File, repositoryURL: String) {
    val level = Logger.getRootLogger().level

    Logger.getRootLogger().level = Level.OFF
    try {
        dir.mkdirs()
        val git = Git.cloneRepository()
            .setDirectory(dir)
            .setURI(repositoryURL)
            .call()
        git.close()
    } catch (e: Exception) {
        throw GitException(e)
    } finally {
        Logger.getRootLogger().level = level
    }
}