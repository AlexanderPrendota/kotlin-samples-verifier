package com.samples.pusher.core

import com.samples.pusher.core.model.PusherConfiguration
import com.samples.pusher.core.utils.cloneRepository
import com.samples.pusher.core.utils.diffWorking
import com.samples.pusher.core.utils.initRepository
import com.samples.pusher.core.utils.pushRepo
import com.samples.verifier.Code
import com.samples.verifier.GitException
import com.samples.verifier.model.CollectionOfRepository
import com.samples.verifier.model.ExecutionResult
import com.samples.verifier.model.ProjectSeverity
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File


typealias CollectionSamples = Map<Code, ExecutionResult>

data class Snippet(val code: Code, val res: ExecutionResult)

class SamplesPusherImpl(
  val url: String,
  val user: String,
  val password: String = "",
  val branch: String = "master",
  val path: String,
  templatePath: String = "templates"
) : SamplesPusher {
  private val templates = TemplateManager()

  init {
    templates.configureTemplate(templatePath)
  }

  private val logger = LoggerFactory.getLogger("Samples Pusher")
  private var configuraton: PusherConfiguration = PusherConfiguration()

  override fun readConfigFromFile(filename: String): SamplesPusherImpl {
    configuraton.readFromFile(filename)
    return this
  }

  override fun configure(fn: PusherConfiguration.() -> Unit): SamplesPusherImpl {
    configuraton.apply(fn)
    return this
  }


  private val ghClient: GitHubClient by lazy { createGHClient() }

  /**
   * @return true if all is ok
   */
  override fun push(collection: CollectionOfRepository, isCreateIssue: Boolean): Boolean {
    if (collection.snippets.isEmpty() && collection.diff?.deletedFiles.isNullOrEmpty()) {
      logger.info("Nothing is to push")
      return true
    }

    val dir = File(url.substringAfterLast('/').substringBeforeLast('.'))

    try {
      val git = if (dir.exists()) {
        logger.debug("Using exist repository... ")
        initRepository(dir)
      } else {
        logger.debug("Cloning the repository... ")
        cloneRepository(dir, url, branch)
      }
      val dirSamples = prepareTargetPath(dir)

      val branchName = templates.getBranchName()
      git.checkout().setCreateBranch(true).setName(branchName).call()

      val mng = SnippetManager(dirSamples)
      val errors =
        writeAndDeleteSnippets(mng, collection.snippets, collection.diff?.deletedFiles ?: emptyList())
      logger.debug(".kt files are  written")

      if (isCreateIssue && errors.isNotEmpty())
        createIssue(errors, collection, collection.url)

      val df = diffWorking(git)
      if (df.isNotEmpty()) {
        commitAndPush(git)
        logger.debug(".kt are pushed into branch: $branchName")
        createPR(collection, errors, branchName)
      }
      return errors.isEmpty()
    } catch (e: GitException) {
      logger.error("${e.message}")
    } finally {
      if (dir.isDirectory) {
        dir.deleteRecursively()
      } else {
        dir.delete()
      }
    }
    return true
  }

  override fun filterBadSnippets(res: CollectionSamples): List<Snippet> {
    return res.filter {
      it.value.errors.any { err ->
        greterOrEqualSeverity(err.severity)
      }
    }.map { Snippet(it.key, it.value) }
  }

  private fun prepareTargetPath(repoDir: File): File {
    val dirSamples = repoDir.resolve(path)
    if (!dirSamples.exists())
      if (!dirSamples.mkdirs()) {
        throw Exception("Can't create directory by path ${dirSamples.path}")
      }

    logger.debug("Created the path ${dirSamples.path}")
    return dirSamples
  }

  private fun writeAndDeleteSnippets(
    manager: SnippetManager,
    res: CollectionSamples,
    deleteFiles: List<String>
  ): List<Snippet> {
    deleteFiles.forEach { manager.removeAllSnippets(it) }
    val badSnippets = mutableListOf<Snippet>()
    res.forEach {
      if (it.value.errors.isNotEmpty()) {
        logger.error("Filename: ${it.value.fileName}")
        logger.error("Code: \n${it.key}")
        logger.error("Errors: \n${it.value.errors.joinToString("\n")}")
      }

      if (it.value.errors.any { err ->
          greterOrEqualSeverity(err.severity)
        }) {
        badSnippets.add(Snippet(it.key, it.value))
      } else {
        manager.addSnippet(it.key, it.value.fileName)
      }
    }
    return badSnippets
  }

  private fun greterOrEqualSeverity(severity: ProjectSeverity): Boolean {
    return severity >= configuraton.severity
  }

  private fun commitAndPush(git: Git) {
    git.add().addFilepattern(".").call()
    git.commit()
      .setAll(true)
      .setAllowEmpty(true)
      .setMessage(configuraton.commitMessage)
      .setCommitter(configuraton.committerName, configuraton.committerEmail)
      .call()

    val credentialsProvider: CredentialsProvider = UsernamePasswordCredentialsProvider(user, password)
    pushRepo(git, url, credentialsProvider)
  }

  // GitHub helpers
  private fun createGHClient(): GitHubClient {
    val client = GitHubClient()

    if (password.isEmpty())
      client.setOAuth2Token(user)
    else
      client.setCredentials(user, password)
    return client
  }

  private fun createPR(res: CollectionOfRepository, badSnippets: List<Snippet>, headBranch: String) {
    val model = HashMap<String, Any>()
    model["badSnippets"] = badSnippets
    model["src"] = res
    val temp = templates.getTemplate("pr.md", model)

    val prServise = org.eclipse.egit.github.core.service.PullRequestService(ghClient)
    var pr = PullRequest()
    pr.title = temp.head
    pr.body = temp.body
    pr.base = PullRequestMarker().setLabel(configuraton.baseBranchPR)
    pr.head = PullRequestMarker().setLabel(headBranch)
    //logger.debug("RepoId: " + RepositoryId.createFromUrl(url).generateId())
    pr = prServise.createPullRequest(RepositoryId.createFromUrl(url), pr)
    logger.info("The Push request is created, url: ${pr.htmlUrl}")
  }


  private fun createIssue(
    badSnippets: List<Snippet>,
    res: CollectionOfRepository,
    repositoryUrl: String = url
  ) {
    val model = HashMap<String, Any>()
    model["snippets"] = badSnippets
    model["src"] = res
    val temp = templates.getTemplate("issue.md", model)

    val issueService = org.eclipse.egit.github.core.service.IssueService(ghClient)
    var issue = Issue()
    issue.title = temp.head
    issue.body = temp.body

    issue = issueService.createIssue(RepositoryId.createFromUrl(repositoryUrl), issue)
    logger.info("The Issue  is created, url: ${issue.htmlUrl}")
  }

  override fun createCommentPR(
    id: Long,
    badSnippets: List<Snippet>,
    res: CollectionOfRepository,
    repositoryUrl: String
  ) {
    val model = HashMap<String, Any>()
    model["snippets"] = badSnippets
    model["src"] = res
    val temp = templates.getTemplate("pr-comment.md", model)

    val issueService = org.eclipse.egit.github.core.service.IssueService(ghClient)

    val comment = issueService.createComment(RepositoryId.createFromUrl(repositoryUrl), id.toInt(), temp.body)
    logger.info("The pr comment  is created, url: ${comment.url}")
  }

}