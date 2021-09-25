package io.github.t45k.contributor.sourceCode

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CodeBlocksTest {
    @Test
    fun testFindInconsistencies() {
        val path = Path.of(".")
        val modified = CodeBlock(path, 1, 10, Statements((1..10).toList()), true)
        val unmodified: List<CodeBlock> = (1..20).map { CodeBlock(path, 1, it, Statements((1..it).toList()), false) }
        val inconsistencies = unmodified.plus(modified).findInconsistencies()

        assertEquals((7..14).toList(), inconsistencies[0].unmodifiedCodeList.map { it.endLine })
    }
}
