package io.github.t45k.contributor.entity

data class GitCommit(val sha: String) {
    init {
        sha.takeIf(Regex("[0-9a-z]+")::matches) ?: throw InvalidCommitShaException(sha)
    }
}

class InvalidCommitShaException(sha: String) : RuntimeException() {
    override val message: String = "$sha is invalid git commit id"
}
