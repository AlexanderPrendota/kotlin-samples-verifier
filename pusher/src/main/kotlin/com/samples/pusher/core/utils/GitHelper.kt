package com.samples.pusher.core.utils

import com.samples.verifier.GitException
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import java.io.File


/*class GitHelper(val git: Git ) : AutoCloseable {
    override fun close() {
        git.close()
    }

}*/

internal fun cloneRepository(dir: File, repositoryURL: String, branch: String): Git{
    try {
        dir.mkdirs()
        return Git.cloneRepository()
            .setDirectory(dir)
            .setURI(repositoryURL)
            .setBranch(branch)
            .call()

    } catch (e: Exception) {
        throw GitException(e)
    }
}

/*
* @return diff between head and actual working file directory
* */
internal fun diffWorking(git: Git) : List<DiffEntry> {
    val level = Logger.getRootLogger().level
    Logger.getRootLogger().level = Level.ERROR
    try {
        val oldTree: ObjectId = git.repository.resolve("HEAD^{tree}")
        git.repository.newObjectReader().use { reader ->
            val oldTreeIter  = CanonicalTreeParser(null, reader, oldTree)
            val newTreeIter: AbstractTreeIterator = FileTreeIterator(git.repository)
            return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
        }
    } catch (e: Exception) {
        throw GitException(e)
    } finally {
        Logger.getRootLogger().level = level
    }
}
