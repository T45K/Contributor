package io.github.t45k.contributor.entity

import java.nio.file.Path

data class CodeBlock(
    val path: Path,
    val beginLine: Int,
    val endLine: Int,
    val statements: Statements,
    val isModified: Boolean,
) {
    companion object {
        const val THRESHOLD = 7
    }

    override fun toString(): String = "$path $beginLine--$endLine"

    fun hasLowerSizeOf(codeBlock: CodeBlock): Boolean =
        this.statements.value.size * 10 / codeBlock.statements.value.size >= THRESHOLD

    fun isSimilarTo(codeBlock: CodeBlock): Boolean {
        val (shorter: List<Int>, longer: List<Int>) =
            if (this.statements.value.size < codeBlock.statements.value.size) {
                this.statements.value to codeBlock.statements.value
            } else {
                codeBlock.statements.value to this.statements.value
            }
        val (n, m) = shorter.size to longer.size

        val invertedIndices: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        for (i in m - 1 downTo 0) {
            invertedIndices.getOrPut(longer[i]) { mutableListOf() }.add(i)
        }

        val lcs = Array(n + 1) { Int.MAX_VALUE }.also { it[0] = -1 }
        val lowerBoundComparator = Comparator { x: Int, y: Int -> if (x >= y) 1 else -1 }
        for (value: Int in shorter) {
            if (!invertedIndices.containsKey(value)) {
                continue
            }
            for (indexOfB in invertedIndices[value]!!) {
                val index: Int = lcs.binarySearch(indexOfB, lowerBoundComparator).inv()
                lcs[index] = indexOfB
            }
        }
        return (lcs.binarySearch(Int.MAX_VALUE - 1).inv() - 1) * 10 / longer.size >= THRESHOLD
    }
}
