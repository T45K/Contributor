package io.github.t45k.sclione.service

import com.google.common.annotations.VisibleForTesting
import io.github.t45k.sclione.entity.PrInfo
import org.jsoup.Jsoup
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder

class GitHubService {

    /**
     * GitHub APIとスクレイピングを用いてPR番号とマージコミットを取得する
     * スクレイピングはやりすぎると接続をブロックされるので注意する
     */
    fun fetchPrInfoSequence(token: String, repositoryName: String): Sequence<PrInfo> =
        buildGitHubClient(token)
            .getRepository(repositoryName)
            .getPullRequests(GHIssueState.CLOSED)
            .asSequence()
            .map { PrInfo(it.number, fetchMergeCommitSha(repositoryName, it.number)) }

    @VisibleForTesting
    fun buildGitHubClient(token: String): GitHub =
        GitHubBuilder()
            .withOAuthToken(token)
            .build()

    private fun fetchMergeCommitSha(repositoryName: String, prNumber: Int): String =
        (Jsoup.connect("https://github.com/$repositoryName/pull/$prNumber")
            .get()
            .text()
            .let { "merged commit [0-9a-z]{7}".toRegex().find(it) }
            ?: throw RuntimeException())
            .value
            .substringAfterLast(" ")
}
