package io.github.t45k.contributor.sourceCode

data class Statements(val value: List<Int>) {
    fun hasSufficientSize(): Boolean = value.size >= 5
}
