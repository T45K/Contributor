package io.github.t45k.contributor

import io.github.t45k.contributor.service.GitHubService
import io.github.t45k.contributor.service.GitService
import io.github.t45k.contributor.service.inconsistencyDetection.InconsistencyDetectionService
import io.github.t45k.contributor.service.inconsistencyDetection.JavaParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isRegularFile
import kotlin.streams.toList

@ExperimentalPathApi
class FindInconsistencyUsecase(
    private val gitHubService: GitHubService,
    private val gitService: GitService,
    private val javaParser: JavaParser,
    private val inconsistencyDetectionService: InconsistencyDetectionService,
) {

    fun findInconsistencies(repositoryName: String, srcDirName: String) {
        val srcDirectory = Path.of("./storage", repositoryName, srcDirName)
        gitHubService.fetchPrInfoSequence(repositoryName)
            .filter { gitService.existsCommit(it.mergeCommitSha) }
            .map { prInfo -> prInfo to gitService.getMergeBaseCommitSha(prInfo.mergeCommitSha) }
            .filter { (prInfo, baseCommitSha) ->
                gitService.checkout(baseCommitSha)
                gitService.includesModifiedJavaFiles(baseCommitSha, prInfo.mergeCommitSha)
            }
            .mapAndFilter { (prInfo, baseCommitSha) ->
                val clones = Files.walk(srcDirectory)
                    .toList()
                    .filter { it.isRegularFile() && it.toString().endsWith(".java") }
                    .map { it.toAbsolutePath() to javaParser.collectCodeBlocks(it) }
                    .onEach { (filePath, codeBlocks) ->
                        for (edit in gitService.calcFileDiff(filePath, baseCommitSha, prInfo.mergeCommitSha)) {
                            codeBlocks
                                .filter { edit.endA >= it.startLine && it.endLine >= edit.beginA }
                                .forEach { it.isModified = true }
                        }
                    }
                    .flatMap { it.second }
                    .partition { it.isModified }
                    .let { (modifiedCodeBlocks, unmodifiedCodeBlocks) ->
                        inconsistencyDetectionService.detectInconsistency(
                            modifiedCodeBlocks,
                            unmodifiedCodeBlocks.sortedBy { it.tokenSequence.size })
                    }
                    .filter { it.unmodifiedCodeList.isNotEmpty() }
                prInfo.number to clones
            }
            .filter { it.second.isNotEmpty() }
            .forEach {
                println(it.first)
                println(it.second.joinToString("\n"))
            }
    }

    private fun <T, R> Sequence<T>.mapAndFilter(function: (T) -> R?): Sequence<R> =
        this.map {
            try {
                function(it)
            } catch (e: Exception) {
                null
            }
        }
            .filter { it != null }
            .map { it!! }
}
