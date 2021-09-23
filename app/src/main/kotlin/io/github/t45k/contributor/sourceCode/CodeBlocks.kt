package io.github.t45k.contributor.sourceCode

fun List<CodeBlock>.findInconsistencies(): List<Inconsistency> {
    val (modifiedCodeBlocks, unmodifiedCodeBlocks) = this.partition { it.isModified }
    return modifiedCodeBlocks
        .map { modifiedCodeBlock ->
            val begin = unmodifiedCodeBlocks
                .binarySearch { if (it.hasLowerSizeOf(modifiedCodeBlock)) 1 else -1 }
                .inv()
            val end = unmodifiedCodeBlocks
                .binarySearch { if (modifiedCodeBlock.hasLowerSizeOf(it)) -1 else 1 }
                .inv()
            unmodifiedCodeBlocks.subList(begin, end)
                .filter { modifiedCodeBlock.isSimilarTo(it) }
                .let { Inconsistency(modifiedCodeBlock, it) }
        }
        .filter { it.unmodifiedCodeList.isNotEmpty() }
}
