package io.github.t45k.contributor.entity

import io.github.t45k.contributor.service.inconsistencyDetection.InconsistencyDetectionService

fun List<CodeBlock>.findInconsistencies(): List<Inconsistency> {
    val (modifiedCodeBlocks, unmodifiedCodeBlocks) = this.partition { it.isModified }
    modifiedCodeBlocks
        .map { modifiedCodeBlock ->
            val begin = unmodifiedCodeBlocks
                .binarySearch { if (it.tokenSequence.size * 10 / modifiedCodeBlock.tokenSequence.size >= InconsistencyDetectionService.THRESHOLD) 1 else -1 }
                .inv()
            val end = unmodifiedCodeBlocks
                .binarySearch { if (modifiedCodeBlock.tokenSequence.size * 10 / it.tokenSequence.size < InconsistencyDetectionService.THRESHOLD) 1 else -1 }
                .inv()
            unmodifiedCodeBlocks.subList(begin, end)
                .filter { compare(modifiedCodeBlock, it) }
                .let { Inconsistency(modifiedCodeBlock, it) }
        }
}
