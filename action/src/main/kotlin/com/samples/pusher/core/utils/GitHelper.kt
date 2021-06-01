package com.samples.pusher.core.utils

import com.samples.verifier.GitException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import java.io.File

internal fun cloneRepository(dir: File, repositoryURL: String, branch: String): Git {
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

internal fun initRepository(dir: File): Git {
  try {
    return Git.init()
      .setDirectory(dir)
      .call()
  } catch (e: Exception) {
    throw GitException(e)
  }
}

/**
 * @return diff between head and actual working file directory
 */
internal fun diffWorking(git: Git): List<DiffEntry> {
  try {
    val oldTree: ObjectId = git.repository.resolve("HEAD^{tree}")
    git.repository.newObjectReader().use { reader ->
      val oldTreeIter = CanonicalTreeParser(null, reader, oldTree)
      val newTreeIter: AbstractTreeIterator = FileTreeIterator(git.repository)
      return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
    }
  } catch (e: Exception) {
    throw GitException(e)
  }
}

internal fun pushRepo(git: Git, url: String, credentialsProvider: CredentialsProvider) {
  val iterResult = git.push().setRemote(url).setCredentialsProvider(credentialsProvider).call().iterator()
  // check result
  if (!iterResult.hasNext()) {
    throw GitException("No push result")
  }
  val result: PushResult = iterResult.next()
  if (result.remoteUpdates.isEmpty()) {
    //logger.warn("No remote updates occurred")
  } else {
    for (update in result.remoteUpdates) {
      if (RemoteRefUpdate.Status.NON_EXISTING != update.status
        && RemoteRefUpdate.Status.OK != update.status
      ) {
        throw GitException("Expected non-existent ref but got: ${update.status.name}:${update.message}")
      }
    }
  }
}

