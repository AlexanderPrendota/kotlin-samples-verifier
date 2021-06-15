package com.samples.pusher.core

import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.IssueService
import org.eclipse.egit.github.core.service.PullRequestService
import org.slf4j.LoggerFactory

class GitHubService(
  private val user: String,
  private val password: String = ""
) {
  private val ghClient by lazy { createGHClient() }
  private val prService by lazy { PullRequestService(ghClient) }
  private val issueService by lazy { IssueService(ghClient) }
  private val logger = LoggerFactory.getLogger("GitHub Service")

  // GitHub helpers
  private fun createGHClient(): GitHubClient {
    val client = GitHubClient()

    if (password.isEmpty()) {
      client.setOAuth2Token(user)
    } else {
      client.setCredentials(user, password)
    }
    return client
  }

  fun getPR(
    repositoryUrl: String,
    baseBranch: String,
    headBranch: String
  ): PullRequest? {
    val repoId = RepositoryId.createFromUrl(repositoryUrl)
    val prs = prService.getPullRequests(repoId, "open") // GitHub API supports the filters
    return prs.find {
      it.base.ref == baseBranch
        && it.head.ref == headBranch
        && it.head.repo.id == it.base.repo.id /*the same repo*/
    }
  }

  fun createCommentPR(
    repositoryUrl: String,
    id: Long,
    temp: TemplateManager.Template
  ) {
    val comment = issueService.createComment(RepositoryId.createFromUrl(repositoryUrl), id.toInt(), temp.body)
    logger.info("The pr comment  is created, url: ${comment.url}")
  }

  fun createIssue(
    repositoryUrl: String,
    temp: TemplateManager.Template
  ) {
    var issue = Issue()
    issue.title = temp.head
    issue.body = temp.body

    issue = issueService.createIssue(RepositoryId.createFromUrl(repositoryUrl), issue)
    logger.info("The Issue  is created, url: ${issue.htmlUrl}")
  }

  fun createPr(
    repositoryUrl: String,
    base: String,
    head: String,
    temp: TemplateManager.Template
  ) {
    var pr = PullRequest()
    pr.title = temp.head
    pr.body = temp.body
    pr.base = PullRequestMarker().setLabel(base)
    pr.head = PullRequestMarker().setLabel(head)
    //logger.debug("RepoId: " + RepositoryId.createFromUrl(url).generateId())
    pr = prService.createPullRequest(RepositoryId.createFromUrl(repositoryUrl), pr)
    logger.info("The Push request is created, url: ${pr.htmlUrl}")
  }

}