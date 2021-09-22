package io.github.t45k.contributor.entity

import java.nio.file.Path

data class CodeBlock(
    val path: Path,
    val startLine: Int,
    val endLine: Int,
    val tokenSequence: List<Int>,
    var isModified: Boolean = false,
) {
    companion object {
        const val THRESHOLD = 7
    }

    override fun toString(): String = "$path $startLine--$endLine"

    fun hasLowerSizeOf(codeBlock: CodeBlock): Boolean =
        this.tokenSequence.size * 10 / codeBlock.tokenSequence.size >= THRESHOLD

    fun hasUpperSizeOf(codeBlock: CodeBlock): Boolean =
        codeBlock.tokenSequence.size * 10 / this.tokenSequence.size < THRESHOLD
}
