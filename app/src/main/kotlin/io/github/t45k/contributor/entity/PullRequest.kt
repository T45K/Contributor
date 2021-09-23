package io.github.t45k.contributor.entity

import org.jsoup.Jsoup

class PullRequest(val number: Int, private val repositoryName: RepositoryName) {
    init {
        number.takeIf { it > 0 } ?: throw InvalidPullRequestNumberException(number)
    }

    val mergeCommit: GitCommit by lazy { fetchMergeCommitSha() }

    private fun fetchMergeCommitSha(): GitCommit =
        (Jsoup.connect("${repositoryName.gitHubUrl}/pull/$number")
            .get()
            .text()
            .let { "merged commit [0-9a-z]{7}".toRegex().find(it) }
            ?: throw MergeCommitNotFoundException(number))
            .value
            .substringAfterLast(" ")
            .let { GitCommit(it) }
}

class InvalidPullRequestNumberException(number: Int) : RuntimeException() {
    override val message: String = "$number is invalid pull request number"
}

class MergeCommitNotFoundException(number: Int) : RuntimeException() {
    override val message: String = "PR number: $number doesn't have a merge commit"
}
