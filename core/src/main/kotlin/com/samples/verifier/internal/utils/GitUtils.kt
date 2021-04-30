package com.samples.verifier.internal.utils

import com.samples.verifier.GitException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
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

/*
	 * @param name
	 *            the name of the branch or commit
 */
internal fun checkout(git: Git, name: String) {
  try {
    git.checkout().setName(name).call()
  } catch (e: Exception) {
    throw GitException(e)
  }
}

internal fun diff(git: Git, commit1: RevCommit, commit2: RevCommit): List<DiffEntry> {
  try {
    val oldTree: ObjectId = commit1.getTree().getId()
    val newTree: ObjectId = commit2.getTree().getId()
    git.repository.newObjectReader().use { reader ->
      val oldTreeIter = CanonicalTreeParser(null, reader, oldTree)
      val newTreeIter = CanonicalTreeParser(null, reader, newTree)
      return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
    }
  } catch (e: Exception) {
    throw GitException(e)
  }
}

/*
  Resolve can return the wrong tree if there is a branch and an abbreviated commit id with this name
 */
internal fun diff(git: Git, commit1: String, commit2: String = "HEAD"): List<DiffEntry> {
  val repo = git.getRepository()
  val oldTree: ObjectId = repo.resolve("$commit1^{tree}")
  val newTree: ObjectId = repo.resolve("$commit2^{tree}")

  git.repository.newObjectReader().use { reader ->
    val oldTreeIter = CanonicalTreeParser(null, reader, oldTree)
    val newTreeIter = CanonicalTreeParser(null, reader, newTree)
    return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call()
  }
}

internal fun getModifiedOrAddedFilenames(entryList: List<DiffEntry>): MutableList<String> {
  val res = mutableListOf<String>()
  entryList.forEach {
    if (it.changeType == DiffEntry.ChangeType.ADD || it.changeType == DiffEntry.ChangeType.MODIFY
      || it.changeType == DiffEntry.ChangeType.RENAME) res.add(it.newPath)
  }
  return res
}

internal fun getDeletedFilenames(entryList: List<DiffEntry>): MutableList<String> {
  val res = mutableListOf<String>()
  entryList.forEach {
    if (it.changeType == DiffEntry.ChangeType.DELETE) res.add(it.oldPath)
    else if (it.changeType == DiffEntry.ChangeType.RENAME) res.add(it.oldPath)
  }
  return res
}

/*
  Extract files from the bare repository
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

/*
* @return diff between head and actual working file directory
* */
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