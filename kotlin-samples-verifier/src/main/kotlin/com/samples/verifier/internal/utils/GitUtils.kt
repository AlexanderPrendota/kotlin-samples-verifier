package com.samples.verifier.internal.utils

import com.samples.verifier.GitException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
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

internal fun diff(git: Git, startCommit: RevCommit?, endCommit: RevCommit): List<DiffEntry> {
  try {
    val oldTree: ObjectId? = startCommit?.tree?.id
    val newTree: ObjectId = endCommit.tree.id
    git.repository.newObjectReader().use { reader ->
      val oldTreeIter = if (oldTree == null)
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
 * RevCommits need to be produced by the same RevWalk instance
 */
internal fun mergeBase(git: Git, base: String, head: String): RevCommit {
  try {
    val repo = git.repository
    RevWalk(repo).use {
      val baseCommit = it.parseCommit(repo.resolve(base))
      val headCommit = it.parseCommit(repo.resolve(head))

      it.setRevFilter(RevFilter.MERGE_BASE)
      it.markStart(headCommit)
      it.markStart(baseCommit)
      return it.parseCommit(it.next()) // Be carefull, commits may have multiple merge bases
    }
  } catch (e: Exception) {
    throw GitException(e)
  }
}

/**
 * Fetch the ref from a remote repository to remote/{ref}
 */
internal fun fetch(git: Git, url: String, branch: String): FetchResult {
  val ref = if(branch.indexOf("refs/")==0) branch else "refs/heads/$branch"
  return git.fetch()
    .setRemote(url)
    .setRefSpecs("+$ref:remote/$ref")
    .call()
}

/**
 * Extract files from the bare repository
 */
internal fun extractFiles(repository: Repository, commit: RevCommit, files: List<String>): Map<String, String> {
  val tree = commit.tree
  TreeWalk(repository).use { treeWalk ->
    return files.associateWith {
      treeWalk.reset()
      treeWalk.addTree(tree)
      treeWalk.isRecursive = true
      treeWalk.filter = PathFilter.create(it)
      check(treeWalk.next()) { "Can't find expected file $it in a repository" }
      val objectId = treeWalk.getObjectId(0)
      val oLoader = repository.open(objectId)
      val contentToBytes = ByteArrayOutputStream()
      oLoader.copyTo(contentToBytes)
      String(contentToBytes.toByteArray())
    }
  }
}