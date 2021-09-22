package io.github.t45k.contributor.entity

import java.nio.file.Path

data class CodeBlock(
    val path: Path,
    val startLine: Int,
    val endLine: Int,
    val tokenSequence: List<Int>,
    var isModified: Boolean = false,
) {
    override fun toString(): String = "$path $startLine--$endLine"
}
