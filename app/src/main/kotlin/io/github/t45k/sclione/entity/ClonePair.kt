package io.github.t45k.sclione.entity

data class ClonePair(
    val first: CodeBlock,
    val second: CodeBlock
) {
    init {
        assert(first.id < second.id)
    }
}
