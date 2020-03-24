package com.samples.verifier.internal.utils

import com.samples.verifier.GitException
import org.eclipse.jgit.api.Git
import java.io.File

internal fun cloneRepository(dir: File, repositoryURL: String) {
    try {
        dir.mkdirs()
        val git = Git.cloneRepository()
            .setDirectory(dir)
            .setURI(repositoryURL)
            .call()
        git.close()
    } catch (e: Exception) {
        throw GitException(e)
    }
}