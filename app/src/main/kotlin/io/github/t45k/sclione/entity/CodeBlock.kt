package io.github.t45k.sclione.entity

import java.nio.file.Path

data class CodeBlock(
    val path: Path,
    val startLine: Int,
    val endLine: Int,
    val id: Int,
    val tokenSequence: List<String>,
) {
    fun isOverlapping(codeBlock: CodeBlock): Boolean =
        this.path == codeBlock.path &&
            (this.include(codeBlock) || codeBlock.include(this))

    private fun include(codeBlock: CodeBlock): Boolean =
        this.startLine <= codeBlock.startLine && this.endLine >= codeBlock.endLine
}
