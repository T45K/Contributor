package io.github.t45k.contributor.entity

import io.github.t45k.contributor.util.walk
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffAlgorithm
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.AbbreviatedObjectId
import org.eclipse.jgit.lib.Constants
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHub
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
        buildGitHubClient(token)
            .getRepository(repositoryName.name)
            .getPullRequests(GHIssueState.CLOSED)
            .filter { it.isMerged }
            .map { PullRequest(it.number, repositoryName) }

    fun includesModifiedJavaFiles(commit: GitCommit): Boolean =
        GitDiff(git, commit)
            .getModifiedJavaFiles()
            .isNotEmpty()

    fun collectJavaFilesOnCommit(srcDir: Path, commit: GitCommit): List<TrackedJavaFiles> {
        checkout(commit)
        val javaFileToDiffEntries: Map<Path, List<BeginEndLine>> = GitDiff(git, commit)
            .getModifiedJavaFiles()
            .associateBy(
                { Path.of(it.oldPath) },
                { calcFileDiff(it.oldId, it.newId).map { edit -> BeginEndLine(edit.beginA, edit.endA) } }
            )
        return srcDir.walk()
            .filter { it.isRegularFile() && it.toString().endsWith(".java") }
            .map { TrackedJavaFiles(it, javaFileToDiffEntries[it.relativize(srcDir)] ?: emptyList()) }
    }

    private fun buildGitHubClient(token: String): GitHub =
        if (token.isNotEmpty()) {
            GitHubBuilder().withOAuthToken(token)
        } else {
            GitHubBuilder()
        }
            .build()

    private fun checkout(commit: GitCommit) {
        git.checkout()
            .setName(commit.sha)
            .call()
    }

    private fun calcFileDiff(oldId: AbbreviatedObjectId, newId: AbbreviatedObjectId): List<Edit> {
        val oldText: RawText = readBlob(oldId)
        val newText: RawText = readBlob(newId)
        return DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS)
            .diff(RawTextComparator.DEFAULT, oldText, newText)
            ?: emptyList()
    }

    private fun readBlob(blobId: AbbreviatedObjectId): RawText =
        git.repository.newObjectReader()
            .open(blobId.toObjectId(), Constants.OBJ_BLOB)
            .run { RawText(this.cachedBytes) }
}
