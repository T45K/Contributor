package io.github.t45k.contributor

import io.github.t45k.contributor.service.GitHubService
import io.github.t45k.contributor.service.GitService
import io.github.t45k.contributor.service.inconsistencyDetection.InconsistencyDetectionService
import io.github.t45k.contributor.service.inconsistencyDetection.JavaParser
import io.github.t45k.contributor.service.inconsistencyDetection.LexicalAnalyzer
import java.util.ResourceBundle
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
fun main(args: Array<String>) {
    // TODO parse
    val (repositoryName, srcDirName) = args
    val token = (ResourceBundle.getBundle("resource")
        ?.getString("GITHUB_TOKEN")
        ?: throw RuntimeException())

    // TODO DI
    val lexicalAnalyzer = LexicalAnalyzer()
    val javaParser = JavaParser(lexicalAnalyzer)
    val inconsistencyDetectionService = InconsistencyDetectionService()
    val gitHubService = GitHubService(token)
    val gitService = GitService(repositoryName)
    val usecase =
        FindInconsistencyUsecase(gitHubService, gitService, javaParser, inconsistencyDetectionService)
    usecase.findInconsistencies(repositoryName, srcDirName)
}
