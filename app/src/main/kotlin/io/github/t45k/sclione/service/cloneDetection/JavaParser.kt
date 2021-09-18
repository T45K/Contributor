package io.github.t45k.sclione.service.cloneDetection

import io.github.t45k.sclione.entity.CodeBlock
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

class JavaParser(private val lexicalAnalyzer: LexicalAnalyzer) {

    @ExperimentalPathApi
    fun collectCodeBlocks(javaFile: Path): List<CodeBlock> {
        ASTParser.newParser(AST.JLS14)
            .apply { this.setSource(javaFile.readText().toCharArray()) }
            .createAST(NullProgressMonitor())
            .let {
                val compilationUnit = it as CompilationUnit
                fun ASTNode.isMoreThanFiveLines(): Boolean =
                    compilationUnit.getLineNumber(this.startPosition + this.length) -
                        compilationUnit.getLineNumber(this.startPosition) + 1 > 5

                object : ASTVisitor() {
                    override fun visit(node: MethodDeclaration): Boolean {
                        if (node.body?.isMoreThanFiveLines() == false) {
                            return false
                        }
                        val tokens: List<Int> = lexicalAnalyzer.analyze(node.body.toString())
                        return false
                    }
                }
                TODO()
            }

    }
}
