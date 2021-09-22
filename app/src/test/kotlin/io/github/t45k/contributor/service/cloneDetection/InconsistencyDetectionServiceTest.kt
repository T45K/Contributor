package io.github.t45k.contributor.service.cloneDetection

import io.github.t45k.contributor.entity.CodeBlock
import io.github.t45k.contributor.service.inconsistencyDetection.InconsistencyDetectionService
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InconsistencyDetectionServiceTest {

    private val sut = InconsistencyDetectionService()

    @Test
    fun testDetectClones() {
        val path = Path.of(".")
        val modified = listOf(CodeBlock(path, 1, 10, (1..10).toList()))
        val stable = (1..20).map { CodeBlock(path, 1, it, (1..it).toList()) }

        val inconsistency = sut.detectInconsistency(modified, stable)

        assertEquals(1, inconsistency.size)
        assertEquals(
            (7..14).toList(),
            inconsistency[0].unmodifiedCodeList.map { it.endLine }
        )
    }
}
