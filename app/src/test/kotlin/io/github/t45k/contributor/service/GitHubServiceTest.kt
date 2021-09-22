package io.github.t45k.contributor.service

import io.github.t45k.contributor.entity.PrInfo
import io.mockk.every
import io.mockk.spyk
import org.kohsuke.github.GitHubBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitHubServiceTest {

    private val sut: GitHubService = spyk(GitHubService(""))

    @Test
    fun testFetchPrInfoList() {
        every { sut.buildGitHubClient("") } returns GitHubBuilder().build()

        val prInfoSequence: Sequence<PrInfo> = sut.fetchPrInfoSequence("T45K/CLIONE")

        prInfoSequence.take(1)
            .forEach {
                assertEquals(125, it.number)
                assertEquals("1ad0910", it.mergeCommitSha)
            }
    }
}
