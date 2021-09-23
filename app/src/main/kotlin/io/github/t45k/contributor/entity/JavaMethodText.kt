package io.github.t45k.contributor.entity

import io.github.t45k.contributor.util.splitBy
import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.compiler.ITerminalSymbols

fun String.toStatements(): Statements =
    ToolFactory.createScanner(false, false, false, "14")
        .also { it.source = this.toCharArray() }
        .let { scanner ->
            generateSequence { scanner.nextToken }
                .takeWhile { it != ITerminalSymbols.TokenNameEOF }
        }.toList()
        .splitBy(deliminators)
        .map { it.hashCode() }
        .toList()
        .let { Statements(it) }

private val deliminators = setOf(
    ITerminalSymbols.TokenNameLBRACE,
    ITerminalSymbols.TokenNameRBRACE,
    ITerminalSymbols.TokenNameSEMICOLON
)
