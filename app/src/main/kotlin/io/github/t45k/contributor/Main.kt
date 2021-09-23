package io.github.t45k.contributor

import io.github.t45k.contributor.git.GitCommit
import io.github.t45k.contributor.git.GitRepository
import io.github.t45k.contributor.git.RepositoryName
import io.github.t45k.contributor.sourceCode.findInconsistencies
import java.nio.file.Path
import java.util.ResourceBundle
import kotlin.io.path.appendText
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists

fun main(args: Array<String>) {
    // TODO parse
    val (repositoryNameText, srcDirName) = args
    val token = ResourceBundle.getBundle("resource")
        ?.getString("GITHUB_TOKEN")
        ?: ""

    val repositoryName = RepositoryName(repositoryNameText)
    val gitRepository = GitRepository.cloneFromGitHubIfNotExists(repositoryName)
    val resultFile = Path.of(".", repositoryNameText.replace("/", "_"))
        .apply { deleteIfExists() }
        .createFile()
    val srcDir = repositoryName.localPath.resolve(srcDirName)
    for (pullRequest in gitRepository.fetchPullRequests(token)) {
        println(pullRequest.number)
        val inconsistencies = runCatching {
            val mergeCommit: GitCommit = pullRequest.mergeCommit
                .takeIf { gitRepository.includesModifiedJavaFiles(it) }
                ?: throw RuntimeException()
            gitRepository.collectJavaFilesOnCommit(srcDir, mergeCommit)
                .flatMap { it.extractCodeBlocks() }
                .findInconsistencies()
                .takeIf { it.isNotEmpty() }
                ?: throw RuntimeException()
        }
            .getOrNull()
            ?: continue

        resultFile.appendText(
            """
            |${pullRequest.number}
            |${inconsistencies.joinToString(System.lineSeparator())}
            |
            |
        """.trimMargin()
        )
    }
}
