package io.github.t45k.sclione.util

fun <T> List<T>.splitBy(elements: Set<T>): Sequence<List<T>> {
    var begin = 0
    var end = 0
    val list = this@splitBy
    return sequence {
        while (end < list.size) {
            if (elements.contains(list[end])) {
                yield(list.subList(begin, end))
                begin = end + 1
                end = begin + 1
            } else {
                end++
            }
        }
        yield(list.subList(begin, list.size))
    }.filter { it.isNotEmpty() }
}
