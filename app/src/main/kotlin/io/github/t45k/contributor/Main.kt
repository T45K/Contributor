package io.github.t45k.contributor

import io.github.t45k.contributor.entity.GitCommit
import io.github.t45k.contributor.entity.GitRepository
import io.github.t45k.contributor.entity.Inconsistency
import io.github.t45k.contributor.entity.RepositoryName
import io.github.t45k.contributor.entity.findInconsistencies
import java.nio.file.Path
import java.util.ResourceBundle
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.writeText

@ExperimentalPathApi
fun main(args: Array<String>) {
    // TODO parse
    val (repositoryNameText, srcDirName) = args
    val token = (ResourceBundle.getBundle("resource")
        ?.getString("GITHUB_TOKEN")
        ?: throw RuntimeException())

    val repositoryName = RepositoryName(repositoryNameText)
    val gitRepository = GitRepository.cloneFromGitHubIfNotExists(repositoryName)
    val resultFile = Path.of(repositoryNameText.replace("/", "_"))
    for (pullRequest in gitRepository.fetchPullRequests(token)) {
        val mergeCommit: GitCommit = pullRequest.mergeCommit
            .takeIf { gitRepository.includesModifiedJavaFiles(it) }
            ?: continue
        val inconsistencies: List<Inconsistency> = runCatching {
            gitRepository.collectJavaFilesOnCommit(Path.of(srcDirName), mergeCommit)
                .flatMap { it.extractCodeBlocks() }
                .findInconsistencies()
        }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: continue

        resultFile.writeText(pullRequest.number.toString() + System.lineSeparator())
        resultFile.writeText(inconsistencies.joinToString(System.lineSeparator()) + System.lineSeparator() + System.lineSeparator())
    }
}
