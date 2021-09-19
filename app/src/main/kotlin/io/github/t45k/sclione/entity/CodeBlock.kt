package io.github.t45k.sclione.entity

import java.nio.file.Path

data class CodeBlock(
    val path: Path,
    val startLine: Int,
    val endLine: Int,
    val tokenSequence: List<Int>,
    var isModified: Boolean = false,
) {
    fun loc(): Int = endLine - startLine + 1
}
