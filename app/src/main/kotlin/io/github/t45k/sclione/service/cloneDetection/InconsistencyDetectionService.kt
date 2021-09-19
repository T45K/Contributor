package io.github.t45k.sclione.service.cloneDetection

import io.github.t45k.sclione.entity.CodeBlock
import io.github.t45k.sclione.entity.Inconsistency

class InconsistencyDetectionService {
    companion object {
        const val THRESHOLD = 7
    }

    fun detectInconsistency(
        modifiedCodeBlocks: List<CodeBlock>,
        unmodifiedCodeBlocks: List<CodeBlock>
    ): List<Inconsistency> =
        modifiedCodeBlocks
            .map { modifiedCodeBlock ->
                val begin = unmodifiedCodeBlocks
                    .binarySearch { if (it.loc() * 10 / modifiedCodeBlock.loc() >= THRESHOLD) 1 else -1 }
                    .inv()
                val end = unmodifiedCodeBlocks
                    .binarySearch { if (modifiedCodeBlock.loc() * 10 / it.loc() < THRESHOLD) 1 else -1 }
                    .inv()
                unmodifiedCodeBlocks.subList(begin, end)
                    .filter { compare(modifiedCodeBlock, it) }
                    .let { Inconsistency(modifiedCodeBlock, it) }
            }

    private fun compare(a: CodeBlock, b: CodeBlock): Boolean {
        val (shorter: List<Int>, longer: List<Int>) =
            if (a.tokenSequence.size < b.tokenSequence.size) {
                a.tokenSequence to b.tokenSequence
            } else {
                b.tokenSequence to a.tokenSequence
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
