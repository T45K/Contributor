package io.github.t45k.contributor.entity

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RepositoryNameTest {
    @Test
    fun testInstance() {
        val repositoryName = RepositoryName("T45K/CLIONE")

        assertEquals("T45K/CLIONE", repositoryName.name)
        assertEquals(Path.of("./storage/T45K/CLIONE"), repositoryName.localPath)
    }

    @Test
    fun testThrowException() {
        assertFailsWith(InvalidRepositoryNameException::class, "T45K is invalid repository name") {
            RepositoryName("T45K")
        }
    }
}

