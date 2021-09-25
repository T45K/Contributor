package io.github.t45k.contributor.sourceCode

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import java.nio.file.Path
import kotlin.io.path.readText

class TrackedJavaFiles(val path: Path, private val diff: List<BeginEndLine>) {

    fun extractCodeBlocks(): List<CodeBlock> =
        ASTParser.newParser(AST.JLS14)
            .apply { setSource(path.readText().toCharArray()) }
            .createAST(NullProgressMonitor())
            .let { it as CompilationUnit }
            .listCodeBlocks()

    private fun CompilationUnit.listCodeBlocks(): List<CodeBlock> {
        val codeBlocks = mutableListOf<CodeBlock>()
        val visitor = object : ASTVisitor() {
            override fun visit(node: MethodDeclaration): Boolean {
                val statements = node.body
                    ?.toString()
                    ?.toStatements()
                    ?.takeIf { it.hasSufficientSize() }
                    ?: return false

                val beginLine = getLineNumber(node.body.startPosition)
                val endLine = getLineNumber(node.body.startPosition + node.body.length)
                val isModified = diff.any { it.overlapWith(BeginEndLine(beginLine, endLine)) }
                val codeBlock = CodeBlock(path, beginLine, endLine, statements, isModified)
                codeBlocks.add(codeBlock)
                return false
            }
        }
        accept(visitor)
        return codeBlocks
    }
}
