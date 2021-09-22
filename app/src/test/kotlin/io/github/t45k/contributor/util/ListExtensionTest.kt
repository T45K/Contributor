package io.github.t45k.contributor.util

import kotlin.test.Test
import kotlin.test.assertEquals


internal class ListExtensionTest {

    @Test
    fun testSplitBy() {
        val list = (1..9).toList()

        val split = list.splitBy(setOf(1, 4, 7)).toList()

        assertEquals(split, listOf(listOf(2, 3), listOf(5, 6), listOf(8, 9)))
    }
}
