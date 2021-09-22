package io.github.t45k.contributor.entity

import io.github.t45k.contributor.util.walk
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHubBuilder
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class GitRepository private constructor(private val repositoryName: RepositoryName, private val git: Git) {

    companion object {
        fun cloneFromGitHubIfNotExists(repositoryName: RepositoryName): GitRepository =
            if (repositoryName.toLocalPath().exists()) {
                Git(FileRepository(repositoryName.toLocalPath().resolve(".git").toFile()))
            } else {
                Git.cloneRepository()
                    .setURI("https://github.com/$repositoryName.git")
                    .setDirectory(repositoryName.toLocalPath().toFile())
                    .call()
            }.let { GitRepository(repositoryName, it) }
    }

    fun fetchPullRequests(token: String): List<PullRequest> =
        GitHubBuilder()
            .withOAuthToken(token)
            .build()
            .getRepository(repositoryName.name)
            .getPullRequests(GHIssueState.CLOSED)
            .filter { it.isMerged }
            .map { PullRequest(it.number, repositoryName) }

    fun checkout(commit: GitCommit) {
        git.checkout()
            .setName(commit.sha)
            .call()
    }

    fun includesModifiedJavaFiles(commit: GitCommit): Boolean =
        GitDiff(git, commit)
            .getModifiedJavaFiles()
            .isNotEmpty()

    fun collectJavaFilesOnCommit(srcDir: Path, commit: GitCommit): List<TrackedJavaFiles> {
        checkout(commit)
        val javaFileToDiffEntries: Map<Path, List<DiffEntry>> = GitDiff(git, commit)
            .getModifiedJavaFiles()
            .groupBy { Path.of(it.oldPath) }
        return srcDir.walk()
            .filter { it.isRegularFile() && it.toString().endsWith(".java") }
            .map { TrackedJavaFiles(it, javaFileToDiffEntries[it.relativize(srcDir)] ?: emptyList()) }
    }
}
