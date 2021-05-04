package com.samples.verifier.internal.utils

import com.samples.verifier.GitException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.*
import org.eclipse.jgit.treewalk.filter.PathFilter
import java.io.ByteArrayOutputStream
import java.io.File


internal fun cloneRepository(dir: File, repositoryURL: String, branch: String, bare: Boolean = false): Git {
  try {
    dir.mkdirs()
    return Git.cloneRepository()
      .setDirectory(dir)
      .setURI(repositoryURL)
      .setBranch(branch)
      .setBare(bare)
      .call()
  } catch (e: Exception) {
    throw GitException(e)
  }
}

internal fun getCommit(repo: Repository, id: String = "HEAD"): RevCommit {
  try {
    RevWalk(repo).use { return it.parseCommit(repo.resolve(id)) }
  } catch (e: Exception) {
    throw GitException(e)
  }
}

/**
 *  @param name the branch or commit
 */
internal fun checkout(git: Git, name: String) {
  try {
    git.checkout().setName(name).call()
  } catch (e: Exception) {
    throw GitException(e)
  }
}

internal fun diff(git: Git, startCommit: RevCommit?, endCommit: RevCommit): List<DiffEntry> {
  try {
    val oldTree: ObjectId? = startCommit?.getTree()?.getId()
    val newTree: ObjectId = endCommit.getTree().getId()
    git.repository.newObjectReader().use { reader ->
      val oldTreeIter =
        if (oldTree == null)
          EmptyTreeIterator()
        else
          CanonicalTreeParser(null, reader, oldTree)

      val newTreeIter = CanonicalTreeParser(null, reader, newTree)
      return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
    }
  } catch (e: Exception) {
    throw GitException(e)
  }
}

/**
 * Resolve can return the wrong tree if there is a branch and an abbreviated commit id with this name
 */
internal fun diff(git: Git, startCommit: String, endCommit: String = "HEAD"): List<DiffEntry> {
  val repo = git.getRepository()
  val oldTree: ObjectId = repo.resolve("$startCommit^{tree}")
  val newTree: ObjectId = repo.resolve("$endCommit^{tree}")

  git.repository.newObjectReader().use { reader ->
    val oldTreeIter = CanonicalTreeParser(null, reader, oldTree)
    val newTreeIter = CanonicalTreeParser(null, reader, newTree)
    return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
  }
}

internal fun getModifiedOrAddedFilenames(entryList: List<DiffEntry>): List<String> {
  return entryList
    .filter {
      it.changeType == DiffEntry.ChangeType.ADD ||
        it.changeType == DiffEntry.ChangeType.MODIFY ||
        it.changeType == DiffEntry.ChangeType.RENAME
    }
    .map { it.newPath }
}

internal fun getDeletedFilenames(entryList: List<DiffEntry>): List<String> {
  return entryList
    .filter { it.changeType == DiffEntry.ChangeType.DELETE || it.changeType == DiffEntry.ChangeType.RENAME }
    .map { it.oldPath }
}

/**
 * Extract files from the bare repository
 */
internal fun extractFiles(repository: Repository, commit: RevCommit, files: List<String>): Map<String, String> {
  val tree = commit.tree
  TreeWalk(repository).use { treeWalk ->
    return files.associate {
      treeWalk.reset()
      treeWalk.addTree(tree)
      treeWalk.isRecursive = true
      treeWalk.filter = PathFilter.create(it)
      check(treeWalk.next()) { "Can't find expected file ${it} in a repository" }
      val objectId = treeWalk.getObjectId(0)
      val oLoader = repository.open(objectId)
      val contentToBytes = ByteArrayOutputStream()
      oLoader.copyTo(contentToBytes)
      it to String(contentToBytes.toByteArray())
    }
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