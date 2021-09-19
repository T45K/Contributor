package io.github.t45k.sclione

import io.github.t45k.sclione.service.GitHubService
import io.github.t45k.sclione.service.GitService
import io.github.t45k.sclione.service.cloneDetection.InconsistencyDetectionService
import io.github.t45k.sclione.service.cloneDetection.JavaParser
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

    fun findInconsistencies(repositoryName: String, srcDirectoryName: String) {
        val srcDirectory = Path.of("./strage", repositoryName, srcDirectoryName)
        gitHubService.fetchPrInfoSequence("", repositoryName) // TODO
            .map { prInfo ->
                val parentCommitSha = gitService.getMergeBasedParentCommitSha(prInfo.mergeCommitSha)
                gitService.checkout(parentCommitSha)
                val clones = Files.walk(srcDirectory)
                    .toList()
                    .filter { it.isRegularFile() && it.toString().endsWith(".java") }
                    .map { it.fileName to javaParser.collectCodeBlocks(it) }
                    .onEach { (filePath, codeBlocks) ->
                        for (edit in gitService.calcFileDiff(filePath, parentCommitSha, prInfo.mergeCommitSha)) {
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
                            unmodifiedCodeBlocks.sortedBy { it.loc() })
                    }
                prInfo.number to clones
            }
            .forEach { println(it) }
    }
}
