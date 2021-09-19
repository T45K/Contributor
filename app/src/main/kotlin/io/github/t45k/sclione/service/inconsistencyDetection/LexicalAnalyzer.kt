package io.github.t45k.sclione.service.inconsistencyDetection

import io.github.t45k.sclione.util.splitBy
import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.compiler.ITerminalSymbols
import org.eclipse.jdt.core.compiler.ITerminalSymbols.TokenNameEOF

/**
 * This class performs lexical analysis for counting the number of tokens.
 * This class is used when you want to filter code blocks by min_tokens.
 */
class LexicalAnalyzer {
    companion object {
        private val deliminators = setOf(
            ITerminalSymbols.TokenNameLBRACE,
            ITerminalSymbols.TokenNameRBRACE,
            ITerminalSymbols.TokenNameSEMICOLON
        )
    }

    fun analyze(text: String): List<Int> =
        ToolFactory.createScanner(false, false, false, "14")
            .also { it.source = text.toCharArray() }
            .let { scanner ->
                generateSequence { scanner.nextToken }
                    .takeWhile { it != TokenNameEOF }
            }.toList()
            .splitBy(deliminators)
            .map { it.hashCode() }
            .sorted()
            .toList()
}
