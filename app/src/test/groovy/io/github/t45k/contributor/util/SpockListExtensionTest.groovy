package io.github.t45k.contributor.util

import kotlin.sequences.SequencesKt
import spock.lang.Specification

class SpockListExtensionTest extends Specification {

    def 'test splitBy'() {
        expect:
        (1..9).toList()
            .with { ListExtensionKt.splitBy(it, delimeter as Set) }
            .with { SequencesKt.toList(it) } ==
            expect

        where:
        delimeter       || expect
        [1, 4, 7]       || [[2, 3], [5, 6], [8, 9]]
        [1, 9]          || [[2, 3, 4, 5, 6, 7, 8]]
        [2, 3, 5, 6]    || [[1], [4], [7, 8, 9]]
        (1..9).toList() || []
    }
}
